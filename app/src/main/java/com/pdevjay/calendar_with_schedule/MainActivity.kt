package com.pdevjay.calendar_with_schedule

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.pdevjay.calendar_with_schedule.notification.AlarmScheduler
import com.pdevjay.calendar_with_schedule.screens.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.ScheduleViewModel
import com.pdevjay.calendar_with_schedule.ui.theme.AppTheme
import com.pdevjay.calendar_with_schedule.utils.PermissionUtils
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
    PermissionUtils.EnsureNotificationPermission()

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