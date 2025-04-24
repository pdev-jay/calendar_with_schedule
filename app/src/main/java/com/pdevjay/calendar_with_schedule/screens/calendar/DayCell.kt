package com.pdevjay.calendar_with_schedule.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule

@Composable
fun DayCell(
    day: CalendarDay,
    height: Dp,
    schedules: List<BaseSchedule>,
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
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick(day) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
//                .border(width = 1.dp, color = Color.LightGray),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
//                    .border(width = 1.dp, color = Color.Green)
                    .background(color = if (day.isToday) MaterialTheme.colorScheme.error else Color.Transparent, shape = RoundedCornerShape(4.dp))
                    .padding(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = if (day.isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

            }

            if (sortedSchedules.isNotEmpty()) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.Top),
                ) {
                    sortedSchedules.take(3).forEachIndexed { index, schedule ->
                        val backgroundColor =
                            calculateScheduleColor(index, totalCount, schedule.color)

                        ScheduleListPreview(
                            backgroundColor,
                            Color.White,
                            Alignment.CenterStart,
                            schedule.title
                        )
                    }

                    if (schedules.size > 3) {
                        ScheduleListPreview(
                            Color.Transparent,
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            Alignment.Center,
                            "+${schedules.size - 3} more"
                        )
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
//            fontSize = 10.sp,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun calculateScheduleColor(index: Int, totalCount: Int, color: Int?): Color {
    val baseColor = color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
    val colorFactor = (index.toFloat() / totalCount.toFloat()) // 인덱스 비율

    return baseColor.copy(
        red = (baseColor.red * (1 - 0.3f * colorFactor)),
        green = (baseColor.green * (1 - 0.3f * colorFactor)),
        blue = (baseColor.blue * (1 - 0.3f * colorFactor))
    )
}
