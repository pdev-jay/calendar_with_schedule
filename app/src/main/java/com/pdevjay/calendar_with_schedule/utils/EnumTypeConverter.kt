package com.pdevjay.calendar_with_schedule.utils

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.pdevjay.calendar_with_schedule.screens.schedule.enum.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enum.RepeatOption

class EnumTypeConverter{
    @TypeConverter
    fun fromRepeatOption(option: RepeatOption): String {
        return option.name
    }

    @TypeConverter
    fun toRepeatOption(value: String): RepeatOption {
        return try {
            RepeatOption.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RepeatOption.NONE
        }
    }

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
