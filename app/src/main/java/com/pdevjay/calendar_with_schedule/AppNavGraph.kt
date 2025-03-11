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
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.ScheduleViewModel
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
            route = "scheduleDetail/{scheduleId}?selectedDate={selectedDate}",
            arguments = listOf(
                navArgument("scheduleId") { type = NavType.StringType },
                navArgument("selectedDate") { type = NavType.StringType; nullable = true }
            ),
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")!!
//            val selectedDate = backStackEntry.arguments?.getString("selectedDate")?.let { LocalDate.parse(it) }

            ScheduleDetailScreen(
                scheduleId = scheduleId,
//                selectedDate = selectedDate, // 🔹 선택된 날짜 추가
                navController = navController,
                scheduleViewModel = scheduleViewModel
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
                    scheduleViewModel.processIntent(ScheduleIntent.AddSchedule(scheduleData))
                    navController.popBackStack()
                }
            )
        }
    }
}
