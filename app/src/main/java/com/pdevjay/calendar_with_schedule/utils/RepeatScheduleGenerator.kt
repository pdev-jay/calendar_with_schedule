package com.pdevjay.calendar_with_schedule.utils

import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringData
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
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
        dateToIgnore: Set<LocalDate> = emptySet(), // 🔹 List -> Set으로 변경 (중복 제거 및 성능 향상)
        repeatUntil: LocalDate? = null // 🔹 repeatUntil 추가
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
                    .takeWhile { it <= selectedDate && (repeatUntil == null || it <= repeatUntil) } // 🔹 repeatUntil 반영
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
            .takeWhile { date -> YearMonth.from(date) <= maxMonth && (repeatUntil == null || date <= repeatUntil) } // 🔹 repeatUntil 반영
            .filterNot { it in dateToIgnore } // 🔹 contains() 대신 `in` 사용 (Set으로 최적화)
            .filter { date ->
                selectedDate?.let { it == date }
                    ?: monthList.contains(YearMonth.from(date))
            }
            .toList()
    }


    fun generateRepeatedScheduleInstances(schedule: BaseSchedule, selectedDay: LocalDate): RecurringData {
       return when(schedule){
            is ScheduleData -> {
                schedule.toRecurringData(selectedDate = selectedDay)
                    .copy(isFirstSchedule = selectedDay == schedule.start.date)
            }
            is RecurringData -> {
                schedule.copy(
                    id = UUID.randomUUID().toString(),
                    start = schedule.start.copy(date = selectedDay),
                    end = schedule.end.copy(date = selectedDay),
                    originalEventId = schedule.originalEventId,
                    originalRecurringDate = selectedDay,
                    originatedFrom = schedule.id,
                    isFirstSchedule = false,
                    isDeleted = false // ui에 보여지는 반복일정이니 false -> isDeleted인 일정은 이미 제외되어 있음
                )

            }

           else -> { schedule as RecurringData }
       }
    }

}

enum class RepeatType(val label: String) {
    NONE("반복 안 함"),
    DAILY("매일"),
    WEEKLY("매주"),
    BIWEEKLY("격주(2주마다)"),
    MONTHLY("매월"),
    YEARLY("매년");

    companion object {
        fun fromLabel(label: String): RepeatType {
            return RepeatType.entries.find { it.label == label } ?: NONE
        }
    }
}
