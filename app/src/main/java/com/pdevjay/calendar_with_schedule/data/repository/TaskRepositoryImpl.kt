package com.pdevjay.calendar_with_schedule.data.repository

import android.util.Log
import com.pdevjay.calendar_with_schedule.data.database.TaskDao
import com.pdevjay.calendar_with_schedule.data.entity.toScheduleData
import com.pdevjay.calendar_with_schedule.data.entity.toTaskEntity
import com.pdevjay.calendar_with_schedule.data.repository.TaskRepository
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<ScheduleData>> {
        return taskDao.getAllTasks()
            .map { taskEntities -> taskEntities.map { it.toScheduleData() } }
    }

    override suspend fun getTasksForDate(date: LocalDate): Flow<List<ScheduleData>> {
        return taskDao.getAllTasks()
            .map { taskEntities ->
                taskEntities.map { it.toScheduleData() }
                    .filter { task ->
                        if (task.repeatOption == RepeatOption.NONE) {
                            task.start.date == date
                        } else {
                            generateRepeatedDates(task).contains(date)
                        }
                    }
            }
    }

    override fun getTasksForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<ScheduleData>>> {
        val monthStrings = months.map { it.toString() } // Convert YearMonth to "YYYY-MM" format
        Log.e("DB_DEBUG", "Querying for months: $monthStrings") // ✅ Debugging

        return taskDao.getTasksForMonths(monthStrings) // Pass List<String> instead of List<YearMonth>
            .map { taskEntities ->
                Log.e("DB_DEBUG", "Fetched ${taskEntities.size} tasks") // ✅ Debugging
                taskEntities.map { it.toScheduleData() }
                    .filter { task ->
                        val taskStartMonth = YearMonth.from(task.start.date)
                        val taskEndMonth = YearMonth.from(task.end.date)
                        months.any { it == taskStartMonth || it == taskEndMonth }
                    }
                    .flatMap { task ->
                        val dateRange = generateDateRange(task.start.date, task.end.date)
                        dateRange.map { date -> date to task }
                    }
                    .groupBy({ it.first }, { it.second })
            }
    }


    override suspend fun saveTask(task: ScheduleData) {
        taskDao.insertTask(task.toTaskEntity())
    }

    override suspend fun deleteTask(task: ScheduleData) {
        taskDao.deleteTask(task.toTaskEntity())
    }

    private fun generateRepeatedDates(event: ScheduleData, maxYears: Int = 10): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = event.start.date
        val now = LocalDate.now()

        repeat(maxYears) {
            if (currentDate.isAfter(now.minusYears(1))) {
                dates.add(currentDate)
            }
            currentDate = when (event.repeatOption) {
                RepeatOption.DAILY -> currentDate.plusDays(1)
                RepeatOption.WEEKLY -> currentDate.plusWeeks(1)
                RepeatOption.BIWEEKLY -> currentDate.plusWeeks(2)
                RepeatOption.MONTHLY -> currentDate.plusMonths(1)
                RepeatOption.YEARLY -> currentDate.plusYears(1)
                else -> return dates
            }
        }
        return dates
    }

    private fun generateDateRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        return generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it <= endDate }
            .toList()
    }
}
