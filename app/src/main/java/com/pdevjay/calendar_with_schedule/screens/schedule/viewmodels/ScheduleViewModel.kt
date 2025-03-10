package com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.states.ScheduleState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleState())
    val state: StateFlow<ScheduleState> = _state

    init {

        viewModelScope.launch {
            repository.getAllSchedules()
                .collect { entities ->
                    _state.value = ScheduleState(schedules = entities.map { it })
                }
        }
    }

    fun processIntent(intent: ScheduleIntent) {
        viewModelScope.launch {
            when (intent) {
                is ScheduleIntent.AddSchedule -> {
                    val entity = intent.schedule
                    repository.saveSchedule(entity)
                }
                is ScheduleIntent.UpdateSchedule -> {
                    val entity = intent.schedule
                    repository.saveSchedule(entity)  // 덮어쓰기 (PrimaryKey 같으면 업데이트)
                }
                is ScheduleIntent.DeleteSchedule -> {
                    val target = _state.value.schedules.find { it.id == intent.scheduleId }
                    if (target != null) {
                        repository.deleteSchedule(target)
                    }
                }
            }
        }
    }

    fun getSchedulesForDate(date: LocalDate): List<ScheduleData> {
        return _state.value.schedules.filter { it.start.date == date || it.end.date == date }
    }

//    fun getSchedulesForDate(date: java.time.LocalDate): List<ScheduleData> {
//        return _state.value.schedules.filter {
//            it.start.date <= date && it.end.date >= date
//        }
//    }
}
