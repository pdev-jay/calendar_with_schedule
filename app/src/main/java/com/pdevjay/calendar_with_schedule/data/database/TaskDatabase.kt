package com.pdevjay.calendar_with_schedule.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pdevjay.calendar_with_schedule.data.entity.TaskEntity
import com.pdevjay.calendar_with_schedule.utils.DateTimePeriodConverter
import com.pdevjay.calendar_with_schedule.utils.EnumTypeConverter

@Database(entities = [TaskEntity::class], version = 1)
@TypeConverters(DateTimePeriodConverter::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
