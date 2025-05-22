package com.pdevjay.calendar_with_schedule.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdevjay.calendar_with_schedule.features.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.features.calendar.data.HolidaySchedule
import com.pdevjay.calendar_with_schedule.features.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.features.schedule.data.rangeTo
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt


@Composable
fun WeekScheduleRow(
    week: List<CalendarDay?>,
    schedules: List<BaseSchedule>,
    rowHeight: Dp,
    maxRow: Int,
    rowSpaceBy: Dp,
    bottomPadding: Dp
) {
    if (week.all { it == null }) return

    val weekDates = week.mapNotNull { it?.date }
    val dayCount = week.size
    val fontSize = 9.sp

    val density = LocalDensity.current

    val paddingPx = with(density) { 2.dp.toPx() }
    val borderPx = with(density) { 0.dp.toPx() }
    val correction = paddingPx + borderPx

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                (rowHeight * maxRow) + (rowSpaceBy * (maxRow - 1))
            )
    ) {
        val boxWidthPx = with(density) { maxWidth.toPx() }
        val dayWidthPx = (boxWidthPx - correction * dayCount) / dayCount

        val firstDay = weekDates.minOrNull() ?: return@BoxWithConstraints
        val lastDay = weekDates.maxOrNull() ?: return@BoxWithConstraints

        val placedSchedules = mutableListOf<MutableList<BaseSchedule>>()
        val hiddenCount = mutableMapOf<LocalDate, Int>()

        val holidays = schedules.filterIsInstance<HolidaySchedule>()

        val allDayEvents = schedules.filter { it.isAllDay && it !is HolidaySchedule }

        val multiDaySchedules = schedules.filter {
            ChronoUnit.DAYS.between(it.start.date, it.end.date) >= 1 && it !is HolidaySchedule
        }

        val singleDaySchedules = schedules - holidays.toSet() - allDayEvents.toSet() - multiDaySchedules.toSet()

        val sortedSchedules = holidays + allDayEvents + multiDaySchedules + singleDaySchedules

        sortedSchedules.forEach { schedule ->
            val start = schedule.start.date
            val end = schedule.end.date

            if (end < firstDay || start > lastDay) return@forEach

            val actualStart = start.coerceAtLeast(firstDay)
            val actualEnd = end.coerceAtMost(lastDay)

            val leftIndex = week.indexOfFirst { it?.date == actualStart }
            val spanDays = (ChronoUnit.DAYS.between(actualStart, actualEnd).toInt() + 1)
                .coerceAtMost(dayCount - leftIndex)

            if (leftIndex == -1 || spanDays <= 0) return@forEach

            val layerIndex = placedSchedules.indexOfFirst { layer ->
                layer.none {
                    val itStart = it.start.date
                    val itEnd = it.end.date
                    !(actualEnd < itStart || actualStart > itEnd)
                }
            }.takeIf { it >= 0 } ?: placedSchedules.size

            if (layerIndex >= (maxRow - 1)) {
                // 숨겨진 일정 처리
                actualStart.rangeTo(actualEnd).forEach { date ->
                    if (date in firstDay..lastDay) {
                        hiddenCount[date] = (hiddenCount[date] ?: 0) + 1
                    }
                }
                return@forEach
            }

            if (placedSchedules.size <= layerIndex) {
                placedSchedules.add(mutableListOf())
            }
            placedSchedules[layerIndex].add(schedule)

            val offsetX = dayWidthPx * leftIndex + correction * (leftIndex + 1)
            val width = dayWidthPx * spanDays + correction * (spanDays - 2)

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            offsetX.roundToInt(),
                            (layerIndex * with(density) { (rowHeight + rowSpaceBy).toPx() }).roundToInt()
                        )
                    }
                    .width(with(density) { width.toDp() })
                    .height(rowHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(calculateScheduleColor(schedule.color))
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = fontSize),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        //  +N more 표시
        hiddenCount.forEach { (date, count) ->
            val index = week.indexOfFirst { it?.date == date }
            if (index != -1) {
                val offsetX = dayWidthPx * index + correction * (index + 1)
                val width = dayWidthPx - correction
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                offsetX.roundToInt(),
                                ((maxRow - 1) * with(density) { (rowHeight + rowSpaceBy).toPx() }).roundToInt()
                            )
                        }
                        .width(with(density) { (width).toDp() })
                        .height(rowHeight)
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$count more",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = fontSize),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(bottomPadding))
    }
}

@Composable
fun calculateScheduleColor(color: Int?): Color {
    val baseColor = color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary

    return baseColor.copy(
        red = (baseColor.red * (1 - 0.1f)),
        green = (baseColor.green * (1 - 0.1f)),
        blue = (baseColor.blue * (1 - 0.1f))
    )
}
