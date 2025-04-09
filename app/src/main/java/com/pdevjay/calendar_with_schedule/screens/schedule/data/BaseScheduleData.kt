package com.pdevjay.calendar_with_schedule.screens.schedule.data

import com.google.gson.annotations.SerializedName
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatType
import java.time.LocalDate

// Json 직렬화시 부모의 필드명과 자식의 필드명이 중복처리되며 에러 -> Transient 추가하여 무시하도록 유도
abstract class BaseSchedule(
    @Transient open val id: String,
    @Transient open val title: String,
    @Transient open val location: String?,
    @Transient open val isAllDay: Boolean,
    @Transient open val start: DateTimePeriod,
    @Transient open val end: DateTimePeriod,
    @Transient open val repeatType: RepeatType,
    @Transient open val repeatUntil: LocalDate?,
    @Transient open val repeatRule: String?,
    @Transient open val alarmOption: AlarmOption,
    @Transient open val branchId: String?,
    @Transient open val color: Int? = null
)

// 겹침 여부 확인 함수
fun BaseSchedule.overlapsWith(other: BaseSchedule): Boolean {
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

data class ScheduleDiff(
    val field: String,
    val oldValue: Any?,
    val newValue: Any?
)

fun BaseSchedule.getDiffsComparedTo(other: BaseSchedule): List<ScheduleDiff> {
    val diffs = mutableListOf<ScheduleDiff>()

    if (title != other.title) diffs.add(ScheduleDiff("title", other.title, title))
    if (location != other.location) diffs.add(ScheduleDiff("location", other.location, location))
    if (isAllDay != other.isAllDay) diffs.add(ScheduleDiff("isAllDay", other.isAllDay, isAllDay))
    if (start != other.start) diffs.add(ScheduleDiff("start", other.start, start))
    if (end != other.end) diffs.add(ScheduleDiff("end", other.end, end))
    if (repeatType != other.repeatType) diffs.add(ScheduleDiff("repeatType", other.repeatType, repeatType))
    if (repeatUntil != other.repeatUntil) diffs.add(ScheduleDiff("repeatUntil", other.repeatUntil, repeatUntil))
    if (alarmOption != other.alarmOption) diffs.add(ScheduleDiff("alarmOption", other.alarmOption, alarmOption))
    if (color != other.color) diffs.add(ScheduleDiff("color", other.color, color))
    return diffs
}
