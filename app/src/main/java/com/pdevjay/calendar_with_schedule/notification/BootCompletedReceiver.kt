package com.pdevjay.calendar_with_schedule.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.pdevjay.calendar_with_schedule.features.widget.SchedyWidget
import com.pdevjay.calendar_with_schedule.works.WidgetWorker
import com.pdevjay.calendar_with_schedule.works.WorkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {

            Log.e("BootReceiver", "기기 재부팅 감지됨 → 위젯 새로고침")
            val work = OneTimeWorkRequestBuilder<WidgetWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(context).enqueue(work)

            Log.e("BootReceiver", "기기 재부팅 감지됨 → 알람 재등록 시작")
            WorkUtils.scheduleDailyAlarmRefreshWork(context) // 주기적 알람 스케줄러 재등록
            WorkUtils.enqueueAlarmRefreshNow(context)         // 한 번 즉시 실행

            Log.e("BootReceiver", "기기 재부팅 감지됨 → holiday sync worker 재시작")
            WorkUtils.scheduleHolidaySyncWork(context)
        }
    }
}
