package com.pdevjay.calendar_with_schedule.screens.calendar

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarMonth
import com.pdevjay.calendar_with_schedule.screens.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.screens.schedule.ScheduleView
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.ScheduleViewModel
import com.pdevjay.calendar_with_schedule.utils.ExpandVerticallyContainerFromTop
import com.pdevjay.calendar_with_schedule.utils.JsonUtils
import com.pdevjay.calendar_with_schedule.utils.LocalDateAdapter
import com.pdevjay.calendar_with_schedule.utils.LocalTimeAdapter
import com.pdevjay.calendar_with_schedule.utils.SlideInVerticallyContainerFromBottom
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.util.stream.Collectors.toList
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

    val isLoading = remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val monthListState = calendarViewModel.monthListState
    val currentVisibleMonth by rememberCurrentVisibleMonth(listState, monthListState)

    val isInitialized = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        calendarViewModel.initializeMonths() // 화면 이동 후에도 유지됨
    }

    LaunchedEffect(isInitialized.value) {

        if (!isInitialized.value) {
//            loadInitialMonths(monthListState)

            val currentMonthIndex = monthListState.indexOfFirst { month ->
                val now = YearMonth.now()
                month.yearMonth.year == now.year && month.yearMonth.monthValue == now.monthValue
            }

            listState.scrollToItem(currentMonthIndex)

            isInitialized.value = true  // 다음부터는 실행 안 함
        }
    }

    // 중앙에 보이는 부분이 어느 달인지
    LaunchedEffect(currentVisibleMonth) {
        currentVisibleMonth?.let { month ->
            calendarViewModel.processIntent(CalendarIntent.MonthChanged(month.yearMonth))
        }
    }

    LaunchedEffect(calendarState.selectedDate) {
        if (calendarState.selectedDate != null) {
            val currentMonthIndex = monthListState.indexOfFirst { month ->
                val now = calendarState.selectedDate
                month.yearMonth.year == (now?.year
                    ?: LocalDate.now().year) && month.yearMonth.monthValue == (now?.monthValue
                    ?: LocalDate.now().monthValue)
            }
            listState.scrollToItem(currentMonthIndex)
        }
    }
    Scaffold(
        topBar = {
            CalendarTopBar(calendarViewModel, listState, navController)
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
                        items(monthListState, key = { it.yearMonth }, contentType = { "month_item" }) { month ->
                            MonthItem(
                                month,
                                calendarState.scheduleMap
                            ) { date ->
                                if (calendarState.selectedDate == null || calendarState.selectedDate != date.date) {
                                    calendarViewModel.processIntent(CalendarIntent.DateSelected(date.date))
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

    // 아래 끝 감지 → 미래 데이터 추가 로드
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .map { it.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex == monthListState.lastIndex && !isLoading.value) {
                    loadNextMonths(monthListState, isLoading)
                }
            }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { firstVisibleIndex ->

                if (firstVisibleIndex == 0 && !isLoading.value) {
                    loadPreviousMonths(monthListState, isLoading, listState)
                }
            }
    }

}

suspend fun loadNextMonths(
    monthList: MutableList<CalendarMonth>,
    isLoading: MutableState<Boolean>
) {
    if (isLoading.value) return

    isLoading.value = true

    val lastMonth = monthList.lastOrNull() ?: return
    val lastYearMonth = YearMonth.of(lastMonth.yearMonth.year, lastMonth.yearMonth.monthValue)

    val newMonths = (1..12).map { offset ->
        val target = lastYearMonth.plusMonths(offset.toLong())
        generateMonth(target.year, target.monthValue)
    }

    monthList.addAll(newMonths)

    isLoading.value = false
}

suspend fun loadPreviousMonths(
    monthList: MutableList<CalendarMonth>,
    isLoading: MutableState<Boolean>,
    listState: LazyListState
) {
    if (isLoading.value) return

    isLoading.value = true

    // 현재 가장 위 아이템과 스크롤 위치 기억
    val firstVisibleItemIndex = listState.firstVisibleItemIndex
    val firstVisibleItemOffset = listState.firstVisibleItemScrollOffset

    val firstMonth = monthList.first()
    val firstYearMonth = YearMonth.of(firstMonth.yearMonth.year, firstMonth.yearMonth.monthValue)

    val newMonths = (1..12).map { offset ->
        val target = firstYearMonth.minusMonths(offset.toLong())
        generateMonth(target.year, target.monthValue)
    }.reversed()

    monthList.addAll(0, newMonths)

    isLoading.value = false

    // 위치 보정: 추가된 만큼 아래로 밀어주기 (기존에 보던 달 유지)
    listState.scrollToItem(
        firstVisibleItemIndex + newMonths.size,
        firstVisibleItemOffset
    )
}

fun generateMonth(year: Int, month: Int): CalendarMonth {
    val firstDay = LocalDate.of(year, month, 1)
    val daysInMonth = firstDay.lengthOfMonth()
    val today = LocalDate.now()

    val days = (1..daysInMonth).map { day ->
        val date = LocalDate.of(year, month, day)
        CalendarDay(
            date = date,
            dayOfWeek = date.dayOfWeek,
            isToday = date == today
        )
    }

    return CalendarMonth(YearMonth.of(year, month), days)
}

@Composable
fun rememberCurrentVisibleMonth(
    listState: LazyListState,
    monthList: List<CalendarMonth>
): State<CalendarMonth?> {
    val visibleMonth = remember { mutableStateOf<CalendarMonth?>(null) }
    var lastValidMiddleItem by rememberSaveable { mutableStateOf<Int?>(null) } // recomposition에서도 유지

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .combine(snapshotFlow { listState.layoutInfo.visibleItemsInfo }) { viewportHeight, visibleItems ->

                // 🛑 viewportHeight == 0 또는 visibleItems가 없으면 lastValidMiddleItem을 변경하지 않음
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

@Composable
fun SchedulePager(
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel,
    scheduleViewModel: ScheduleViewModel,
    onEventClick: (BaseSchedule) -> Unit,
    onBackButtonClicked: () -> Unit
) {
    val calendarState by calendarViewModel.state.collectAsState() // ViewModel의 State 구독
    val coroutineScope = rememberCoroutineScope()

    // scheduleList가 변경될 때 `selectedDate`를 유지하도록 설정
    val scheduleList by remember {
        derivedStateOf {
            calendarState.scheduleMap.toList().sortedBy { it.first }
        }
    }
    
    // 현재 선택된 날짜의 인덱스를 찾음
    val selectedIndex by remember {
        derivedStateOf {
            scheduleList.indexOfFirst { it.first == calendarState.selectedDate }
                .takeIf { it >= 0 } ?: 0
        }
    }

    // PagerState를 유지하면서 현재 `selectedDate`를 반영
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { scheduleList.size }
    )

    // `scheduleList`가 변경될 때 현재 페이지 유지 + 로딩 추가
    LaunchedEffect(scheduleList) {
        val newIndex = scheduleList.indexOfFirst { it.first == calendarState.selectedDate }
            .takeIf { it >= 0 } ?: 0

        if (newIndex != pagerState.currentPage) {
            coroutineScope.launch {
                pagerState.scrollToPage(newIndex) // 부드러운 전환
            }
        }
    }

    // 사용자가 스크롤할 때 `selectedDate` 및 `MonthChanged` 업데이트
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage } // settledPage를 감지하여 이동이 끝난 후 실행
            .distinctUntilChanged()
            .collectLatest { page ->
                val newDate = scheduleList.getOrNull(page)?.first ?: calendarState.selectedDate

                // 날짜가 변경되었으면 `selectedDate` 업데이트
                if (calendarState.selectedDate != newDate) {
                    calendarViewModel.processIntent(CalendarIntent.DateSelected(newDate ?: LocalDate.now()))
                }

                // 페이지가 첫 번째 또는 마지막일 때 `MonthChanged` 호출
                if (page == scheduleList.size - 1 || page == 0) {
                    val newMonth = YearMonth.from(newDate)
                    calendarViewModel.processIntent(CalendarIntent.MonthChanged(newMonth))
                }
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            ScheduleView(
                modifier = modifier,
                selectedDay = scheduleList[page].first,
                scheduleViewModel = scheduleViewModel,
                schedules = if (page == scheduleList.size -1 || page == 0) emptyList() else scheduleList[page].second, // scheduleList가 갱신되면서 깜빡이는 것처럼 보이는 현상때문에 처음, 마지막 page에서 recomposition 되기 전에는 빈 리스트로 설정
                onEventClick = onEventClick,
                onBackButtonClicked = onBackButtonClicked
            )
        }
    }
}