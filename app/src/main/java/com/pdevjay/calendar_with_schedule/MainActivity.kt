package com.pdevjay.calendar_with_schedule

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.TaskViewModel
import com.pdevjay.calendar_with_schedule.ui.theme.Calendar_with_scheduleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            Calendar_with_scheduleTheme {
                AppRoot()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val calendarViewModel: CalendarViewModel = hiltViewModel()
    val taskViewModel: TaskViewModel = hiltViewModel()

    Scaffold(modifier = Modifier.fillMaxSize()) {
        AppNavGraph(navController, calendarViewModel, taskViewModel)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Calendar_with_scheduleTheme {
        Greeting("Android")
    }
}