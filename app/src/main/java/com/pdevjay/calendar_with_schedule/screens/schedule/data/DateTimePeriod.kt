package com.pdevjay.calendar_with_schedule.screens.schedule.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class DateTimePeriod(
    val date: LocalDate,
    val time: LocalTime
)

fun DateTimePeriod.toDateTime(): LocalDateTime {
    return LocalDateTime.of(date, time)
}

// DateTimePeriod -> 분 단위 변환
fun DateTimePeriod.toMinutes(): Int {
    return this.date.dayOfYear * 1440 + this.time.hour * 60 + this.time.minute
}

operator fun LocalDate.rangeTo(other: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var current = this
    while (!current.isAfter(other)) {
        dates.add(current)
        current = current.plusDays(1)
    }
    return dates
}
