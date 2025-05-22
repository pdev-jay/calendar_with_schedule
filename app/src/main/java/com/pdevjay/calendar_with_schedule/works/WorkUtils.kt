package com.pdevjay.calendar_with_schedule.works

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pdevjay.calendar_with_schedule.core.utils.SharedPreferencesUtil
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

    fun scheduleHolidaySyncWork(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val initialOneTimeWork = OneTimeWorkRequestBuilder<HolidaySyncWorker>()
            .addTag("HolidaySyncInitial")
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<HolidaySyncWorker>(6, TimeUnit.HOURS)
            .addTag("HolidaySyncPeriodic")
            .build()

        // 앱 설치 후 최초 실행인지 확인
        val alreadyScheduled = SharedPreferencesUtil.getBoolean(context, SharedPreferencesUtil.KEY_FIRST_HOLIDAY_SYNC, false)

        if (!alreadyScheduled) {
            // 1회 초기 실행
            workManager.enqueueUniqueWork(
                "HolidayInitialSyncWork",
                ExistingWorkPolicy.KEEP,
                initialOneTimeWork
            )

            // 주기적 워크 등록
            workManager.enqueueUniquePeriodicWork(
                "HolidaySyncWork",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )
            Log.e("HolidaySyncWorker", "HolidaySyncWork first scheduled")
            SharedPreferencesUtil.putBoolean(context, SharedPreferencesUtil.KEY_FIRST_HOLIDAY_SYNC, true)
        } else {
            // 앱 재시작 후에도 확실히 등록되도록 보장
            workManager.enqueueUniquePeriodicWork(
                "HolidaySyncWork",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )
            Log.e("HolidaySyncWorker", "HolidaySyncWork already scheduled")
        }

    }
}
