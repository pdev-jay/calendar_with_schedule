package com.pdevjay.calendar_with_schedule.core.utils.helpers

import com.pdevjay.calendar_with_schedule.features.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.features.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.features.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.features.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.features.schedule.data.toDateTime
import com.pdevjay.calendar_with_schedule.features.schedule.data.toRecurringData
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

object RepeatScheduleGenerator {
    /**
     * 반복 규칙(RepeatType)에 따라 일정 날짜를 생성하는 함수
     *
     * @param repeatType 반복 타입 (DAILY, WEEKLY, MONTHLY 등)
     * @param startDate 일정 시작 날짜
     * @param monthList 현재 로드된 달 (현재 달 - 1, 현재 달, 현재 달 + 1)
     * @param selectedDate 특정 날짜만 반환할 경우
     * @return 반복 일정이 적용된 날짜 리스트
     */

    fun generateRepeatedDates(
        repeatType: RepeatType,
        startDate: LocalDate,
        monthList: List<YearMonth>? = null,
        selectedDate: LocalDate? = null,
        dateToIgnore: Set<LocalDate> = emptySet(), //  List -> Set으로 변경 (중복 제거 및 성능 향상)
        repeatUntil: LocalDate? = null //  repeatUntil 추가
    ): List<LocalDate> {
        if (monthList == null) {
            return selectedDate?.let {
                generateSequence(startDate) { currentDate ->
                    when (repeatType) {
                        RepeatType.DAILY -> currentDate.plusDays(1)
                        RepeatType.WEEKLY -> currentDate.plusWeeks(1)
                        RepeatType.BIWEEKLY -> currentDate.plusWeeks(2)
                        RepeatType.MONTHLY -> currentDate.plusMonths(1)
                        RepeatType.YEARLY -> currentDate.plusYears(1)
                        else -> null
                    }
                }
                    .takeWhile { it <= selectedDate && (repeatUntil == null || it <= repeatUntil) } //  repeatUntil 반영
                    .filterNot { it in dateToIgnore }
                    .find { it == selectedDate }
                    ?.let { listOf(it) }
                    ?: emptyList()
            } ?: emptyList()
        }

        if (monthList.isEmpty()) return emptyList()

        val maxMonth = monthList.maxOrNull() ?: return emptyList()

        return generateSequence(startDate) { currentDate ->
            when (repeatType) {
                RepeatType.DAILY -> currentDate.plusDays(1)
                RepeatType.WEEKLY -> currentDate.plusWeeks(1)
                RepeatType.BIWEEKLY -> currentDate.plusWeeks(2)
                RepeatType.MONTHLY -> currentDate.plusMonths(1)
                RepeatType.YEARLY -> currentDate.plusYears(1)
                else -> null
            }
        }
            .takeWhile { date -> YearMonth.from(date) <= maxMonth && (repeatUntil == null || date <= repeatUntil) } //  repeatUntil 반영
            .filterNot { it in dateToIgnore } //  contains() 대신 `in` 사용 (Set으로 최적화)
            .filter { date ->
                selectedDate?.let { it == date }
                    ?: monthList.contains(YearMonth.from(date))
            }
            .toList()
    }


    fun generateRepeatedDatesWithIndex(
        repeatType: RepeatType,
        startDate: LocalDate,
        monthList: List<YearMonth>? = null,
        indicesToIgnore: Set<Int> = emptySet(), //  인덱스 기반 필터링
        repeatUntil: LocalDate? = null
    ): List<Pair<Int, LocalDate>> {
        val result = mutableListOf<Pair<Int, LocalDate>>()

        var current = startDate
        var index = 1

        while (true) {
            if (repeatUntil != null && current > repeatUntil) break
            if (monthList != null && YearMonth.from(current) > monthList.maxOrNull()) break
            if (index !in indicesToIgnore) {
                result.add(index to current)
            }

            current = when (repeatType) {
                RepeatType.DAILY -> current.plusDays(1)
                RepeatType.WEEKLY -> current.plusWeeks(1)
                RepeatType.BIWEEKLY -> current.plusWeeks(2)
                RepeatType.MONTHLY -> current.plusMonths(1)
                RepeatType.YEARLY -> current.plusYears(1)
                else -> break
            }
            index++
        }

        return result
    }
    fun generateRepeatedDatesForAlarm(
        repeatType: RepeatType,
        startDate: LocalDate,
        repeatUntil: LocalDate? = null
    ): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        var current = startDate

        while (true) {
            if (repeatUntil != null && current > repeatUntil) break
            result.add(current) //  시작 날짜 포함

            current = when (repeatType) {
                RepeatType.DAILY -> current.plusDays(1)
                RepeatType.WEEKLY -> current.plusWeeks(1)
                RepeatType.BIWEEKLY -> current.plusWeeks(2)
                RepeatType.MONTHLY -> current.plusMonths(1)
                RepeatType.YEARLY -> current.plusYears(1)
                else -> break
            }
        }

        return result
    }


    fun generateRepeatedScheduleInstances(
        schedule: BaseSchedule,
        selectedDate: LocalDate,
        index: Int
    ): RecurringData {
        // 시작일 → LocalDateTime
        val originalStartDateTime = schedule.start.toDateTime()
        val originalEndDateTime = schedule.end.toDateTime()

        // duration 계산
        val duration = Duration.between(originalStartDateTime, originalEndDateTime)

        // 새 start 시간 기준의 end 계산
        val newStart = schedule.start.copy(date = selectedDate)
        val newEndDateTime = newStart.toDateTime().plus(duration)
        val newEnd = DateTimePeriod(
            date = newEndDateTime.toLocalDate(),
            time = newEndDateTime.toLocalTime()
        )

        return when(schedule) {
            is ScheduleData -> {
                schedule.toRecurringData(selectedDate = selectedDate, repeatIndex = index).copy(
                    isFirstSchedule = (index == 1),
                    start = newStart,
                    end = newEnd,

                    repeatIndex = index
                )
            }

            is RecurringData -> {
                schedule.copy(
                    id = if (index == 1) schedule.id else UUID.randomUUID().toString(),
                    start = newStart,
                    end = newEnd,
                    originalEventId = schedule.originalEventId,
                    originalRecurringDate = selectedDate,
                    originatedFrom = schedule.id,
                    isFirstSchedule = (index == 1),
                    isDeleted = false,
                    repeatIndex = index
                )
            }

            else -> {
                throw IllegalArgumentException("Invalid schedule type")
            }
        }
    }
//    fun generateRepeatedScheduleInstances(
//        schedule: ScheduleData,
//        selectedDate: LocalDate,
//        index: Int
//    ): RecurringData {
//        // 시작일 → LocalDateTime
//        val originalStartDateTime = schedule.start.toDateTime()
//        val originalEndDateTime = schedule.end.toDateTime()
//
//        // duration 계산
//        val duration = Duration.between(originalStartDateTime, originalEndDateTime)
//
//        // 새 start 시간 기준의 end 계산
//        val newStart = schedule.start.copy(date = selectedDate)
//        val newEndDateTime = newStart.toDateTime().plus(duration)
//        val newEnd = DateTimePeriod(
//            date = newEndDateTime.toLocalDate(),
//            time = newEndDateTime.toLocalTime()
//        )
//
//        return schedule.toRecurringData(selectedDate = selectedDate, repeatIndex = index).copy(
//            isFirstSchedule = (index == 1),
//            start = newStart,
//            end = newEnd,
//
//            repeatIndex = index
//        )
//    }

//    fun generateRepeatedScheduleInstances(
//        schedule: RecurringData,
//        selectedDate: LocalDate,
//        index: Int
//    ): RecurringData {
//        // 시작일 → LocalDateTime
//        val originalStartDateTime = schedule.start.toDateTime()
//        val originalEndDateTime = schedule.end.toDateTime()
//
//        // duration 계산
//        val duration = Duration.between(originalStartDateTime, originalEndDateTime)
//
//        // 새 start 시간 기준의 end 계산
//        val newStart = schedule.start.copy(date = selectedDate)
//        val newEndDateTime = newStart.toDateTime().plus(duration)
//        val newEnd = DateTimePeriod(
//            date = newEndDateTime.toLocalDate(),
//            time = newEndDateTime.toLocalTime()
//        )
//
//        return schedule.copy(
//            id = if (index == 1) schedule.id else UUID.randomUUID().toString(),
//            start = newStart,
//            end = newEnd,
//            originalEventId = schedule.originalEventId,
//            originalRecurringDate = selectedDate,
//            originatedFrom = schedule.id,
//            isFirstSchedule = (index == 1),
//            isDeleted = false,
//            repeatIndex = index
//        )
//    }
//
//
}

