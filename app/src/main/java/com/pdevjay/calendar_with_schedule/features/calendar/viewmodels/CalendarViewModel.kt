package com.pdevjay.calendar_with_schedule.features.calendar.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.repository.RemoteDataRepository
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.features.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.features.calendar.data.CalendarMonth
import com.pdevjay.calendar_with_schedule.features.calendar.data.CalendarWeek
import com.pdevjay.calendar_with_schedule.features.calendar.data.HolidayData
import com.pdevjay.calendar_with_schedule.features.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.features.calendar.states.CalendarState
import com.pdevjay.calendar_with_schedule.features.schedule.data.BaseSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state
    private val _weeks = MutableStateFlow<List<CalendarWeek>>(emptyList())
    val weeks: StateFlow<List<CalendarWeek>> = _weeks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var selectedDate = MutableStateFlow(LocalDate.now()) // 사용자가 선택한 날짜

    init {
        initializeMonths()
        viewModelScope.launch {
            combine(
                scheduleRepository.scheduleMap,
                scheduleRepository.holidayMap
            ) { scheduleMap, holidayMap ->
                Pair(scheduleMap, holidayMap)
            }
                .filter { (s, h) -> s.isNotEmpty() || h.isNotEmpty() } // 아무 값도 없으면 무시
                .collect { (newScheduleMap, newHolidayMap) ->
                    Log.e("viemodel_calendar", " scheduleMap updated: ${newScheduleMap.size}")
                    Log.e("viemodel_calendar", " holidayMap updated: ${newHolidayMap.size}")

                    _state.value = _state.value.copy(
                        scheduleMap = newScheduleMap,
                        holidayMap = newHolidayMap
                    )
                    _isLoading.value = false
                }
        }
    }

    fun initializeMonths(refreshScheduleMap: Boolean = false) {
        Log.e("", "initializeMonths")
            viewModelScope.launch {
                val now = YearMonth.now()
                val months = (-6..6).map { offset ->
                    val yearMonth = now.plusMonths(offset.toLong())
                    generateMonth(yearMonth.year, yearMonth.monthValue)
                }
//                val months = (-12 * 25..12 * 25).map { offset ->
//                    val yearMonth = now.plusMonths(offset.toLong())
//                    generateMonth(yearMonth.year, yearMonth.monthValue)
//                }
                _state.value = _state.value.copy(months = months.toMutableList())

                if (refreshScheduleMap) {
                    loadScheduleMap(now)
                }
            }
    }

    fun loadNextMonth(){
        val lastMonth = _state.value.months.lastOrNull() ?: return
        val lastYearMonth = YearMonth.of(lastMonth.yearMonth.year, lastMonth.yearMonth.monthValue)

        val newMonths = (1..6).map { offset ->
            val target = lastYearMonth.plusMonths(offset.toLong())
            generateMonth(target.year, target.monthValue)
        }

        loadScheduleMap(lastYearMonth)
        val months = (_state.value.months.drop(6) + newMonths).toMutableList()
        _state.value = _state.value.copy(months = months)
    }

    fun loadPreviousMonth(): List<CalendarMonth> {
        val firstMonth = _state.value.months.first()
        val firstYearMonth = YearMonth.of(firstMonth.yearMonth.year, firstMonth.yearMonth.monthValue)
        Log.e("LazyRow", "loadPreviousMonths")

        val newMonths = (1..6).map { offset ->
            val target = firstYearMonth.minusMonths(offset.toLong())
            generateMonth(target.year, target.monthValue)
        }.reversed()

        loadScheduleMap(firstYearMonth)
        val months = (newMonths + _state.value.months).take(_state.value.months.size).toMutableList()
        _state.value = _state.value.copy(months = months)

        return newMonths
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
                val newMonth = YearMonth.of(intent.date.year, intent.date.monthValue) // 선택된 날짜의 YearMonth 가져오기
                _state.value = _state.value.copy(
                    selectedDate = intent.date,
                    currentMonth = newMonth
                )
            }

            is CalendarIntent.DateUnselected -> {
                _state.value = _state.value.copy(selectedDate = null)
            }

            is CalendarIntent.MonthChanged -> {
                _state.value = _state.value.copy(currentMonth = intent.month)
            }

            is CalendarIntent.LoadNextMonths -> {
                loadNextMonth()
            }
            is CalendarIntent.LoadPreviousMonths -> {
                loadPreviousMonth()
            }
        }
    }
    private fun generateMonth(year: Int, month: Int): CalendarMonth {
        val firstDay = LocalDate.of(year, month, 1)
        val daysInMonth = firstDay.lengthOfMonth()
        val today = LocalDate.now()

        val days = (1..daysInMonth).map { day ->
            val date = LocalDate.of(year, month, day)
            CalendarDay(
                date = date,
                dayOfWeek = date.dayOfWeek,
                isToday = date == today
            )
        }

        return CalendarMonth(YearMonth.of(year, month), days)
    }

    private fun loadScheduleMap(month: YearMonth) {
        viewModelScope.launch {
            val monthsToLoad = (-6..6).map{
                month.plusMonths(it.toLong())
            }
            scheduleRepository.loadSchedulesForMonths(monthsToLoad)
        }
    }

    fun getMappedSchedulesForMonth(month: CalendarMonth): Map<LocalDate, List<BaseSchedule>> {
        val scheduleMap = _state.value.scheduleMap //  최신 일정 데이터 가져오기
        return month.mapSchedulesToDays(scheduleMap)
    }
    fun getMappedHolidayForMonth(month: CalendarMonth): Map<LocalDate, List<HolidayData>> {
        val holidayMap = _state.value.holidayMap //  최신 일정 데이터 가져오기
        return month.mapHolidaysToDays(holidayMap)
    }
}
