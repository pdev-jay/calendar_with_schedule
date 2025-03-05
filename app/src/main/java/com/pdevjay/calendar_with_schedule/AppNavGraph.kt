package com.pdevjay.calendar_with_schedule

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pdevjay.calendar_with_schedule.intents.TaskIntent
import com.pdevjay.calendar_with_schedule.screens.calendar.MainCalendarView
import com.pdevjay.calendar_with_schedule.screens.schedule.AddScheduleScreen
import com.pdevjay.calendar_with_schedule.screens.schedule.ScheduleDetailScreen
import com.pdevjay.calendar_with_schedule.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.viewmodels.TaskViewModel

@Composable
fun AppNavGraph(
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "calendar") {
        composable("calendar") {
            MainCalendarView(navController, calendarViewModel, taskViewModel)
        }
        composable(
            route = "scheduleDetail/{scheduleId}",
            arguments = listOf(navArgument("scheduleId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")!!
            ScheduleDetailScreen(
                scheduleId = scheduleId,
                navController = navController,
                taskViewModel = taskViewModel
            )
        }
    }
}
