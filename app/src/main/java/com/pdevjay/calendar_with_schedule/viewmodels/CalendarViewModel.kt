package com.pdevjay.calendar_with_schedule.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.pdevjay.calendar_with_schedule.datamodels.CalendarDay
import com.pdevjay.calendar_with_schedule.datamodels.CalendarMonth
import com.pdevjay.calendar_with_schedule.datamodels.CalendarWeek
import com.pdevjay.calendar_with_schedule.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.states.CalendarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state

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
                _state.value = _state.value.copy(selectedDate = intent.date, isExpanded = false)
            }

            is CalendarIntent.DateUnselected -> {
                _state.value = _state.value.copy(selectedDate = null, isExpanded = true)
            }

            is CalendarIntent.MonthChanged -> {
                _state.value = _state.value.copy(currentMonth = intent.month)
            }

            is CalendarIntent.SetExpanded -> {
                _state.value = _state.value.copy(isExpanded = intent.isExpanded)
            }
        }
    }

    fun generateCalendarMonths(currentMonth: YearMonth = YearMonth.now(), existingMonths: List<CalendarMonth> = emptyList()): List<CalendarMonth> {
        val months = mutableListOf<CalendarMonth>()

        for (i in -5..5) {
            val candidateMonth = currentMonth.plusMonths(i.toLong())
            // 이미 existingMonths에 해당 candidateMonth가 있으면 건너뜁니다.
            if (existingMonths.any { it.yearMonth == candidateMonth }) continue

            val firstDayOfMonth = candidateMonth.atDay(1)
            // ISO 기준: 월요일=1, 일요일=7; 만약 일요일부터 시작하려면 % 7를 사용.
            // 여기서는 일요일=0, 월요일=1 ... 토요일=6 로 가정.
            val firstDayOffset = firstDayOfMonth.dayOfWeek.value % 7
            val daysInMonth = candidateMonth.lengthOfMonth()
            // totalSlots: 몇 주가 필요한지 계산
            val totalSlots = ((firstDayOffset + daysInMonth + 6) / 7) * 7

            val days = mutableListOf<CalendarDay>()
            for (index in 0 until totalSlots) {
                val (date, isCurrentMonth) = when {
                    // 이전 달의 날짜
                    index < firstDayOffset -> {
                        val prevMonth = candidateMonth.minusMonths(1)
                        val daysInPrevMonth = prevMonth.lengthOfMonth()
                        // 예: firstDayOffset=3 이면, index 0→prevMonth.atDay(daysInPrevMonth-2),
                        // index 1→prevMonth.atDay(daysInPrevMonth-1), index 2→prevMonth.atDay(daysInPrevMonth)
                        val day = daysInPrevMonth - (firstDayOffset - index - 1)
                        prevMonth.atDay(day) to false
                    }
                    // 현재 달의 날짜
                    index < firstDayOffset + daysInMonth -> {
                        val day = index - firstDayOffset + 1
                        candidateMonth.atDay(day) to true
                    }
                    // 다음 달의 날짜
                    else -> {
                        val nextMonth = candidateMonth.plusMonths(1)
                        val day = index - (firstDayOffset + daysInMonth) + 1
                        nextMonth.atDay(day) to false
                    }
                }

                // isFirstDayOfMonth: 현재 달에서만 1일인 경우 true, 그렇지 않으면 false.
                val isFirstDayOfMonth = isCurrentMonth && date.dayOfMonth == 1

                // isToday: 오늘과 같은지 비교
                val isToday = date == LocalDate.now()

                days.add(CalendarDay(date = date, isCurrentMonth = isCurrentMonth, isFirstDayOfMonth = isFirstDayOfMonth, isToday = isToday))
            }

            val weeks = days.chunked(7).mapIndexed { index, weekDays ->
                // week에 고유 id 부여 (예: "YYYY-M-week-INDEX")
                CalendarWeek(id = "${candidateMonth.year}-${candidateMonth.monthValue}-week-$index", days = weekDays)
            }
            months.add(CalendarMonth(candidateMonth, weeks))
        }

        return months
    }

    // 전체 주(week)의 개수를 구하는 헬퍼 함수
    fun getTotalWeeks(months: List<CalendarMonth>): Int {
        return months.sumOf { it.weeks.size }
    }

}
