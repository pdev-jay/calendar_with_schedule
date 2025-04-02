package com.pdevjay.calendar_with_schedule.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.pdevjay.calendar_with_schedule.BuildConfig
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toDateTime
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object AlarmScheduler {

    fun scheduleAlarmsFromScheduleMap(context: Context, scheduleMap: Map<LocalDate, List<BaseSchedule>>, daysAhead: Long = 30) {
        val today = LocalDate.now()
        val until = today.plusDays(daysAhead)

        scheduleMap
            .filterKeys { it in today..until }
            .forEach { (_, schedules) ->
                schedules.filterIsInstance<RecurringData>()
                    .filter { it.alarmOption != AlarmOption.NONE && !it.isDeleted }
                    .forEach { recurring ->
                        val triggerTime = calculateTriggerTimeMillis(recurring)
                        if (triggerTime > System.currentTimeMillis()) {
                            scheduleAlarm(context, recurring, triggerTime)
                        }
                    }
            }
    }

    fun scheduleMultipleAlarms(context: Context, schedule: BaseSchedule, daysAhead: Long = 30) {
        if (schedule.repeatType == RepeatType.NONE) {
            scheduleAlarm(context, schedule)
            return
        }
        val today = LocalDate.now()
        val until = today.plusDays(daysAhead)

        val repeatedDates = RepeatScheduleGenerator.generateRepeatedDatesForAlarm(
            repeatType = schedule.repeatType,
            startDate = schedule.start.date,
            repeatUntil = schedule.repeatUntil ?: until // 🔥 repeatUntil 없어도 안전하게 범위 제한
        ).takeWhile { it <= until }

        repeatedDates.forEachIndexed { index, date ->
            val temp = when (schedule) {
                is RecurringData -> schedule.copy(start = schedule.start.copy(date = date), end = schedule.end.copy(date = date), repeatIndex = index + 1)
                is ScheduleData -> schedule.toRecurringData(selectedDate = date, repeatIndex = index + 1)
                else -> throw IllegalArgumentException("Unknown schedule type")
            }
            val triggerTime = calculateTriggerTimeMillis(temp)
            if (triggerTime > System.currentTimeMillis()) {
                scheduleAlarm(context, temp, triggerTime)
            }
        }
    }

    fun scheduleAlarm(context: Context, schedule: BaseSchedule, baseTimeMillis: Long? = null) {

        var triggerTimeMillis = baseTimeMillis ?: calculateTriggerTimeMillis(schedule)

        if (triggerTimeMillis <= System.currentTimeMillis()) {
            Log.e("AlarmLogger", "과거 알람 시도: $triggerTimeMillis → 무시됨")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("scheduleId", schedule.id.hashCode())
            putExtra("title", schedule.title)
            putExtra("alarmOption", schedule.alarmOption.name)
            putExtra("date", schedule.start.date.toString()) // 🔥 추가
        }
        val requestCode = getAlarmRequestCode(schedule)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmLogger", "Exact alarm permission not granted.")
                return
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
//            Log.e("AlarmLogger", "Alarm scheduled for ${schedule.title} / ${schedule.start.date} / ${schedule.alarmOption.name}")
        } catch (e: SecurityException) {
            Log.e("AlarmLogger", "SecurityException when setting alarm", e)
        }
    }

    fun cancelAlarmsFromScheduleMap(
        context: Context,
        scheduleMap: Map<LocalDate, List<BaseSchedule>>,
        daysAhead: Long = 30
    ) {
        val today = LocalDate.now()
        val until = today.plusDays(daysAhead)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        scheduleMap
            .filterKeys { it in today..until }
            .forEach { (_, schedules) ->
                schedules.filterIsInstance<RecurringData>()
                    .filter { it.alarmOption != AlarmOption.NONE && !it.isDeleted }
                    .forEach { schedule ->
                        val requestCode = getAlarmRequestCode(schedule)

                        val intent = Intent(context, AlarmReceiver::class.java).apply {
                            putExtra("scheduleId", schedule.id.hashCode())
                            putExtra("title", schedule.title)
                            putExtra("alarmOption", schedule.alarmOption.name)
                        }
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            requestCode,  // 또는 schedule.id.hashCode() + repeatIndex
                            intent,
                            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                        )
                        pendingIntent?.let {
                            alarmManager.cancel(it)
                            Log.i("AlarmScheduler", "❌ 알람 취소됨: ${schedule.title} (${schedule.start.date})")
                        }
                    }
            }
    }


    fun cancelAlarm(context: Context, schedule: RecurringData) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("scheduleId", schedule.id.hashCode())
            putExtra("title", schedule.title)
            putExtra("alarmOption", schedule.alarmOption.name)
        }
        val requestCode = getAlarmRequestCode(schedule)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
            Log.e("AlarmLogger", "❌ 알람 취소됨: ${schedule.title} ${schedule.start.date} $requestCode")
        }

        // 확인
        val checkIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (checkIntent == null) {
            Log.e("AlarmLogger", "✅ 알람 성공적으로 취소됨.")
        } else {
            Log.e("AlarmLogger", "⚠️ 알람 취소 실패 또는 여전히 존재함.")
        }

    }

    fun cancelThisAndFutureAlarms(
        context: Context,
        schedule: RecurringData,
        scheduleMap: Map<LocalDate, List<BaseSchedule>>
    ) {
        if (schedule.branchId == null || schedule.repeatIndex == null) return

        val candidates = scheduleMap
            .flatMap { it.value }
            .filterIsInstance<RecurringData>()
            .filter {
                it.branchId == schedule.branchId &&
                        it.repeatIndex != null &&
                        it.repeatIndex!! >= schedule.repeatIndex!! &&
                        !it.isDeleted &&
                        it.alarmOption != AlarmOption.NONE
            }

        for (target in candidates) {
            cancelAlarm(context, target)
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
        val baseTime = schedule.start.toDateTime() ?: return Long.MIN_VALUE
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

    fun getAlarmRequestCode(schedule: BaseSchedule): Int {
        val baseId = when (schedule) {
            is RecurringData -> {
                val key = listOfNotNull(
                    schedule.originalEventId,       // 반복의 기준 ID
                    schedule.start.date.toString(), // 반복 인스턴스의 날짜
                    schedule.repeatIndex?.toString(), // 반복 인덱스 (있다면 넣기)
                    schedule.alarmOption.name       // 알람 옵션
                ).joinToString("_")
                key
            }

            is ScheduleData -> {
                val key = listOfNotNull(
                    schedule.id,                    // ScheduleData는 고정 ID
                    schedule.start.date.toString(),
                    schedule.alarmOption.name
                ).joinToString("_")
                key
            }

            else -> throw IllegalArgumentException("Unknown schedule type")
        }

        return baseId.hashCode()
    }

    fun logRegisteredAlarms(context: Context, scheduleMap: Map<LocalDate, List<RecurringData>>) {
        if (!BuildConfig.DEBUG) return  // ✅ release 빌드에서는 아예 실행 안 함

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val today = LocalDate.now()
        val until = today.plusDays(30)

        var total = 0
        var found = 0

        scheduleMap
            .filterKeys { it in today..until }  // ✅ 30일 범위 내로 제한
            .forEach { (_, schedules) ->
                for (schedule in schedules) {
                    total++
                    val intent = Intent(context, AlarmReceiver::class.java)
                    val requestCode = getAlarmRequestCode(schedule)

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (pendingIntent != null) {
                        found++
                        Log.e(
                            "AlarmLogger",
                            "✅ 등록됨: ${schedule.title} | ${schedule.start.date} ${schedule.start.time} | isDeleted : ${schedule.isDeleted} | ${schedule.alarmOption.name} | requestCode=$requestCode"
                        )
                    } else {
                        Log.e(
                            "AlarmLogger",
                            "❌ 미등록: ${schedule.title} | ${schedule.start.date} ${schedule.start.time} | isDeleted : ${schedule.isDeleted} | ${schedule.alarmOption.name} | requestCode=$requestCode"
                        )
                    }
                }
            }

        Log.e("AlarmLogger", "총 $total 개 중 $found 개 알람이 등록되어 있음.")
    }

}
