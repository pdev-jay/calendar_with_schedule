package com.pdevjay.calendar_with_schedule.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val location: String?,
    val start: DateTimePeriod,
    val end: DateTimePeriod
)

// ScheduleData <-> TaskEntity 변환 함수들
fun ScheduleData.toTaskEntity() = TaskEntity(
    id = id,
    title = title,
    location = location,
    start = start,
    end = end
)

fun TaskEntity.toScheduleData() = ScheduleData(
    id = id,
    title = title,
    location = location,
    start = start,
    end = end
)
