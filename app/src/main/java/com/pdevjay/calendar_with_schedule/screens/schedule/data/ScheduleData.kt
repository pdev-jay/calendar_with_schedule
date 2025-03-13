package com.pdevjay.calendar_with_schedule.screens.schedule.data

import com.google.gson.annotations.SerializedName
import com.pdevjay.calendar_with_schedule.data.entity.ScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID


// 단일 이벤트(일정)를 나타내는 데이터 클래스
data class ScheduleData(
    @SerializedName("id") override val id: String = UUID.randomUUID().toString(),
    @SerializedName("title") override val title: String = "New Event",
    @SerializedName("location") override val location: String? = null,
    @SerializedName("isAllDay") override val isAllDay: Boolean = false,
    @SerializedName("start") override val start: DateTimePeriod,
    @SerializedName("end") override val end: DateTimePeriod,
    @SerializedName("repeatType") override val repeatType: RepeatType = RepeatType.NONE,
    @SerializedName("repeatUntil") override val repeatUntil: LocalDate? = null,
    @SerializedName("repeatRule") override val repeatRule: String? = null,
    @SerializedName("alarmOption") override val alarmOption: AlarmOption = AlarmOption.NONE,
    @SerializedName("isOriginalSchedule") override val isOriginalSchedule: Boolean = true
) : BaseSchedule(id, title, location, isAllDay, start, end, repeatType, repeatUntil, repeatRule, alarmOption, isOriginalSchedule)

// ScheduleData <-> TaskEntity 변환 함수들
fun ScheduleData.toScheduleEntity(): ScheduleEntity {
    return ScheduleEntity(
        id = this.id,
        title = this.title,
        location = this.location,
        isAllDay = this.isAllDay,
        start = this.start,
        end = this.end,
        repeatType = this.repeatType,
        repeatUntil = this.repeatUntil,
        repeatRule = this.repeatRule,
        alarmOption = this.alarmOption,
        isOriginalSchedule = this.isOriginalSchedule
    )
}


fun ScheduleData.toRecurringData(selectedDate: LocalDate): RecurringData {
    return RecurringData(
        id = "${this.id}_${selectedDate}", // 고유 ID 생성
        originalEventId = this.id,
        originalRecurringDate = selectedDate, // 원본 반복 일정 날짜
        title = this.title,
        location = this.location,
        isAllDay = this.isAllDay,
        start = this.start.copy(date = selectedDate),
        end = this.end.copy(date = selectedDate),
        repeatType = this.repeatType,
        repeatUntil = this.repeatUntil,
        repeatRule = this.repeatRule,
        alarmOption = this.alarmOption,
        isOriginalSchedule = false,
        isDeleted = false // 기본적으로 삭제되지 않음
    )
}


fun generateRepeatRule(repeatType: RepeatType): String? {
    return when (repeatType) {
        RepeatType.NONE -> null
        RepeatType.DAILY -> "FREQ=DAILY"
        RepeatType.WEEKLY -> "FREQ=WEEKLY"
        RepeatType.BIWEEKLY -> "FREQ=WEEKLY;INTERVAL=2"
        RepeatType.MONTHLY -> "FREQ=MONTHLY"
        RepeatType.YEARLY -> "FREQ=YEARLY"
    }
}
