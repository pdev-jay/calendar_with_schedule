package com.pdevjay.calendar_with_schedule.screens.calendar

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.calendar.data.HolidayData
import com.pdevjay.calendar_with_schedule.screens.calendar.data.toBaseSchedule
import com.pdevjay.calendar_with_schedule.screens.calendar.data.toHolidaySchedule
import com.pdevjay.calendar_with_schedule.screens.calendar.data.toMergedHolidaySchedules
import com.pdevjay.calendar_with_schedule.screens.calendar.data.toRecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.utils.SharedPreferencesUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DaysGrid(
    days: List<CalendarDay>,
    scheduleMap: Map<LocalDate, List<BaseSchedule>>,
    holidayMap: Map<LocalDate, List<HolidayData>>,
    onDayClick: (CalendarDay) -> Unit
) {
    val context = LocalContext.current
    val isShowLunarDate = SharedPreferencesUtil.getBoolean(context, SharedPreferencesUtil.KEY_SHOW_LUNAR_DATE, false)

    val weeks = calculateWeeks(days)
    val firstWeek = weeks.firstOrNull() ?: emptyList() //  첫 번째 주 가져오기

    val density = LocalDensity.current

    val lunarDateHeight = if (isShowLunarDate) (with(density) { MaterialTheme.typography.labelSmall.lineHeight.toDp() }) else 0.dp
    val dayCellHeight =
        (with(density) { MaterialTheme.typography.bodyLarge.lineHeight.toDp() }) + lunarDateHeight
    val monthLabelHeight = with(density) { MaterialTheme.typography.titleLarge.lineHeight.toDp() }

    val dividerHeight = 1.dp

    // 날짜 터치 영역의 사이즈를 구하기 위한 WeekScheduleRow의 높이에 관련된 변수들
    val eventPreviewRowHeight =
        (with(density) { MaterialTheme.typography.labelSmall.lineHeight.toDp() })
    val eventPreviewMaxRow = 5
    val eventPreviewRowSpaceBy = 1.dp
    val eventPreviewBottomPadding = 2.dp

    // 날짜 cell padding
    val dayCellPadding = 2.dp

    val cellTotalHeight =
        dayCellHeight +
                (eventPreviewRowHeight * eventPreviewMaxRow) +
                (eventPreviewRowSpaceBy * (eventPreviewMaxRow - 1)) + dividerHeight + (dayCellPadding * 2) + eventPreviewBottomPadding

    weeks.forEach { week ->
        val weekHoliday = (holidayMap
            .filterKeys { it in week.mapNotNull { it?.date } }
            .flatMap { it.value }
            .toMergedHolidaySchedules()
//            .map { it.toHolidaySchedule() } // RecurringData 변환
        )

        val weekSchedules = scheduleMap
            .filterKeys { it in week.mapNotNull { it?.date } }
            .flatMap { it.value }
            .distinctBy { it.id }

        val mergedSchedules = weekSchedules + weekHoliday

        Box(
            modifier = Modifier
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
            ) {
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { dayOrNull ->
                        if (dayOrNull == null) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dayCellHeight)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                if (dayOrNull.date.dayOfMonth == 1) {
                                    val formatter =
                                        DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
                                    Text(
                                        text = dayOrNull.date.format(formatter),
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .height(monthLabelHeight),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (firstWeek.contains(dayOrNull)) {
                                    Spacer(Modifier.height(monthLabelHeight))
                                }

                                HorizontalDivider(
                                    thickness = dividerHeight,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                )

                                DayCell(
                                    day = dayOrNull,
                                    dayCellPadding = dayCellPadding,
                                    isShowLunarDate = isShowLunarDate
                                )
                            }

                        }
                    }
                }
                WeekScheduleRow(
                    week = week,
                    schedules = mergedSchedules,
                    rowHeight = eventPreviewRowHeight,
                    maxRow = eventPreviewMaxRow,
                    rowSpaceBy = eventPreviewRowSpaceBy,
                    bottomPadding = eventPreviewBottomPadding
                )
            }
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(
                            Modifier
                                .weight(1f)
                                .height(cellTotalHeight)
                        )
                    } else {

                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            if (firstWeek.contains(day)) {
                                Spacer(Modifier.height(monthLabelHeight))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cellTotalHeight)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { onDayClick(day) },
                            )
                        }
                    }
                }
            }
        }
    }
}

fun calculateWeeks(days: List<CalendarDay>): List<List<CalendarDay?>> {
    if (days.isEmpty()) return emptyList()

    val startOffset = days.first().date.dayOfWeek.value % 7
    val paddedStart = List(startOffset) { null } + days

    // 마지막 주도 7일로 채우기 위해 패딩 추가
    val endOffset = (7 - (paddedStart.size % 7)) % 7
    val paddedDays = paddedStart + List(endOffset) { null }

    return paddedDays.chunked(7)
}
