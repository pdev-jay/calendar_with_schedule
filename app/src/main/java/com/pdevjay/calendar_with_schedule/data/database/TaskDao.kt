package com.pdevjay.calendar_with_schedule.data.database

import androidx.room.*
import com.pdevjay.calendar_with_schedule.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("""
    SELECT * FROM tasks 
    WHERE strftime('%Y-%m', substr(startDate, 1, instr(startDate, '|') - 1)) IN (:months) 
       OR strftime('%Y-%m', substr(endDate, 1, instr(endDate, '|') - 1)) IN (:months)
""")
    fun getTasksForMonths(months: List<String>): Flow<List<TaskEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}
