package com.pdevjay.calendar_with_schedule.data.database

import androidx.room.*
import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    /**
     * 모든 스케줄을 조회합니다.
     *
     * @return 모든 스케줄 목록을 Flow 형태로 반환
     */
    @Query("SELECT * FROM schedules")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    /**
     * 특정 날짜의 스케줄을 조회합니다.
     *
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 해당 날짜의 스케줄 목록을 Flow 형태로 반환
     */
    @Query("""
        SELECT * FROM schedules 
        WHERE (repeatType = 'NONE' AND strftime('%Y-%m-%d', substr(startDate, 1, instr(startDate, '|') - 1)) = :date)
           OR (repeatType != 'NONE' AND strftime('%Y-%m-%d', substr(endDate, 1, instr(endDate, '|') - 1)) <= :date)
    """)
    fun getSchedulesForDate(date: String): Flow<List<ScheduleEntity>>

    /**
     * 특정 월에 포함된 스케줄을 조회합니다.
     *
     * @param months 조회할 월 목록 (yyyy-MM 형식)
     * @param minMonth 반복 일정에서 최소 검색 범위 (yyyy-MM 형식)
     * @param maxMonth 반복 일정에서 최대 검색 범위 (yyyy-MM 형식)
     * @return 해당 월의 스케줄 목록을 Flow 형태로 반환
     */
    @Query("""
    SELECT * FROM schedules 
    WHERE (repeatType = 'NONE' AND (
        strftime('%Y-%m', substr(startDate, 1, instr(startDate, '|') - 1)) IN (:months)
        OR strftime('%Y-%m', substr(endDate, 1, instr(endDate, '|') - 1)) IN (:months)
    ))
    OR (repeatType != 'NONE' AND 
        strftime('%Y-%m', substr(startDate, 1, instr(startDate, '|') - 1)) <= :maxMonth
        AND (repeatUntil IS NULL OR strftime('%Y-%m', repeatUntil) >= :minMonth) -- 🔥 추가된 조건
    )
""")
    fun getSchedulesForMonths(months: List<String>, minMonth: String, maxMonth: String): Flow<List<ScheduleEntity>>

    /**
     * 새로운 스케줄을 추가하거나 기존 스케줄을 갱신합니다.
     *
     * @param schedule 추가 또는 업데이트할 스케줄
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    /**
     * 특정 스케줄을 삭제합니다.
     *
     * @param schedule 삭제할 스케줄
     */
    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    /**
     * 특정 ID를 포함하며, 지정된 날짜 이후의 모든 스케줄을 삭제합니다.
     *
     * @param scheduleId 삭제할 스케줄의 ID
     * @param date 해당 날짜(yyyy-MM-dd) 이후의 스케줄 삭제
     */
    @Query("DELETE FROM schedules WHERE id LIKE :scheduleId || '%' AND strftime('%Y-%m-%d', substr(startDate, 1, instr(startDate, '|') - 1)) >= :date")
    suspend fun deleteFutureSchedule(scheduleId: String, date: String)


    /**
     * 특정 원본 이벤트 ID를 기반으로 반복 일정의 `repeatUntil`을 업데이트합니다.
     */
    @Query("UPDATE schedules SET repeatUntil = :repeatUntil WHERE id = :originalEventId")
    suspend fun updateRepeatUntil(originalEventId: String, repeatUntil: String)

    /**
     * 특정 원본 이벤트 ID와 반복 일정의 `repeatUntil`을 기반으로 `repeatUntil` 이후 반복 일정을 삭제합니다.
     */
    @Query("DELETE FROM schedules WHERE id LIKE :originalEventId || '%' AND strftime('%Y-%m-%d', substr(startDate, 1, instr(startDate, '|') - 1)) >= :repeatUntil")
    suspend fun deleteFutureRecurringSchedule(originalEventId: String, repeatUntil: String)

}
