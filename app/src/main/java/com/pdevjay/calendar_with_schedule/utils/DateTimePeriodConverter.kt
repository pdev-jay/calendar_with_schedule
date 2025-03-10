package com.pdevjay.calendar_with_schedule.utils

import androidx.room.TypeConverter
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import java.time.LocalDate
import java.time.LocalTime

class DateTimePeriodConverter {
    companion object{
        @TypeConverter
        @JvmStatic
        fun fromDateTimePeriod(period: DateTimePeriod): String {
            return "${period.date}|${period.time}"
        }

        @TypeConverter
        @JvmStatic
        fun toDateTimePeriod(data: String): DateTimePeriod {
            val parts = data.split("|")
            return DateTimePeriod(
                date = LocalDate.parse(parts[0]),
                time = LocalTime.parse(parts[1])
            )
        }
    }
}
