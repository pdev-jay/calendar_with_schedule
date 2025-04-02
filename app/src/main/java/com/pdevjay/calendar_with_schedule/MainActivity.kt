package com.pdevjay.calendar_with_schedule

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.pdevjay.calendar_with_schedule.notification.AlarmReceiver
import com.pdevjay.calendar_with_schedule.notification.AlarmScheduler
import com.pdevjay.calendar_with_schedule.notification.RequestNotificationPermission
import com.pdevjay.calendar_with_schedule.screens.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.ScheduleViewModel
import com.pdevjay.calendar_with_schedule.ui.theme.AppTheme
import com.pdevjay.calendar_with_schedule.utils.WorkUtils
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigateDateString = intent?.getStringExtra("date")

        AlarmScheduler.createNotificationChannel(this)
        WorkUtils.scheduleDailyAlarmRefreshWork(this)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppRoot(navigateDateString)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppRoot(
    navigateDateString: String? = null,
) {

    val navController = rememberNavController()
    val calendarViewModel: CalendarViewModel = hiltViewModel()
    val scheduleViewModel: ScheduleViewModel = hiltViewModel()
    RequestNotificationPermission()

    LaunchedEffect(navigateDateString) {
        navigateDateString?.let {
            val parsedDate = LocalDate.parse(it)
            calendarViewModel.processIntent(CalendarIntent.DateSelected(parsedDate))
        }
    }

    AppTheme{
        Scaffold(modifier = Modifier.fillMaxSize()) {
            AppNavGraph(navController, calendarViewModel, scheduleViewModel)
        }
    }
}

fun testDummyAlarmRegistrationAndCancellation(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val requestCode = 999999
    val triggerTimeMillis = System.currentTimeMillis() + 60_000

    // ✅ 정확히 같은 intent (setAction 방식)
    val intent = Intent(context, AlarmReceiver::class.java).setAction("com.pdevjay.DUMMY_ALARM")

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 등록
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerTimeMillis,
        pendingIntent
    )
    Log.e("AlarmLogger", "✅ 더미 알람 등록됨: requestCode=$requestCode")

    // 취소
    alarmManager.cancel(pendingIntent)
    Log.e("AlarmLogger", "❌ 더미 알람 취소 시도: requestCode=$requestCode")

    // 취소 여부 확인
    val test = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )

    if (test == null) {
        Log.e("AlarmLogger", "✅ 취소 확인 완료 (PendingIntent 없음)")
    } else {
        Log.e("AlarmLogger", "⚠️ 취소 실패 (PendingIntent 여전히 존재)")
    }
}
