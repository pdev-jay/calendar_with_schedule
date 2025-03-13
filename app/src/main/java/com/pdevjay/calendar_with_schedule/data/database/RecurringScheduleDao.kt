package com.pdevjay.calendar_with_schedule.data.database

import androidx.room.*
import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringScheduleDao {

    /**
     * 특정 날짜의 반복 일정 변경 사항을 조회합니다.
     *
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 해당 날짜의 반복 일정 변경 목록을 Flow 형태로 반환
     */
    @Query("SELECT * FROM recurring_schedules WHERE strftime('%Y-%m-%d', substr(startDate, 1, instr(startDate, '|') - 1)) = :date")
    fun getRecurringScheduleChangesForDate(date: String): Flow<List<RecurringScheduleEntity>>

    /**
     * 특정 월에 포함된 반복 일정을 조회합니다.
     *
     * @param months 조회할 월 목록 (yyyy-MM 형식)
     * @return 해당 월의 반복 일정 목록을 Flow 형태로 반환
     */
    @Query("""
        SELECT * FROM recurring_schedules 
        WHERE strftime('%Y-%m', substr(startDate, 1, instr(startDate, '|') - 1)) IN (:months)
           OR strftime('%Y-%m', substr(endDate, 1, instr(endDate, '|') - 1)) IN (:months)
    """)
    fun getRecurringSchedulesForMonths(months: List<String>): Flow<List<RecurringScheduleEntity>>

    /**
     * 특정 날짜에 삭제된 반복 일정의 원본 이벤트 ID를 조회합니다.
     *
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 해당 날짜에 삭제된 반복 일정의 원본 이벤트 ID 목록을 Flow 형태로 반환
     */
    @Query("SELECT originalEventId FROM recurring_schedules WHERE startDate = :date AND isDeleted = 1")
    fun getDeletedRecurringSchedulesForDate(date: String): Flow<List<String>>

    /**
     * 특정 ID를 포함하며, 지정된 날짜 이후의 반복 일정을 삭제합니다.
     *
     * @param scheduleId 삭제할 반복 일정의 ID
     * @param date 해당 날짜(yyyy-MM-dd) 이후의 반복 일정 삭제
     */
    @Query("DELETE FROM recurring_schedules WHERE id LIKE :scheduleId || '%' AND strftime('%Y-%m-%d', substr(startDate, 1, instr(startDate, '|') - 1)) >= :date")
    suspend fun deleteFutureRecurringSchedule(scheduleId: String, date: String)

    /**
     * 새로운 반복 일정을 추가하거나 기존 반복 일정을 갱신합니다.
     *
     * @param schedule 추가 또는 업데이트할 반복 일정
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringSchedule(schedule: RecurringScheduleEntity)

    /**
     * 특정 원본 이벤트 ID와 시작 날짜를 기반으로 반복 일정을 삭제합니다.
     *
     * @param eventId 삭제할 반복 일정의 원본 이벤트 ID
     * @param date 삭제할 반복 일정의 시작 날짜 (yyyy-MM-dd 형식)
     */
    @Query("DELETE FROM recurring_schedules WHERE originalEventId = :eventId AND startDate = :date")
    suspend fun deleteRecurringSchedule(eventId: String, date: String)
}
