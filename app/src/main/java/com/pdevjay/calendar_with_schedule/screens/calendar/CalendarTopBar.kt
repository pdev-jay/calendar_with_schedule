package com.pdevjay.calendar_with_schedule.screens.calendar

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.screens.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.utils.ExpandVerticallyContainerFromTop
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarTopBar(
    viewModel: CalendarViewModel,
    listState: LazyListState,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val weekDates = state.selectedDate?.let { getWeekDatesForDate(it) }
    val coroutineScope = rememberCoroutineScope()
    Column(
        ) {
        CalendarHeader(
            state,
            navController,
            onTodayClick = {
                if (state.selectedDate == null) {
                    val now = YearMonth.now()
                    val currentMonthIndex = viewModel.monthListState.indexOfFirst { month ->
                        month.yearMonth == now
                    }

                    if (currentMonthIndex != -1) { // 🔹 현재 월이 존재할 때만 스크롤
                        coroutineScope.launch {
                            listState.animateScrollToItem(currentMonthIndex) // 🔹 부드러운 스크롤 적용
                        }
                    }
                } else {
                    viewModel.processIntent(CalendarIntent.DateSelected(LocalDate.now()))
                }
            },
            onClick = { viewModel.processIntent(CalendarIntent.DateUnselected) }
        )
        WeekHeader()

        // 애니메이션 제대로 동작하게 key 추가
        ExpandVerticallyContainerFromTop (
            isVisible = weekDates != null,
        ) {
            weekDates?.let {
//                WeekPager(state.selectedDate, viewModel = viewModel, onDateClick = { date ->
//                    viewModel.processIntent(CalendarIntent.DateSelected(date))})
                WeekRow(
                    weekDates = it,
                    selectedDate = state.selectedDate,
                    onDateClick = { date ->
                        if (date != state.selectedDate) {
                            viewModel.processIntent(CalendarIntent.DateSelected(date))
                        } else {
                            viewModel.processIntent(CalendarIntent.DateUnselected)
                        }
                    }
                )
            }
        }
    }
}

//@Composable
//fun WeekPager(
//    selectedDate: LocalDate?,
//    onDateClick: (LocalDate) -> Unit,
//    viewModel: CalendarViewModel
//) {
//    val initialPage = Int.MAX_VALUE / 2 // 중앙에서 시작하여 무한 스크롤처럼 보이게 설정
//    val pagerState = rememberPagerState(initialPage, pageCount = { Int.MAX_VALUE })
//    val coroutineScope = rememberCoroutineScope()
//
//    HorizontalPager(
//        state = pagerState,
////        count = Int.MAX_VALUE, // 사실상 무한 스크롤
//        modifier = Modifier.fillMaxWidth()
//    ) { page ->
//        // 현재 선택된 날짜를 기준으로 week 계산
//        val weeksFromToday = page - initialPage
//        val currentWeekStart = (selectedDate ?: LocalDate.now()).plusWeeks(weeksFromToday.toLong())
//        val weekDates = getWeekDatesForDate(currentWeekStart)
//
//        WeekRow(
//            weekDates = weekDates,
//            selectedDate = selectedDate,
//            onDateClick = { date ->
//                onDateClick(date)
//                coroutineScope.launch {
//                    pagerState.animateScrollToPage(initialPage) // 스와이프 후 다시 중앙으로 복귀
//                }
//            }
//        )
//    }
//
//    // 스와이프 후 `selectedDate` 업데이트
//    LaunchedEffect(pagerState.currentPage) {
//        val weeksFromToday = pagerState.currentPage - initialPage
//        val newDate = (selectedDate ?: LocalDate.now()).plusWeeks(weeksFromToday.toLong())
//
//        if (newDate != selectedDate) {
//            viewModel.processIntent(CalendarIntent.DateSelected(newDate))
//        }
//    }
//}


//@Composable
//fun WeekRow(
//    weekDates: List<LocalDate>,
//    selectedDate: LocalDate?,
//    onDateClick: (LocalDate) -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp),
//        horizontalArrangement = Arrangement.SpaceEvenly,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        weekDates.forEach { date ->
//            val isSelected = date == selectedDate
//            val isToday = date == LocalDate.now()
//
//            Box(
//                modifier = Modifier
//                    .size(40.dp)
//                    .clip(CircleShape)
//                    .background(
//                        when {
//                            isSelected -> Color.Red.copy(alpha = 0.7f)
//                            else -> Color.Transparent
//                        }
//                    )
//                    .clickable { onDateClick(date) },
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = date.dayOfMonth.toString(),
//                    style = TextStyle(
//                        color = when {
//                            isSelected -> Color.White
//                            isToday -> Color.Red
//                            else -> Color.Black
//                        },
//                    )
//                )
//            }
//        }
//    }
//}

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
                            isSelected -> Color.Red.copy(alpha = 0.7f)
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
                        else -> Color.Black
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
