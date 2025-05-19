package com.pdevjay.calendar_with_schedule.utils

import com.github.usingsky.calendar.KoreanLunarCalendar
import java.time.LocalDate

object LunarCalendarUtils {
    fun getLunarMonthDay(date: LocalDate): String {
        val calendar = KoreanLunarCalendar.getInstance()
        val isValidDate = calendar.setSolarDate(
            date.year,
            date.monthValue,
            date.dayOfMonth
        )

        val lunarMonthDay = if (isValidDate){
            val parts = calendar.lunarIsoFormat.split("-")
            val regex = Regex("""\d+""")
            val match = regex.find(parts[2])?.value ?: ""
            "${parts[1].toInt()}/${match.toInt()}"
        } else {
            ""
        }

        return lunarMonthDay
    }
}