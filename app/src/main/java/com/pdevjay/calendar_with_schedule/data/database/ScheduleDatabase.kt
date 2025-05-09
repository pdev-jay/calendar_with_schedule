package com.pdevjay.calendar_with_schedule.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.data.entity.ScheduleEntity
import com.pdevjay.calendar_with_schedule.utils.AlarmOptionConverter
import com.pdevjay.calendar_with_schedule.utils.DateTimePeriodConverter

@Database(entities = [ScheduleEntity::class, RecurringScheduleEntity::class], version = 1)
@TypeConverters(DateTimePeriodConverter::class, AlarmOptionConverter::class)
abstract class ScheduleDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun recurringScheduleDao(): RecurringScheduleDao //  추가
}
