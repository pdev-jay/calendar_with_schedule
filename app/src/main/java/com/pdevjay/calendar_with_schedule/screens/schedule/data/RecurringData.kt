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
    @SerializedName("originalStartDate") override val originalStartDate: LocalDate = start.date,
    @SerializedName("repeatType") override val repeatType: RepeatType,
    @SerializedName("repeatUntil") override val repeatUntil: LocalDate?,
    @SerializedName("repeatRule") override val repeatRule: String?,
    @SerializedName("alarmOption") override val alarmOption: AlarmOption,
    @SerializedName("isOriginalSchedule") override val isOriginalSchedule: Boolean = false,
    @SerializedName("isDeleted") val isDeleted: Boolean,
    @SerializedName("originalRepeatUntil") val originalRepeatUntil: LocalDate? = null,
    @SerializedName("isFirstSchedule") val isFirstSchedule: Boolean = false,
    @SerializedName("branchId") val branchId: String? = null
) : BaseSchedule(id, title, location, isAllDay, start, end, originalStartDate, repeatType, repeatUntil, repeatRule, alarmOption, isOriginalSchedule)


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
        originalStartDate = this.start.date,
        repeatType = this.repeatType,
        repeatUntil = this.repeatUntil,
        repeatRule = RRuleHelper.generateRRule(this.repeatType, this.start.date, this.repeatUntil),
        alarmOption = this.alarmOption,
        isOriginalSchedule = this.isOriginalSchedule,
        isDeleted = this.isDeleted,
        originalRepeatUntil = this.originalRepeatUntil,
        isFirstSchedule = this.isFirstSchedule,
        branchId = this.branchId
    )
}

fun RecurringData.toOverriddenRecurringData(
    originalRecurringDate: LocalDate? = null,
    selectedDate: LocalDate,
): RecurringData {
    return this.copy(
        id = UUID.randomUUID().toString(),
        originalRecurringDate = originalRecurringDate ?: selectedDate,
        originatedFrom = this.id,
        start = this.start.copy(date = selectedDate),
        end = this.end.copy(date = selectedDate),
        repeatType = this.repeatType,
        repeatUntil = null,
        isOriginalSchedule = false,
        isDeleted = false,
        branchId = UUID.randomUUID().toString() // ✅ override vs branch
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
//        start = this.start.copy(date = originalRecurringDate),
//        end = this.end.copy(date = originalRecurringDate),
//        repeatType = RepeatType.NONE,
//        repeatUntil = null,
//        repeatRule = null,
        isDeleted = true
    )
}

// OnlyContentChanged할 때 사용
//fun RecurringData.toOnlyContentChangedData():RecurringData{
//    return this.copy(
//        repeatType = RepeatType.NONE,
//        repeatUntil = null,
//        repeatRule = null,
//    )
//}

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


// recurring data를 기반으로 새로운 schedule data를 저장할 때
fun RecurringData.toScheduleData(): ScheduleData {
    return ScheduleData(
        id = this.originalEventId,
        title = this.title ?: "New Event", // 기본 제목 설정
        location = this.location,
        isAllDay = this.isAllDay,
        start = this.start,
        end = this.end,
        repeatType = this.repeatType,
        repeatUntil = this.repeatUntil,
        repeatRule = this.repeatRule,
        alarmOption = this.alarmOption,
        isOriginalSchedule = true
    )
}

fun RecurringData.resolveDisplayFieldsFromBranch(branchRoots: List<RecurringData>): RecurringData {
    if (this.isFirstSchedule || this.repeatType != RepeatType.NONE) return this

    val branchRoot = branchRoots.find { it.branchId == this.branchId && it.isFirstSchedule }
    return if (branchRoot != null) {
        this.copy(
            repeatType = branchRoot.repeatType,
            repeatUntil = branchRoot.repeatUntil,
            repeatRule = branchRoot.repeatRule,
            alarmOption = branchRoot.alarmOption
        )
    } else {
        this
    }
}

fun RecurringData.resolveDisplayOnly(branchRoot: RecurringData): RecurringData {
    return this.copy(
        repeatType = branchRoot.repeatType,
        repeatUntil = branchRoot.repeatUntil,
        repeatRule = branchRoot.repeatRule,
        alarmOption = branchRoot.alarmOption
    )
}


