package com.pdevjay.calendar_with_schedule.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DayCell(
    day: CalendarDay,
    height: Dp,
    schedules: List<ScheduleData>,
    onClick: (CalendarDay) -> Unit
) {
    val sortedSchedules = schedules.sortedWith(
        compareBy({ it.start.date }, { it.start.time }) // 시작 날짜 → 시작 시간 순으로 정렬
    )
    val totalCount = schedules.size

    Box(
        modifier = Modifier
            .size(height)
            .padding(2.dp)
            .clickable (
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ){ onClick(day) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            ) {

            Column(
                modifier = Modifier.padding(vertical = 4.dp),
//                verticalArrangement = Arrangement.Top
            ){
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = TextStyle(fontStyle = MaterialTheme.typography.bodyLarge.fontStyle, color = if (day.isToday) Color.Red else Color.Black),
                    textAlign = TextAlign.Center
                )

            }

            if (sortedSchedules.isNotEmpty()) {

                Column (
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ){
                    sortedSchedules.take(3).forEachIndexed { index, schedule ->
                        val backgroundColor = calculateScheduleColor(index, totalCount)

                        ScheduleListPreview(backgroundColor, Color.White, Alignment.CenterStart, schedule.title)
                    }

                    if (schedules.size > 3) {
                        ScheduleListPreview(Color.Transparent, Color.LightGray, Alignment.Center, "+${schedules.size - 4} more")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleListPreview(
    backgroundColor: Color,
    textColor: Color,
    alignment: Alignment,
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = backgroundColor, shape = RoundedCornerShape(2.dp))
            .padding(2.dp),
        contentAlignment = alignment
    ) {

        Text(
            text = title,
            style = TextStyle(color = textColor),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

fun calculateScheduleColor(index: Int, totalCount: Int): Color {
    val baseColor = Color(0xFF03A9F4) // 기본 파랑색
    val colorFactor = (index.toFloat() / totalCount.toFloat()) // 인덱스 비율

    return baseColor.copy(
        red = (baseColor.red * (1 - 0.3f * colorFactor)),
        green = (baseColor.green * (1 - 0.3f * colorFactor)),
        blue = (baseColor.blue * (1 - 0.3f * colorFactor))
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewDayCell() {
    val sampleDay = CalendarDay(
        date = LocalDate.now(),
        dayOfWeek = LocalDate.now().dayOfWeek,
        isToday = true
    )

    val sampleSchedules = listOf(
        ScheduleData(
            title = "Morning Standup",
            location = "Office",
            start = DateTimePeriod(LocalDate.now(), LocalTime.of(9, 0)),
            end = DateTimePeriod(LocalDate.now(), LocalTime.of(9, 30))
        ),
        ScheduleData(
            title = "Client Meeting",
            location = "Zoom",
            start = DateTimePeriod(LocalDate.now(), LocalTime.of(11, 0)),
            end = DateTimePeriod(LocalDate.now(), LocalTime.of(12, 0))
        ),
        ScheduleData(
            title = "Lunch Break",
            location = "Cafe",
            start = DateTimePeriod(LocalDate.now(), LocalTime.of(2, 30)),
            end = DateTimePeriod(LocalDate.now(), LocalTime.of(13, 30))
        ),
        ScheduleData(
            title = "Project Review",
            location = "Conference Room",
            start = DateTimePeriod(LocalDate.now(), LocalTime.of(15, 0)),
            end = DateTimePeriod(LocalDate.now(), LocalTime.of(16, 0))
        )
    )

    DayCell(
        day = sampleDay,
        height = 120.dp,
        schedules = sampleSchedules,
        onClick = { /* 클릭 이벤트 처리 */ }
    )
}
