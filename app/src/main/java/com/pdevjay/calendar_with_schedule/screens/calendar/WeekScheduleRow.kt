package com.pdevjay.calendar_with_schedule.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import java.time.LocalDate

@Composable
fun WeekScheduleRow(
    week: List<CalendarDay?>,
    schedules: List<BaseSchedule>,
    modifier: Modifier = Modifier,
) {
    val weekStart = week.firstOrNull()?.date ?: return
    val weekEnd = week.lastOrNull()?.date ?: return

    // 1. 주에 걸친 일정만 추출
    val visibleSchedules = schedules.filter { it.start.date <= weekEnd && it.end.date >= weekStart }

    // 2. 겹치지 않게 줄 분배
    val rows = mutableListOf<MutableList<BaseSchedule>>()

    visibleSchedules.forEach { schedule ->
        val placed = rows.firstOrNull { row ->
            row.none { isOverlapInWeek(it, schedule, weekStart, weekEnd) }
        }
        if (placed != null) {
            placed.add(schedule)
        } else {
            rows.add(mutableListOf(schedule))
        }
    }

    // 3. UI
    Column(modifier = modifier.fillMaxWidth()) {
        rows.forEach { rowSchedules ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .padding(vertical = 1.dp)
            ) {
                val totalDays = week.size
                val dayToIndex = week.mapIndexedNotNull { index, day -> day?.date to index }.toMap()

                rowSchedules.forEach { schedule ->
                    val startIndex = dayToIndex[schedule.start.date]?.coerceAtLeast(0)
                        ?: (dayToIndex.keys.indexOfFirst { it!! > schedule.start.date }.takeIf { it >= 0 } ?: 0)

                    val endIndex = dayToIndex[schedule.end.date]?.coerceAtMost(totalDays - 1)
                        ?: (dayToIndex.keys.indexOfLast { it!! < schedule.end.date }
                            .takeIf { it >= 0 } ?: (totalDays - 1))

                    val span = (endIndex - startIndex + 1).coerceAtLeast(1)

                    Spacer(modifier = Modifier.weight(startIndex.toFloat()))

                    Box(
                        modifier = Modifier
                            .weight(span.toFloat())
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                    )

                    Spacer(modifier = Modifier.weight((totalDays - endIndex - 1).toFloat()))
                }
            }
        }
    }
}

private fun isOverlapInWeek(a: BaseSchedule, b: BaseSchedule, weekStart: LocalDate, weekEnd: LocalDate): Boolean {
    val aStart = a.start.date.coerceAtLeast(weekStart)
    val aEnd = a.end.date.coerceAtMost(weekEnd)

    val bStart = b.start.date.coerceAtLeast(weekStart)
    val bEnd = b.end.date.coerceAtMost(weekEnd)

    return aStart <= bEnd && bStart <= aEnd
}
