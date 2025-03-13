package com.pdevjay.calendar_with_schedule.screens.schedule.intents

import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData

sealed class ScheduleIntent {
    data class AddSchedule(val schedule: ScheduleData) : ScheduleIntent()
    data class UpdateSchedule(val schedule: ScheduleData) : ScheduleIntent()

    data class UpdateFutureRecurringSchedule(val schedule: RecurringData) : ScheduleIntent()
    data class UpdateSingleRecurringSchedule(val schedule: RecurringData) : ScheduleIntent()

    data class DeleteFutureRecurringSchedule(val schedule: RecurringData) : ScheduleIntent()
    data class DeleteSingleRecurringSchedule(val schedule: RecurringData) : ScheduleIntent()

    data class UpdateFutureSchedule(val schedule: ScheduleData) : ScheduleIntent()
    data class UpdateSingleSchedule(val schedule: ScheduleData) : ScheduleIntent()

    data class DeleteSingleSchedule(val schedule: ScheduleData) : ScheduleIntent()
    data class DeleteFutureSchedule(val schedule: ScheduleData) : ScheduleIntent()
}
