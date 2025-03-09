package com.pdevjay.calendar_with_schedule.data.repository

import com.pdevjay.calendar_with_schedule.data.database.TaskDao
import com.pdevjay.calendar_with_schedule.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    override fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    override suspend fun saveTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    override suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }
}
