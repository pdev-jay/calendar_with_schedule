package com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.states.ScheduleState
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator.generateRepeatedScheduleInstances
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
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
                    scheduleRepository.saveSchedule(intent.schedule)
                }
                is ScheduleIntent.UpdateSchedule -> {
                    scheduleRepository.saveSchedule(intent.schedule)  // 덮어쓰기 (PrimaryKey 같으면 업데이트)
                }

                is ScheduleIntent.UpdateFutureSchedule -> {
                    scheduleRepository.saveFutureScheduleChange(intent.schedule)
                }

                is ScheduleIntent.UpdateSingleSchedule -> {
                    scheduleRepository.saveSingleScheduleChange(intent.schedule)
                }

                is ScheduleIntent.UpdateFutureRecurringSchedule -> {
                    scheduleRepository.saveFutureRecurringScheduleChange(intent.schedule)
                }
                is ScheduleIntent.UpdateSingleRecurringSchedule -> {
                    scheduleRepository.saveSingleRecurringScheduleChange(intent.schedule)
                }

                is ScheduleIntent.DeleteFutureRecurringSchedule -> {
                    scheduleRepository.deleteFutureRecurringSchedule(intent.schedule)
                }

                is ScheduleIntent.DeleteSingleRecurringSchedule -> {
                    scheduleRepository.saveSingleRecurringScheduleChange(intent.schedule.copy(isDeleted = true))
                }

                is ScheduleIntent.DeleteFutureSchedule -> {
                    scheduleRepository.deleteFutureSchedule(intent.schedule)
                }
                is ScheduleIntent.DeleteSingleSchedule -> {
                    scheduleRepository.deleteSingleSchedule(intent.schedule)
                }
            }
        }
    }
}
