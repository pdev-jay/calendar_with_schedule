package com.pdevjay.calendar_with_schedule.notification

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pdevjay.calendar_with_schedule.MainActivity
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra("title") ?: "알림"
        val alarmOption = intent?.getStringExtra("alarmOption")?.let {
            AlarmOption.valueOf(it)
        } ?: AlarmOption.NONE

        val date = intent?.getStringExtra("date") ?: LocalDate.now().toString() // 받은 날짜 그대로 전달

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("fromAlarm", true)
            putExtra("date", date)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = when (alarmOption) {
            AlarmOption.AT_TIME -> "지금 일정이 시작됩니다."
            AlarmOption.MIN_5 -> "5분 뒤에 일정이 시작됩니다."
            AlarmOption.MIN_10 -> "10분 뒤에 일정이 시작됩니다."
            AlarmOption.MIN_15 -> "15분 뒤에 일정이 시작됩니다."
            AlarmOption.MIN_30 -> "30분 뒤에 일정이 시작됩니다."
            AlarmOption.HOUR_1 -> "1시간 뒤에 일정이 시작됩니다."
            AlarmOption.HOUR_2 -> "2시간 뒤에 일정이 시작됩니다."
            AlarmOption.DAY_1 -> "내일 일정이 시작됩니다."
            AlarmOption.DAY_2 -> "모레 일정이 시작됩니다."
            AlarmOption.WEEK_1 -> "1주일 뒤에 일정이 시작됩니다."
            AlarmOption.NONE -> "일정 알림이 도착했어요."
        }

        val notification = NotificationCompat.Builder(context, "default_channel")
            .setSmallIcon(R.drawable.ic_notification_schedy)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) //  여기서 클릭 시 동작 연결
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(System.currentTimeMillis().toInt(), notification)
        }
        Log.e("AlarmReceiver", "Alarm scheduled for $title notified")

    }
}
