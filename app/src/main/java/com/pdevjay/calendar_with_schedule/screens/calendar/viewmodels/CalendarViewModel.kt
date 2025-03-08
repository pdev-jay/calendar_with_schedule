package com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.entity.toScheduleData
import com.pdevjay.calendar_with_schedule.data.repository.TaskRepository
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarMonth
import com.pdevjay.calendar_with_schedule.screens.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.screens.calendar.loadInitialMonths
import com.pdevjay.calendar_with_schedule.screens.calendar.states.CalendarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state
    val monthListState = mutableStateListOf<CalendarMonth>()


    fun initializeMonths() {
        if (monthListState.isEmpty()) {
            viewModelScope.launch {
                loadInitialMonths(monthListState)
            }
        }
    }

    fun processIntent(intent: CalendarIntent) {
        when (intent) {
            is CalendarIntent.PreviousMonth -> {
                val newMonth = _state.value.currentMonth.minusMonths(1)
                _state.value = _state.value.copy(currentMonth = newMonth)
            }

            is CalendarIntent.NextMonth -> {
                val newMonth = _state.value.currentMonth.plusMonths(1)
                _state.value = _state.value.copy(currentMonth = newMonth)
            }

            is CalendarIntent.DateSelected -> {
                _state.value = _state.value.copy(selectedDate = intent.date)
            }

            is CalendarIntent.DateUnselected -> {
                _state.value = _state.value.copy(selectedDate = null)
            }

            is CalendarIntent.MonthChanged -> {
                _state.value = _state.value.copy(currentMonth = intent.month)
                loadSchedulesForMonth(intent.month)
            }

        }
    }

    private fun loadSchedulesForMonth(centerMonth: YearMonth) {
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { allTasks ->
                val monthsToLoad = listOf(
                    centerMonth.minusMonths(1), // 이전 달
                    centerMonth, // 현재 달
                    centerMonth.plusMonths(1) // 다음 달
                )

                val monthSchedules = allTasks
                    .filter { task ->
                        val taskStartDate = task.start.date
                        val taskEndDate = task.end.date
                        val taskStartMonth = YearMonth.from(taskStartDate)
                        val taskEndMonth = YearMonth.from(taskEndDate)

                        // ✅ 일정이 세 개의 달 중 하나와 겹치는 경우 포함
                        monthsToLoad.any { it == taskStartMonth || it == taskEndMonth }
                    }
                    .flatMap { task ->
                        // ✅ 일정이 여러 날에 걸쳐 있으면 각 날짜별로 복제하여 포함
                        val dateRange = generateDateRange(task.start.date, task.end.date)
                        dateRange.map { date -> date to task.toScheduleData() }
                    }
                    .groupBy({ it.first }, { it.second }) // ✅ 날짜 기준으로 그룹화

                _state.value = _state.value.copy(scheduleMap = monthSchedules)
            }
        }
    }

    private fun generateDateRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        return generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it <= endDate }
            .toList()
    }
}