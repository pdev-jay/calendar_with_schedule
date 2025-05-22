package com.pdevjay.calendar_with_schedule.works

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pdevjay.calendar_with_schedule.data.repository.RemoteDataRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HolidaySyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val remoteDataRepository: RemoteDataRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.e("HolidaySyncWorker", "✅ doWork 진입됨")

        return try {
            remoteDataRepository.refreshHolidays()
            Log.e("HolidaySyncWorker", "refreshHolidays from HolidaySyncWorker called")
            Result.success()
        } catch (e: Exception) {
            Log.e("HolidaySyncWorker", "에러 발생", e)
            Result.retry()
        }
    }
}
