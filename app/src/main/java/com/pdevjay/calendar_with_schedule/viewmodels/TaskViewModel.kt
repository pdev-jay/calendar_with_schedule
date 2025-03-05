package com.pdevjay.calendar_with_schedule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.entity.TaskEntity
import com.pdevjay.calendar_with_schedule.data.repository.TaskRepository
import com.pdevjay.calendar_with_schedule.datamodels.ScheduleData
import com.pdevjay.calendar_with_schedule.intents.TaskIntent
import com.pdevjay.calendar_with_schedule.states.TaskState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
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

    // ScheduleData <-> TaskEntity 변환 함수들
    private fun ScheduleData.toTaskEntity() = TaskEntity(
        id = id,
        title = title,
        location = location,
        start = start,
        end = end
    )

    private fun TaskEntity.toScheduleData() = ScheduleData(
        id = id,
        title = title,
        location = location,
        start = start,
        end = end
    )
}
