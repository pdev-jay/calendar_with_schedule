package com.pdevjay.calendar_with_schedule.screens.schedule.data

import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatOption
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
    val repeatOption: RepeatOption = RepeatOption.NONE, // 🔹 반복 옵션 추가
    val repeatRule: String? = null, // 🔹 RRule 저장
    val alarmOption: AlarmOption = AlarmOption.NONE // 🔹 알림 옵션 추가
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

fun generateRepeatRule(repeatOption: RepeatOption, repeatCount: Int = 30): String? {
    return when (repeatOption) {
        RepeatOption.NONE -> null
        RepeatOption.DAILY -> "FREQ=DAILY;COUNT=$repeatCount"
        RepeatOption.WEEKLY -> "FREQ=WEEKLY;COUNT=$repeatCount"
        RepeatOption.BIWEEKLY -> "FREQ=WEEKLY;INTERVAL=2;COUNT=$repeatCount"
        RepeatOption.MONTHLY -> "FREQ=MONTHLY;COUNT=$repeatCount"
        RepeatOption.YEARLY -> "FREQ=YEARLY;COUNT=$repeatCount"
    }
}
