package com.pdevjay.calendar_with_schedule.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.datamodels.LaneData
import com.pdevjay.calendar_with_schedule.datamodels.ScheduleData
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ScheduleView(
    selectedDay: LocalDate,
    events: List<ScheduleData>,
    modifier: Modifier = Modifier
) {
    val hourHeight = 60.dp
    val totalHeight = hourHeight * 24

    // 선택된 날짜에 해당하는 이벤트만 필터링
    val dayEvents = events.filter { event ->
        event.start.date <= selectedDay && event.end.date >= selectedDay
    }

    val scrollState = rememberScrollState()

    Column(modifier = modifier.verticalScroll(scrollState)) {
        Row(modifier = Modifier.height(totalHeight)) {
            TimeColumn(hourHeight = hourHeight)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(totalHeight)
                    .background(Color.White)
            ) {
                TimeLines(hourHeight = hourHeight)

                dayEvents.forEach { event ->
                    val overlappingGroup = dayEvents.filter { isOverlapping(it, event, selectedDay) }
                    val groupCount = overlappingGroup.size
                    val groupIndex = overlappingGroup.indexOf(event)

                    val startOffset = calculateEffectiveOffset(event, selectedDay, hourHeight)
                    val eventHeight = calculateEffectiveEventHeight(event, selectedDay, hourHeight)

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val availableWidthDp = with(LocalDensity.current) { constraints.maxWidth.toDp() }
                        val laneWidth = availableWidthDp / groupCount
                        val xOffset = laneWidth * groupIndex

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = xOffset.roundToPx(),
                                        y = startOffset.roundToPx()
                                    )
                                }
                                .width(laneWidth)
                                .height(eventHeight)
                                .padding(horizontal = 2.dp)
                                .background(Color(0xFF87CEFA).copy(alpha = 0.3f))
                                .padding(4.dp)
                        ) {
                            Column {
                                Text(event.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                                event.location?.let { loc ->
                                    Text(loc, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                }
                                val (effStart, effEnd) = effectiveEventTimesForDay(event, selectedDay)
                                Text("$effStart - $effEnd", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun effectiveEventTimesForDay(event: ScheduleData, selectedDay: LocalDate): Pair<LocalTime, LocalTime> {
    return when {
        selectedDay == event.start.date && event.end.date > event.start.date ->
            event.start.time to LocalTime.of(23, 59)
        selectedDay == event.end.date && event.start.date < event.end.date ->
            LocalTime.MIDNIGHT to event.end.time
        selectedDay > event.start.date && selectedDay < event.end.date ->
            LocalTime.MIDNIGHT to LocalTime.of(23, 59)
        else -> event.start.time to event.end.time
    }
}

fun isOverlapping(a: ScheduleData, b: ScheduleData, selectedDay: LocalDate): Boolean {
    val (aEffStart, aEffEnd) = effectiveEventTimesForDay(a, selectedDay)
    val (bEffStart, bEffEnd) = effectiveEventTimesForDay(b, selectedDay)
    return aEffStart < bEffEnd && bEffStart < aEffEnd
}

fun calculateEffectiveOffset(event: ScheduleData, selectedDay: LocalDate, hourHeight: Dp): Dp {
    val (effectiveStart, _) = effectiveEventTimesForDay(event, selectedDay)
    val totalMinutes = effectiveStart.hour * 60 + effectiveStart.minute
    val ratio = totalMinutes / 60f
    return hourHeight * ratio
}

fun calculateEffectiveEventHeight(event: ScheduleData, selectedDay: LocalDate, hourHeight: Dp): Dp {
    val (effectiveStart, effectiveEnd) = effectiveEventTimesForDay(event, selectedDay)
    val startMinutes = effectiveStart.hour * 60 + effectiveStart.minute
    val endMinutes = effectiveEnd.hour * 60 + effectiveEnd.minute
    val duration = (endMinutes - startMinutes).coerceAtLeast(0)
    val ratio = duration / 60f
    return hourHeight * ratio
}
