//package com.pdevjay.calendar_with_schedule.datamodels
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxWithConstraints
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.Divider
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.dp
//import java.time.LocalDate
//import java.time.LocalTime
//
//// 더미 Task 데이터 모델
//// 단일 이벤트(일정)를 나타내는 데이터 클래스
//data class CalendarEvent(
//    val id: Int,
//    val title: String,
//    val location: String? = null,
//    val startTime: LocalTime,
//    val endTime: LocalTime
//)
//
///**
// * 이벤트 리스트를 받아서 각 이벤트를 (laneIndex)로 배정한 결과를 반환.
// * - laneIndex: 0부터 시작, 이벤트가 겹치지 않는다면 같은 레인 사용
// */
//fun assignLanesToEvents(events: List<CalendarEvent>): List<LaneEvent> {
//    // 시작 시각 기준으로 정렬
//    val sorted = events.sortedBy { it.startTime }
//    val lanes = mutableListOf<MutableList<CalendarEvent>>() // 각 lane마다 이벤트 리스트
//
//    // 결과를 저장할 리스트: LaneEvent(이벤트, 레인 인덱스)
//    val result = mutableListOf<LaneEvent>()
//
//    for (event in sorted) {
//        // 기존 레인 중에서 겹치지 않는 레인을 찾는다
//        var placedLaneIndex: Int? = null
//        for ((laneIndex, laneEvents) in lanes.withIndex()) {
//            // 이 레인에 있는 이벤트들과 겹치지 않으면 배정 가능
//            if (laneEvents.none { isOverlapping(it, event) }) {
//                laneEvents.add(event)
//                placedLaneIndex = laneIndex
//                break
//            }
//        }
//        // 기존 레인에 배정 못 했다면 새 레인 생성
//        if (placedLaneIndex == null) {
//            lanes.add(mutableListOf(event))
//            placedLaneIndex = lanes.size - 1
//        }
//        result.add(LaneEvent(event, placedLaneIndex))
//    }
//
//    return result
//}
//
///** 두 이벤트가 시간이 겹치는지 판단 */
//fun isOverlapping(a: CalendarEvent, b: CalendarEvent): Boolean {
//    return a.startTime < b.endTime && b.startTime < a.endTime
//}
//
///** 레인 배정 결과: 어떤 이벤트가 몇 번 레인에 들어가는지 */
//data class LaneEvent(
//    val event: CalendarEvent,
//    val laneIndex: Int
//)
//
//
//@Composable
//fun DayScheduleView(
//    events: List<CalendarEvent>,
//    modifier: Modifier = Modifier
//) {
//    val hourHeight = 60.dp
//    val totalHeight = hourHeight * 24
//
//    // 이벤트 레인 배정: 겹치는 이벤트들이 서로 다른 레인(lane)에 배정됨.
//    val laneEvents = remember(events) { assignLanesToEvents(events) }
//    val maxLaneCount = laneEvents.maxOfOrNull { it.laneIndex }?.plus(1) ?: 1
//
//    // 단일 스크롤 상태: 전체 타임라인(시간 축과 이벤트)이 스크롤됨.
//    val scrollState = rememberScrollState()
//
//    // 전체 타임라인을 하나의 Column에 넣어서 스크롤 가능하게 합니다.
//    Column(modifier = modifier.verticalScroll(scrollState)) {
//        Row(modifier = Modifier.height(totalHeight)) {
//            // 왼쪽 시간 축: 0시부터 23시까지 시간을 표시합니다.
//            TimeColumn(hourHeight = hourHeight)
//            // 오른쪽 이벤트 영역: 전체 높이를 채우고, 이벤트들을 오버레이 합니다.
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxHeight()
//                    .background(Color.White)
//            ) {
//                // 시간 가이드 라인 (옵션)
//                TimeLines(hourHeight = hourHeight)
//                // 이벤트 블록 배치: 각 이벤트의 startTime과 endTime을 기반으로 세로 위치와 높이 계산
//                laneEvents.forEach { laneEvent ->
//                    val event = laneEvent.event
//                    val laneIndex = laneEvent.laneIndex
//
//                    val startOffset = calculateOffset(event.startTime, hourHeight)
//                    val eventHeight = calculateEventHeight(event.startTime, event.endTime, hourHeight)
//
//                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
//                        // 현재 영역의 전체 너비를 Dp로 변환하여, 레인 수만큼 분할
//                        val laneWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() } / maxLaneCount
//                        val xOffset = laneWidth * laneIndex
//
//                        Box(
//                            modifier = Modifier
//                                .offset {
//                                    IntOffset(
//                                        x = xOffset.roundToPx(),
//                                        y = startOffset.roundToPx()
//                                    )
//                                }
//                                .width(laneWidth)
//                                .height(eventHeight)
//                                .padding(horizontal = 2.dp)
//                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
//                                .padding(4.dp)
//                        ) {
//                            Column {
//                                Text(
//                                    text = event.title,
//                                    fontWeight = FontWeight.SemiBold,
//                                    style = MaterialTheme.typography.bodyMedium
//                                )
////                                event.location?.let { loc ->
////                                    Text(
////                                        text = loc,
////                                        style = MaterialTheme.typography.bodyMedium,
////                                        color = Color.DarkGray
////                                    )
////                                }
//                                Text(
//                                    text = "${event.startTime} - ${event.endTime}",
//                                    style = MaterialTheme.typography.bodySmall,
//                                    color = Color.Gray
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//
//
//
//
//@Composable
//fun TimeColumn(hourHeight: Dp) {
//    Column(
//        modifier = Modifier
//            .width(50.dp)  // 시간 표시 열의 너비
//            .fillMaxHeight()
//    ) {
//        // 0시부터 23시까지
//        for (hour in 0..23) {
//            Box(
//                modifier = Modifier
//                    .height(hourHeight)
//                    .fillMaxWidth(),
//            ) {
//                Text(
//                    text = String.format("%02d:00", hour),
//                    modifier = Modifier.align(Alignment.TopCenter),
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun TimeLines(hourHeight: Dp) {
//    // 24개의 수평선을 그려 시각적으로 시간대 구분
//    // (Canvas로 그려도 되고, Box로 만들어도 됩니다.)
//    Canvas(modifier = Modifier.fillMaxSize()) {
//        for (hour in 0..24) {
//            val y = hourHeight.toPx() * hour
//            drawLine(
//                color = Color.LightGray.copy(alpha = 0.5f),
//                start = Offset(0f, y),
//                end = Offset(size.width, y),
//                strokeWidth = 1.dp.toPx()
//            )
//        }
//    }
//}
//
///**
// * 이벤트의 시작 시각을 기준으로 Y 오프셋을 계산
// * 예: 08:30 -> hour=8, minute=30 -> offset = (8 + 30/60) * hourHeight
// */
//fun calculateOffset(startTime: LocalTime, hourHeight: Dp): Dp {
//    val totalMinutes = startTime.hour * 60 + startTime.minute
//    val ratio = totalMinutes / 60f  // 몇 시간인지 소수로
//    return hourHeight * ratio
//}
//
///**
// * 이벤트 높이를 startTime~endTime의 시간 차이에 비례하도록 계산
// * 예: 08:30~09:45 -> 1시간 15분 = 75분 -> hourHeight*(75/60)
// */
//fun calculateEventHeight(startTime: LocalTime, endTime: LocalTime, hourHeight: Dp): Dp {
//    val startMinutes = startTime.hour * 60 + startTime.minute
//    val endMinutes = endTime.hour * 60 + endTime.minute
//    val duration = (endMinutes - startMinutes).coerceAtLeast(0) // 음수 방지
//    val ratio = duration / 60f
//    return hourHeight * ratio
//}
//
//
//
//// 종일 이벤트를 가로로 나열하는 예시
//@Composable
//fun AllDayEventsSection(allDayEvents: List<CalendarEvent>) {
//    // 배경이나 디자인은 상황에 맞춰 꾸밀 수 있음
//    Column(modifier = Modifier
//        .fillMaxWidth()
//        .background(MaterialTheme.colorScheme.surfaceVariant)
//        .padding(8.dp)) {
//        Text(
//            text = "All-day",
//            style = MaterialTheme.typography.titleSmall,
//            modifier = Modifier.padding(bottom = 4.dp)
//        )
//        // 간단히 Column으로 표시
//        allDayEvents.forEach { event ->
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
//                    .padding(8.dp)
//            ) {
//                Text(text = event.title, style = MaterialTheme.typography.bodyLarge)
//            }
//        }
//    }
//}
//
//// 시간대별 스케줄 표시
//@Composable
//fun TimeLine(events: List<CalendarEvent>) {
//    // 0시부터 23시까지 24개의 time slot
//    LazyColumn(modifier = Modifier.fillMaxSize()) {
//        items(24) { hour ->
//            TimeSlotView(hour = hour, events = events)
//        }
//    }
//}
//
//// 각 시간 슬롯(예: 08:00 ~ 08:59)에 해당하는 이벤트를 표시
//@Composable
//fun TimeSlotView(hour: Int, events: List<CalendarEvent>) {
//    // 이 시간대에 걸치는 이벤트를 찾아 표시
//    // 예: 08:00 <= startTime < 09:00 or endTime > 08:00 등
//    val slotEvents = events.filter { event ->
//        val startHour = event.startTime.hour
//        val endHour = event.endTime.hour
//        // (hour <= endHour) && (hour >= startHour) 등으로 간단히 판단 가능
//        // 좀 더 정확히 하려면 분 단위까지 비교
//        (event.startTime.hour <= hour && event.endTime.hour >= hour)
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(4.dp)
//    ) {
//        // 시간 라벨
//        Text(
//            text = String.format("%02d:00", hour),
//            style = MaterialTheme.typography.bodyMedium,
//            modifier = Modifier.padding(vertical = 2.dp)
//        )
//        // 이벤트 목록
//        if (slotEvents.isNotEmpty()) {
//            slotEvents.forEach { event ->
//                // 이벤트가 여러 시간대에 걸쳐 있으면 중복 표시될 수 있음
//                // 더 정교하게 표현하려면 Custom Layout으로 높이를 비례 배분할 수도 있음
//                EventItem(event)
//            }
//        } else {
//            // 이벤트가 없으면 빈 공간 또는 라인 표시
//            Divider(modifier = Modifier.padding(start = 16.dp, end = 16.dp))
//        }
//    }
//}
//
//// 개별 이벤트 표시
//@Composable
//fun EventItem(event: CalendarEvent) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 2.dp)
//            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
//            .padding(8.dp)
//    ) {
//        Column {
//            Text(
//                text = event.title,
//                style = MaterialTheme.typography.bodyLarge,
//                fontWeight = FontWeight.SemiBold
//            )
//            event.location?.let { loc ->
//                Text(
//                    text = loc,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//@Preview(showBackground = true)
//fun DaySchedulePreview() {
//    // 샘플 이벤트
//    val sampleEvents = listOf(
//        CalendarEvent(
//            id = 1,
//            title = "Pay bills",
//            startTime = LocalTime.of(0, 0),
//            endTime = LocalTime.of(0, 0) // 여기선 0분짜리(실제로는 종일 처리?)
//        ),
//        CalendarEvent(
//            id = 2,
//            title = "Pilates",
//            location = "Laan der Hesperiden, 1076 DX Amsterdam",
//            startTime = LocalTime.of(8, 0),
//            endTime = LocalTime.of(10, 0)
//        ),
//        CalendarEvent(
//            id = 3,
//            title = "Vitamin team meeting",
//            location = "Goudfazant, Aambeeldstraat 10h",
//            startTime = LocalTime.of(9, 0),
//            endTime = LocalTime.of(10, 30)
//        ),
//        CalendarEvent(
//            id = 4,
//            title = "Climbing gym",
//            startTime = LocalTime.of(9, 0),
//            endTime = LocalTime.of(18, 30)
//        )
//    )
//    DayScheduleView(events = sampleEvents)
//}
