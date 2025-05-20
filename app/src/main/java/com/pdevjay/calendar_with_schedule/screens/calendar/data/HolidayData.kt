package com.pdevjay.calendar_with_schedule.screens.calendar.data

import com.pdevjay.calendar_with_schedule.data.entity.HolidayDataEntity

data class HolidayData(
    val date: String,
    val name: String,
    val isHoliday: Boolean,
    val seq: Int,
    val updatedAt: String
)

fun HolidayData.toEntity() = HolidayDataEntity(date, name, isHoliday, seq, updatedAt)
