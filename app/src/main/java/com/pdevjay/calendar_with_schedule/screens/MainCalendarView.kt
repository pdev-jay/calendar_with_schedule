package com.pdevjay.calendar_with_schedule.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.pdevjay.calendar_with_schedule.datamodels.CalendarMonth
import com.pdevjay.calendar_with_schedule.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.ui.theme.Calendar_with_scheduleTheme
import com.pdevjay.calendar_with_schedule.viewmodels.CalendarViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainCalendarView(
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    // 단일 LazyColumn 스크롤 상태 사용
    val listState = rememberLazyListState()
    // months 상태 리스트
    val months = remember { mutableStateListOf<CalendarMonth>() }

    // 초기 데이터 로드
    LaunchedEffect(Unit) {
        months.addAll(viewModel.generateCalendarMonths())
    }

    // 초기 데이터가 로드된 후 한번만 실행
    // 현재 달이 리스트의 중앙에 위치하도록 스크롤
    LaunchedEffect(Unit) {
        snapshotFlow { months.isNotEmpty() }
            .filter { it }
            .first() // 처음 true가 될 때 한 번만 실행
        val startIndex = getFlatIndexForCurrentMonth(months, state.currentMonth)
        listState.scrollToItem(startIndex)
    }


    // 무한 스크롤 로직: listState의 첫 항목 인덱스를 보고 months 리스트에 달 데이터를 추가
    LaunchedEffect(listState) {
        snapshotFlow { Pair(listState.firstVisibleItemIndex, listState.isScrollInProgress) }
            .distinctUntilChanged()
            .collectLatest { (flatIndex, isDragging) ->
                val currentMonthIndex = findMonthIndexFromFlatIndex(flatIndex, months)
                viewModel.processIntent(CalendarIntent.MonthChanged(months[currentMonthIndex].yearMonth))
                Log.d("LazyColumn", "Current month index: $currentMonthIndex")
                // 앞쪽으로 스크롤 시 (현재 달이 리스트의 초반부에 가까워지면)
                if (!isDragging) {
                    val prevFirstIndex = listState.firstVisibleItemIndex
                    val prevOffset = listState.firstVisibleItemScrollOffset //  마지막 offset 저장

                    if (currentMonthIndex < 1) {
                        val firstMonth = months.first().yearMonth.minusMonths(6)
                        val newMonths = viewModel.generateCalendarMonths(firstMonth)
                        months.addAll(0, newMonths)
                        val totalWeeks = newMonths.sumOf { it.weeks.size }
                        listState.scrollToItem(prevFirstIndex + totalWeeks, prevOffset)

                    }
                    // 뒤쪽으로 스크롤 시 (현재 달이 리스트의 후반부에 가까워지면)
                    if (currentMonthIndex > months.size - 2) {
                        val lastMonth = months.last().yearMonth.plusMonths(6)
                        val newMonths = viewModel.generateCalendarMonths(lastMonth)
                        months.addAll(newMonths)
                    }
                }
            }
    }

    // 선택한 날짜가 변경되면, 해당 날짜가 속한 주(WeekRow)로 애니메이션 스크롤
    LaunchedEffect(state.selectedDate) {
        state.selectedDate?.let { date ->
            // week list로 변환
            val flatItems = months.flatMap { month ->
                        month.weeks.map { CalendarListItem.WeekItem(it) }
            }
            val targetIndex = flatItems.indexOfFirst { item ->
                item is CalendarListItem.WeekItem && item.week.days.any { it.date == date }
            }
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex, scrollOffset = 0)
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                CalendarHeader(state)
                WeekHeader()
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            DaysGrid(
                months = months,
                selectedDate = state.selectedDate,
                onDateSelected = { date ->
                    if (state.selectedDate == null || state.selectedDate != date) {
                        viewModel.processIntent(CalendarIntent.DateSelected(date))
                    } else {
                        viewModel.processIntent(CalendarIntent.DateUnselected)
                    }
                },
                listState = listState
            )
        }
    }
}

@Preview
@Composable
fun MainCalendarPreview() {
    Calendar_with_scheduleTheme {
        MainCalendarView(viewModel = hiltViewModel())
    }

}