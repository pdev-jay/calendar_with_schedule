package com.pdevjay.calendar_with_schedule.screens.calendar

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.screens.calendar.states.CalendarState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CalendarHeader(state: CalendarState,
                   navController: NavController,
                   onClick: () -> Unit = {}) {
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
        },
        actions = {
            IconButton(onClick = {
                val destination = "add_schedule/${state.selectedDate ?: LocalDate.now()}"

                navController.navigate(destination)
            }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
                )
            }
        }
    )
}