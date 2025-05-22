package com.pdevjay.calendar_with_schedule.features.schedule.states

import com.pdevjay.calendar_with_schedule.features.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.features.schedule.data.ScheduleData
import java.time.LocalDate

data class ScheduleState(
    val schedules: List<ScheduleData> = emptyList(),
    val recurringSchedules: List<RecurringData> = emptyList(),
    val selectedDate: LocalDate? = null
)
