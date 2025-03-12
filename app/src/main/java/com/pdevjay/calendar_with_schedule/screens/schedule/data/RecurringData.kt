package com.pdevjay.calendar_with_schedule.screens.schedule.data

import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity

data class RecurringData(
    val id: String,  // "originalEventId_selectedDate" 형식으로 저장
    val originalEventId: String, // 원본 일정 ID
    val start: DateTimePeriod, // 시작 날짜 및 시간
    val end: DateTimePeriod, // 종료 날짜 및 시간
    val title: String?, // 변경된 제목 (null이면 원본 제목 사용)
    val isDeleted: Boolean // 해당 날짜의 일정이 삭제되었는지 여부
)

fun RecurringScheduleEntity.toRecurringData(): RecurringData {
    return RecurringData(
        id = this.id,
        originalEventId = this.originalEventId,
        start = this.start,
        end = this.end,
        title = this.title,
        isDeleted = this.isDeleted
    )
}

fun RecurringData.toRecurringScheduleEntity(): RecurringScheduleEntity {
    return RecurringScheduleEntity(
        id = this.id,
        originalEventId = this.originalEventId,
        start = this.start,
        end = this.end,
        title = this.title,
        isDeleted = this.isDeleted
    )
}

fun RecurringData.toScheduleData(originalSchedule: ScheduleData): ScheduleData {
    return originalSchedule.copy(
        id = this.id, // 🔹 특정 날짜의 반복 일정이므로 ID 변경
        title = this.title ?: originalSchedule.title, // 🔹 변경된 제목이 있으면 적용
        start = this.start, // 🔹 변경된 날짜 및 시간 적용
        end = this.end,
        isOriginalEvent = false // 🔹 원본이 아님을 표시
    )
}
