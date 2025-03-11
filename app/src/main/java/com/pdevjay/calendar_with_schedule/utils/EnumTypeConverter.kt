package com.pdevjay.calendar_with_schedule.utils

import androidx.room.TypeConverter
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption

class AlarmOptionConverter{
    @TypeConverter
    fun fromAlarmOption(option: AlarmOption): String {
        return option.name
    }

    @TypeConverter
    fun toAlarmOption(value: String): AlarmOption {
        return try {
            AlarmOption.valueOf(value)
        } catch (e: IllegalArgumentException) {
            AlarmOption.NONE
        }
    }

}
