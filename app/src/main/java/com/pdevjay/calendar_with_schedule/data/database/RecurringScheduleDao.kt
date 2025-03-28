package com.pdevjay.calendar_with_schedule.data.database

import androidx.room.*
import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.data.entity.ScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RecurringScheduleDao {

    @Query("""
    SELECT * FROM recurring_schedules 
    WHERE (repeatType = 'NONE' AND (
        strftime('%Y-%m', substr(startDate, 1, instr(startDate, '|') - 1)) IN (:months)
        OR strftime('%Y-%m', substr(endDate, 1, instr(endDate, '|') - 1)) IN (:months)
    ))
    OR (repeatType != 'NONE' AND 
        strftime('%Y-%m', substr(startDate, 1, instr(startDate, '|') - 1)) <= :maxMonth
                AND (repeatUntil IS NULL OR strftime('%Y-%m', repeatUntil) >= :minMonth) -- 🔥 추가된 조건
    )
""")
    fun getRecurringSchedulesForMonths(months: List<String>, minMonth: String, maxMonth: String): Flow<List<RecurringScheduleEntity>>

    /**
     * 새로운 반복 일정을 추가하거나 기존 반복 일정을 갱신합니다.
     *
     * @param schedule 추가 또는 업데이트할 반복 일정
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringSchedule(schedule: RecurringScheduleEntity)

    @Query("UPDATE recurring_schedules SET isDeleted = 1 WHERE id = :id")
    suspend fun markRecurringScheduleAsDeleted(id: String): Int

    /**
     * 특정 이벤트 ID를 기반으로 반복 일정의 `repeatUntil`을 업데이트합니다.
     */
    @Query("UPDATE recurring_schedules SET repeatUntil = :repeatUntil WHERE branchId = :branchId")
    suspend fun updateRepeatUntil(branchId: String, repeatUntil: String)

    suspend fun updateContentOnly(schedule: RecurringScheduleEntity){
        updateContentOnly(
            originalEventId = schedule.originalEventId,
            title = schedule.title ?: "",
            location = schedule.location ?: "",
            isAllDay = schedule.isAllDay,
            alarmOption = schedule.alarmOption,
            startDate = schedule.start.date
        )
    }

    @Query("""
    UPDATE recurring_schedules 
    SET title = :title, 
        location = :location, 
        isAllDay = :isAllDay, 
        alarmOption = :alarmOption 
    WHERE originalEventId = :originalEventId AND strftime('%Y-%m-%d', substr(startDate, 1, instr(startDate, '|') - 1)) >= :startDate
""")
    suspend fun updateContentOnly(
        originalEventId: String,
        title: String,
        location: String,
        isAllDay: Boolean,
        alarmOption: AlarmOption,
        startDate: LocalDate
    )

    @Query("DELETE FROM recurring_schedules WHERE originalEventId = :originalEventId AND strftime('%Y-%m-%d', substr(startDate, 1, instr(startDate, '|') - 1)) >= :selectedDate")
    suspend fun deleteThisAndFutureRecurringData(originalEventId: String, selectedDate: LocalDate)
}
