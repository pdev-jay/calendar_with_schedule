package com.pdevjay.calendar_with_schedule.features.settings.enums

import android.content.Context
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.features.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType.BIWEEKLY
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType.DAILY
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType.MONTHLY
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType.NONE
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType.WEEKLY
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType.YEARLY

enum class Countries {
    NONE,
    KOREA;

    fun getLabel(context: Context): String {
        return when (this) {
            NONE -> context.getString(R.string.none)
            KOREA -> context.getString(R.string.korea)
        }
    }

    companion object {
        fun fromLabel(context: Context, label: String): Countries {
            return Countries.entries.find { it.getLabel(context) == label } ?: Countries.NONE
        }
    }
}