package com.pdevjay.calendar_with_schedule.utils

import android.util.Log
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringData
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

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

//      // while이 성능에 더 좋을 수 있음
//    fun generateRepeatedDates(
//        repeatType: RepeatType,
//        startDate: LocalDate,
//        monthList: List<YearMonth>? = null,  // 🔹 특정 월 리스트
//        selectedDate: LocalDate? = null // 🔹 특정 날짜만 반환할 경우
//    ): List<LocalDate> {
//        val dates = mutableListOf<LocalDate>()
//        var currentDate = startDate
//
//        while (true) {
//            // 특정 날짜가 주어진 경우, 해당 날짜만 반환
//            if (selectedDate != null) {
//            Log.e("RepeatScheduleGenerator", "generateRepeatedDates: $selectedDate")
//            Log.e("RepeatScheduleGenerator", "currentDate: $currentDate")
//                if (currentDate == selectedDate) return listOf(currentDate)
//                if (currentDate > selectedDate) return dates
//            }
//
//            // 현재 날짜가 monthList 범위를 벗어나면 중단
//            val currentYearMonth = YearMonth.from(currentDate)
//            if (monthList != null) {
//                Log.e("RepeatScheduleGenerator", "monthList: $monthList")
//                Log.e("RepeatScheduleGenerator", "currentYearMonth: $currentYearMonth")
//                if (currentYearMonth > monthList.maxOrNull()!!) return dates // 🔹 종료 조건
//
//                // 특정 월 리스트에 포함된 날짜만 저장
//                if (monthList.contains(currentYearMonth)) {
//                    dates.add(currentDate)
//                }
//            }
//            // 다음 반복 날짜 설정
//            currentDate = when (repeatType) {
//                RepeatType.DAILY -> currentDate.plusDays(1)
//                RepeatType.WEEKLY -> currentDate.plusWeeks(1)
//                RepeatType.BIWEEKLY -> currentDate.plusWeeks(2)
//                RepeatType.MONTHLY -> currentDate.plusMonths(1)
//                RepeatType.YEARLY -> currentDate.plusYears(1)
//                else -> return dates // NONE 또는 CUSTOM일 경우 즉시 반환
//            }
//        }
//    }
//
//}

    fun generateRepeatedDates(
        repeatType: RepeatType,
        startDate: LocalDate,
        monthList: List<YearMonth>? = null,
        selectedDate: LocalDate? = null,
        dateToIgnore: List<LocalDate> = emptyList(),
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
                    .filterNot { dateToIgnore.contains(it) }
                    .find { it == selectedDate }?.let { listOf(it) }
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
            .filterNot { dateToIgnore.contains(it) }
            .filter { date ->
                selectedDate?.let { it == date }
                    ?: monthList.contains(YearMonth.from(date))
            }
            .toList()
    }

    fun generateRepeatedScheduleInstances(schedule: ScheduleData, selectedDay: LocalDate): RecurringData {
//        return schedule.copy(
//            id = "${schedule.id}_${selectedDay}", // 🔹 ID를 다르게 하여 중복 방지
//            start = schedule.start.copy(date = selectedDay), // 🔹 선택된 날짜에 맞게 조정
//            end = schedule.end.copy(date = selectedDay), // 🔹 종료 날짜도 선택된 날짜로 조정
//            isOriginalSchedule = false,
//        )
        return schedule.copy().toRecurringData(selectedDay)
    }

    fun isValidRepeatDate(repeatType: RepeatType, originalStartDate: LocalDate, modifiedDate: LocalDate): Boolean {
        return when (repeatType) {
            // DAILY: 하루 단위로 반복되므로, 항상 true
            RepeatType.DAILY -> true

            // WEEKLY: 7일 단위로 반복 & 요일이 동일해야 함
            RepeatType.WEEKLY -> ChronoUnit.DAYS.between(
                originalStartDate,
                modifiedDate
            ) % 7 == 0L &&
                    originalStartDate.dayOfWeek == modifiedDate.dayOfWeek

            // BIWEEKLY: 14일(2주) 단위로 반복 & 요일이 동일해야 함
            RepeatType.BIWEEKLY -> ChronoUnit.WEEKS.between(
                originalStartDate,
                modifiedDate
            ) % 2 == 0L &&
                    originalStartDate.dayOfWeek == modifiedDate.dayOfWeek

            // MONTHLY: 같은 날에 반복 (예: 매월 12일)
            RepeatType.MONTHLY -> ChronoUnit.MONTHS.between(originalStartDate, modifiedDate) >= 0 &&
                    originalStartDate.dayOfMonth == modifiedDate.dayOfMonth

            // YEARLY: 같은 달, 같은 날에 반복 (예: 매년 3월 12일)
            RepeatType.YEARLY -> ChronoUnit.YEARS.between(originalStartDate, modifiedDate) >= 0 &&
                    originalStartDate.month == modifiedDate.month &&
                    originalStartDate.dayOfMonth == modifiedDate.dayOfMonth

            else -> true // NONE 또는 CUSTOM은 모든 날짜 허용
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
