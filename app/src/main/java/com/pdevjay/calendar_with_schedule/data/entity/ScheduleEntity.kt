package com.pdevjay.calendar_with_schedule.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatOption
import com.pdevjay.calendar_with_schedule.utils.RepeatType

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val location: String?,
    @ColumnInfo(name = "startDate") val start: DateTimePeriod,
    @ColumnInfo(name = "endDate") val end: DateTimePeriod,
    @ColumnInfo(name = "repeatType") val repeatType: RepeatType = RepeatType.NONE, // 🔹 RepeatType 사용
    @ColumnInfo(name = "repeatRule") val repeatRule: String? = null, // 🔹 RRule을 저장할 문자열
    @ColumnInfo(name = "alarmOption")val alarmOption: AlarmOption,
    @ColumnInfo(name = "isOriginalEvent") val isOriginalEvent: Boolean = true
)



