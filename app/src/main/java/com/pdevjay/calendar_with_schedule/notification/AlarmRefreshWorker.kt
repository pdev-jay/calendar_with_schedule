package com.pdevjay.calendar_with_schedule.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AlarmRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduleRepository: ScheduleRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val scheduleMap = scheduleRepository.scheduleMap.value
        AlarmScheduler.scheduleAlarmsFromScheduleMap(applicationContext, scheduleMap)
        return Result.success()
    }
}
