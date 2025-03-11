package com.pdevjay.calendar_with_schedule

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pdevjay.calendar_with_schedule.screens.calendar.CalendarScreen
import com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.screens.schedule.ScheduleAddScreen
import com.pdevjay.calendar_with_schedule.screens.schedule.ScheduleDetailScreen
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.ScheduleViewModel
import com.pdevjay.calendar_with_schedule.utils.LocalDateAdapter
import com.pdevjay.calendar_with_schedule.utils.LocalTimeAdapter
import java.net.URLDecoder
import java.time.LocalDate
import java.time.LocalTime

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

//            route = "scheduleDetail/{scheduleId}",
//            arguments = listOf(
//                navArgument("scheduleId") { type = NavType.StringType },
//            ),
        ) { backStackEntry ->
//            val scheduleId = backStackEntry.arguments?.getString("scheduleId")!!
            val gson: Gson = GsonBuilder()
                .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
                .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
                .create()

            val scheduleJson = backStackEntry.arguments?.getString("scheduleJson")!!
            val schedule = gson.fromJson(URLDecoder.decode(scheduleJson, "UTF-8"), ScheduleData::class.java)
            ScheduleDetailScreen(
                schedule = schedule,
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
