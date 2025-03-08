package com.pdevjay.calendar_with_schedule

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pdevjay.calendar_with_schedule.screens.calendar.CalendarScreen
import com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.screens.schedule.ScheduleAddScreen
import com.pdevjay.calendar_with_schedule.screens.schedule.ScheduleDetailScreen
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.TaskIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.TaskViewModel
import java.time.LocalDate

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel()
) {

    NavHost(navController = navController, startDestination = "calendar") {
        composable("calendar") {
            CalendarScreen(navController = navController, calendarViewModel = calendarViewModel, taskViewModel =taskViewModel)
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
        composable(
            route = "add_schedule/{selectedDate}",
            arguments = listOf(navArgument("selectedDate") { type = NavType.StringType }),
        ) { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString("selectedDate")
            val selectedDate = dateString?.let { LocalDate.parse(it) }

            ScheduleAddScreen(
                selectedDate = selectedDate,
                onDismiss = { navController.popBackStack() },
                onSave = { scheduleData ->
                    // 저장 처리
                    taskViewModel.processIntent(TaskIntent.AddSchedule(scheduleData))
                    navController.popBackStack()
                }
            )
        }
    }
}
