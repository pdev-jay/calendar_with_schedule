package com.pdevjay.calendar_with_schedule.screens.schedule.data

import androidx.room.ColumnInfo
import com.pdevjay.calendar_with_schedule.data.entity.ScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatOption
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID


// 단일 이벤트(일정)를 나타내는 데이터 클래스
data class ScheduleData(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Event",
    val location: String? = null,
    val start: DateTimePeriod,
    val end: DateTimePeriod,
    val repeatType: RepeatType = RepeatType.NONE, // 🔹 RepeatType 사용
    val repeatRule: String? = null, // 🔹 RRule을 저장할 문자열
    val alarmOption: AlarmOption = AlarmOption.NONE, // 🔹 알림 옵션 추가
    val isOriginalEvent: Boolean = true
)

// 겹침 여부 확인 함수
fun ScheduleData.overlapsWith(other: ScheduleData): Boolean {
    // 현재 일정의 시작/종료 시간을 분(Minutes) 단위로 변환
    val thisStart = this.start.toMinutes()
    val thisEnd = this.end.toMinutes()

    // 비교 대상 일정의 시작/종료 시간을 분(Minutes) 단위로 변환
    val otherStart = other.start.toMinutes()
    val otherEnd = other.end.toMinutes()

    // 시간이 겹치는 경우의 조건:
    //    1. 현재 일정의 시작 시간이 다른 일정의 종료 시간보다 앞이어야 함 (thisStart < otherEnd)
    //    2. 현재 일정의 종료 시간이 다른 일정의 시작 시간보다 뒤이어야 함 (thisEnd > otherStart)
    return thisStart < otherEnd && thisEnd > otherStart
}

// ScheduleData <-> TaskEntity 변환 함수들
fun ScheduleData.toScheduleEntity() = ScheduleEntity(
    id = id,
    title = title,
    location = location,
    start = start,
    end = end,
    repeatType = repeatType,
    repeatRule = repeatRule,              // RRule 그대로 저장
    alarmOption = alarmOption,             // Enum 변환
    isOriginalEvent = isOriginalEvent
)

fun ScheduleEntity.toScheduleData() = ScheduleData(
    id = id,
    title = title,
    location = location,
    start = start,
    end = end,
    repeatType = repeatType,         // Enum 변환 유지
    repeatRule = repeatRule,             // RRule 그대로 유지
    alarmOption = alarmOption,            // Enum 변환 유지
    isOriginalEvent = isOriginalEvent
)
data class DateTimePeriod(
    val date: LocalDate,
    val time: LocalTime
)

fun DateTimePeriod.toDateTime(): LocalDateTime {
    return LocalDateTime.of(date, time)
}

// DateTimePeriod -> 분 단위 변환
fun DateTimePeriod.toMinutes(): Int {
    return this.date.dayOfYear * 1440 + this.time.hour * 60 + this.time.minute
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
