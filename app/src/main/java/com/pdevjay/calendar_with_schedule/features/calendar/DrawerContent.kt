package com.pdevjay.calendar_with_schedule.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun DrawerContent(
    navController: NavController,
    coroutineScope: CoroutineScope,
    drawerState: DrawerState
) {
    BoxWithConstraints {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surfaceDim)
                    .fillMaxHeight()
                    .width(maxWidth * 0.4f)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomMenuItem(icon = Icons.Default.Settings, contentDescription = "설정") {
                    coroutineScope.launch { drawerState.close() }
                    val destination = "settings"
                    navController.navigate(destination)
                }
            }
    }
}

@Composable
fun CustomMenuItem(
    icon: ImageVector,
    contentDescription: String,
    onItemClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(16.dp)
            ,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = contentDescription,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}