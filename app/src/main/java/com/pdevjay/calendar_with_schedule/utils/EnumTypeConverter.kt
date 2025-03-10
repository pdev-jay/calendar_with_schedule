package com.pdevjay.calendar_with_schedule.utils

import androidx.room.TypeConverter
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatOption


class RepeatOptionConverter {
    @TypeConverter
    fun fromRepeatOption(option: RepeatOption): String {
        return option.name
    }

    @TypeConverter
    fun toRepeatOption(value: String): RepeatOption {
        return try {
//                RepeatOption.valueOf(value)
            enumValueOf<RepeatOption>(value)
        } catch (e: IllegalArgumentException) {
            RepeatOption.NONE
        }
    }
}

class AlarmOptionConverter{
    @TypeConverter
    fun fromAlarmOption(option: AlarmOption): String {
        return option.name
    }

    @TypeConverter
    fun toAlarmOption(value: String): AlarmOption {
        return try {
//                AlarmOption.valueOf(value)
            enumValueOf<AlarmOption>(value)
        } catch (e: IllegalArgumentException) {
            AlarmOption.NONE
        }
    }

}
//class EnumTypeConverter{
//    companion object{
//        @TypeConverter
//        @JvmStatic
//        fun fromRepeatOption(option: RepeatOption): String {
//            return option.name
//        }
//
//        @TypeConverter
//        @JvmStatic
//        fun toRepeatOption(value: String): RepeatOption {
//            return try {
////                RepeatOption.valueOf(value)
//                enumValueOf<RepeatOption>(value)
//            } catch (e: IllegalArgumentException) {
//                RepeatOption.NONE
//            }
//        }
//
//        @TypeConverter
//        @JvmStatic
//        fun fromAlarmOption(option: AlarmOption): String {
//            return option.name
//        }
//
//        @TypeConverter
//        @JvmStatic
//        fun toAlarmOption(value: String): AlarmOption {
//            return try {
////                AlarmOption.valueOf(value)
//                enumValueOf<AlarmOption>(value)
//            } catch (e: IllegalArgumentException) {
//                AlarmOption.NONE
//            }
//        }
//    }
//}
