package com.pdevjay.calendar_with_schedule.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.e("BootReceiver", "📦 기기 재부팅 감지됨 → 알람 재등록 시작")

            val scheduleMap = AlarmPersistenceUtils.loadPersistedScheduleMap(context)
            if (scheduleMap.isNotEmpty()) {
                AlarmScheduler.scheduleAlarmsFromScheduleMap(context, scheduleMap)
                Log.e("BootReceiver", "✅ 알람 재등록 요청 완료 (${scheduleMap.size}일)")
            } else {
                Log.w("BootReceiver", "⚠️ 저장된 scheduleMap 없음 → 알람 재등록 생략")
            }
        }
    }
}
