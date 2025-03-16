package com.pdevjay.calendar_with_schedule.screens.schedule.states

import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import java.time.LocalDate

data class ScheduleState(
    val schedules: List<ScheduleData> = emptyList(),
    val recurringSchedules: List<RecurringData> = emptyList(),
    val selectedDate: LocalDate? = null
)
