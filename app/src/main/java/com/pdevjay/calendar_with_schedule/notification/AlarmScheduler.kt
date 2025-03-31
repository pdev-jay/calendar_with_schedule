package com.pdevjay.calendar_with_schedule.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toDateTime
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

object AlarmScheduler {

    fun scheduleAlarm(context: Context, schedule: BaseSchedule, baseTimeMillis: Long? = null) {

        var triggerTimeMillis = baseTimeMillis ?: calculateTriggerTimeMillis(schedule)

        if (triggerTimeMillis <= 0L) {
            Log.e("Alarm", "❌ 유효하지 않은 triggerTimeMillis: $triggerTimeMillis → 알람 예약하지 않음")
            return
        }

        // 🔥 과거면 다음 반복으로 보정
        val now = System.currentTimeMillis()
        if (triggerTimeMillis <= now && schedule.repeatType != RepeatType.NONE) {
            // 반복이 있는 경우 → 미래 반복 알림을 계산
            do {
                triggerTimeMillis = calculateNextTriggerTime(triggerTimeMillis, schedule.repeatType)
            } while (triggerTimeMillis <= now)
        } else if (triggerTimeMillis <= now && schedule.repeatType == RepeatType.NONE) {
            return
        }


        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("scheduleId", schedule.id.hashCode())
            putExtra("title", schedule.title)
            putExtra("alarmOption", schedule.alarmOption.name)
            putExtra("repeatType", schedule.repeatType.name)
            putExtra("repeatUntil", schedule.repeatUntil?.toString() ?: "")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmScheduler", "Exact alarm permission not granted.")
                return
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
            Log.e("AlarmScheduler", "Alarm scheduled for $triggerTimeMillis")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "SecurityException when setting alarm", e)
        }
    }

    fun scheduleNextAlarm(
        context: Context,
        id: Int,
        title: String,
        alarmOption: AlarmOption,
        repeatType: RepeatType,
        repeatUntil: String,
        baseTimeMillis: Long
    ) {
        if (repeatType == RepeatType.NONE) return

        // 🔥 repeatUntil 검사
        val nextDate = Instant.ofEpochMilli(baseTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val nextRepeatDate = when (repeatType) {
            RepeatType.DAILY -> nextDate.plusDays(1)
            RepeatType.WEEKLY -> nextDate.plusWeeks(1)
            RepeatType.BIWEEKLY -> nextDate.plusWeeks(2)
            RepeatType.MONTHLY -> nextDate.plusMonths(1)
            RepeatType.YEARLY -> nextDate.plusYears(1)
            else -> nextDate
        }

        if (nextRepeatDate.isAfter(LocalDate.parse(repeatUntil))) {
            Log.i("AlarmScheduler", "다음 반복 날짜가 repeatUntil 이후이므로 알림 예약 안 함")
            return
        }


        val nextTime = calculateNextTriggerTime(baseTimeMillis, repeatType)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("scheduleId", id)
            putExtra("title", title)
            putExtra("alarmOption", alarmOption.name)
            putExtra("repeatType", repeatType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmScheduler", "Exact alarm permission not granted.")
                return
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "SecurityException when setting next alarm", e)
        }
    }

    fun cancelAlarm(context: Context, schedule: BaseSchedule) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel",
                "일정 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "일정 시작 전에 알림을 보냅니다."
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun calculateTriggerTimeMillis(schedule: BaseSchedule): Long {
        val baseTime = schedule.start?.toDateTime() ?: return Long.MIN_VALUE
        val offsetMinutes = when (schedule.alarmOption) {
            AlarmOption.AT_TIME -> 0
            AlarmOption.MIN_5 -> 5
            AlarmOption.MIN_10 -> 10
            AlarmOption.MIN_15 -> 15
            AlarmOption.MIN_30 -> 30
            AlarmOption.HOUR_1 -> 60
            AlarmOption.HOUR_2 -> 120
            AlarmOption.DAY_1 -> 1440
            AlarmOption.DAY_2 -> 2880
            AlarmOption.WEEK_1 -> 10080
            AlarmOption.NONE -> return Long.MIN_VALUE
        }

        return ZonedDateTime.of(baseTime, ZoneId.systemDefault())
            .minusMinutes(offsetMinutes.toLong())
            .toInstant()
            .toEpochMilli()
    }

    private fun calculateNextTriggerTime(currentMillis: Long, repeatType: RepeatType): Long {
        val current = Instant.ofEpochMilli(currentMillis).atZone(ZoneId.systemDefault())
        val next = when (repeatType) {
            RepeatType.DAILY -> current.plusDays(1)
            RepeatType.WEEKLY -> current.plusWeeks(1)
            RepeatType.BIWEEKLY -> current.plusWeeks(2)
            RepeatType.MONTHLY -> current.plusMonths(1)
            RepeatType.YEARLY -> current.plusYears(1)
            else -> return currentMillis
        }
        return next.toInstant().toEpochMilli()
    }
}
