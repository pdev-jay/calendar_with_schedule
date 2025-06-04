package com.pdevjay.calendar_with_schedule.features.schedule.enums

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.pdevjay.calendar_with_schedule.R

//  일정 반복 옵션 Enum
//enum class RepeatType(val label: String) {
//    NONE("반복 안 함"),
//    DAILY("매일"),
//    WEEKLY("매주"),
//    BIWEEKLY("격주(2주마다)"),
//    MONTHLY("매월"),
//    YEARLY("매년");
//
//    companion object {
//        fun fromLabel(label: String): RepeatType {
//            return RepeatType.entries.find { it.label == label } ?: NONE
//        }
//    }
//}

enum class RepeatType {
    NONE,
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    YEARLY;

    fun getLabel(context: Context): String {
        return when (this) {
            NONE -> context.getString(R.string.repeat_none)
            DAILY -> context.getString(R.string.repeat_daily)
            WEEKLY -> context.getString(R.string.repeat_weekly)
            BIWEEKLY -> context.getString(R.string.repeat_biweekly)
            MONTHLY -> context.getString(R.string.repeat_monthly)
            YEARLY -> context.getString(R.string.repeat_yearly)

        }
    }

    companion object {
        fun fromLabel(context: Context, label: String): RepeatType {
            return entries.find { it.getLabel(context) == label } ?: NONE
        }
    }

    // 기존 label 프로퍼티를 대체하려면 아래처럼 별도 함수 사용
    fun getRawLabel(): String {
        return when (this) {
            NONE -> "반복 안 함"
            DAILY -> "매일"
            WEEKLY -> "매주"
            BIWEEKLY -> "격주(2주마다)"
            MONTHLY -> "매월"
            YEARLY -> "매년"
        }
    }
}


//  일정 알림 옵션 Enum
//enum class AlarmOption(val label: String) {
//    NONE("알림 없음"),
//    AT_TIME("정시에 알림"),
//    MIN_5("5분 전"),
//    MIN_10("10분 전"),
//    MIN_15("15분 전"),
//    MIN_30("30분 전"),
//    HOUR_1("1시간 전"),
//    HOUR_2("2시간 전"),
//    DAY_1("1일 전"),
//    DAY_2("2일 전"),
//    WEEK_1("1주일 전");
//
//    fun requiresPermission(): Boolean {
//        return this != NONE
//    }
//
//    companion object {
//        fun fromLabel(label: String): AlarmOption {
//            return entries.find { it.label == label } ?: NONE
//        }
//    }
//}

enum class AlarmOption {
    NONE,
    AT_TIME,
    MIN_5,
    MIN_10,
    MIN_15,
    MIN_30,
    HOUR_1,
    HOUR_2,
    DAY_1,
    DAY_2,
    WEEK_1;

    fun requiresPermission(): Boolean {
        return this != NONE
    }

    fun getLabel(context: Context): String {
        return when (this) {
            NONE -> context.getString(R.string.alarm_none)
            AT_TIME -> context.getString(R.string.alarm_at_time)
            MIN_5 -> context.getString(R.string.alarm_5_min)
            MIN_10 -> context.getString(R.string.alarm_10_min)
            MIN_15 -> context.getString(R.string.alarm_15_min)
            MIN_30 -> context.getString(R.string.alarm_30_min)
            HOUR_1 -> context.getString(R.string.alarm_1_hour)
            HOUR_2 -> context.getString(R.string.alarm_2_hour)
            DAY_1 -> context.getString(R.string.alarm_1_day)
            DAY_2 -> context.getString(R.string.alarm_2_day)
            WEEK_1 -> context.getString(R.string.alarm_1_week)
        }
    }

    fun getRawLabel(): String {
        return when (this) {
            NONE -> "알림 없음"
            AT_TIME -> "정시에 알림"
            MIN_5 -> "5분 전"
            MIN_10 -> "10분 전"
            MIN_15 -> "15분 전"
            MIN_30 -> "30분 전"
            HOUR_1 -> "1시간 전"
            HOUR_2 -> "2시간 전"
            DAY_1 -> "1일 전"
            DAY_2 -> "2일 전"
            WEEK_1 -> "1주일 전"
        }
    }

    companion object {
        fun fromLabel(context: Context, label: String): AlarmOption {
            return entries.find { it.getLabel(context) == label } ?: NONE
        }
    }
}


//@Composable
//fun RepeatType.getLabel(): String {
//    val context = LocalContext.current
//    return when (this) {
//        RepeatType.NONE -> context.getString(R.string.repeat_none)
//        RepeatType.DAILY -> context.getString(R.string.repeat_daily)
//        RepeatType.WEEKLY -> context.getString(R.string.repeat_weekly)
//        RepeatType.BIWEEKLY -> context.getString(R.string.repeat_biweekly)
//        RepeatType.MONTHLY -> context.getString(R.string.repeat_monthly)
//        RepeatType.YEARLY -> context.getString(R.string.repeat_yearly)
//    }
//}
//
//@Composable
//fun AlarmOption.getLabel(): String {
//    val context = LocalContext.current
//    return when (this) {
//        AlarmOption.NONE -> context.getString(R.string.alarm_none)
//        AlarmOption.AT_TIME -> context.getString(R.string.alarm_at_time)
//        AlarmOption.MIN_5 -> context.getString(R.string.alarm_5_min)
//        AlarmOption.MIN_10 -> context.getString(R.string.alarm_10_min)
//        AlarmOption.MIN_15 -> context.getString(R.string.alarm_15_min)
//        AlarmOption.MIN_30 -> context.getString(R.string.alarm_30_min)
//        AlarmOption.HOUR_1 -> context.getString(R.string.alarm_1_hour)
//        AlarmOption.HOUR_2 -> context.getString(R.string.alarm_2_hour)
//        AlarmOption.DAY_1 -> context.getString(R.string.alarm_1_day)
//        AlarmOption.DAY_2 -> context.getString(R.string.alarm_2_day)
//        AlarmOption.WEEK_1 -> context.getString(R.string.alarm_1_week)
//    }
//}


enum class ScheduleEditType {
    ONLY_THIS_EVENT,
    THIS_AND_FUTURE,
    ALL_EVENTS
}

enum class ScheduleColor(val colorInt: Int, val displayName: String) {
    RED(0xFFFF3B30.toInt(), "Red"),
    GREEN(0xFF34C759.toInt(), "Green"),
    BLUE(0xFF007AFF.toInt(), "Blue"),
    ORANGE(0xFFFF9500.toInt(), "Orange"),
    PURPLE(0xFF5856D6.toInt(), "Purple"),
    CYAN(0xFF5AC8FA.toInt(), "Cyan"),
    YELLOW(0xFFFFCC00.toInt(), "Yellow"),
    GRAY(0xFF8E8E93.toInt(), "Gray");

    companion object {
        fun fromColorInt(colorInt: Int?): ScheduleColor? {
            return entries.find { it.colorInt == colorInt }
        }
    }
}
