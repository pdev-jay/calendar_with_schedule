package com.pdevjay.calendar_with_schedule.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.ScheduleColor

@Composable
fun ColorPicker(
    selectedColor: Int?,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    colors: List<ScheduleColor> = ScheduleColor.entries
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        colors.forEach { color ->
            val isSelected = color.colorInt == selectedColor

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(color.colorInt))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color.colorInt) }
            )
        }
    }
}
