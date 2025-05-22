package com.pdevjay.calendar_with_schedule.notification

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pdevjay.calendar_with_schedule.MainActivity
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.features.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.core.utils.SharedPreferencesUtil
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val isNotificationEnabled = SharedPreferencesUtil.getBoolean(context, SharedPreferencesUtil.KEY_NOTIFICATION_ENABLED)

        if (isNotificationEnabled){
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
                AlarmOption.AT_TIME -> context.getString(R.string.alarm_message_at_time)
                AlarmOption.MIN_5 -> context.getString(R.string.alarm_message_5_min)
                AlarmOption.MIN_10 -> context.getString(R.string.alarm_message_10_min)
                AlarmOption.MIN_15 -> context.getString(R.string.alarm_message_15_min)
                AlarmOption.MIN_30 -> context.getString(R.string.alarm_message_30_min)
                AlarmOption.HOUR_1 -> context.getString(R.string.alarm_message_1_hour)
                AlarmOption.HOUR_2 -> context.getString(R.string.alarm_message_2_hour)
                AlarmOption.DAY_1 -> context.getString(R.string.alarm_message_1_day)
                AlarmOption.DAY_2 -> context.getString(R.string.alarm_message_2_day)
                AlarmOption.WEEK_1 -> context.getString(R.string.alarm_message_1_week)
                AlarmOption.NONE -> context.getString(R.string.alarm_message_none)
            }

            val notification = NotificationCompat.Builder(context, "default_channel")
                .setSmallIcon(R.drawable.ic_notification_schedy_2)
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
        } else {
            Log.e("AlarmReceiver", "Notification is not enabled")
        }

    }
}
