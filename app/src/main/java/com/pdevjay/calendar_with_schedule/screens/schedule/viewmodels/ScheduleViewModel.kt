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
            combine(
                scheduleRepository.getSchedulesForDate(date),  // 원본 일정 가져오기
                scheduleRepository.getRecurringSchedulesForDate(date) // 특정 날짜에서 수정된 반복 일정 가져오기
            ) { schedules, recurringSchedules ->
                val updatedSchedules = schedules.flatMap { schedule ->
                    // 반복 일정이 없는 경우 (단일 일정 또는 시작 날짜가 `date`와 일치하는 일정)
                    if ((schedule.repeatType == RepeatType.NONE || schedule.repeatRule.isNullOrEmpty()) ||
                        (schedule.repeatType != RepeatType.NONE && schedule.start.date == date)) {
                        listOf(schedule) // 그대로 반환
                    } else {
                        // `recurringSchedules`에서 해당 일정이 수정되었는지 확인
                        val modifiedRecurringEvent = recurringSchedules.firstOrNull {
                            it.originalEventId == schedule.id && it.start.date == date
                        }

                        if (modifiedRecurringEvent != null) {
                            if (modifiedRecurringEvent.isDeleted) {
                                emptyList() // 해당 날짜에서 삭제된 일정이면 반환하지 않음
                            } else {
                                listOf(modifiedRecurringEvent.toScheduleData(schedule)) // 수정된 일정 반영
                            }
                        } else {
                            // 수정되지 않은 반복 일정 생성
                            val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(
                                schedule.repeatType,  // 반복 유형 (DAILY, WEEKLY 등)
                                schedule.start.date,  // 반복 일정의 시작 날짜
                                monthList = null,     // 특정 월 리스트 사용 안 함
                                selectedDate = date   // 특정 날짜에 해당하는 일정만 생성
                            )

                            // 반복 일정 생성
                            repeatedDates.map { selectedDate -> generateRepeatedScheduleInstances(schedule, selectedDate) }
                        }
                    }
                }

                // UI 상태 업데이트
                _state.value = _state.value.copy(
                    schedules = updatedSchedules,       // 업데이트된 전체 일정 리스트
                    recurringSchedules = recurringSchedules // 특정 날짜에서 수정된 반복 일정 리스트
                )
            }.launchIn(viewModelScope) // ViewModelScope에서 실행하여 Lifecycle 관리
        }
    }



}
