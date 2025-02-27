package com.pdevjay.calendar_with_schedule.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainCalendarView(
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val outerListState = rememberLazyListState(initialFirstVisibleItemIndex = 6)
    val months = remember { mutableStateListOf<CalendarMonth>() }
    var currentVisibleMonth by remember { mutableStateOf(state.currentMonth) }

    // 내부(주) 스크롤 상태 – 현재 보여지는 달에 대해 사용
    val weekListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    // 초기 데이터 로드
    LaunchedEffect(Unit) {
        months.addAll(viewModel.generateCalendarMonths())
    }

    // 무한 스크롤: 달 단위로 처리 (기존 로직 유지)
    LaunchedEffect(outerListState) {
        snapshotFlow { outerListState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collectLatest { flatIndex ->
                val monthIndex = flatIndex
                currentVisibleMonth =
                    months.getOrNull(monthIndex)?.yearMonth ?: return@collectLatest
                viewModel.processIntent(CalendarIntent.MonthChanged(currentVisibleMonth))

                if (monthIndex < 3) {
                    val firstMonth = months.first().yearMonth.minusMonths(6)
                    months.addAll(0, viewModel.generateCalendarMonths(firstMonth))
                    outerListState.scrollToItem(monthIndex + 6)
                }

                if (monthIndex > months.size - 5) {
                    val lastMonth = months.last().yearMonth.plusMonths(6)
                    months.addAll(viewModel.generateCalendarMonths(lastMonth))
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
            // 현재 달만 화면에 표시하도록 (outer LazyColumn의 아이템 높이를 화면 전체로 고정)
            LazyColumn(
                state = outerListState,
                modifier = Modifier.fillMaxWidth()
            ) {
                months.forEach { month ->
                    item {
                        Box(modifier = Modifier.height(maxHeight)) {
                            // DaysGridWithScrolling를 사용하여 주 단위 스크롤 가능하게 변경
                            DaysGridWithScrolling(
                                calendarMonth = month,
                                selectedDate = state.selectedDate,
                                onDateSelected = { date ->
                                    if (state.selectedDate == null || state.selectedDate != date) {
                                        viewModel.processIntent(CalendarIntent.DateSelected(date))
                                    } else {
                                        viewModel.processIntent(CalendarIntent.DateUnselected)
                                    }
                                },
                                weekListState = weekListState,
                            )
                        }
                    }
                }
            }
        }
    }
}




//  이전 달 추가 함수
@RequiresApi(Build.VERSION_CODES.O)
suspend fun addPreviousMonths(
    prevFirstIndex: Int,
    prevOffset: Int,
    months: MutableList<YearMonth>,
    listState: LazyListState
) {
    val firstMonth = months.first()
    val newMonths = (1..6).map { firstMonth.minusMonths(it.toLong()) }.reversed()

    Log.d("LazyColumn", "Adding previous months: $newMonths")
    months.addAll(0, newMonths)

    val expectedIndex = prevFirstIndex + newMonths.size
    Log.d(
        "LazyColumn",
        "Before scrollToItem: firstVisibleItemIndex = ${listState.firstVisibleItemIndex}, expectedIndex = $expectedIndex"
    )

    listState.scrollToItem(expectedIndex, prevOffset)
}

//  이후 달 추가 함수
@RequiresApi(Build.VERSION_CODES.O)
suspend fun addNextMonths(
    prevFirstIndex: Int,
    prevOffset: Int,
    months: MutableList<YearMonth>,
    listState: LazyListState
) {
    val lastMonth = months.last()
    val newMonths = (1..6).map { lastMonth.plusMonths(it.toLong()) }

    Log.d("LazyColumn", "Adding next months: $newMonths")
    months.addAll(newMonths)

    listState.scrollToItem(prevFirstIndex, prevOffset)
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun MainCalendarPreview() {
    Calendar_with_scheduleTheme {
        MainCalendarView(viewModel = hiltViewModel())
    }

}