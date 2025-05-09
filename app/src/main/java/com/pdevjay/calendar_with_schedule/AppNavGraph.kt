package com.pdevjay.calendar_with_schedule

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.ScheduleViewModel
import com.pdevjay.calendar_with_schedule.screens.settings.SettingsScreen
import com.pdevjay.calendar_with_schedule.utils.JsonUtils
import java.time.LocalDate

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    scheduleViewModel: ScheduleViewModel = hiltViewModel()
) {

    NavHost(navController = navController, startDestination = "calendar") {

        composable("calendar") {
            CalendarScreen(navController = navController, calendarViewModel = calendarViewModel, scheduleViewModel = scheduleViewModel)
        }

        composable(
            route = "scheduleDetail/{scheduleJson}",
            arguments = listOf(navArgument("scheduleJson") { type = NavType.StringType }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(200)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(200)
                )
            }
        ) { backStackEntry ->
            val scheduleJson = backStackEntry.arguments?.getString("scheduleJson")!!
            val schedule = JsonUtils.parseRecurringScheduleJson(scheduleJson)

            ScheduleDetailScreen(
                schedule = schedule,
                navController = navController,
                scheduleViewModel = scheduleViewModel
            )
        }

        composable(
            route = "add_schedule/{selectedDate}",
            arguments = listOf(navArgument("selectedDate") { type = NavType.StringType }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(200)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(200)
                )
            }
        ) { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString("selectedDate")
            val selectedDate = dateString?.let { LocalDate.parse(it) }

            ScheduleAddScreen(
                selectedDate = selectedDate,
                navController = navController,
                scheduleViewModel = scheduleViewModel,
            )
        }

        composable(
            "settings",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(200)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(200)
                )
            }
        ) {
            SettingsScreen(navController = navController)
        }

    }
}
