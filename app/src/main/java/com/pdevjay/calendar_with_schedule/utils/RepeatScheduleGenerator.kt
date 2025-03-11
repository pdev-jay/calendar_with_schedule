package com.pdevjay.calendar_with_schedule.utils

import android.util.Log
import java.time.LocalDate
import java.time.YearMonth

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
        monthList: List<YearMonth>? = null,  // 🔹 특정 월 리스트 (필수)
        selectedDate: LocalDate? = null // 🔹 특정 날짜만 반환할 경우
    ): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = startDate

        while (true) {
            // 특정 날짜가 주어진 경우, 해당 날짜만 반환
            if (selectedDate != null) {
            Log.e("RepeatScheduleGenerator", "generateRepeatedDates: $selectedDate")
            Log.e("RepeatScheduleGenerator", "currentDate: $currentDate")
                if (currentDate == selectedDate) return listOf(currentDate)
                if (currentDate > selectedDate) return dates
            }

            // 현재 날짜가 monthList 범위를 벗어나면 중단
            val currentYearMonth = YearMonth.from(currentDate)
            if (monthList != null) {
                Log.e("RepeatScheduleGenerator", "monthList: $monthList")
                Log.e("RepeatScheduleGenerator", "currentYearMonth: $currentYearMonth")
                if (currentYearMonth > monthList.maxOrNull()!!) return dates // 🔹 종료 조건

                // 특정 월 리스트에 포함된 날짜만 저장
                if (monthList.contains(currentYearMonth)) {
                    dates.add(currentDate)
                }
            }
            // 다음 반복 날짜 설정
            currentDate = when (repeatType) {
                RepeatType.DAILY -> currentDate.plusDays(1)
                RepeatType.WEEKLY -> currentDate.plusWeeks(1)
                RepeatType.BIWEEKLY -> currentDate.plusWeeks(2)
                RepeatType.MONTHLY -> currentDate.plusMonths(1)
                RepeatType.YEARLY -> currentDate.plusYears(1)
                else -> return dates // NONE 또는 CUSTOM일 경우 즉시 반환
            }
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
