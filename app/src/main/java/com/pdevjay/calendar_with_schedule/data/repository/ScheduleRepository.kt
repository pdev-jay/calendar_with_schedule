package com.pdevjay.calendar_with_schedule.data.repository

import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.YearMonth

interface ScheduleRepository {
    val scheduleMap: StateFlow<Map<LocalDate, List<BaseSchedule>>>

    fun getAllSchedules(): Flow<List<ScheduleData>>


    suspend fun loadSchedulesForMonths(months: List<YearMonth>)

    suspend fun deleteSingleSchedule(schedule: ScheduleData)


    suspend fun saveSingleScheduleChange(schedule: ScheduleData) // 🔹 특정 날짜에서 변경 저장

    /**
     * 현재 달 전후 1개월 씩의 스케줄을 가져옵니다.
     * `schedules` 테이블에서 해당 기간의 데이터와
     * 해당 기간의 가장 마지막 달 이전에 존재하는 반복 일정을 조회하고,
     * 해당 기간에 존재하는 반복 일정 중 변경 사항이 있는 데이터를 `recurring_schedules` 테이블에서 조회하여
     * Calendar에 보여줄 데이터 생성하여 return
     */
    fun getSchedulesForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<BaseSchedule>>>

    /**
     * 새로운 스케줄을 추가하거나 기존 스케줄을 갱신
     */
    suspend fun saveSchedule(schedule: ScheduleData)

    /**
     * 원본 스케쥴과 반복 스케쥴의 내용 변경
     */
    suspend fun saveFutureScheduleChange(schedule: ScheduleData)

    /**
     * 특정 날짜 이후의 원본 스케줄을 삭제합니다.
     * `schedules` 테이블과 `recurring_schedules` 테이블에서 모두 삭제
     */
    suspend fun deleteFutureSchedule(schedule: ScheduleData)

    /**
     * 반복 일정의 삽입, 갱신
     */
    suspend fun saveSingleRecurringScheduleChange(recurringData: RecurringData) // 🔹 특정 날짜에서 변경 저장

    /**
     * 특정 날짜의 반복 일정에서 해당 날짜 이후의 모든 반복 일정의 내용 변경
     * 원본 일정의 repeatUntil을 해당 날짜 -1로 변경 후,
     * 원본 일정의 id를 기반으로 새로운 일정 등록
     */

    suspend fun saveFutureRecurringScheduleChange(recurringData: RecurringData)

    /**
     * 특정 날짜 이후의 반복 일정을 삭제합니다.
     * 원본 일정의 repeatUntil을 해당 날짜 -1로 변경 후,
     * 그 이후의 날에 등록되어있는 모든 반복 일정(`schedules table`) 삭제,
     * 또한 `recurring_schedules` 테이블에서도 해당 날짜 이후의 모든 반복 일정 삭제
     */
    suspend fun deleteFutureRecurringSchedule(recurringData: RecurringData)
    suspend fun deleteRecurringSchedule(recurringData: RecurringData)
}
