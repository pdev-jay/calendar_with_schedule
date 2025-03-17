package com.pdevjay.calendar_with_schedule.screens.schedule

import android.util.Log
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.ui.theme.AppTheme
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.generateRepeatRule
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.ScheduleViewModel
import com.pdevjay.calendar_with_schedule.utils.RRuleHelper.generateRRule
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import com.pdevjay.calendar_with_schedule.utils.SlideInHorizontallyContainer
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAddScreen(
    selectedDate: LocalDate?,
    navController: NavController,
    scheduleViewModel: ScheduleViewModel,
) {
    val now = remember { LocalTime.now() }
    val initialDate = remember { selectedDate ?: LocalDate.now() }

    var isVisible by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var start by remember { mutableStateOf(DateTimePeriod(initialDate, LocalTime.of(now.hour, 0))) }
    var end by remember { mutableStateOf(DateTimePeriod(initialDate, LocalTime.of(now.plusHours(1).hour, 0))) }
    var allDay by remember { mutableStateOf(false) }
    // repeat과 alarm 상태 추가
    var repeatType by remember { mutableStateOf(RepeatType.NONE) }
    var isRepeatUntilEnabled by remember { mutableStateOf(false) }
    var repeatUntil by remember { mutableStateOf(end.date.plusWeeks(1)) }
    var alarmOption by remember { mutableStateOf(AlarmOption.NONE) }

    var showDatePickerForStart by remember { mutableStateOf(false) }
    var showTimePickerForStart by remember { mutableStateOf(false) }
    var showDatePickerForEnd by remember { mutableStateOf(false) }
    var showTimePickerForEnd by remember { mutableStateOf(false) }
    var showDatePickerForRepeatUntil by remember { mutableStateOf(false) }

    BackHandler {
        isVisible = false
        navController.popBackStack()
    }

    LaunchedEffect(Unit) { isVisible = true }

    LaunchedEffect(repeatType){
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
                SlideInHorizontallyContainer(isVisible) {
                    TopAppBar(
                        title = { Text(stringResource(R.string.add_schedule)) },
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

                GroupContainer (
                ){
                    // All-day Toggle
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
                        SwitchSelector(label = stringResource(R.string.set_repeat_until), option = isRepeatUntilEnabled, onSwitch = {isRepeatUntilEnabled = it})
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 2.dp, color = Color.LightGray)
                        if (isRepeatUntilEnabled){
                            DateTimeSelector(stringResource(R.string.repeat_until), date = repeatUntil, onDateClick = {showDatePickerForRepeatUntil = true})
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
                            isOriginalSchedule = true // ✅ 변경됨
                        )
                        scheduleViewModel.processIntent(ScheduleIntent.AddSchedule(newSchedule))
                        isVisible = false
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.save))
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
    }
}

@Composable
fun SwitchSelector(label:String, option: Boolean, onSwitch: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp)
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
            .background(Color.LightGray.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        BasicTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(label, color = Color.Gray, fontSize = 16.sp)
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun DateTimeSelector(
    label: String,
//    dateTime: DateTimePeriod,
    date: LocalDate,
    time: LocalTime? = null,
    onDateClick: () -> Unit,
    onTimeClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onDateClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(date.toString(), fontSize = 16.sp)
            }

            if (time != null && onTimeClick != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onTimeClick)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(time.toString(), fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun RepeatUntilSelector(label:String, repeatUntil: LocalDate, onClick: () -> Unit, ){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = repeatUntil.toString())
    }
}

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
        onExpandedChange = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true } // 클릭 시 Dropdown 열기
                .padding(12.dp),

            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title)
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
                Text(text = "${selectedOption}", color = Color.White, fontSize = 16.sp)
                ExposedDropdownMenu(
                    modifier = Modifier.wrapContentSize(),
                    expanded = expanded,
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
fun GroupContainer(content: @Composable () -> Unit){
    Column(
        modifier = Modifier
            .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp))
            .animateContentSize()
    ){
        content()
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewScheduleAddScreen(){
//    AppTheme {
//        ScheduleAddScreen(LocalDate.now(), {}, {})
//    }
//}