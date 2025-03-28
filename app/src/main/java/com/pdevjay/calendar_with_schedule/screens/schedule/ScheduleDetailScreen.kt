package com.pdevjay.calendar_with_schedule.screens.schedule

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.getDiffsComparedTo
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.ScheduleEditType
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.ScheduleViewModel
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import com.pdevjay.calendar_with_schedule.utils.SlideInHorizontallyContainer
import java.time.LocalDate
import java.time.LocalTime

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ScheduleDetailScreen(
    schedule: RecurringData,
    navController: NavController,
    scheduleViewModel: ScheduleViewModel
) {
    var isVisible by remember { mutableStateOf(false) }

    var showDatePickerForStart by remember { mutableStateOf(false) }
    var showTimePickerForStart by remember { mutableStateOf(false) }
    var showDatePickerForEnd by remember { mutableStateOf(false) }
    var showTimePickerForEnd by remember { mutableStateOf(false) }
    var showDatePickerForRepeatUntil by remember { mutableStateOf(false) }
    var showDeleteBottomSheet by remember{ mutableStateOf(false) }
    var showUpdateBottomSheet by remember{ mutableStateOf(false) }

    var showWarning by remember { mutableStateOf(false) }

    BackHandler {
        isVisible = false
        navController.popBackStack()
    }

    LaunchedEffect(schedule) {
        if (schedule == null) {
            isVisible = false
            navController.popBackStack()
        }
    }

    var title by remember { mutableStateOf(schedule?.title ?: "New Event") }
    var location by remember { mutableStateOf(schedule?.location ?: "") }
    var start by remember { mutableStateOf(schedule?.start ?: DateTimePeriod(LocalDate.now(), LocalTime.of(9, 0))) }
    var end by remember { mutableStateOf(schedule?.end ?: DateTimePeriod(LocalDate.now(), LocalTime.of(10, 0))) }
    var allDay by remember { mutableStateOf(false) }
    var repeatType by remember { mutableStateOf(schedule?.repeatType ?: RepeatType.NONE) }
    var isRepeatUntilEnabled by remember { mutableStateOf(if (schedule.repeatUntil == null) false else true) }
    var repeatUntil by remember { mutableStateOf(schedule.repeatUntil) }
    var alarmOption by remember { mutableStateOf(schedule?.alarmOption ?: AlarmOption.NONE) }


    LaunchedEffect(Unit) { isVisible = true }


    fun updateSchedule(schedule: RecurringData, editType: ScheduleEditType) {
        try {
            val updatedRecurringData = schedule.copy(
                title = title,
                location = location,
                start = start,
                end = end,
                repeatType = repeatType,
                repeatUntil = if (isRepeatUntilEnabled) repeatUntil else null,
                alarmOption = alarmOption
            )

            if (
                editType == ScheduleEditType.ONLY_THIS_EVENT &&
                (schedule.repeatUntil != updatedRecurringData.repeatUntil ||
                        schedule.repeatType != updatedRecurringData.repeatType ||
                        schedule.repeatRule != updatedRecurringData.repeatRule)
            ) {
                showWarning = true
            } else {

                val diffs = updatedRecurringData.getDiffsComparedTo(schedule)
                val isOnlyContentChanged = diffs.all {
                    it.field in listOf(
                        "title",
                        "location",
                        "alarmOption",
                        "isAllDay"
                    )
                }
                scheduleViewModel.processIntent(
                    ScheduleIntent.UpdateSchedule(
                        updatedRecurringData,
                        editType,
                        isOnlyContentChanged
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (!showWarning) {
                isVisible = false
                navController.popBackStack()
            }
        }
    }

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

                GroupContainer(
                ) {
                    SwitchSelector(label = stringResource(R.string.all_day), option = allDay, onSwitch = { allDay = it })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 2.dp, color = Color.LightGray)
                    DateTimeSelector(stringResource(R.string.starts), start.date, start.time, onDateClick = {showDatePickerForStart = true}, onTimeClick = {showTimePickerForStart = true})
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 2.dp, color = Color.LightGray)
                    DateTimeSelector(stringResource(R.string.ends), end.date, end.time, onDateClick = {showDatePickerForEnd = true}, onTimeClick = {showTimePickerForEnd = true})
                }

                Spacer(modifier = Modifier.height(16.dp))

                GroupContainer {
                    // 반복 옵션
                    DropdownMenuSelector(
                        title = stringResource(R.string.repeat),
                        options = RepeatType.entries.map { it.label },
                        selectedOption = repeatType.label,
                        onOptionSelected = { label -> repeatType = RepeatType.fromLabel(label) }
                    )

                    // 반복 옵션을 선택하면 나타나는 반복 마지막 날 선택 옵션
                    if (repeatType != RepeatType.NONE) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 2.dp, color = Color.LightGray)
                        SwitchSelector(label = stringResource(R.string.set_repeat_until), option = isRepeatUntilEnabled,
                            onSwitch = {
                                // FIXME:
                                repeatUntil = if (it) schedule.repeatUntil ?: LocalDate.now() else null
                                isRepeatUntilEnabled = it
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 2.dp, color = Color.LightGray)
                        if (isRepeatUntilEnabled){
                            DateTimeSelector(stringResource(R.string.repeat_until), date = repeatUntil ?: LocalDate.now(), onDateClick = {showDatePickerForRepeatUntil = true})
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Alarm Dropdown
                GroupContainer {
                    DropdownMenuSelector(
                        title = stringResource(R.string.notification),
                        options = AlarmOption.entries.map { it.label },
                        selectedOption = alarmOption.label,
                        onOptionSelected = { label -> alarmOption = AlarmOption.fromLabel(label) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showUpdateBottomSheet = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.update_schedule))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        showDeleteBottomSheet = true
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
                    end = end.copy(date = it)
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

        if (showDatePickerForRepeatUntil) {
            DatePickerView(
                initialDate = repeatUntil ?: LocalDate.now(),
                minDate = start.date, // 시작 날짜 이후만 선택 가능
                onDateSelected = { repeatUntil = it },
                onDismiss = { showDatePickerForRepeatUntil = false }
            )
        }

        if (showWarning) {
            RepeatSettingsIgnoredDialog(
                onDismissRequest = { showWarning = false },
                onConfirm = {
                    showWarning = false
                    // 계속 진행

                }
            )
        }

        ConfirmBottomSheet(
            title = stringResource(R.string.update_schedule),
            description = stringResource(R.string.update_description),
            single = stringResource(R.string.update_single),
            future = stringResource(R.string.update_future),
            isSingleAvailable = (schedule.repeatUntil == repeatUntil && schedule.repeatType == repeatType),
            isVisible = showUpdateBottomSheet,
            onDismiss = { showUpdateBottomSheet = false },
            onSingle = {
                updateSchedule(schedule, ScheduleEditType.ONLY_THIS_EVENT)
            },
            onFuture = {
                updateSchedule(schedule, ScheduleEditType.THIS_AND_FUTURE)
            },
        )

        ConfirmBottomSheet(
            title = stringResource(R.string.delete_schedule),
            description = stringResource(R.string.delete_description),
            single = stringResource(R.string.delete_single),
            future = stringResource(R.string.delete_future),
            isVisible = showDeleteBottomSheet,
            onDismiss = { showDeleteBottomSheet = false },
            onSingle = {

                try {
                    scheduleViewModel.processIntent(ScheduleIntent.DeleteSchedule(schedule, ScheduleEditType.ONLY_THIS_EVENT))
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isVisible = false
                    navController.popBackStack()
                }
            },
            onFuture = {
                try {
                    scheduleViewModel.processIntent(ScheduleIntent.DeleteSchedule(schedule, ScheduleEditType.THIS_AND_FUTURE))
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isVisible = false
                    navController.popBackStack()
                }
            },
        )

    }
}

@Composable
fun RepeatSettingsIgnoredDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "반복 설정 변경은 적용되지 않습니다",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "이 일정만 변경하는 경우, 반복 종료일이나 반복 주기 설정은 무시됩니다.\n\n반복 설정을 변경하려면 '이후 일정부터 변경' 또는 '전체 일정 변경'을 선택해 주세요.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("취소")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text("계속")
                    }
                }
            }
        }
    }
}
