package com.pdevjay.calendar_with_schedule.data.repository

import com.pdevjay.calendar_with_schedule.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<TaskEntity>>
    suspend fun saveTask(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)
}
