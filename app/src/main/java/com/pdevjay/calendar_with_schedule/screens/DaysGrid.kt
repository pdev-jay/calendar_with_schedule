package com.pdevjay.calendar_with_schedule.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.datamodels.CalendarMonth
import com.pdevjay.calendar_with_schedule.ui.theme.Calendar_with_scheduleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("UnusedBoxWithConstraintsScope")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DaysGrid(
    calendarMonth: CalendarMonth,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        for (week in calendarMonth.weeks) {
            WeekRow(
                modifier = Modifier.weight(1f),
                week = week,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DaysGridWithScrolling(
    calendarMonth: CalendarMonth,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    weekListState: LazyListState,
) {
    LaunchedEffect(selectedDate) {
        selectedDate?.let { date ->
            val weekIndex = calendarMonth.weeks.indexOfFirst { week ->
                week.days.any { it.date == date }
            }
            Log.d("WeekScroll", "Selected week index: $weekIndex")
            if (weekIndex >= 0) {
                snapshotFlow { weekListState.layoutInfo.visibleItemsInfo }
                    .filter { it.isNotEmpty() }
                    .first()
                // 약간의 추가 delay를 줄 수도 있습니다.
                delay(300)
                weekListState.animateScrollToItem(weekIndex)
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {

        LazyColumn(
            state = weekListState,
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(calendarMonth.weeks) { index, week ->
                Box(
                    modifier = Modifier.height(maxHeight/calendarMonth.weeks.size)
                ){
                    WeekRow(
                        week = week,
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun DaysGridPreview(){
    Calendar_with_scheduleTheme {
//        DaysGrid(currentMonth = YearMonth.now(), onDateSelected = {})
    }

}