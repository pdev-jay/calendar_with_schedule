package com.pdevjay.calendar_with_schedule.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pdevjay.calendar_with_schedule.datamodels.CalendarMonth
import com.pdevjay.calendar_with_schedule.datamodels.CalendarWeek
import com.pdevjay.calendar_with_schedule.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.viewmodels.CalendarViewModel

@Composable
fun CalendarTopBar(
    months: SnapshotStateList<CalendarMonth>,
    viewModel: CalendarViewModel
) {
    val state by viewModel.state.collectAsState()

    Column {
        CalendarHeader(state)
        WeekHeader()
        AnimatedVisibility(
            visible = state.selectedDate != null,
            enter = fadeIn(animationSpec = tween(durationMillis = 100)) + expandVertically(
                animationSpec = tween(durationMillis = 100)
            ),
            exit = fadeOut(animationSpec = tween(durationMillis = 100)) + shrinkVertically(
                animationSpec = tween(durationMillis = 100)
            )
        ) {
            // 선택된 날짜가 속한 주를 계산합니다.
            val selectedWeek = months
                .flatMap { it.weeks }
                .find { week ->
                    week.days.any {
                        it.isCurrentMonth && it.date == state.selectedDate
                    }
                }
            // 선택된 주를 TopAppBar 영역 하단에 추가 (원하는 스타일 적용 가능)
            BoxWithConstraints {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxHeight * 0.1f)
                        .background(color = Color.LightGray),
                ) {
                    WeekRow(
                        isInTopBar = true,
                        modifier = Modifier.fillMaxSize(),
                        week = selectedWeek ?: CalendarWeek("", emptyList()),
                        selectedDate = state.selectedDate,
                        onDateSelected = { date ->
                            if (state.selectedDate == date)
                                viewModel.processIntent(CalendarIntent.DateUnselected)
                            else
                                viewModel.processIntent(CalendarIntent.DateSelected(date))
                        }
                    )
                }
            }
        }
    }
}