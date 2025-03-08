package com.pdevjay.calendar_with_schedule.screens.schedule

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.TaskIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.TaskViewModel

@Composable
fun ScheduleDetailScreen(
    scheduleId: String,
    navController: NavController,
    taskViewModel: TaskViewModel
) {
    val taskState by taskViewModel.state.collectAsState()

    // 일정 찾기
    val schedule = remember(taskState.schedules) {
        taskState.schedules.firstOrNull { it.id == scheduleId }
    }

    var isVisible by remember { mutableStateOf(false) }

    // 일정이 없으면 즉시 뒤로 가기
    if (schedule == null) {
        LaunchedEffect(Unit) {
            isVisible = false
            navController.popBackStack()
        }
        return
    }

    BackHandler {
        isVisible = false
        navController.popBackStack()
    }

    LaunchedEffect(Unit) {
        isVisible = true  // 진입 시 애니메이션 시작
    }

    var title by remember { mutableStateOf(schedule.title) }
    var location by remember { mutableStateOf(schedule.location ?: "") }
    var start by remember { mutableStateOf(schedule.start) }
    var end by remember { mutableStateOf(schedule.end) }

    var showDatePickerForStart by remember { mutableStateOf(false) }
    var showTimePickerForStart by remember { mutableStateOf(false) }
    var showDatePickerForEnd by remember { mutableStateOf(false) }
    var showTimePickerForEnd by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
        exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
    ) {
        // DatePicker / TimePicker
        if (showDatePickerForStart) {
            DatePickerView(
                initialDate = start.date,
                onDateSelected = {
                    start = start.copy(date = it)
                    if (end.date.isBefore(it)) end = end.copy(date = it)
                },
                onDismiss = { showDatePickerForStart = false }
            )
        }

        if (showTimePickerForStart) {
            TimePickerDialogView(
                initialTime = start.time,
                onTimeSelected = {
                    start = start.copy(time = it)
                    if (start.date == end.date && it >= end.time) {
                        end = end.copy(time = it.plusHours(1))
                    }
                },
                onDismiss = { showTimePickerForStart = false }
            )
        }

        if (showDatePickerForEnd) {
            DatePickerView(
                initialDate = end.date,
                minDate = start.date,
                onDateSelected = { end = end.copy(date = it) },
                onDismiss = { showDatePickerForEnd = false }
            )
        }

        if (showTimePickerForEnd) {
            TimePickerDialogView(
                initialTime = end.time,
                onTimeSelected = {
                    if (end.date == start.date && it <= start.time) {
                        end = end.copy(time = start.time.plusHours(1))
                    } else {
                        end = end.copy(time = it)
                    }
                },
                onDismiss = { showTimePickerForEnd = false }
            )
        }

        Column(Modifier.padding(16.dp).statusBarsPadding()) {
            Text("Schedule Detail", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") })
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") })
            Spacer(modifier = Modifier.height(8.dp))

            // Start
            DateTimeField(
                label = "Start",
                dateTime = start,
                onDateClick = { showDatePickerForStart = true },
                onTimeClick = { showTimePickerForStart = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            DateTimeField(
                label = "End",
                dateTime = end,
                onDateClick = { showDatePickerForEnd = true },
                onTimeClick = { showTimePickerForEnd = true }
            )

            Button(onClick = {
                taskViewModel.processIntent(
                    TaskIntent.UpdateSchedule(
                        schedule.copy(
                            title = title,
                            location = location,
                            start = start,
                            end = end
                        )
                    )
                )
                isVisible = false
                navController.popBackStack()
            }) {
                Text("Update")
            }
            Button(onClick = {
                taskViewModel.processIntent(TaskIntent.DeleteSchedule(schedule.id))
            }) {
                Text("Delete")
            }
            Button(
                onClick = {
                    isVisible = false
                    navController.popBackStack()
                }
            ) {
                Text("Cancel")
            }
        }
    }
}

