package com.pdevjay.calendar_with_schedule.screens

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.pdevjay.calendar_with_schedule.datamodels.CalendarListItem
import com.pdevjay.calendar_with_schedule.datamodels.CalendarMonth
import com.pdevjay.calendar_with_schedule.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.ui.theme.Calendar_with_scheduleTheme
import com.pdevjay.calendar_with_schedule.viewmodels.CalendarViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.time.YearMonth

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
        snapshotFlow { Triple(listState.firstVisibleItemIndex, listState.isScrollInProgress, listState.layoutInfo.visibleItemsInfo) }
            .distinctUntilChanged()
            .collectLatest { (flatIndex, isDragging, visibleItems) ->
                // 전체 주 수를 계산합니다.
                val totalWeeks = viewModel.getTotalWeeks(months)
                Log.d("LazyColumn", "Total weeks: $totalWeeks, Current flat index: $flatIndex")

                val prevFirstIndex = visibleItems.first().index
                val prevOffset = listState.firstVisibleItemScrollOffset
                val lastVisibleIndex = visibleItems.last().index

                // 현재 보이는 달 인덱스 계산 (flatIndex를 이용)
                val currentMonthIndex = findMonthIndexFromFlatIndex(flatIndex, months)

                // ViewModel에 현재 달 변경 이벤트 전달
                viewModel.processIntent(CalendarIntent.MonthChanged(months[currentMonthIndex].yearMonth))

                // 임계값(threshold)을 주 단위로 설정 (예: 10주)
                val threshold = 10

                // 앞쪽으로 스크롤: 현재 보이는 주가 threshold 미만이면 이전 데이터를 추가
                if (flatIndex < threshold) {
                    val firstMonth = months.first().yearMonth.minusMonths(6)
                    val newMonths = viewModel.generateCalendarMonths(firstMonth, months)
                    months.addAll(0, newMonths)
                    val newWeeks = newMonths.sumOf { it.weeks.size }
                    // 이전 데이터를 추가하면 스크롤 오프셋 보정
                    listState.scrollToItem(prevFirstIndex + newWeeks, prevOffset)
                }
                // 뒤쪽으로 스크롤: 현재 주 인덱스가 전체 주 개수에서 threshold 미만이면 이후 데이터를 추가
                if (lastVisibleIndex >= totalWeeks - threshold) {
                    val lastMonth = months.last().yearMonth.plusMonths(6)
                    val newMonths = viewModel.generateCalendarMonths(lastMonth, months)
                    months.addAll(newMonths)
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
        BoxWithConstraints(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            DaysGrid(
                maxHeight = maxHeight,
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

// 현재 달의 평탄화(flattened) 인덱스를 계산하는 헬퍼 함수
fun getFlatIndexForCurrentMonth(months: List<CalendarMonth>, currentMonth: YearMonth): Int {
    val monthIndex = months.indexOfFirst { it.yearMonth == currentMonth }
    return if (monthIndex >= 0) {
        months.take(monthIndex).sumOf { it.weeks.size }
    } else 0
}


@Preview
@Composable
fun MainCalendarPreview() {
    Calendar_with_scheduleTheme {
        MainCalendarView(viewModel = hiltViewModel())
    }

}