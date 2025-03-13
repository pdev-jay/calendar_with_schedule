package com.pdevjay.calendar_with_schedule.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
     * 🔹 RRule을 파싱하여 반복 유형과 종료 날짜를 반환
     */
    fun parseRRule(rrule: String): Pair<RepeatType, LocalDate?> {
        val params = rrule.split(";").associate {
            val (key, value) = it.split("=")
            key to value
        }

        val repeatType = when (params["FREQ"]) {
            "DAILY" -> RepeatType.DAILY
            "WEEKLY" -> if (params["INTERVAL"] == "2") RepeatType.BIWEEKLY else RepeatType.WEEKLY
            "MONTHLY" -> RepeatType.MONTHLY
            "YEARLY" -> RepeatType.YEARLY
            else -> RepeatType.NONE
        }

        val repeatUntil = params["UNTIL"]?.let {
            LocalDate.parse(it, DateTimeFormatter.BASIC_ISO_DATE) // YYYYMMDD 형식
        }

        return repeatType to repeatUntil
    }

    /**
     * 🔹 주어진 RRule을 기반으로 반복 일정 날짜 리스트 반환
     */
    fun getRecurringDates(rrule: String, startDate: LocalDate, maxDate: LocalDate): List<LocalDate> {
        val (repeatType, repeatUntil) = parseRRule(rrule)
        val untilDate = repeatUntil ?: maxDate

        return generateSequence(startDate) { current ->
            when (repeatType) {
                RepeatType.DAILY -> current.plusDays(1)
                RepeatType.WEEKLY -> current.plusWeeks(1)
                RepeatType.BIWEEKLY -> current.plusWeeks(2)
                RepeatType.MONTHLY -> current.plusMonths(1)
                RepeatType.YEARLY -> current.plusYears(1)
                else -> null
            }
        }.takeWhile { it <= untilDate }.toList()
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
