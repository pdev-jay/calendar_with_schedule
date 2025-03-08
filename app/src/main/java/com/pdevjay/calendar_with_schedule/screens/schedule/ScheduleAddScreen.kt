package com.pdevjay.calendar_with_schedule.screens.schedule

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAddScreen(
    selectedDate: LocalDate?,
    onDismiss: () -> Unit,
    onSave: (ScheduleData) -> Unit
) {

    val now = remember { LocalTime.now() }
    val initialDate = remember { selectedDate ?: LocalDate.now() }

    var isVisible by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var start by remember { mutableStateOf(DateTimePeriod(initialDate, LocalTime.of(now.hour, 0))) }
    var end by remember { mutableStateOf(DateTimePeriod(initialDate, LocalTime.of(now.plusHours(1).hour, 0))) }

    var showDatePickerForStart by remember { mutableStateOf(false) }
    var showTimePickerForStart by remember { mutableStateOf(false) }
    var showDatePickerForEnd by remember { mutableStateOf(false) }
    var showTimePickerForEnd by remember { mutableStateOf(false) }

    BackHandler {
        isVisible = false
        onDismiss()
    }

    LaunchedEffect(Unit) {

        isVisible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Schedule") },
                navigationIcon = {
                    IconButton(onClick = {
                        isVisible = false
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isVisible = false
                        val newSchedule = ScheduleData(
                            title = if (title.isBlank()) "New Event" else title,
                            location = location,
                            start = start,
                            end = end
                        )
                        onSave(newSchedule)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
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
