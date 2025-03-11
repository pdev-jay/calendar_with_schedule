package com.pdevjay.calendar_with_schedule.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import java.time.LocalDate

@Entity(tableName = "recurring_schedules")
data class RecurringScheduleEntity(
    @PrimaryKey val id: String,  // "originalEventId_selectedDate" 형식으로 저장
    @ColumnInfo(name = "originalEventId") val originalEventId: String, // 원본 일정 ID
    @ColumnInfo(name = "startDate") val start: DateTimePeriod,
    @ColumnInfo(name = "endDate") val end: DateTimePeriod,
    @ColumnInfo(name = "title") val title: String?, // 변경된 제목 (null이면 원본 제목 사용)
    @ColumnInfo(name = "isDeleted") val isDeleted: Boolean // 해당 날짜의 일정이 삭제되었는지 여부
)
