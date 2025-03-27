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

    /**
     * 특정 월에 포함된 반복 일정을 조회합니다.
     *
     * @param months 조회할 월 목록 (yyyy-MM 형식)
     * @return 해당 월의 반복 일정 목록을 Flow 형태로 반환
     */
//    @Query("""
//        SELECT * FROM recurring_schedules
//        WHERE strftime('%Y-%m', substr(startDate, 1, instr(startDate, '|') - 1)) IN (:months)
//           OR strftime('%Y-%m', substr(endDate, 1, instr(endDate, '|') - 1)) IN (:months)
//    """)
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
     * 특정 ID를 포함하며, 지정된 날짜 이후의 반복 일정을 삭제합니다.
     *
     * @param scheduleId 삭제할 반복 일정의 ID
     * @param date 해당 날짜(yyyy-MM-dd) 이후의 반복 일정 삭제
     */
    @Query("DELETE FROM recurring_schedules WHERE id LIKE :scheduleId || '%' AND strftime('%Y-%m-%d', substr(startDate, 1, instr(startDate, '|') - 1)) >= :date")
    suspend fun deleteFutureRecurringSchedule(scheduleId: String, date: String)

    @Query("DELETE FROM recurring_schedules WHERE originalEventId = :scheduleId")
    suspend fun deleteAllRecurringScheduleForScheduleId(scheduleId: String)

    /**
     * 새로운 반복 일정을 추가하거나 기존 반복 일정을 갱신합니다.
     *
     * @param schedule 추가 또는 업데이트할 반복 일정
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringSchedule(schedule: RecurringScheduleEntity)

    suspend fun insertRecurringScheduleIfNotExists(schedule: RecurringScheduleEntity) {
        insertRecurringScheduleIfNotExists(
            id = schedule.id,
            originalEventId = schedule.originalEventId,
            originalRecurringDate = schedule.originalRecurringDate,
            originatedFrom = schedule.originatedFrom,
            startDate = schedule.start,
            endDate = schedule.end,
            title = schedule.title,
            location = schedule.location,
            isAllDay = schedule.isAllDay,
            repeatUntil = schedule.repeatUntil,
            repeatRule = schedule.repeatRule,
            alarmOption = schedule.alarmOption,
            isOriginalSchedule = schedule.isOriginalSchedule,
            originalRepeatUntil = schedule.originalRepeatUntil,
            isFirstSchedule = schedule.isFirstSchedule
        )
    }

    @Query("""
    INSERT OR IGNORE INTO recurring_schedules (
        id, originalEventId, originalRecurringDate, originatedFrom, startDate, endDate, title, 
        location, isAllDay, repeatType, repeatUntil, repeatRule, alarmOption, 
        isOriginalSchedule, isDeleted, originalRepeatUntil, isFirstSchedule
    ) VALUES (
        :id, :originalEventId, :originalRecurringDate, :originatedFrom, :startDate, :endDate, :title, 
        :location, :isAllDay, 'NONE', :repeatUntil, :repeatRule, :alarmOption, 
        :isOriginalSchedule, 1, :originalRepeatUntil, :isFirstSchedule
    )
""")
    suspend fun insertRecurringScheduleIfNotExists(
        id: String,
        originalEventId: String,
        originalRecurringDate: LocalDate,
        originatedFrom: String,
        startDate: DateTimePeriod,
        endDate: DateTimePeriod,
        title: String?,
        location: String?,
        isAllDay: Boolean,
        repeatUntil: LocalDate?,
        repeatRule: String?,
        alarmOption: AlarmOption,
        isOriginalSchedule: Boolean,
        originalRepeatUntil: LocalDate?,
        isFirstSchedule: Boolean
    )

    @Query("UPDATE recurring_schedules SET isDeleted = 1 WHERE id = :id")
    suspend fun markRecurringScheduleAsDeleted(id: String): Int

    /**
     * 특정 이벤트 ID를 기반으로 반복 일정의 `repeatUntil`을 업데이트합니다.
     */
    @Query("UPDATE recurring_schedules SET repeatUntil = :repeatUntil WHERE id = :eventId")
    suspend fun updateRepeatUntil(eventId: String, repeatUntil: String)

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

    @Query("""
        DELETE FROM recurring_schedules WHERE originalEventId = :originalEventId AND isFirstSchedule = 1
    """)
    suspend fun deleteIsFirstRecurringSchedule(originalEventId: String)
}
