package com.pdevjay.calendar_with_schedule.screens.schedule.data

import com.google.gson.annotations.SerializedName
import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import java.time.LocalDate

data class RecurringData(
    @SerializedName("id") override val id: String,
    @SerializedName("originalEventId") val originalEventId: String,
    @SerializedName("originalRecurringDate") val originalRecurringDate: LocalDate,
    @SerializedName("title") override val title: String,
    @SerializedName("location") override val location: String?,
    @SerializedName("isAllDay") override val isAllDay: Boolean,
    @SerializedName("start") override val start: DateTimePeriod,
    @SerializedName("end") override val end: DateTimePeriod,
    @SerializedName("repeatType") override val repeatType: RepeatType,
    @SerializedName("repeatUntil") override val repeatUntil: LocalDate?,
    @SerializedName("repeatRule") override val repeatRule: String?,
    @SerializedName("alarmOption") override val alarmOption: AlarmOption,
    @SerializedName("isOriginalSchedule") override val isOriginalSchedule: Boolean = false,
    @SerializedName("isDeleted") val isDeleted: Boolean
) : BaseSchedule(id, title, location, isAllDay, start, end, repeatType, repeatUntil, repeatRule, alarmOption, isOriginalSchedule)


fun RecurringData.toRecurringScheduleEntity(): RecurringScheduleEntity {
    return RecurringScheduleEntity(
        id = this.id,
        originalEventId = this.originalEventId,
        originalRecurringDate = this.originalRecurringDate,
        title = this.title,
        location = this.location,
        isAllDay = this.isAllDay,
        start = this.start,
        end = this.end,
        repeatType = this.repeatType,
        repeatUntil = this.repeatUntil,
        repeatRule = this.repeatRule,
        alarmOption = this.alarmOption,
        isOriginalSchedule = this.isOriginalSchedule,
        isDeleted = this.isDeleted
    )
}

// recurring data를 기반으로 새로운 schedule data를 저장할 때
fun RecurringData.toScheduleData(): ScheduleData {
    return ScheduleData(
        id = this.id,
        title = this.title ?: "New Event", // 기본 제목 설정
        location = this.location,
        isAllDay = this.isAllDay,
        start = this.start,
        end = this.end,
        repeatType = this.repeatType,
        repeatUntil = this.repeatUntil,
        repeatRule = this.repeatRule,
        alarmOption = this.alarmOption,
        isOriginalSchedule = true // 반복 일정이므로 false
    )
}
