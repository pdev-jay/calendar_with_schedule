package com.pdevjay.calendar_with_schedule.features.schedule.data

import com.google.gson.annotations.SerializedName
import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.features.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.core.utils.helpers.RRuleHelper
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType
import java.time.LocalDate
import java.time.temporal.ChronoUnit
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
    @SerializedName("branchId") override val branchId: String? = null,
    @SerializedName("repeatIndex") val repeatIndex: Int,
    @SerializedName("color") override val color: Int? = null
) : BaseSchedule(id, title, location, isAllDay, start, end, repeatType, repeatUntil, repeatRule, alarmOption, branchId, color)


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
        branchId = this.branchId,
        repeatIndex = this.repeatIndex,
        color = this.color
    )
}

fun RecurringData.toScheduleData(): ScheduleData {
    return ScheduleData(
        id = this.originalEventId,
        start = this.start,
        end = this.end,
        location = this.location,
        title = this.title,
        repeatType = this.repeatType,
        repeatUntil = this.repeatUntil,
        repeatRule = this.repeatRule,
        alarmOption = this.alarmOption,
        isAllDay = this.isAllDay,
        color = this.color
    )
}

fun RecurringData.toSingleChangeData(needNewId: Boolean): RecurringData {
    return this.copy(
        id = if (needNewId) UUID.randomUUID().toString() else this.id,
        repeatType = RepeatType.NONE,
        repeatUntil = null,
        repeatRule = null,
    )
}

fun RecurringData.toMarkAsDeletedData(): RecurringData {
    return this.copy(
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
        end = this.end.copy(date = this.start.date),
        repeatIndex = 1
    )
}

fun RecurringData.resolveDisplayOnly(branchRoot: RecurringData): RecurringData {
    return this.copy(
        repeatType = branchRoot.repeatType,
        repeatUntil = branchRoot.repeatUntil,
        repeatRule = branchRoot.repeatRule,
    )
}

fun RecurringData.resolveDisplayFieldsFromBranch(branchRoots: List<RecurringData>): RecurringData {
    val branchRoot = branchRoots.find { it.branchId == this.branchId && it.isFirstSchedule } ?: return this

    //  수정된 인스턴스에 대해서는 branch 정보를 덮어씌움
    val resolved = this.copy(
        repeatType = branchRoot.repeatType,
        repeatUntil = branchRoot.repeatUntil,
        repeatRule = branchRoot.repeatRule,
    )

    //  반복 주기 안에 있는 경우 날짜도 보정
    return if (isInRepeatPattern(this.start.date, branchRoot.start.date, branchRoot.repeatType)) {
        val newDate = calculateRepeatDateFromIndex(
            startDate = branchRoot.start.date,
            repeatType = branchRoot.repeatType,
            index = this.repeatIndex
        )

        resolved.copy(
            start = resolved.start.copy(date = newDate),
            end = resolved.end.copy(date = newDate)
        )
    } else {
        resolved
    }
}


fun calculateRepeatDateFromIndex(
    startDate: LocalDate,
    repeatType: RepeatType,
    index: Int
): LocalDate {
    return when (repeatType) {
        RepeatType.DAILY -> startDate.plusDays((index - 1).toLong())
        RepeatType.WEEKLY -> startDate.plusWeeks((index - 1).toLong())
        RepeatType.BIWEEKLY -> startDate.plusWeeks(2L * (index - 1))
        RepeatType.MONTHLY -> startDate.plusMonths((index - 1).toLong())
        RepeatType.YEARLY -> startDate.plusYears((index - 1).toLong())
        else -> startDate
    }
}

fun isInRepeatPattern(
    candidateDate: LocalDate,
    startDate: LocalDate,
    repeatType: RepeatType
): Boolean {
    if (candidateDate < startDate) return false

    return when (repeatType) {
        RepeatType.DAILY -> true
        RepeatType.WEEKLY -> {
            val weeks = ChronoUnit.WEEKS.between(startDate, candidateDate)
            startDate.plusWeeks(weeks) == candidateDate
        }

        RepeatType.BIWEEKLY -> {
            val weeks = ChronoUnit.WEEKS.between(startDate, candidateDate)
            weeks >= 0 && weeks % 2L == 0L && startDate.plusWeeks(weeks) == candidateDate
        }

        RepeatType.MONTHLY -> {
            val months = ChronoUnit.MONTHS.between(startDate, candidateDate)
            startDate.plusMonths(months) == candidateDate
        }

        RepeatType.YEARLY -> {
            val years = ChronoUnit.YEARS.between(startDate, candidateDate)
            startDate.plusYears(years) == candidateDate
        }

        else -> false
    }
}
