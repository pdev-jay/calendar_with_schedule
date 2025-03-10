package com.pdevjay.calendar_with_schedule.screens.schedule

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatOption
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.ScheduleViewModel
import com.pdevjay.calendar_with_schedule.utils.SlideInHorizontallyContainer

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ScheduleDetailScreen(
    scheduleId: String,
    navController: NavController,
    scheduleViewModel: ScheduleViewModel
) {
    val scheduleState by scheduleViewModel.state.collectAsState()
    val schedule = remember(scheduleState.schedules) {
        scheduleState.schedules.firstOrNull { it.id == scheduleId }
    }

    var isVisible by remember { mutableStateOf(false) }

    var showDatePickerForStart by remember { mutableStateOf(false) }
    var showTimePickerForStart by remember { mutableStateOf(false) }
    var showDatePickerForEnd by remember { mutableStateOf(false) }
    var showTimePickerForEnd by remember { mutableStateOf(false) }

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

    var title by remember { mutableStateOf(schedule.title) }
    var location by remember { mutableStateOf(schedule.location ?: "") }
    var start by remember { mutableStateOf(schedule.start) }
    var end by remember { mutableStateOf(schedule.end) }
    var allDay by remember { mutableStateOf(false) }
    var repeatOption by remember { mutableStateOf(schedule.repeatOption) }
    var alarmOption by remember { mutableStateOf(schedule.alarmOption) }

    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        topBar = {
            SlideInHorizontallyContainer(isVisible) {
                TopAppBar(
                    title = { Text("Schedule Detail") },
                    navigationIcon = {
                        IconButton(onClick = {
                            isVisible = false
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close"
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        SlideInHorizontallyContainer(isVisible) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                StyledTextField(value = title, label = stringResource(R.string.title), onValueChange = { title = it })
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp))
                ) {
                    AllDaySwitch(allDay)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 2.dp, color = Color.LightGray)
                    DateTimeSelector(stringResource(R.string.starts), start, onDateClick = {showDatePickerForStart = true}, onTimeClick = {showTimePickerForStart = true})
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 2.dp, color = Color.LightGray)
                    DateTimeSelector(stringResource(R.string.ends), end, onDateClick = {showDatePickerForEnd = true}, onTimeClick = {showTimePickerForEnd = true})
                }

                Spacer(modifier = Modifier.height(16.dp))

//                DropdownMenuSelector(
//                    title = stringResource(R.string.repeat),
//                    options = RepeatOption.entries.map { it.label },
//                    selectedOption = repeatOption.label,
//                    onOptionSelected = { label -> repeatOption = RepeatOption.fromLabel(label) }
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                DropdownMenuSelector(
//                    title = stringResource(R.string.notification),
//                    options = AlarmOption.entries.map { it.label },
//                    selectedOption = alarmOption.label,
//                    onOptionSelected = { label -> alarmOption = AlarmOption.fromLabel(label) }
//                )
//                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isVisible = false
                        scheduleViewModel.processIntent(
                            ScheduleIntent.UpdateSchedule(
                                schedule.copy(
                                    title = title,
                                    location = location,
                                    start = start,
                                    end = end,
                                    repeatOption = repeatOption,
                                    alarmOption = alarmOption
                                )
                            )
                        )
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.update_schedule))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scheduleViewModel.processIntent(ScheduleIntent.DeleteSchedule(schedule.id))
                        isVisible = false
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.delete))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

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
    }
}

