package com.pdevjay.calendar_with_schedule.screens.schedule

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdevjay.calendar_with_schedule.screens.schedule.data.DateTimePeriod
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.generateRepeatRule
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatOption
import com.pdevjay.calendar_with_schedule.ui.theme.Calendar_with_scheduleTheme
import com.pdevjay.calendar_with_schedule.utils.SlideInHorizontallyContainer
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

    var isVisible by remember { mutableStateOf(true) }

    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var start by remember { mutableStateOf(DateTimePeriod(initialDate, LocalTime.of(now.hour, 0))) }
    var end by remember { mutableStateOf(DateTimePeriod(initialDate, LocalTime.of(now.plusHours(1).hour, 0))) }
    var allDay by remember { mutableStateOf(false) }
    // repeat과 alarm 상태 추가
    var repeatOption by remember { mutableStateOf(RepeatOption.NONE) }
    var alarmOption by remember { mutableStateOf(AlarmOption.NONE) }

    var showDatePickerForStart by remember { mutableStateOf(false) }
    var showTimePickerForStart by remember { mutableStateOf(false) }
    var showDatePickerForEnd by remember { mutableStateOf(false) }
    var showTimePickerForEnd by remember { mutableStateOf(false) }

    BackHandler {
        isVisible = false
        onDismiss()
    }

    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        topBar = {
                SlideInHorizontallyContainer(isVisible) {
                    TopAppBar(
                        title = { Text("Add Schedule") },
                        navigationIcon = {
                            IconButton(onClick = {
                                isVisible = false
                                onDismiss()
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
                StyledTextField(value = title, label = "Title", onValueChange = { title = it })

                Spacer(modifier = Modifier.height(16.dp))

                Column (
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp))
                ){
                    // All-day Toggle
                    AllDaySwitch(allDay)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 2.dp, color = Color.LightGray)
                    DateTimeSelector("Starts", start, onDateClick = {}, onTimeClick = {})
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 2.dp, color = Color.LightGray)
                    DateTimeSelector("Ends", end, onDateClick = {}, onTimeClick = {})

                }

                Spacer(modifier = Modifier.height(16.dp))

                DropdownMenuSelector(
                    title = "Repeat",
                    options = RepeatOption.entries.map { it.label },
                    selectedOption = repeatOption.label,
                    onOptionSelected = { label -> repeatOption = RepeatOption.fromLabel(label) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Alarm Dropdown
                DropdownMenuSelector(
                    title = "Alarm",
                    options = AlarmOption.entries.map { it.label },
                    selectedOption = alarmOption.label,
                    onOptionSelected = { label -> alarmOption = AlarmOption.fromLabel(label) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isVisible = false
                        val newSchedule = ScheduleData(
                            title = if (title.isBlank()) "New Event" else title,
                            location = location,
                            start = start,
                            end = end,
                            repeatOption = repeatOption,
                            repeatRule = generateRepeatRule(repeatOption), // 🔹 RRule 자동 생성
                            alarmOption = alarmOption
                        )
                        Log.e("","ScheduleAddScreen: $newSchedule")
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

@Composable
private fun AllDaySwitch(allDay: Boolean) {
    var allDay1 = allDay
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("All-day", fontSize = 16.sp)
        Switch(
            checked = allDay1,
            onCheckedChange = { allDay1 = it }
        )
    }
}

@Composable
fun DateTimeField(
    label: String,
    dateTime: DateTimePeriod,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
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

@Composable
fun StyledTextField(value: String, label: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        BasicTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp, color = Color.White),
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
    dateTime: DateTimePeriod,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
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
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onDateClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(dateTime.date.toString(), color = Color.White, fontSize = 16.sp)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onTimeClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(dateTime.time.toString(), color = Color.White, fontSize = 16.sp)
            }
        }
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
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp))
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

@Preview(showBackground = true)
@Composable
fun PreviewScheduleAddScreen(){
    Calendar_with_scheduleTheme {
        ScheduleAddScreen(LocalDate.now(), {}, {})
    }
}