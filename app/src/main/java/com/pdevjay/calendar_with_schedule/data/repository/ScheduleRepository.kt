package com.pdevjay.calendar_with_schedule.data.repository

import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface ScheduleRepository {
    fun getAllSchedules(): Flow<List<ScheduleData>>
    suspend fun getSchedulesForDate(date: LocalDate): Flow<List<ScheduleData>>
    fun getSchedulesForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<ScheduleData>>>
    suspend fun saveSchedule(schedule: ScheduleData)
    suspend fun deleteSchedule(schedule: ScheduleData)

    suspend fun getRecurringSchedulesForDate(date: LocalDate): Flow<List<RecurringData>>
    suspend fun saveRecurringScheduleChange(recurringData: RecurringData) // 🔹 특정 날짜에서 변경 저장
    suspend fun markRecurringScheduleDeleted(recurringData: RecurringData) // 🔹 특정 날짜에서 삭제
}
