package com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.entity.TaskEntity
import com.pdevjay.calendar_with_schedule.data.entity.toScheduleData
import com.pdevjay.calendar_with_schedule.data.entity.toTaskEntity
import com.pdevjay.calendar_with_schedule.data.repository.TaskRepository
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.TaskIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.states.TaskState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TaskState())
    val state: StateFlow<TaskState> = _state

    init {

        viewModelScope.launch {
            repository.getAllTasks()
                .collect { entities ->
                    _state.value = TaskState(schedules = entities.map { it.toScheduleData() })
                }
        }
    }

    fun processIntent(intent: TaskIntent) {
        viewModelScope.launch {
            when (intent) {
                is TaskIntent.AddSchedule -> {
                    val entity = intent.schedule.toTaskEntity()
                    repository.saveTask(entity)
                }
                is TaskIntent.UpdateSchedule -> {
                    val entity = intent.schedule.toTaskEntity()
                    repository.saveTask(entity)  // 덮어쓰기 (PrimaryKey 같으면 업데이트)
                }
                is TaskIntent.DeleteSchedule -> {
                    val target = _state.value.schedules.find { it.id == intent.scheduleId }
                    if (target != null) {
                        repository.deleteTask(target.toTaskEntity())
                    }
                }
            }
        }
    }

    fun getSchedulesForDate(date: java.time.LocalDate): List<ScheduleData> {
        return _state.value.schedules.filter {
            it.start.date <= date && it.end.date >= date
        }
    }
}
