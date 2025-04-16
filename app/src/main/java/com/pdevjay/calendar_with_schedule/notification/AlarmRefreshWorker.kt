package com.pdevjay.calendar_with_schedule.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class AlarmRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduleRepository: ScheduleRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
//        val scheduleMap = scheduleRepository.scheduleMap.value
        val scheduleMap = scheduleRepository.scheduleMapFlowForWorker.first() // ✅ Flow의 첫 값을 안전하게 가져옴

        AlarmScheduler.scheduleAlarmsFromScheduleMap(applicationContext, scheduleMap)
        Log.e("AlarmLogger", "alarm refresh done : ${scheduleMap.size}")
        return Result.success()
    }
}
