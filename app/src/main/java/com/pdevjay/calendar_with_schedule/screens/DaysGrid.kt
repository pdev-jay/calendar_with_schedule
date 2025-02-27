package com.pdevjay.calendar_with_schedule.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.datamodels.CalendarMonth
import com.pdevjay.calendar_with_schedule.datamodels.CalendarWeek
import com.pdevjay.calendar_with_schedule.ui.theme.Calendar_with_scheduleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DaysGrid(
    months: List<CalendarMonth>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    listState: LazyListState,
) {
    // derivedStateOf를 사용해 months 내용이 변경될 때마다 calendarItems를 재계산
    val weeks by remember {
        derivedStateOf {
            months.flatMap { month -> month.weeks }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        itemsIndexed(weeks) { index, week ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                WeekRow(
                    modifier = Modifier.fillMaxSize(),
                    week = week,
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected
                )
            }
        }
    }

}

// 3. flatIndex를 month 인덱스로 변환하는 헬퍼 함수
fun findMonthIndexFromFlatIndex(flatIndex: Int, months: List<CalendarMonth>): Int {
    var count = 0
    for ((i, month) in months.withIndex()) {
        val itemsForMonth = month.weeks.size // 주 항목 수
        if (flatIndex < count + itemsForMonth) {
            return i
        }
        count += itemsForMonth
    }
    return months.size - 1
}


// 현재 달의 평탄화(flattened) 인덱스를 계산하는 헬퍼 함수
fun getFlatIndexForCurrentMonth(months: List<CalendarMonth>, currentMonth: YearMonth): Int {
    val monthIndex = months.indexOfFirst { it.yearMonth == currentMonth }
    return if (monthIndex >= 0) {
        months.take(monthIndex).sumOf { it.weeks.size }
    } else 0
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun DaysGridPreview(){
    Calendar_with_scheduleTheme {
//        DaysGrid(currentMonth = YearMonth.now(), onDateSelected = {})
    }

}