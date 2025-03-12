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

                is ScheduleIntent.DeleteRecurringSchedule -> {
                    val scheduleElement = intent.schedule.id.split("_")
                    val recurringData = RecurringData(
                        id = intent.schedule.id,
                        originalEventId = scheduleElement.first(),
                        start = intent.schedule.start,
                        end = intent.schedule.end,
                        title = null,
                        isDeleted = true // 삭제된 상태로 저장
                    )
                    scheduleRepository.markRecurringScheduleDeleted(recurringData)
                }
                is ScheduleIntent.UpdateRecurringSchedule -> {
                    val scheduleElement = intent.schedule.id.split("_")
                    val recurringData = RecurringData(
                        id = intent.schedule.id,
                        originalEventId = scheduleElement.first(),
                        start = intent.schedule.start,
                        end = intent.schedule.end,
                        title = intent.schedule.title, // 변경된 제목 저장
                        isDeleted = false // 삭제되지 않은 상태
                    )

                    scheduleRepository.saveRecurringScheduleChange(recurringData)
                }
            }
        }
    }

//    fun getSchedulesForDate(date: LocalDate){
////        viewModelScope.launch {
////            scheduleRepository.getSchedulesForDate(date).collect { schedules ->
////                _state.value = _state.value.copy(schedules = schedules)
////            }
////            scheduleRepository.getRecurringSchedulesForDate(date).collect{ recurringSchedules ->
////                _state.value = _state.value.copy(recurringSchedules = recurringSchedules)
////            }
////        }
//
//        viewModelScope.launch {
//            combine(
//                scheduleRepository.getSchedulesForDate(date),
//                scheduleRepository.getRecurringSchedulesForDate(date)
//            ) { schedules, recurringSchedules ->
//                // recurringSchedules 적용하여 schedules 업데이트
//                val updatedSchedules = schedules.map { original ->
//                    recurringSchedules.firstOrNull { it.originalEventId == original.id }
//                        ?.toScheduleData(original) ?: original
//                }.filter { schedule ->
//                    // 삭제된 반복 일정은 제거
//                    recurringSchedules.none { it.originalEventId == schedule.id && it.isDeleted }
//                }
//
//                _state.value = _state.value.copy(
//                    schedules = updatedSchedules,
//                    recurringSchedules = recurringSchedules
//                )
//            }.collect()
//        }
//    }

//    fun getSchedulesForDate(date: LocalDate) {
//                viewModelScope.launch {
//
//                    combine(
//                        scheduleRepository.getSchedulesForDate(date),
//                        scheduleRepository.getRecurringSchedulesForDate(date)
//                    ) { schedules, recurringSchedules ->
//                        // recurringSchedules 적용하여 schedules 업데이트
//                        val updatedSchedules = schedules.map { original ->
//                            recurringSchedules.firstOrNull { it.originalEventId == original.id }
//                                ?.toScheduleData(original) ?: original
//                        }.filter { schedule ->
//                            // 삭제된 반복 일정 제거
//                            recurringSchedules.none { it.originalEventId == schedule.id && it.isDeleted }
//                        }
//
//                        _state.value = _state.value.copy(
//                            schedules = updatedSchedules,
//                            recurringSchedules = recurringSchedules
//                        )
//                    }.launchIn(viewModelScope) // 오류 해결
//                }
//    }

    fun getSchedulesForDate(date: LocalDate) {
        viewModelScope.launch {
            scheduleRepository.getSchedulesForDate(date).collect { schedules ->
                _state.value = _state.value.copy(schedules = schedules)
            }
        }
    }



}
