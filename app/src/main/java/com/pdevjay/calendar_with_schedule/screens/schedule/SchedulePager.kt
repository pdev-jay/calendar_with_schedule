package com.pdevjay.calendar_with_schedule.screens.schedule

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.pdevjay.calendar_with_schedule.screens.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.ScheduleViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate

@Composable
fun SchedulePager(
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel,
    scheduleViewModel: ScheduleViewModel,
    onEventClick: (BaseSchedule) -> Unit,
    onBackButtonClicked: () -> Unit
) {
    val calendarState by calendarViewModel.state.collectAsState() // ViewModel의 State 구독

//    val allDays = calendarState.months.flatMap { it.days } // 모든 CalendarDay 리스트
//    val selectedDayIndex = allDays.indexOfFirst { it.date == calendarState.selectedDate }
//        .takeIf { it >= 0 } ?: 0 // 선택된 날짜가 없으면 첫 번째 페이지
    val allDays by remember(calendarState.months) {
        derivedStateOf { calendarState.months.flatMap { it.days } }
    }

    val selectedDayIndex by remember(allDays, calendarState.selectedDate) {
        derivedStateOf {
            allDays.indexOfFirst { it.date == calendarState.selectedDate }
                .takeIf { it >= 0 } ?: 0
        }
    }

    val pagerState = rememberPagerState(initialPage = selectedDayIndex, pageCount = { allDays.size })

    LaunchedEffect(calendarState.selectedDate) {
        snapshotFlow { calendarState.selectedDate }
            .distinctUntilChanged()
            .collect { selectedDate ->
                Log.e("CalendarIntent.DateSelected", "pagerState4 : selectedDate: ${selectedDate}")
                Log.e("CalendarIntent.DateSelected", "pagerState4 : state: ${calendarState.scheduleMap.values}")
                if (selectedDate == null) return@collect
                val page = allDays.indexOfFirst { it.date == selectedDate }
                    .takeIf { it >= 0 } ?: 0
                Log.e("CalendarIntent.DateSelected", "pagerState4 : selectedDate index: ${page}")
                if (pagerState.currentPage != page) {
                    pagerState.animateScrollToPage(page)
                }
            }
    }

    LaunchedEffect(pagerState){
        snapshotFlow{pagerState.targetPage}
            .distinctUntilChanged()
            .debounce(200)
            .collectLatest { page ->
                if (calendarState.selectedDate != allDays[page].date) {
                    calendarViewModel.processIntent(CalendarIntent.DateSelected(allDays[page].date))
                }
//                pagerState.animateScrollToPage(page)
                if (pagerState.currentPage == 0){
                    calendarViewModel.loadPreviousMonth()
                } else if(pagerState.currentPage == pagerState.pageCount - 1) {
                    calendarViewModel.loadNextMonth()
                }
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { index -> allDays[index].date.toString() } //  페이지 키를 CalendarDay.date 기준으로 설정
        ) { page ->
            val selectedDay = allDays[page]
            val schedules by remember { derivedStateOf { calendarState.scheduleMap[selectedDay.date] ?: emptyList() } }
            ScheduleView(
                modifier = modifier,
                selectedDay = selectedDay.date,
                scheduleViewModel = scheduleViewModel,
                schedules = schedules,
                onEventClick = onEventClick,
                onBackButtonClicked = onBackButtonClicked
            )
        }
    }
}