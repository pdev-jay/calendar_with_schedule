package com.pdevjay.calendar_with_schedule.features.schedule

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.core.utils.extensions.NotificationPermissionDeniedDialog
import com.pdevjay.calendar_with_schedule.core.utils.extensions.PermissionUtils
import com.pdevjay.calendar_with_schedule.core.utils.helpers.RRuleHelper.generateRRule
import com.pdevjay.calendar_with_schedule.features.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.features.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.features.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType
import com.pdevjay.calendar_with_schedule.features.schedule.enums.ScheduleColor
import com.pdevjay.calendar_with_schedule.features.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.features.schedule.viewmodels.ScheduleViewModel
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAddScreen(
    selectedDate: LocalDate?,
    selectedTime: LocalTime? = null,
    navController: NavController,
    scheduleViewModel: ScheduleViewModel,
) {
    val now = remember { LocalTime.now() }
    val initialDate = remember { selectedDate ?: LocalDate.now() }
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var start by remember {
        mutableStateOf(
            DateTimePeriod(
                initialDate,
                LocalTime.of(selectedTime?.hour ?: now.hour, selectedTime?.minute ?: 0)
            )
        )
    }
    var end by remember {
        mutableStateOf(
            DateTimePeriod(
                initialDate,
                LocalTime.of(
                    selectedTime?.hour?.plus(1) ?: now.plusHours(1).hour,
                    selectedTime?.minute ?: 0
                )
            )
        )
    }
    var allDay by remember { mutableStateOf(false) }
    // repeat과 alarm 상태 추가
    var repeatType by remember { mutableStateOf(RepeatType.NONE) }
    var isRepeatUntilEnabled by remember { mutableStateOf(false) }
    var repeatUntil by remember { mutableStateOf(end.date.plusWeeks(1)) }
    var alarmOption by remember { mutableStateOf(AlarmOption.NONE) }
    var selectedColor by remember { mutableStateOf<Int?>(ScheduleColor.CYAN.colorInt) }

    var showDatePickerForStart by remember { mutableStateOf(false) }
    var showTimePickerForStart by remember { mutableStateOf(false) }
    var showDatePickerForEnd by remember { mutableStateOf(false) }
    var showTimePickerForEnd by remember { mutableStateOf(false) }
    var showDatePickerForRepeatUntil by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    BackHandler {
        navController.popBackStack()
    }

    LaunchedEffect(repeatType) {
        repeatUntil = when (repeatType) {
            RepeatType.DAILY -> start.date.plusDays(30)  // 기본 30일 후
            RepeatType.WEEKLY -> start.date.plusMonths(3) // 기본 3개월 후
            RepeatType.BIWEEKLY -> start.date.plusMonths(6) // 기본 6개월 후
            RepeatType.MONTHLY -> start.date.plusYears(1)  // 기본 1년 후
            RepeatType.YEARLY -> start.date.plusYears(3)   // 기본 3년 후
            RepeatType.NONE -> end.date.plusWeeks(1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_schedule)) },
                navigationIcon = {
                    IconButton(onClick = {
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            StyledTextField(
                value = title,
                label = stringResource(R.string.title),
                onValueChange = { title = it })

            Spacer(modifier = Modifier.height(16.dp))

            GroupContainer(
            ) {
                // All-day Toggle
                SwitchSelector(
                    label = stringResource(R.string.all_day),
                    option = allDay,
                    onSwitch = { allDay = it })
                CustomHorizontalDivider()
                DateTimeSelector(
                    stringResource(R.string.starts),
                    start.date,
                    start.time,
                    onDateClick = { showDatePickerForStart = true },
                    onTimeClick = { showTimePickerForStart = true },
                    isAllDay = allDay
                )
                CustomHorizontalDivider()
                DateTimeSelector(
                    stringResource(R.string.ends),
                    end.date,
                    end.time,
                    onDateClick = { showDatePickerForEnd = true },
                    onTimeClick = { showTimePickerForEnd = true },
                    isAllDay = allDay
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            GroupContainer {
                // 반복 옵션
                DropdownMenuSelector(
                    title = stringResource(R.string.repeat),
                    options = RepeatType.entries.map { it.getLabel(context) },
                    selectedOption = repeatType.getLabel(context),
                    onOptionSelected = { label -> repeatType = RepeatType.fromLabel(context, label) }
                )

                // 반복 옵션을 선택하면 나타나는 반복 마지막 날 선택 옵션
                if (repeatType != RepeatType.NONE) {
                    CustomHorizontalDivider()
                    SwitchSelector(
                        label = stringResource(R.string.set_repeat_until),
                        option = isRepeatUntilEnabled,
                        onSwitch = { isRepeatUntilEnabled = it })
                    if (isRepeatUntilEnabled) {
                        CustomHorizontalDivider()
                        DateTimeSelector(
                            stringResource(R.string.repeat_until),
                            date = repeatUntil,
                            onDateClick = { showDatePickerForRepeatUntil = true })
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Alarm Dropdown
            GroupContainer {
                DropdownMenuSelector(
                    title = stringResource(R.string.notification),
                    options = AlarmOption.entries.map { it.getLabel(context) },
                    selectedOption = alarmOption.getLabel(context),
                    onOptionSelected = { label ->
                        val selected = AlarmOption.fromLabel(context, label)
                        if (selected.requiresPermission()) {
                            if (PermissionUtils.hasNotificationPermission(context)) {
                                alarmOption = selected
                            } else {
                                showPermissionDialog = true
                                alarmOption = AlarmOption.NONE
                            }
                        } else {
                            alarmOption = selected
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            GroupContainer {
                ColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it },
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val newSchedule = ScheduleData(
                        title = if (title.isBlank()) "New Event" else title,
                        location = location,
                        isAllDay = allDay,
                        start = start,
                        end = end,
                        repeatType = repeatType,
                        repeatUntil = if (isRepeatUntilEnabled) repeatUntil else null,
                        repeatRule = generateRRule(
                            repeatType = repeatType,
                            startDate = start.date,
                            repeatUntil = if (isRepeatUntilEnabled) repeatUntil else null
                        ), // RRule 자동 생성
                        alarmOption = alarmOption,
                        color = selectedColor
                    )
                    scheduleViewModel.processIntent(ScheduleIntent.AddSchedule(newSchedule))
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save), style = MaterialTheme.typography.bodyLarge)
            }
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
            initialDate = repeatUntil,
            minDate = start.date, // 시작 날짜 이후만 선택 가능
            onDateSelected = { repeatUntil = it },
            onDismiss = { showDatePickerForRepeatUntil = false }
        )
    }

    if (showPermissionDialog) {
        NotificationPermissionDeniedDialog(onDismiss = { showPermissionDialog = false })
    }
}

@Composable
fun SwitchSelector(label: String, option: Boolean, onSwitch: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Switch(
            checked = option,
            onCheckedChange = onSwitch
        )
    }
}

@Composable
fun DateTimeField(
    label: String,
    dateTime: DateTimePeriod,
    onDateClick: () -> Unit,
    onTimeClick: (() -> Unit)? = null
) {
    Row {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = dateTime.date.toString(),
                onValueChange = {},
                label = { Text("$label Date") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pick date"
                    )
                }
            )
            // 투명 클릭 레이어
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDateClick() }
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        if (onTimeClick != null) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = dateTime.time.toString(),
                    onValueChange = {},
                    label = { Text("$label Time") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Pick time"
                        )
                    }
                )
                // 투명 클릭 레이어
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onTimeClick() }
                )
            }
        }
    }
}

@Composable
fun StyledTextField(value: String, label: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
//            .background(Color.LightGray.copy(alpha = 0.5f))
            .background(MaterialTheme.colorScheme.surfaceBright)
            .padding(12.dp)
    ) {
        BasicTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(label, color = Color.Gray)
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun DateTimeSelector(
    label: String,
    date: LocalDate,
    time: LocalTime? = null,
    onDateClick: () -> Unit,
    onTimeClick: (() -> Unit)? = null,
    isAllDay: Boolean = false
) {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onDateClick)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(date.toString(), color = MaterialTheme.colorScheme.onPrimary)
            }

            if (time != null && onTimeClick != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !isAllDay, onClick = onTimeClick)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        time.toString(),
                        color = if (isAllDay) Color.Gray else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DropdownMenuSelector(
//    title: String,
//    options: List<String>,
//    selectedOption: String,
//    onOptionSelected: (String) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded }
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { expanded = true } // 클릭 시 Dropdown 열기
//                .padding(12.dp),
//
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(text = title)
//            Box(
//                modifier = Modifier
//                    .menuAnchor(
//                        MenuAnchorType.PrimaryNotEditable,
//                        true
//                    ) // 필수: DropdownMenu와 연결
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(MaterialTheme.colorScheme.primary)
//                    .padding(horizontal = 16.dp, vertical = 8.dp)
//            ) {
//                Text(text = "${selectedOption}", color = MaterialTheme.colorScheme.onPrimary)
//                ExposedDropdownMenu(
//                    modifier = Modifier.wrapContentSize(),
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false }
//                ) {
//                    options.forEach { option ->
//                        DropdownMenuItem(
//                            modifier = Modifier.wrapContentSize(),
//
//                            text = { Text(option) },
//                            onClick = {
//                                onOptionSelected(option)
//                                expanded = false
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuSelector(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),

            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .menuAnchor(
                        MenuAnchorType.PrimaryNotEditable,
                        true
                    ) // 필수: DropdownMenu와 연결
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "${selectedOption}", color = MaterialTheme.colorScheme.onPrimary)
                ExposedDropdownMenu(
                    modifier = Modifier.wrapContentSize(),
                    expanded = expanded,
                    shape = RoundedCornerShape(20.dp),
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            modifier = Modifier.wrapContentSize(),

                            text = { Text(option) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupContainer(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceBright, shape = RoundedCornerShape(10.dp))
            .animateContentSize()
    ) {
        content()
    }
}

@Composable
fun CustomHorizontalDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 12.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}