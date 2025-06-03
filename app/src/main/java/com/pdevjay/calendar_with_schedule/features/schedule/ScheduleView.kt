package com.pdevjay.calendar_with_schedule.features.schedule

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.features.calendar.data.HolidaySchedule
import com.pdevjay.calendar_with_schedule.features.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.features.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.features.schedule.data.overlapsWith
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType
import com.pdevjay.calendar_with_schedule.features.schedule.enums.ScheduleEditType
import com.pdevjay.calendar_with_schedule.features.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.features.schedule.viewmodels.ScheduleViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ScheduleView(
    modifier: Modifier = Modifier,
    scheduleViewModel: ScheduleViewModel,
    selectedDay: LocalDate?,
    schedules: List<BaseSchedule>, //  BaseSchedule 사용 (ScheduleData + RecurringData 모두 처리 가능)
    navController: NavController,
    onEventClick: (BaseSchedule) -> Unit, //  BaseSchedule로 변경
    onBackButtonClicked: () -> Unit
) {

    val scrollState = rememberScrollState()
    var showDeleteBottomSheet by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var currentEvent by remember { mutableStateOf<BaseSchedule?>(null) }
    var anchorPosition by remember { mutableStateOf(Offset.Zero) }

    BackHandler {
        onBackButtonClicked()
    }

    val dayEvents = schedules
    val allDayEvents = dayEvents
        .filter { it.isAllDay }
        .sortedWith(compareByDescending<BaseSchedule> { it is HolidaySchedule })

    val nonAllDayEvents = dayEvents.filter { !it.isAllDay }
    val groupedEvents = remember(nonAllDayEvents) { groupOverlappingEvents(nonAllDayEvents) }
    Box(modifier = modifier.fillMaxSize()) {
        Column {
            if (allDayEvents.isNotEmpty()) {
                AllDayEventColumn(allDayEvents, onEventClick)
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
            }

            Column(
                Modifier
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {

                Row(modifier = Modifier.fillMaxSize()) {
                    TimeColumn()

                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .height(1440.dp)  // 24시간 = 1440분
                    ) {
                        val density = LocalDensity.current
                        val maxWidth = maxWidth
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = { offset ->
                                            val yInDp = with(density) { offset.y.toDp() }
                                            val totalMinutes = yInDp.value.toInt().coerceIn(0, 1439)
                                            val hour = totalMinutes / 60
                                            val minute = totalMinutes % 60
                                            val roundedMinute = (minute / 15) * 15

                                            Log.d(
                                                "LongPressTime",
                                                "롱클릭 시간: %02d:%02d".format(hour, roundedMinute)
                                            )
                                            val destination =
                                                "add_schedule/${selectedDay}?hour=${hour}&minute=${roundedMinute}"
                                            navController.navigate(destination)
                                        }
                                    )
                                }
                        )
                        Canvas(modifier = Modifier.matchParentSize()) {
                            for (hour in 0 until 24) {
                                val y = hour * 60f.dp.toPx()
                                drawLine(
                                    color = Color.LightGray,
                                    start = androidx.compose.ui.geometry.Offset(0f, y),
                                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                        // 이벤트 블록 표시
                        Box{

                            groupedEvents.forEach { group ->
                                val totalCount = group.size
                                group.forEachIndexed { index, event ->
                                    if (selectedDay != null) { // selectedDay가 null이 아닐 때만 실행
                                        EventBlock(
                                            event,
                                            index,
                                            totalCount,
                                            maxWidth,
                                            selectedDay,
                                            onEventClick = onEventClick,
                                            onEventLongClick = { event ->
                                                currentEvent = event
                                                showDeleteBottomSheet = true
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedDay == LocalDate.now()) {
                            // 현재 시간 표시줄
                            NowIndicator()
                        }
                    }
                }
            }
        }
    }

    if (currentEvent != null) {
        if (currentEvent!!.branchId == null) {
            ConfirmBottomSheet(
                title = stringResource(R.string.delete_schedule),
                description = stringResource(R.string.delete_description_for_single_occurrence),
                event = currentEvent!!,
                single = stringResource(R.string.delete_single_occurrence),
                isVisible = showDeleteBottomSheet,
                onDismiss = { showDeleteBottomSheet = false },
                onSingle = {
                    try {
                        scheduleViewModel.processIntent(
                            ScheduleIntent.DeleteSchedule(
                                currentEvent as RecurringData,
                                ScheduleEditType.ONLY_THIS_EVENT
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
            )

        } else {
            ConfirmBottomSheet(
                title = stringResource(R.string.delete_schedule),
                description = stringResource(R.string.delete_description),
                event = currentEvent!!,
                single = stringResource(R.string.delete_single),
                future = stringResource(R.string.delete_future),
                isVisible = showDeleteBottomSheet,
                onDismiss = { showDeleteBottomSheet = false },
                onSingle = {

                    try {
                        scheduleViewModel.processIntent(
                            ScheduleIntent.DeleteSchedule(
                                currentEvent as RecurringData,
                                ScheduleEditType.ONLY_THIS_EVENT
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onFuture = {
                    try {
                        scheduleViewModel.processIntent(
                            ScheduleIntent.DeleteSchedule(
                                currentEvent as RecurringData,
                                ScheduleEditType.THIS_AND_FUTURE
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
            )
        }
    }
    LaunchedEffect(Unit) {
        val now = LocalTime.now()
        val nowOffset = now.hour * 60 + now.minute
        scrollState.scrollTo(nowOffset + 200)
    }
}

@Composable
fun TimeColumn() {
    Column(
        modifier = Modifier
            .width(50.dp)
            .height(1440.dp)
            .verticalScroll(rememberScrollState())
    ) {
        for (hour in 0 until 24) {
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(text = "${hour}:00", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventBlock(
    event: BaseSchedule,
    index: Int,
    totalCount: Int,
    maxWidth: Dp,
    selectedDay: LocalDate,
    onEventClick: (BaseSchedule) -> Unit,
    onEventLongClick: (BaseSchedule) -> Unit
) {
    val startMinutes = if (event.start.date < selectedDay) {
        0  // 전날부터 이어진 이벤트는 오늘 0시부터 표시
    } else {
        event.start.time.hour * 60 + event.start.time.minute
    }

    val endMinutes = if (event.end.date > selectedDay) {
        1440
    } else {
        // 반복일정 -> time만 고려하기 때문에 종료 지점 찾기에 문제 없음
        event.end.time.hour * 60 + event.end.time.minute
    }

    val durationMinutes = endMinutes - startMinutes

    val blockWidth = maxWidth / totalCount
    val xOffset = index * blockWidth

    // 색상 진하기 조절 (index가 클수록 진한 색상)
//    val baseColor = Color(0xFF03A9F4)
    val baseColor =
        event.color?.let { Color(it).copy(alpha = 0.7f) } ?: MaterialTheme.colorScheme.primary
    val colorFactor = (index + 1).toFloat() / totalCount.toFloat()
    val darkerColor = baseColor.copy(
        red = (baseColor.red * (1 - 0.3f * colorFactor)),
        green = (baseColor.green * (1 - 0.3f * colorFactor)),
        blue = (baseColor.blue * (1 - 0.3f * colorFactor))
    )



    BoxWithConstraints(modifier = Modifier
        .offset(x = xOffset, y = startMinutes.dp)
        .width(blockWidth)
        .defaultMinSize(minHeight = 30.dp)
        .height(durationMinutes.dp)
        .combinedClickable(
            onClick = {
                if (event !is HolidaySchedule) {
                    onEventClick(event)
                }
            },
            onLongClick = {
                if (event !is HolidaySchedule) {
                    onEventLongClick(event)
                }
            },
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )

//        .border(0.5.dp, MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(8.dp))
        .background(darkerColor, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.TopStart

    ) {
        val boxHeight = maxHeight

        val adjustedFontSize = when {
            boxHeight < 20.dp -> 6.sp
            boxHeight < 40.dp -> 8.sp
            boxHeight < 60.dp -> 10.sp
            boxHeight < 80.dp -> 11.sp
            boxHeight < 100.dp -> 12.sp
            boxHeight < 120.dp -> 13.sp
            else -> 14.sp
        }

        val adjustedIconSize = when {
            boxHeight < 20.dp -> 6.dp
            boxHeight < 40.dp -> 8.dp
            boxHeight < 60.dp -> 10.dp
            boxHeight < 80.dp -> 11.dp
            boxHeight < 100.dp -> 12.dp
            boxHeight < 120.dp -> 13.dp
            else -> 14.dp
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            Text(
                event.title,
                color = Color.White,
                fontSize = adjustedFontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (event.repeatType != RepeatType.NONE) {
                Icon(
                    modifier = Modifier.size(adjustedIconSize),
                    painter = painterResource(id = R.drawable.ic_repeat),
                    tint = Color.White,
                    contentDescription = "repeat"
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllDayEventColumn(
    allDayEvents: List<BaseSchedule>,
    onEventClick: (BaseSchedule) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(text = "All-Day Events", style = MaterialTheme.typography.titleMedium)
        Row() {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                maxItemsInEachRow = 3
            ) {

                allDayEvents.forEach { event ->
                    val baseColor =
                        event.color?.let { Color(it).copy(alpha = 0.7f) }
                            ?: MaterialTheme.colorScheme.primary
                    val darkerColor = baseColor.copy(
                        red = (baseColor.red * (1 - 0.1f)),
                        green = (baseColor.green * (1 - 0.1f)),
                        blue = (baseColor.blue * (1 - 0.1f))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .background(darkerColor, shape = RoundedCornerShape(8.dp))
                            .clickable {
                                if (event !is HolidaySchedule) {
                                    onEventClick(event)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween

                        ) {
                            Text(
                                event.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.padding(2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (event.repeatType != RepeatType.NONE) {
                                Icon(
                                    modifier = Modifier.size(14.dp),
                                    painter = painterResource(id = R.drawable.ic_repeat),
                                    tint = Color.White,
                                    contentDescription = "repeat"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun NowIndicator() {
    val currentMinute = remember { mutableIntStateOf(getCurrentMinuteOfDay()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5 * 60 * 1000L)
            currentMinute.intValue = getCurrentMinuteOfDay()
        }
    }

    val density = LocalDensity.current

    Box(modifier = Modifier
        .offset {
            with(density) {
                IntOffset(
                    x = 0, y = currentMinute.intValue.dp.roundToPx()
                )
            }
        }
        .fillMaxWidth()
        .height(2.dp)
        .background(Color.Red))
}


// 현재 시간을 분 단위로 반환
fun getCurrentMinuteOfDay(): Int {
    val now = LocalTime.now()
    return now.hour * 60 + now.minute
}


fun groupOverlappingEvents(events: List<BaseSchedule>): List<List<BaseSchedule>> {
    if (events.isEmpty()) return emptyList() //  빈 리스트가 들어오면 빈 리스트 반환
    //  일정들을 시작 시간 기준으로 정렬 (시간을 분 단위로 변환하여 비교)
    val sorted = events.sortedBy { it.start.time.hour * 60 + it.start.time.minute }

    val result = mutableListOf<MutableList<BaseSchedule>>() //  그룹화된 결과 리스트
    var currentGroup = mutableListOf(sorted.first()) //  첫 번째 일정으로 첫 그룹 시작

    //  두 번째 일정부터 순회하면서 그룹화 진행
    for (i in 1 until sorted.size) {
        val prev = currentGroup.last() //  현재 그룹에서 마지막 일정
        val curr = sorted[i] //  현재 비교 중인 일정
        val isOverlap = prev.overlapsWith(curr)
        if (isOverlap) { //  이전 일정과 현재 일정이 겹치면 같은 그룹에 추가
            currentGroup.add(curr)
        } else { //  겹치지 않으면 새로운 그룹을 시작
            result.add(currentGroup) // 기존 그룹 저장
            currentGroup = mutableListOf(curr) // 새로운 그룹 생성
        }
    }

    result.add(currentGroup) //  마지막 그룹 추가 (루프 종료 후 처리)
    return result //  최종 그룹화된 리스트 반환
}


