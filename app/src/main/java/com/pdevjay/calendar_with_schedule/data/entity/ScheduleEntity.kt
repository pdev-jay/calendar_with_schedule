package com.pdevjay.calendar_with_schedule.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pdevjay.calendar_with_schedule.features.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.features.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.features.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType
import java.time.LocalDate

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title")val title: String,
    @ColumnInfo(name = "location")val location: String?,
    @ColumnInfo(name = "isAllDay") val isAllDay: Boolean,
    @ColumnInfo(name = "startDate") val start: DateTimePeriod,
    @ColumnInfo(name = "endDate") val end: DateTimePeriod,
    @ColumnInfo(name = "repeatType") val repeatType: RepeatType = RepeatType.NONE,
    @ColumnInfo(name = "repeatUntil") val repeatUntil: LocalDate? = null,
    @ColumnInfo(name = "repeatRule") val repeatRule: String? = null,
    @ColumnInfo(name = "alarmOption") val alarmOption: AlarmOption,
    @ColumnInfo(name = "branchId") val branchId: String? = null,
    @ColumnInfo(name = "color") val color: Int? = null
)

fun ScheduleEntity.toScheduleData(): ScheduleData {
    return ScheduleData(
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
        branchId = this.branchId,
        color = this.color
    )
}
