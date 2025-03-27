package com.pdevjay.calendar_with_schedule.screens.schedule.enums

// 🔹 일정 반복 옵션 Enum
enum class RepeatOption(val label: String) {
    NONE("반복 안 함"),
    DAILY("매일"),
    WEEKLY("매주"),
    BIWEEKLY("격주(2주마다)"),
    MONTHLY("매월"),
    YEARLY("매년");

    companion object {
        fun fromLabel(label: String): RepeatOption {
            return entries.find { it.label == label } ?: NONE
        }
    }
}

// 🔹 일정 알림 옵션 Enum
enum class AlarmOption(val label: String) {
    NONE("알림 없음"),
    AT_TIME("정시에 알림"),
    MIN_5("5분 전"),
    MIN_10("10분 전"),
    MIN_15("15분 전"),
    MIN_30("30분 전"),
    HOUR_1("1시간 전"),
    HOUR_2("2시간 전"),
    DAY_1("1일 전"),
    DAY_2("2일 전"),
    WEEK_1("1주일 전");

    companion object {
        fun fromLabel(label: String): AlarmOption {
            return entries.find { it.label == label } ?: NONE
        }
    }
}

enum class ScheduleEditType {
    ONLY_THIS_EVENT,
    THIS_AND_FUTURE,
    ALL_EVENTS
}
