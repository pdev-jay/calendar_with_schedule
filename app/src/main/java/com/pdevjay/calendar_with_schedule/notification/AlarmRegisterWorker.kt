package com.pdevjay.calendar_with_schedule.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.utils.JsonUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.net.URLEncoder

@HiltWorker
class AlarmRegisterWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val json = inputData.getString("scheduleJson") ?: return Result.failure()
        val schedule = JsonUtils.parseScheduleJson(json)

        // 실제 알람 등록
        AlarmScheduler.scheduleMultipleAlarms(applicationContext, schedule)

        Log.e("AlarmLogger", "✅ WorkManager 통해 알람 등록 완료: ${schedule.title}")
        return Result.success()
    }

    companion object {
        fun enqueue(context: Context, schedule: RecurringData) {
            val json = JsonUtils.gson.toJson(schedule)
            val input = workDataOf("scheduleJson" to URLEncoder.encode(json, "UTF-8"))

            val work = OneTimeWorkRequestBuilder<AlarmRegisterWorker>()
                .setInputData(input)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueue(work)
        }
    }
}
