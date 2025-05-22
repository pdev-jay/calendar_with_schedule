package com.pdevjay.calendar_with_schedule.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pdevjay.calendar_with_schedule.data.entity.HolidayDataEntity
import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.data.entity.ScheduleEntity
import com.pdevjay.calendar_with_schedule.core.utils.converters.AlarmOptionConverter
import com.pdevjay.calendar_with_schedule.core.utils.converters.DateTimePeriodConverter
import com.pdevjay.calendar_with_schedule.data.database.dao.HolidayDao
import com.pdevjay.calendar_with_schedule.data.database.dao.RecurringScheduleDao
import com.pdevjay.calendar_with_schedule.data.database.dao.ScheduleDao

@Database(entities = [ScheduleEntity::class, RecurringScheduleEntity::class, HolidayDataEntity::class], version = 2)
@TypeConverters(DateTimePeriodConverter::class, AlarmOptionConverter::class)
abstract class ScheduleDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun recurringScheduleDao(): RecurringScheduleDao
    abstract fun holidayDao(): HolidayDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS holidays (
                date TEXT NOT NULL,
                name TEXT NOT NULL,
                isHoliday INTEGER NOT NULL,
                seq INTEGER NOT NULL,
                updatedAt TEXT NOT NULL DEFAULT '2000-01-01T00:00:00Z',
                PRIMARY KEY(date, seq)
            )
        """.trimIndent()
                )
            }
        }
    }
}
