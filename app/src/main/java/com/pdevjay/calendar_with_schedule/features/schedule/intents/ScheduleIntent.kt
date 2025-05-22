package com.pdevjay.calendar_with_schedule.features.schedule.intents

import com.pdevjay.calendar_with_schedule.features.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.features.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.features.schedule.enums.ScheduleEditType

sealed class ScheduleIntent {
    data class AddSchedule(val schedule: ScheduleData) : ScheduleIntent()
    data class UpdateSchedule(val oldSchedule: RecurringData, val newSchedule: RecurringData, val editType: ScheduleEditType, val isOnlyContentChanged: Boolean = false) : ScheduleIntent()
    data class DeleteSchedule(val schedule: RecurringData, val editType: ScheduleEditType) : ScheduleIntent()
}
