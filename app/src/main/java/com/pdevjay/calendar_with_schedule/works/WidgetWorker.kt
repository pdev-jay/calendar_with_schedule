package com.pdevjay.calendar_with_schedule.works

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pdevjay.calendar_with_schedule.features.widget.SchedyWidget

class WidgetWorker(
    val context: Context,
    val params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Fetch data or do some work and then update all instance of your widget
        return try{
            SchedyWidget().updateAll(context)
            Result.success()
        } catch (e: Exception){
            Log.e("widget", "위젯 업데이트 실패: ${e.message}", e)
            Result.failure() // 혹은 Result.failure() 로 보고
        }
    }
}
