package com.pdevjay.calendar_with_schedule.screens.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TimeColumn(hourHeight: Dp) {
    Column(
        modifier = Modifier
            .width(50.dp)  // 시간 표시 열의 너비
            .fillMaxHeight()
    ) {
        // 0시부터 23시까지
        for (hour in 0..23) {
            Box(
                modifier = Modifier
                    .height(hourHeight)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = String.format("%02d:00", hour),
                    modifier = Modifier.align(Alignment.TopCenter),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}