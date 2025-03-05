package com.pdevjay.calendar_with_schedule.states

import com.pdevjay.calendar_with_schedule.datamodels.ScheduleData

data class TaskState(
    val schedules: List<ScheduleData> = emptyList()
)
