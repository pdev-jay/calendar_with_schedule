package com.pdevjay.calendar_with_schedule

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.pdevjay.calendar_with_schedule.app.AppNavGraph
import com.pdevjay.calendar_with_schedule.notification.AlarmScheduler
import com.pdevjay.calendar_with_schedule.features.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.features.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.features.schedule.viewmodels.ScheduleViewModel
import com.pdevjay.calendar_with_schedule.ui.theme.AppTheme
import com.pdevjay.calendar_with_schedule.core.utils.extensions.AppVersionUtils
import com.pdevjay.calendar_with_schedule.core.utils.extensions.PermissionUtils
import com.pdevjay.calendar_with_schedule.core.utils.SharedPreferencesUtil
import com.pdevjay.calendar_with_schedule.core.utils.other_viewmodels.SplashViewModel
import com.pdevjay.calendar_with_schedule.works.WorkUtils
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition{
            splashViewModel.isSplashRunning.value
        }


        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Create your custom animation.
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.TRANSLATION_Y,
                0f,
                -splashScreenView.view.height.toFloat()
            )
            slideUp.duration = 200L

            // Call SplashScreenView.remove at the end of your custom animation.
            slideUp.doOnEnd { splashScreenView.remove() }

            // Run your animation.
            slideUp.start()
        }

        AppVersionUtils.checkAppVersion(this)

        val navigateDateString = intent?.getStringExtra("date")

        AlarmScheduler.createNotificationChannel(this)
        WorkUtils.scheduleDailyAlarmRefreshWork(this)
        WorkUtils.scheduleHolidaySyncWork(this)

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
    val context = LocalContext.current

    val navController = rememberNavController()
    val calendarViewModel: CalendarViewModel = hiltViewModel()
    val scheduleViewModel: ScheduleViewModel = hiltViewModel()

    val isFirstLaunch = SharedPreferencesUtil.getBoolean(context, SharedPreferencesUtil.KEY_FIRST_LAUNCH, true)

    if (isFirstLaunch){
        PermissionUtils.EnsureNotificationPermission()
    }

    SharedPreferencesUtil.putBoolean(context, SharedPreferencesUtil.KEY_FIRST_LAUNCH, false)

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