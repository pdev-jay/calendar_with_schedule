package com.pdevjay.calendar_with_schedule.screens.schedule.intents

import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData

sealed class ScheduleIntent {
    data class AddSchedule(val schedule: ScheduleData) : ScheduleIntent()
    data class UpdateSchedule(val schedule: ScheduleData) : ScheduleIntent()
    data class DeleteSchedule(val scheduleId: String) : ScheduleIntent()
}
