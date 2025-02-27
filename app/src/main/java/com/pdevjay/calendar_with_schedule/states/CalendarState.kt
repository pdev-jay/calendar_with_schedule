package com.pdevjay.calendar_with_schedule.states

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.YearMonth

// 1. 캘린더 상태 정의 (현재 월, 선택된 날짜)
@RequiresApi(Build.VERSION_CODES.O)
data class CalendarState  constructor(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,
    val isExpanded: Boolean = true
)