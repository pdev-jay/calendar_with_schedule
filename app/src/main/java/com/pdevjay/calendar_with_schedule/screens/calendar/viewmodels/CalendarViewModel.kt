package com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarMonth
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarWeek
import com.pdevjay.calendar_with_schedule.screens.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.screens.calendar.states.CalendarState
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state
    private val _weeks = MutableStateFlow<List<CalendarWeek>>(emptyList())
    val weeks: StateFlow<List<CalendarWeek>> = _weeks.asStateFlow()

    var selectedDate = MutableStateFlow(LocalDate.now()) // 사용자가 선택한 날짜


    init {
        initializeMonths()

        viewModelScope.launch {
            scheduleRepository.scheduleMap.collect { newScheduleMap ->
                _state.value = _state.value.copy(scheduleMap = newScheduleMap)
                Log.e("", "scheduleMap updated ")
            }
        }
    }

    private fun loadInitialWeeks(selectedDate: LocalDate) {
        val initialWeeks = generateWeeksAround(selectedDate) // 선택된 날짜 기준
        _weeks.value = initialWeeks
    }

    fun findWeekIndexForDate(date: LocalDate): Int {
        val index = weeks.value.indexOfFirst { week -> week.contains(date) }

        if (index == -1) {
            Log.e("CalendarViewModel", "❌ 해당 날짜 ($date)의 주가 리스트에 없음!")
            Log.e("CalendarViewModel", "📅 현재 주 리스트: ${weeks.value.map { it.startDate }}")
        }

        return index
    }

    fun loadMoreWeeks(isNext: Boolean) {
        val referenceDate = if (isNext) {
            _weeks.value.last().endDate.plusDays(1)
        } else {
            _weeks.value.first().startDate.minusDays(7)
        }

        val newWeeks = generateWeeks(referenceDate, isNext)

        _weeks.value = if (isNext) {
            // ✅ 중복 방지: 기존 리스트에 없는 주만 추가
            (_weeks.value + newWeeks).distinctBy { it.startDate }
        } else {
            (newWeeks + _weeks.value).distinctBy { it.startDate }
        }
    }

    private fun generateWeeksAround(date: LocalDate): List<CalendarWeek> {
        val pastWeeks = generateWeeks(date, isNext = false)
        val futureWeeks = generateWeeks(date, isNext = true)
        return pastWeeks + futureWeeks
    }

    private fun generateWeeks(startDate: LocalDate, isNext: Boolean, count: Int = 5): List<CalendarWeek> {
        val generatedWeeks = (0 until count).map { i ->
            val weekStart = if (isNext) {
                startDate.plusWeeks(i.toLong()).with(DayOfWeek.SUNDAY) // 🔹 다음 주 일요일로 이동
            } else {
                startDate.minusWeeks(i.toLong()).with(DayOfWeek.SUNDAY) // 🔹 이전 주 일요일로 이동
            }
            CalendarWeek.from(weekStart, _state.value.months.flatMap { it.days })
        }

        return generatedWeeks
            .distinctBy { it.startDate }
            .sortedBy { it.startDate }
    }

    fun initializeMonths() {
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
                // 🔹 초기 주 데이터 설정
//                loadInitialWeeks(LocalDate.now())
//                loadScheduleMap(YearMonth.now())
                loadScheduleMap(now)
//                scheduleRepository.loadSchedulesForMonths(months.map { it.yearMonth })

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

                // 🔹 선택된 날짜가 속한 주로 `weeks` 업데이트
                loadInitialWeeks(intent.date)
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
        val scheduleMap = _state.value.scheduleMap // ✅ 최신 일정 데이터 가져오기
        return month.mapSchedulesToDays(scheduleMap)
    }
}
