package com.pdevjay.calendar_with_schedule.data.database

import androidx.room.*
import com.pdevjay.calendar_with_schedule.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM tasks")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("""
    SELECT * FROM tasks 
    WHERE repeatType = 'NONE' AND (
        strftime('%Y-%m', substr(startDate, 1, instr(startDate, '|') - 1)) IN (:months)
        OR strftime('%Y-%m', substr(endDate, 1, instr(endDate, '|') - 1)) IN (:months)
    )
    OR repeatType != 'NONE'
""")
    fun getSchedulesForMonths(months: List<String>): Flow<List<ScheduleEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE (repeatType = 'NONE' AND strftime('%Y-%m-%d', substr(startDate, 1, instr(startDate, '|') - 1)) = :date)
           OR (repeatType != 'NONE' AND strftime('%Y-%m-%d', substr(endDate, 1, instr(endDate, '|') - 1)) <= :date)
    """)
    fun getSchedulesForDate(date: String): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
}
