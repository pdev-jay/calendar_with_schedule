package com.pdevjay.calendar_with_schedule.utils

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pdevjay.calendar_with_schedule.notification.AlarmRefreshWorker
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object WorkUtils {
    fun scheduleDailyAlarmRefreshWork(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<AlarmRefreshWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_alarm_refresh",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
        Log.e("AlarmLogger", "daily alarm refresh scheduled")
    }

    fun enqueueAlarmRefreshNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<AlarmRefreshWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
        Log.e("AlarmLogger", "alarm refresh now")
    }

    private fun calculateInitialDelay(): Long {
        val now = ZonedDateTime.now()
        val nextMidnight = now.plusDays(1).toLocalDate().atStartOfDay(now.zone)
        return Duration.between(now, nextMidnight).toMillis()
    }
}
