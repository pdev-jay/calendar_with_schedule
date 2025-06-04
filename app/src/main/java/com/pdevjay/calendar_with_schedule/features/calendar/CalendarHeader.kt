package com.pdevjay.calendar_with_schedule.features.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.features.calendar.states.CalendarState
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CalendarHeader(
    state: CalendarState,
    navController: NavController,
    onTodayClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        title = {
            val formatter = DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())
            Row(modifier = Modifier.fillMaxWidth()) {

                Column {
                    Text(
                        text = state.currentMonth.year.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(text = state.currentMonth.format(formatter))
                }
            }
        },
        navigationIcon = {
            if (state.selectedDate != null) {
                IconButton(
                    onClick = onClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                IconButton(
                    enabled = false,
                    onClick = {}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.schedy_white_svg),
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "More options"
                    )
                }
            }
        },
        actions = {
            TextButton(
                onClick = onTodayClick
            ) {
                Text(text = "Today", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
            }

            Box(
                modifier = Modifier
            ) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    shape = RoundedCornerShape(20.dp),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = "Menu") },
                        text = { Text(stringResource(R.string.setting)) },
                        onClick = {
                            expanded = false
                            val destination = "settings"
                            navController.navigate(destination)
                        },
                    )
                }
            }

        }
    )
}