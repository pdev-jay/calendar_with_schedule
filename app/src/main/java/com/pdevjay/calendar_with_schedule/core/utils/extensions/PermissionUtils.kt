package com.pdevjay.calendar_with_schedule.core.utils.extensions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.core.utils.SharedPreferencesUtil

object PermissionUtils {
    @Composable
    fun EnsureNotificationPermission(
        onPermissionGranted: () -> Unit = {},
        onPermissionDenied: () -> Unit = {}
    ) {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
            SharedPreferencesUtil.putBoolean(
                context,
                SharedPreferencesUtil.KEY_NOTIFICATION_ENABLED,
                isGranted
            )
        }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                        onPermissionGranted()
                        SharedPreferencesUtil.putBoolean(
                            context,
                            SharedPreferencesUtil.KEY_NOTIFICATION_ENABLED,
                            true
                        )
                    }
                    else -> {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                // TIRAMISU 미만은 자동 허용
                val firstLaunch = SharedPreferencesUtil.getBoolean(
                    context,
                    SharedPreferencesUtil.KEY_FIRST_LAUNCH,
                    true
                )
                if (firstLaunch) {
                    SharedPreferencesUtil.putBoolean(
                        context,
                        SharedPreferencesUtil.KEY_NOTIFICATION_ENABLED,
                        true
                    )
                }
                onPermissionGranted()
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // TIRAMISU 미만은 기본적으로 허용
        }
    }
}

@Composable
fun NotificationPermissionDeniedDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.notification_dialog_title))
        },
        text = {
            Text(stringResource(R.string.notification_dialog_body))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // 설정 화면으로 이동
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.notification_dialog_go_to_setting))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}