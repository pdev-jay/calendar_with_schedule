package com.pdevjay.calendar_with_schedule.screens.schedule.data

import com.google.gson.annotations.SerializedName
import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.utils.RRuleHelper
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import java.time.LocalDate
import java.util.UUID

data class RecurringData(
    @SerializedName("id") override val id: String,
    @SerializedName("originalEventId") val originalEventId: String,
    @SerializedName("originalRecurringDate") val originalRecurringDate: LocalDate,
    @SerializedName("originatedFrom") val originatedFrom: String,
    @SerializedName("title") override val title: String,
    @SerializedName("location") override val location: String?,
    @SerializedName("isAllDay") override val isAllDay: Boolean,
    @SerializedName("start") override val start: DateTimePeriod,
    @SerializedName("end") override val end: DateTimePeriod,
    @SerializedName("repeatType") override val repeatType: RepeatType,
    @SerializedName("repeatUntil") override val repeatUntil: LocalDate?,
    @SerializedName("repeatRule") override val repeatRule: String?,
    @SerializedName("alarmOption") override val alarmOption: AlarmOption,
    @SerializedName("isDeleted") val isDeleted: Boolean,
    @SerializedName("isFirstSchedule") val isFirstSchedule: Boolean = false,
    @SerializedName("branchId") override val branchId: String? = null
) : BaseSchedule(id, title, location, isAllDay, start, end, repeatType, repeatUntil, repeatRule, alarmOption, branchId)


fun RecurringData.toRecurringScheduleEntity(): RecurringScheduleEntity {
    return RecurringScheduleEntity(
        id = this.id,
        originalEventId = this.originalEventId,
        originalRecurringDate = this.originalRecurringDate,
        originatedFrom = this.originatedFrom,
        title = this.title,
        location = this.location,
        isAllDay = this.isAllDay,
        start = this.start,
        end = this.end,
        repeatType = this.repeatType,
        repeatUntil = this.repeatUntil,
        repeatRule = RRuleHelper.generateRRule(this.repeatType, this.start.date, this.repeatUntil),
        alarmOption = this.alarmOption,
        isDeleted = this.isDeleted,
        isFirstSchedule = this.isFirstSchedule,
        branchId = this.branchId
    )
}

fun RecurringData.toSingleChangeData(): RecurringData{
    return this.copy(
        id = UUID.randomUUID().toString(),
        repeatType = RepeatType.NONE,
        repeatUntil = null,
        repeatRule = null,
    )
}

fun RecurringData.toMarkAsDeletedData(originalRecurringDate: LocalDate): RecurringData{
    return this.copy(
        originalRecurringDate = originalRecurringDate,
        isDeleted = true
    )
}

fun RecurringData.toNewBranchData(): RecurringData {
    return this.copy(
        id = UUID.randomUUID().toString(),
        isFirstSchedule = true,
        branchId = UUID.randomUUID().toString(),
        originalRecurringDate = this.start.date,
        start = this.start.copy(date = this.start.date),
        end = this.end.copy(date = this.start.date)
    )
}

fun RecurringData.resolveDisplayOnly(branchRoot: RecurringData): RecurringData {
    return this.copy(
        repeatType = branchRoot.repeatType,
        repeatUntil = branchRoot.repeatUntil,
        repeatRule = branchRoot.repeatRule,
    )
}


