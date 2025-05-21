package com.pdevjay.calendar_with_schedule.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pdevjay.calendar_with_schedule.utils.JsonUtils
import com.pdevjay.calendar_with_schedule.utils.WorkUtils

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.e("BootReceiver", "기기 재부팅 감지됨 → 알람 재등록 시작")
            WorkUtils.scheduleDailyAlarmRefreshWork(context) // 주기적 알람 스케줄러 재등록
            WorkUtils.enqueueAlarmRefreshNow(context)         // 한 번 즉시 실행
        }
    }
}
