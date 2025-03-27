package com.pdevjay.calendar_with_schedule.screens.schedule.intents

import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.ScheduleEditType
import java.time.LocalDate

sealed class ScheduleIntent {
    data class AddSchedule(val schedule: ScheduleData) : ScheduleIntent()

    data class UpdateSchedule(val schedule: RecurringData, val editType: ScheduleEditType, val isOnlyContentChanged: Boolean = false) : ScheduleIntent()
    data class DeleteSchedule(val schedule: RecurringData, val editType: ScheduleEditType) : ScheduleIntent()
}
