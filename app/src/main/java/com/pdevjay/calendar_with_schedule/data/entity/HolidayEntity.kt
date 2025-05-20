package com.pdevjay.calendar_with_schedule.data.entity

import androidx.room.Entity
import com.pdevjay.calendar_with_schedule.screens.calendar.data.HolidayData

@Entity(tableName = "holidays", primaryKeys = ["date", "seq"])
data class HolidayDataEntity(
    val date: String,
    val name: String,
    val isHoliday: Boolean,
    val seq: Int,
    val updatedAt: String
) {
    fun toModel() = HolidayData(date, name, isHoliday, seq, updatedAt)
}

