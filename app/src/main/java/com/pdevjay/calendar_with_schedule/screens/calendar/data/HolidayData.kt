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
        color = 0xFFFF6B81.toInt() // Soft red (휴일용 고정 색상)
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
        color = 0xFFFF6B81.toInt() // Soft red
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
        color = 0xFFFF6B81.toInt() // 또는 특정 색상 코드 예: Color.Red.hashCode()
    )
}
