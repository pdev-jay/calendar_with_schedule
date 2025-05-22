package com.pdevjay.calendar_with_schedule.features.calendar

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.features.calendar.data.CalendarMonth
import com.pdevjay.calendar_with_schedule.features.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.features.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.features.schedule.SchedulePager
import com.pdevjay.calendar_with_schedule.features.schedule.viewmodels.ScheduleViewModel
import com.pdevjay.calendar_with_schedule.core.utils.DoubleBackToExitHandler
import com.pdevjay.calendar_with_schedule.core.utils.ExpandVerticallyContainerFromTop
import com.pdevjay.calendar_with_schedule.core.utils.helpers.JsonUtils
import com.pdevjay.calendar_with_schedule.core.utils.SlideInVerticallyContainerFromBottom
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import java.net.URLEncoder
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    calendarViewModel: CalendarViewModel,
    scheduleViewModel: ScheduleViewModel,
) {
    val calendarState by calendarViewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val isLoading = remember { mutableStateOf(false) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = calendarState.months.size / 2) // 현재 달을 기준으로 앞뒤로 12개월씩 로드

    val currentVisibleMonth by rememberCurrentVisibleMonth(listState, calendarState.months)

    DoubleBackToExitHandler()

    // 중앙에 보이는 부분이 어느 달인지
    LaunchedEffect(currentVisibleMonth) {
        // MonthItem이 보이는 경우에만 작동
        if (calendarState.selectedDate == null) {
            currentVisibleMonth?.let { month ->
                Log.e("selected", "currentVisibleMonth : ${currentVisibleMonth} ")
                calendarViewModel.processIntent(CalendarIntent.MonthChanged(month.yearMonth))
            }
        }
    }

    LaunchedEffect(calendarState.selectedDate) {
        if (calendarState.selectedDate != null) {
            val selectedDate = calendarState.selectedDate ?: return@LaunchedEffect
            val currentMonthIndex = calendarState.months.indexOfFirst { month ->
                month.yearMonth == YearMonth.from(selectedDate)
            }

            if (currentMonthIndex != listState.firstVisibleItemIndex) { //  중복 실행 방지
                listState.scrollToItem(currentMonthIndex)
            }
        }
    }

    Scaffold(
        topBar = {
            CalendarTopBar(calendarViewModel, listState, navController,
                coroutineScope
            )
        },
        floatingActionButton = {
            Row(){
                FloatingActionButton(
                    onClick = {
                        val destination = "add_schedule/${calendarState.selectedDate ?: LocalDate.now()}"
                        navController.navigate(destination)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) { innerPadding ->

        BoxWithConstraints(
            modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SlideInVerticallyContainerFromBottom (
                isVisible = calendarState.selectedDate == null,
            ) {
                key(calendarState.selectedDate) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {

                        items(calendarState.months, key = { it.yearMonth.toString() }) { month ->
//                            val mappedSchedules = remember(calendarState.scheduleMap) {
//                                calendarViewModel.getMappedSchedulesForMonth(month)
//                            }
//                            val mappedHolidays = remember(calendarState.scheduleMap) {
//                                calendarViewModel.getMappedHolidayForMonth(month)
//                            }

                            val (mappedSchedules, mappedHolidays) = remember(calendarState.scheduleMap) {
                                Pair(calendarViewModel.getMappedSchedulesForMonth(month), calendarViewModel.getMappedHolidayForMonth(month))
                            }
                            MonthItem(
                                month,
                                mappedSchedules,
                                mappedHolidays
                            ) { date ->
                                if (calendarState.selectedDate == null || calendarState.selectedDate != date.date) {
                                    calendarViewModel.processIntent(
                                        CalendarIntent.DateSelected(
                                            date.date
                                        )
                                    )
                                } else {
                                    calendarViewModel.processIntent(CalendarIntent.DateUnselected)
                                }
                            }
                        }

                        item {
                            if (isLoading.value) {
                                CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
            ExpandVerticallyContainerFromTop (
                isVisible = calendarState.selectedDate != null,
            ) {
                SchedulePager(
                    modifier = Modifier.height(maxHeight),
                    calendarViewModel = calendarViewModel,
                    scheduleViewModel = scheduleViewModel,
                    onEventClick = { event ->
                        val jsonSchedule = URLEncoder.encode(JsonUtils.gson.toJson(event), "UTF-8")
                        navController.navigate("scheduleDetail/${URLEncoder.encode(jsonSchedule, "UTF-8")}")
                    },
                    onBackButtonClicked = {
                        calendarViewModel.processIntent(CalendarIntent.DateUnselected)
                    }
                )
            }
        }
    }

    LaunchedEffect(listState) {
        //  첫 번째 아이템 감지 → 이전 달 데이터 로드
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .debounce(50)
            .collectLatest { firstVisibleIndex ->
                Log.e("LazyRow", " 현재 첫 번째 아이템 인덱스: $firstVisibleIndex") //  디버깅 로그 추가

                if (firstVisibleIndex <= 0 && !isLoading.value) {
                    loadPreviousMonths(calendarState.months, isLoading, listState, calendarViewModel)
                }
            }
    }

    LaunchedEffect(listState) {
        //  마지막 아이템 감지 → 다음 달 데이터 로드
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }
            .filterNotNull() //  Null 방지
            .distinctUntilChanged()
            .debounce(50)
            .collectLatest { lastVisibleIndex ->
                if (lastVisibleIndex == calendarState.months.lastIndex && !isLoading.value) {
                    Log.e("LazyRow", " 현재 마지막 아이템 인덱스: $lastVisibleIndex") //  디버깅 로그 추가
                    loadNextMonths(calendarState.months, isLoading, calendarViewModel)
                }
            }
    }



}

suspend fun loadNextMonths(
    monthList: MutableList<CalendarMonth>,
    isLoading: MutableState<Boolean>,
    viewModel: CalendarViewModel
) {
    if (isLoading.value) return

    isLoading.value = true
//    viewModel.loadNextMonth()
    viewModel.processIntent(CalendarIntent.LoadNextMonths)

    isLoading.value = false
}

suspend fun loadPreviousMonths(
    monthList: MutableList<CalendarMonth>,
    isLoading: MutableState<Boolean>,
    listState: LazyListState,
    viewModel: CalendarViewModel
) {
    if (isLoading.value) return

    isLoading.value = true
    //  현재 첫 번째 보이는 아이템의 인덱스 저장
    val firstVisibleItemIndex = listState.firstVisibleItemIndex
    val firstVisibleItemOffset = listState.firstVisibleItemScrollOffset // 현재 스크롤 오프셋 저장

    //  이전 달 데이터 불러오기
//    val newMonths = viewModel.loadPreviousMonth() // 예: 6개월 추가됨
    viewModel.processIntent(CalendarIntent.LoadPreviousMonths)
    isLoading.value = false

    //  추가된 개월 수 만큼 첫 번째 아이템 인덱스 조정하여 원래 보던 위치 유지
//    listState.scrollToItem(firstVisibleItemIndex + newMonths.size, firstVisibleItemOffset)
}

@Composable
fun rememberCurrentVisibleMonth(
    listState: LazyListState,
    monthList: List<CalendarMonth>
): State<CalendarMonth?> {
    val visibleMonth = remember { mutableStateOf<CalendarMonth?>(null) }
    var lastValidMiddleItem by rememberSaveable { mutableStateOf<Int?>(null) } // recomposition에서도 유지
    val updatedMonthList = rememberUpdatedState(monthList)

    LaunchedEffect(listState, updatedMonthList.value) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .combine(snapshotFlow { listState.layoutInfo.visibleItemsInfo }) { viewportHeight, visibleItems ->

//                  viewportHeight == 0 또는 visibleItems가 없으면 lastValidMiddleItem을 변경하지 않음
                if (viewportHeight == 0 || visibleItems.isEmpty()) {
                    return@combine lastValidMiddleItem ?: listState.firstVisibleItemIndex
                }

                val screenCenter = viewportHeight / 2
                val middleItem = visibleItems.minByOrNull { item ->
                    val itemCenter = item.offset + (item.size / 2)
                    abs(itemCenter - screenCenter)
                }?.index ?: listState.firstVisibleItemIndex

                // middleItem이 실제로 달라진 경우에만 업데이트
                if (middleItem != lastValidMiddleItem) {
                    lastValidMiddleItem = middleItem
                }

                middleItem
            }
            .distinctUntilChanged()
            .collectLatest { index ->
                visibleMonth.value = monthList.getOrNull(index)
            }
    }

    return visibleMonth
}

