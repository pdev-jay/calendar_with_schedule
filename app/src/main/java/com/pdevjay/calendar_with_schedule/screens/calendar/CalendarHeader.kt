package com.pdevjay.calendar_with_schedule.screens.calendar

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.screens.calendar.states.CalendarState
import com.pdevjay.calendar_with_schedule.utils.SlideInHorizontallyContainer
import com.pdevjay.calendar_with_schedule.utils.SlideInHorizontallyContainerFromStart
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CalendarHeader(state: CalendarState,
                   navController: NavController,
                   onTodayClick: () -> Unit = {},
                   onClick: () -> Unit = {}) {
    TopAppBar(
        title = {
            val formatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)
            Row(modifier = Modifier.fillMaxWidth()){

                Column {
                    Text(
                        text = state.currentMonth.year.toString(),
                        style = TextStyle(fontSize = 14.sp)
                    )
                    Text(text = state.currentMonth.format(formatter))
                }
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
            Text(modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onTodayClick
            ),
                text = "Today",
            )
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