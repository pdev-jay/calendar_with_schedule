package com.pdevjay.calendar_with_schedule.screens.schedule

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.datamodels.DateTimePeriod
import com.pdevjay.calendar_with_schedule.datamodels.ScheduleData
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt


@Composable
fun AddScheduleScreen(
    onDismiss: () -> Unit,
    onSave: (ScheduleData) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var start by remember { mutableStateOf(DateTimePeriod(LocalDate.now(), LocalTime.of(9, 0))) }
    var end by remember { mutableStateOf(DateTimePeriod(LocalDate.now(), LocalTime.of(10, 0))) }

    ScheduleBottomSheet(onDismiss = onDismiss) { nestedScrollConnection, scrollState ->

        // 모달 내부 스크롤 가능한 입력 폼 등
        Box(modifier = Modifier.fillMaxSize()) {
            // 예시 콘텐츠 (입력 필드, Save 버튼 등)
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .nestedScroll(nestedScrollConnection)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Add Schedule",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                OutlinedTextField(
                    value = "${start.date} ${start.time}",
                    onValueChange = { /* DateTimePicker 띄우고 업데이트 */ },
                    label = { Text("Start") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "${end.date} ${end.time}",
                    onValueChange = { /* DateTimePicker 띄우고 업데이트 */ },
                    label = { Text("End") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val newSchedule = ScheduleData(
                            title = title,
                            location = location,
                            start = start,
                            end = end
                        )
                        onSave(newSchedule)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    }
}


@Composable
fun ScheduleBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable (nestedScrollConnection: NestedScrollConnection, scrollState: ScrollState) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // 화면 높이 (dp 및 픽셀)
    val screenHeightDp = configuration.screenHeightDp.dp
    val screenHeightPx = with(density) { screenHeightDp.toPx() }

    // 초기 상태: 화면 하단(숨김 상태)에서 시작
    val offsetYAnim = remember { Animatable(screenHeightPx) }

    val scrollState = rememberScrollState()

    // NestedScrollConnection: 콘텐츠 스크롤이 최상단(0)일 때 아래로 드래그하면 바텀시트 오프셋을 함께 업데이트
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (offsetYAnim.value > 0f) {
                    // 바텀시트가 열리는 중이면 스크롤 이벤트 먹어버림 (내부로 전달 안 함)
                    coroutineScope.launch {
                        offsetYAnim.snapTo((offsetYAnim.value + available.y).coerceAtMost(screenHeightPx))
                    }
                    return available // 여기서 소비하면 내부 스크롤 안 움직임
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (scrollState.value == 0 && available.y > 0f) {
                    Log.d("nestedscroll","onPostScroll : scrollState.value : ${scrollState.value} / available.y : ${available.y} / consumed : ${consumed.y}")
                    coroutineScope.launch {
                        offsetYAnim.snapTo((offsetYAnim.value + available.y).coerceAtMost(screenHeightPx))
                    }
                    // available을 모두 소비함
                    return available
                }

                return super.onPostScroll(consumed, available, source)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                coroutineScope.launch {
                    if (offsetYAnim.value > screenHeightPx / 2) {
                        offsetYAnim.animateTo(screenHeightPx, tween(300))
                        onDismiss()
                    } else {
                        offsetYAnim.animateTo(0f, tween(300))
                    }
                }

                return super.onPostFling(consumed, available)
            }
        }
    }

    // 처음 나타날 때, 바텀시트가 위로 슬라이드되며 나타나도록 애니메이션
    LaunchedEffect(Unit) {
        offsetYAnim.animateTo(0f, animationSpec = tween(durationMillis = 300))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(color = Color.Black.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .offset { IntOffset(0, offsetYAnim.value.roundToInt()) }
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .draggable(
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            offsetYAnim.snapTo((offsetYAnim.value + delta).coerceAtLeast(0f))
                        }
                    },
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        coroutineScope.launch {
                            if (offsetYAnim.value > screenHeightPx / 2) {
                                offsetYAnim.animateTo(screenHeightPx, tween(300))
                                onDismiss()
                            } else {
                                offsetYAnim.animateTo(0f, tween(300))
                            }
                        }
                    }
                )
        ) {
            Column (
            ){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 4.dp)
                            .align(Alignment.Center)
                            .background(
                                color = Color.Gray,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
                content(nestedScrollConnection, scrollState)
            }
        }
    }
}
