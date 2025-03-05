package com.pdevjay.calendar_with_schedule.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pdevjay.calendar_with_schedule.datamodels.DateTimePeriod

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val location: String?,
    val start: DateTimePeriod,
    val end: DateTimePeriod
)
