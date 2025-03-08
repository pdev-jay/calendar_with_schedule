package com.pdevjay.calendar_with_schedule.screens.schedule.intents

import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData

sealed class TaskIntent {
    data class AddSchedule(val schedule: ScheduleData) : TaskIntent()
    data class UpdateSchedule(val schedule: ScheduleData) : TaskIntent()
    data class DeleteSchedule(val scheduleId: String) : TaskIntent()
}
