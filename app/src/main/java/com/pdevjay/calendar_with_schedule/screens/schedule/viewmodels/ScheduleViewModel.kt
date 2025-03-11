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
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleState())
    val state: StateFlow<ScheduleState> = _state

    fun processIntent(intent: ScheduleIntent) {
        viewModelScope.launch {
            when (intent) {
                is ScheduleIntent.AddSchedule -> {
                    val entity = intent.schedule
                    scheduleRepository.saveSchedule(entity)
                }
                is ScheduleIntent.UpdateSchedule -> {
                    val entity = intent.schedule
                    scheduleRepository.saveSchedule(entity)  // 덮어쓰기 (PrimaryKey 같으면 업데이트)
                }
                is ScheduleIntent.DeleteSchedule -> {
                    val target = _state.value.schedules.find { it.id == intent.scheduleId }
                    if (target != null) {
                        scheduleRepository.deleteSchedule(target)
                    }
                }
            }
        }
    }

    fun getSchedulesForDate(date: LocalDate){
        viewModelScope.launch {
            scheduleRepository.getSchedulesForDate(date).collect { schedules ->
                _state.value = _state.value.copy(schedules = schedules)
            }
        }
    }
}
