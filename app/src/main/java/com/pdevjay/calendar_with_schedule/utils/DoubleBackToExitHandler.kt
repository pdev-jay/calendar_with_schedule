package com.pdevjay.calendar_with_schedule.utils

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun DoubleBackToExitHandler(
    drawerState: DrawerState
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var lastBackPressedTime by remember { mutableLongStateOf(0L) }
    val toast = remember { Toast.makeText(context, "한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT) }
    val scope = rememberCoroutineScope()

    BackHandler {
        if (drawerState.isClosed) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressedTime < 1000) {
                toast.cancel()
                if (activity != null) {
                    activity.finish()
                }
            } else {
                lastBackPressedTime = currentTime
                toast.show()
            }
        } else if (drawerState.isOpen) {
            scope.launch {
                drawerState.close()
            }
        }

    }
}
