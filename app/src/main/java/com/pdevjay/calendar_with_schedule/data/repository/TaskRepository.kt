package com.pdevjay.calendar_with_schedule.data.repository

import com.pdevjay.calendar_with_schedule.data.entity.TaskEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface TaskRepository {
    fun getAllTasks(): Flow<List<ScheduleData>>
    suspend fun getTasksForDate(date: LocalDate): Flow<List<ScheduleData>>
    fun getTasksForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<ScheduleData>>>
    suspend fun saveTask(task: ScheduleData)
    suspend fun deleteTask(task: ScheduleData)
}
