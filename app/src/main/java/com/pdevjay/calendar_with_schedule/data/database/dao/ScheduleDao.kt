package com.pdevjay.calendar_with_schedule.data.database.dao

import androidx.room.*
import com.pdevjay.calendar_with_schedule.data.entity.ScheduleEntity
import com.pdevjay.calendar_with_schedule.features.schedule.enums.AlarmOption
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
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
        AND (repeatUntil IS NULL OR strftime('%Y-%m', repeatUntil) >= :minMonth) --  추가된 조건
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

    @Query("""
        DELETE FROM schedules WHERE id = :scheduleId
    """)
    suspend fun deleteScheduleById(scheduleId: String)
    @Query("""
        DELETE FROM schedules WHERE branchId = :branchId
    """)
    suspend fun deleteScheduleByBranchId(branchId: String)
    /**
     * 특정 원본 이벤트 ID를 기반으로 반복 일정의 `repeatUntil`을 업데이트합니다.
     */
    @Query("UPDATE schedules SET repeatUntil = :repeatUntil WHERE branchId = :branchId AND (repeatUntil >= :repeatUntil OR repeatUntil IS NULL)")
    suspend fun updateRepeatUntil(branchId: String, repeatUntil: String)

    suspend fun updateContentOnly(scheduleId: String? = null, schedule: ScheduleEntity){
        updateContentOnly(
            scheduleId = scheduleId ?: schedule.id,
            title = schedule.title,
            location = schedule.location ?: "",
            isAllDay = schedule.isAllDay,
            alarmOption = schedule.alarmOption
        )
    }

    @Query("""
    UPDATE schedules 
    SET title = :title, 
        location = :location, 
        isAllDay = :isAllDay, 
        alarmOption = :alarmOption 
    WHERE id = :scheduleId
""")
    suspend fun updateContentOnly(
        scheduleId: String,
        title: String,
        location: String,
        isAllDay: Boolean,
        alarmOption: AlarmOption
    )

    @Query("SELECT COUNT(*) FROM schedules WHERE branchId = :branchId")
    suspend fun countByBranchId(branchId: String): Int

}
