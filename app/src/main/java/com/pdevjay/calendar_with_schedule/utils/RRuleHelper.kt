package com.pdevjay.calendar_with_schedule.utils

import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object RRuleHelper {

    /**
     * 🔹 RRule 생성 (반복 유형, 시작 날짜, 종료 날짜 기반)
     */
    fun generateRRule(
        repeatType: RepeatType,
        startDate: LocalDate,
        repeatUntil: LocalDate? = null
    ): String? {
        return when (repeatType) {
            RepeatType.NONE -> null
            RepeatType.DAILY -> "FREQ=DAILY" + getUntilClause(repeatUntil)
            RepeatType.WEEKLY -> "FREQ=WEEKLY;BYDAY=${startDate.dayOfWeek.toRRule()}" + getUntilClause(repeatUntil)
            RepeatType.BIWEEKLY -> "FREQ=WEEKLY;INTERVAL=2;BYDAY=${startDate.dayOfWeek.toRRule()}" + getUntilClause(repeatUntil)
            RepeatType.MONTHLY -> "FREQ=MONTHLY;BYMONTHDAY=${startDate.dayOfMonth}" + getUntilClause(repeatUntil)
            RepeatType.YEARLY -> "FREQ=YEARLY;BYMONTH=${startDate.monthValue};BYMONTHDAY=${startDate.dayOfMonth}" + getUntilClause(repeatUntil)
        }
    }

    /**
     * 🔹 UNTIL (반복 종료일) 구문 추가
     */
    private fun getUntilClause(repeatUntil: LocalDate?): String {
        return repeatUntil?.let { ";UNTIL=${it.format(DateTimeFormatter.BASIC_ISO_DATE)}" } ?: ""
    }

    /**
     * 🔹 DayOfWeek → RRule 형식 변환
     */
    private fun DayOfWeek.toRRule(): String {
        return name.take(2).uppercase(Locale.US)
    }
}
