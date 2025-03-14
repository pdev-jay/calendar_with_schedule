package com.pdevjay.calendar_with_schedule.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import java.time.LocalDate

@Entity(tableName = "recurring_schedules")
data class RecurringScheduleEntity(
    @PrimaryKey val id: String, // "originalEventId_selectedDate"
    @ColumnInfo(name = "originalEventId") val originalEventId: String,
    @ColumnInfo(name = "originalRecurringDate") val originalRecurringDate: LocalDate,
    @ColumnInfo(name = "originatedFrom") val originatedFrom: String,
    @ColumnInfo(name = "startDate") val start: DateTimePeriod,
    @ColumnInfo(name = "endDate") val end: DateTimePeriod,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "location") val location: String?, // ✅ 추가
    @ColumnInfo(name = "isAllDay") val isAllDay: Boolean, // ✅ 추가
    @ColumnInfo(name = "repeatType") val repeatType: RepeatType, // ✅ 추가
    @ColumnInfo(name = "repeatUntil") val repeatUntil: LocalDate?, // ✅ 추가
    @ColumnInfo(name = "repeatRule") val repeatRule: String?, // ✅ 추가
    @ColumnInfo(name = "alarmOption") val alarmOption: AlarmOption, // ✅ 추가
    @ColumnInfo(name = "isOriginalSchedule") val isOriginalSchedule: Boolean = false,
    @ColumnInfo(name = "isDeleted") val isDeleted: Boolean, // 해당 날짜의 일정이 삭제되었는지 여부
    @ColumnInfo(name = "originalRepeatUntil") val originalRepeatUntil: LocalDate? // ✅ 추가
)

fun RecurringScheduleEntity.toRecurringData(): RecurringData {
    return RecurringData(
        id = this.id,
        originalEventId = this.originalEventId,
        originatedFrom = this.originatedFrom,
        originalRecurringDate = this.originalRecurringDate,
        title = this.title ?: "",
        location = this.location,
        isAllDay = this.isAllDay,
        start = this.start,
        end = this.end,
        repeatType = this.repeatType,
        repeatUntil = this.repeatUntil,
        repeatRule = this.repeatRule,
        alarmOption = this.alarmOption,
        isOriginalSchedule = this.isOriginalSchedule,
        isDeleted = this.isDeleted,
        originalRepeatUntil = this.originalRepeatUntil
    )
}
