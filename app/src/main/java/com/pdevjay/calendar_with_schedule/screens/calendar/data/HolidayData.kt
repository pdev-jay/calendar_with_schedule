package com.pdevjay.calendar_with_schedule.screens.calendar.data

import com.pdevjay.calendar_with_schedule.data.entity.HolidayDataEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatType
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class HolidayData(
    val date: String,
    val name: String,
    val isHoliday: Boolean,
    val seq: Int,
    val updatedAt: String
)

data class HolidaySchedule(
    override val id: String,
    override val title: String,
    override val location: String?,
    override val isAllDay: Boolean,
    override val start: DateTimePeriod,
    override val end: DateTimePeriod,
    override val repeatType: RepeatType,
    override val repeatUntil: LocalDate?,
    override val repeatRule: String?,
    override val alarmOption: AlarmOption,
    override val branchId: String?,
    override val color: Int?
) : BaseSchedule(
    id, title, location, isAllDay, start, end,
    repeatType, repeatUntil, repeatRule, alarmOption, branchId, color
)

fun HolidayData.toHolidaySchedule(): HolidaySchedule {
    val localDate = LocalDate.parse(this.date)

    return HolidaySchedule(
        id = "holiday_${this.date}_${this.seq}",
        title = this.name,
        location = null,
        isAllDay = true,
        start = DateTimePeriod(localDate, LocalTime.MIN),
        end = DateTimePeriod(localDate, LocalTime.MAX),
        repeatType = RepeatType.NONE,
        repeatUntil = null,
        repeatRule = null,
        alarmOption = AlarmOption.NONE,
        branchId = null,
        color = 0xFFFF6B81.toInt()
    )
}


fun HolidayData.toEntity() = HolidayDataEntity(date, name, isHoliday, seq, updatedAt)


fun HolidayData.toBaseSchedule(): BaseSchedule {
    val localDate = LocalDate.parse(this.date)
    return HolidaySchedule(
        id = "holiday_${this.date}_${this.seq}",
        title = this.name,
        location = null,
        isAllDay = true,
        start = DateTimePeriod(localDate, LocalTime.MIN),
        end = DateTimePeriod(localDate, LocalTime.MAX),
        repeatType = RepeatType.NONE,
        repeatUntil = null,
        repeatRule = null,
        alarmOption = AlarmOption.NONE,
        branchId = null,
        color = 0xFFFF6B81.toInt()
    )
}

fun HolidayData.toRecurringData(): RecurringData {
    val date = LocalDate.parse(this.date)

    return RecurringData(
        id = UUID.randomUUID().toString(),
        originalEventId = "holiday_${this.date}_${this.seq}",
        originalRecurringDate = date,
        originatedFrom = "HOLIDAY",
        title = this.name,
        location = null,
        isAllDay = true,
        start = DateTimePeriod(date, LocalTime.MIN),
        end = DateTimePeriod(date, LocalTime.MAX),
        repeatType = RepeatType.NONE,
        repeatUntil = null,
        repeatRule = null,
        alarmOption = AlarmOption.NONE,
        isDeleted = false,
        isFirstSchedule = true,
        branchId = null,
        repeatIndex = 1,
        color = 0xFFFF6B81.toInt()
    )
}

fun List<HolidayData>.toMergedHolidaySchedules(): List<HolidaySchedule> {
    // 1. 이름 기준으로 그룹핑
    return this
        .groupBy { it.name }
        .flatMap { (name, holidaysWithSameName) ->
            // 2. 날짜 기준 정렬
            holidaysWithSameName
                .map { it.copy(date = it.date.trim()) }
                .sortedBy { it.date }
                .fold(mutableListOf<MutableList<HolidayData>>()) { acc, current ->
                    val currentDate = LocalDate.parse(current.date)

                    if (acc.isEmpty()) {
                        acc.add(mutableListOf(current))
                    } else {
                        val lastGroup = acc.last()
                        val lastDate = LocalDate.parse(lastGroup.last().date)

                        if (lastDate.plusDays(1) == currentDate) {
                            lastGroup.add(current) // 인접하면 현재 그룹에 추가
                        } else {
                            acc.add(mutableListOf(current)) // 아니면 새 그룹
                        }
                    }

                    acc
                }
                .map { group ->
                    val sorted = group.sortedBy { it.date }
                    val startDate = LocalDate.parse(sorted.first().date)
                    val endDate = LocalDate.parse(sorted.last().date)

                    HolidaySchedule(
                        id = "holiday_${name}_${startDate}_$endDate",
                        title = name,
                        location = null,
                        isAllDay = true,
                        start = DateTimePeriod(startDate, LocalTime.MIN),
                        end = DateTimePeriod(endDate, LocalTime.MAX),
                        repeatType = RepeatType.NONE,
                        repeatUntil = null,
                        repeatRule = null,
                        alarmOption = AlarmOption.NONE,
                        branchId = null,
                        color = 0xFFFF6B81.toInt()
                    )
                }
        }
}
