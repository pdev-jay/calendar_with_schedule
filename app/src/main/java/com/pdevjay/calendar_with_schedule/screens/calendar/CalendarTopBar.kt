package com.pdevjay.calendar_with_schedule.screens.calendar

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarWeek
import com.pdevjay.calendar_with_schedule.screens.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.utils.ExpandVerticallyContainerFromTop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarTopBar(
    viewModel: CalendarViewModel,
    listState: LazyListState,
    navController: NavController,
    drawerState: DrawerState,
    coroutineScope: CoroutineScope
) {
    val state by viewModel.state.collectAsState()
//    val months by viewModel.months.collectAsState()
//    val coroutineScope = rememberCoroutineScope()

    val baseDate = state.selectedDate ?: LocalDate.now()
    val infiniteStartPage = remember { Int.MAX_VALUE / 2 }
    val weekDates = state.selectedDate?.let { getWeekDatesForDate(it) }

    //  `selectedDate`를 기반으로 초기 페이지 설정
    val initialPage = remember {
        infiniteStartPage
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { Int.MAX_VALUE }
    )

    //  사용자가 스와이프할 때 `selectedDate` 업데이트
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collectLatest { pageIndex ->
                val offsetWeeks = pageIndex - infiniteStartPage
                val newDate = baseDate.plusDays(offsetWeeks * 7L)

                if (state.selectedDate != null && state.selectedDate != newDate) {
                    Log.e("CalendarIntent.DateSelected", "pagerState2 : Date changed: $newDate")
                    viewModel.processIntent(CalendarIntent.DateSelected(newDate))
                }
            }
    }

    Column {
        CalendarHeader(
            state,
            navController,
            onTodayClick = {
                if (state.selectedDate == null) {
                    viewModel.initializeMonths(true)
                    coroutineScope.launch {
                        viewModel.state.collect { newState -> //  state 변경을 감지한 후 실행
                            val now = YearMonth.now()
                            val currentMonthIndex = newState.months.indexOfFirst { month ->
                                month.yearMonth == now
                            }
                            if (currentMonthIndex != -1) {
                                listState.animateScrollToItem(currentMonthIndex)
                            }
                            cancel() //  한 번 실행한 후 collect 종료
                        }
                    }
                } else {
                    viewModel.initializeMonths(true)
                    coroutineScope.launch {
                        viewModel.state.collect { newState -> //  state 변경을 감지한 후 실행
                            Log.e("CalendarIntent.DateSelected", "pagerState3 : new state")

                            viewModel.processIntent(CalendarIntent.MonthChanged(YearMonth.now()))
                            viewModel.processIntent(CalendarIntent.DateSelected(LocalDate.now()))
                            Log.e("CalendarIntent.DateSelected", "pagerState3 : date selected ${newState.selectedDate}}")
                            cancel() //  한 번 실행한 후 collect 종료
                        }
                    }
                }
            },
            onClick = { viewModel.processIntent(CalendarIntent.DateUnselected) },
            onMenuClick = {
                coroutineScope.launch { drawerState.open() }
            }
        )

        WeekHeader()

        // 애니메이션 제대로 동작하게 key 추가
        ExpandVerticallyContainerFromTop(
            isVisible = state.selectedDate != null
        ) {
            if (weekDates != null) {
                WeekRow(
                    weekDates = weekDates,
                    selectedDate = state.selectedDate,
                    onDateClick = { date ->
                        if (date != state.selectedDate) {
                            Log.e("CalendarIntent.DateSelected", "pagerState4 : Date changed: ${date}")

                            viewModel.processIntent(CalendarIntent.DateSelected(date))
                        } else {
                            viewModel.processIntent(CalendarIntent.DateUnselected)
                        }

                        //  선택된 날짜가 포함된 페이지로 이동
                        val newPageIndex =
                            infiniteStartPage + ChronoUnit.WEEKS.between(baseDate, date).toInt()
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(newPageIndex)
                        }
                    }
                )
            }
            }
        }
    }


@Composable
fun WeekRow(
    weekDates: List<LocalDate>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        weekDates.forEach { date ->
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSelected -> MaterialTheme.colorScheme.error
                            else -> Color.Transparent
                        }
                    )
                    .clickable { onDateClick(date) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = TextStyle(
                    color = when {
                        isSelected -> Color.White
                        isToday -> Color.Red
                        else -> MaterialTheme.colorScheme.onSurface
                    },

                    )
                )
            }
        }
    }
}




fun getWeekDatesForDate(selectedDate: LocalDate): List<LocalDate> {
    val dayOfWeek = selectedDate.dayOfWeek  // 월요일=1, 일요일=7
    val startOfWeek = selectedDate.minusDays(dayOfWeek.value.toLong() % 7)

    return (0 until 7).map { offset ->
        startOfWeek.plusDays(offset.toLong())
    }
}

fun getWeeksFromMonth(monthDays: List<CalendarDay>): List<List<LocalDate>> {
    if (monthDays.isEmpty()) return emptyList()

    val localDates = monthDays.map { it.date } //  `CalendarDay` → `LocalDate` 변환
    val weeks = mutableListOf<List<LocalDate>>()
    var currentWeekStart = getWeekDatesForDate(localDates.first()).first()

    while (currentWeekStart.isBefore(localDates.last().plusDays(1))) {
        val weekDates = getWeekDatesForDate(currentWeekStart)
        weeks.add(weekDates)
        currentWeekStart = currentWeekStart.plusWeeks(1) //  다음 주 시작일로 이동
    }

    return weeks
}

