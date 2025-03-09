package com.pdevjay.calendar_with_schedule.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.enum.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enum.RepeatOption

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val location: String?,
    val start: DateTimePeriod,
    val end: DateTimePeriod,
//    val repeatOption: RepeatOption,
//
//    val repeatRule: String?,
//
//    val alarmOption: AlarmOption
)

// ScheduleData <-> TaskEntity 변환 함수들
fun ScheduleData.toTaskEntity() = TaskEntity(
    id = id,
    title = title,
    location = location,
    start = start,
    end = end,
//    repeatOption = repeatOption,          // Enum 변환
//    repeatRule = repeatRule,              // RRule 그대로 저장
//    alarmOption = alarmOption             // Enum 변환
)

fun TaskEntity.toScheduleData() = ScheduleData(
    id = id,
    title = title,
    location = location,
    start = start,
    end = end,
//    repeatOption = repeatOption,         // Enum 변환 유지
//    repeatRule = repeatRule,             // RRule 그대로 유지
//    alarmOption = alarmOption            // Enum 변환 유지
)

