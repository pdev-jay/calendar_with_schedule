package com.pdevjay.calendar_with_schedule.viewmodels

import androidx.lifecycle.ViewModel
import com.pdevjay.calendar_with_schedule.datamodels.ScheduleData
import com.pdevjay.calendar_with_schedule.intents.TaskIntent
import com.pdevjay.calendar_with_schedule.states.TaskState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class TaskViewModel : ViewModel() {

    private val _state = MutableStateFlow(TaskState())
    val state: StateFlow<TaskState> = _state

    fun processIntent(intent: TaskIntent) {
        when (intent) {
            is TaskIntent.AddSchedule -> {
                _state.value = _state.value.copy(
                    schedules = _state.value.schedules + intent.schedule
                )
            }
            is TaskIntent.UpdateSchedule -> {
                _state.value = _state.value.copy(
                    schedules = _state.value.schedules.map {
                        if (it.id == intent.schedule.id) intent.schedule else it
                    }
                )
            }
            is TaskIntent.DeleteSchedule -> {
                _state.value = _state.value.copy(
                    schedules = _state.value.schedules.filterNot { it.id == intent.scheduleId }
                )
            }
        }
    }

    fun getSchedulesForDate(date: LocalDate): List<ScheduleData> {
        return _state.value.schedules.filter {
            it.start.date <= date && it.end.date >= date
        }
    }
}
