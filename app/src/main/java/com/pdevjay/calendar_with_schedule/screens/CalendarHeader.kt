package com.pdevjay.calendar_with_schedule.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.pdevjay.calendar_with_schedule.states.CalendarState
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CalendarHeader(state: CalendarState, onClick: () -> Unit = {}) {
    TopAppBar(
        title = {
            val formatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)
            Column {
                Text(
                    text = state.currentMonth.year.toString(),
                    style = TextStyle(fontSize = 14.sp)
                )
                Text(text = state.currentMonth.format(formatter))
            }
        },
        navigationIcon = {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}