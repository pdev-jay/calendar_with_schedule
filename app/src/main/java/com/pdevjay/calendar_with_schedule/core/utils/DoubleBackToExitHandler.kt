package com.pdevjay.calendar_with_schedule.core.utils

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
import androidx.compose.ui.res.stringResource
import com.pdevjay.calendar_with_schedule.R
import kotlinx.coroutines.launch

@Composable
fun DoubleBackToExitHandler(
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var lastBackPressedTime by remember { mutableLongStateOf(0L) }
    val toast = remember { Toast.makeText(context, context.getString(R.string.double_back_to_exit), Toast.LENGTH_SHORT) }

    BackHandler {
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
    }
}
