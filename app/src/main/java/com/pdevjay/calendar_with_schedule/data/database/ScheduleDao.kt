package com.pdevjay.calendar_with_schedule.data.database

import androidx.room.*
import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("""
    SELECT * FROM schedules 
    WHERE (repeatType = 'NONE' AND (
        strftime('%Y-%m', substr(startDate, 1, instr(startDate, '|') - 1)) IN (:months)
        OR strftime('%Y-%m', substr(endDate, 1, instr(endDate, '|') - 1)) IN (:months)
    ))
    OR (repeatType != 'NONE' AND 
        strftime('%Y-%m', substr(startDate, 1, instr(startDate, '|') - 1)) <= :maxMonth
    )
""")
    fun getSchedulesForMonths(months: List<String>, maxMonth: String): Flow<List<ScheduleEntity>>

    @Query("""
        SELECT * FROM schedules 
        WHERE (repeatType = 'NONE' AND strftime('%Y-%m-%d', substr(startDate, 1, instr(startDate, '|') - 1)) = :date)
           OR (repeatType != 'NONE' AND strftime('%Y-%m-%d', substr(endDate, 1, instr(endDate, '|') - 1)) <= :date)
    """)
    fun getSchedulesForDate(date: String): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)


    // 반복 일정 테이블

    // 🔹 특정 날짜에서 변경된 반복 일정 가져오기
    @Query("SELECT * FROM recurring_schedules WHERE strftime('%Y-%m-%d', substr(startDate, 1, instr(startDate, '|') - 1)) = :date")
    fun getRecurringScheduleChangesForDate(date: String): Flow<List<RecurringScheduleEntity>>

    // 🔹 특정 날짜에서 삭제된 반복 일정 조회
    @Query("SELECT originalEventId FROM recurring_schedules WHERE startDate = :date AND isDeleted = 1")
    fun getDeletedRecurringSchedulesForDate(date: String): Flow<List<String>>

    // 🔹 변경된 반복 일정 저장
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringSchedule(schedule: RecurringScheduleEntity)

    // 🔹 특정 날짜에서의 반복 일정 삭제 처리
    @Query("DELETE FROM recurring_schedules WHERE originalEventId = :eventId AND startDate = :date")
    suspend fun deleteRecurringSchedule(eventId: String, date: String)
}
