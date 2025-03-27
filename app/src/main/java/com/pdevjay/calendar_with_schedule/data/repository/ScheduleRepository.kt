package com.pdevjay.calendar_with_schedule.data.repository

import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.ScheduleEditType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.YearMonth

interface ScheduleRepository {
    val scheduleMap: StateFlow<Map<LocalDate, List<RecurringData>>>

    suspend fun loadSchedulesForMonths(months: List<YearMonth>)

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


    suspend fun updateSchedule(schedule: RecurringData, scheduleEditType: ScheduleEditType, isOnlyContentChanged: Boolean)
    suspend fun deleteSchedule(schedule: RecurringData, scheduleEditType: ScheduleEditType)
}
