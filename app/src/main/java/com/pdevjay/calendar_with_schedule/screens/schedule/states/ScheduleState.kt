package com.pdevjay.calendar_with_schedule.screens.schedule.states

import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData

data class ScheduleState(
    val schedules: List<ScheduleData> = emptyList()
)
