package com.pdevjay.calendar_with_schedule.screens.calendar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.data.entity.ScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

@Composable
fun DaysGrid(
    days: List<CalendarDay>,
    scheduleMap: Map<LocalDate, List<BaseSchedule>>,
    onDayClick: (CalendarDay) -> Unit
) {
    val weeks = calculateWeeks(days)
    val firstWeek = weeks.firstOrNull() ?: emptyList() //  첫 번째 주 가져오기

    val cellHeight = 120.dp //  셀 높이 설정
    val monthLabelHeight = 36.dp
    val dividerHeight = 1.dp
    val gridHeight = (cellHeight + dividerHeight) * weeks.size + monthLabelHeight

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight),
        userScrollEnabled = false
    ) {
        items(weeks.flatten()) { dayOrNull ->
            if (dayOrNull == null) {
                Spacer(Modifier.size(cellHeight))
            } else {
                val schedules = scheduleMap[dayOrNull.date] ?: emptyList()
                val isInFirstWeek = firstWeek.contains(dayOrNull) //  첫 번째 주에 속하는지 확인

                Column(){

                    if (dayOrNull.date.dayOfMonth == 1){
                        val formatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
                        Text(text = dayOrNull.date.format(formatter), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.CenterHorizontally).height(monthLabelHeight))
                    } else if (isInFirstWeek){
                        Spacer(Modifier.size(monthLabelHeight))
                    }
                    HorizontalDivider(thickness = dividerHeight, color = MaterialTheme.colorScheme.surfaceContainerHigh)
                    DayCell(dayOrNull, cellHeight, schedules, onDayClick)
                }
            }
        }
    }
}



// 날짜 데이터 -> 주 단위로 나누기
fun calculateWeeks(days: List<CalendarDay>): List<List<CalendarDay?>> {
    val startOffset = days.first().date.dayOfWeek.value % 7
    val paddedDays = List(startOffset) { null } + days
    return paddedDays.chunked(7)
}


