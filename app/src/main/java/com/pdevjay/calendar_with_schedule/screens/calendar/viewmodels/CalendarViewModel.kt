package com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
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
    private val scheduleRepository: ScheduleRepository
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
                val newMonth = YearMonth.of(intent.date.year, intent.date.monthValue) // 🔹 선택된 날짜의 YearMonth 가져오기
                _state.value = _state.value.copy(
                    selectedDate = intent.date,
                    currentMonth = newMonth // 🔹 선택된 날짜의 달로 업데이트
                )
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
        val monthsToLoad = listOf(
            centerMonth.minusMonths(1), // Previous month
            centerMonth, // Current month
            centerMonth.plusMonths(1) // Next month
        )

        viewModelScope.launch {
            scheduleRepository.getSchedulesForMonths(monthsToLoad).collect { monthSchedules ->
                _state.value = _state.value.copy(scheduleMap = monthSchedules)
            }
        }
    }
}