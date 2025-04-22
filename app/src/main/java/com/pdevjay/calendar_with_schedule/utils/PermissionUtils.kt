package com.pdevjay.calendar_with_schedule.utils

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
import androidx.core.content.ContextCompat

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
            SharedPreferencesUtil.putBoolean(context,"notification_enabled", isGranted)
        }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                        onPermissionGranted()
                        SharedPreferencesUtil.putBoolean(context,"notification_enabled", true)
                    }
                    else -> {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                // TIRAMISU 미만은 자동 허용
                val firstLaunch = SharedPreferencesUtil.getBoolean(context, "first_launch", true)
                if (firstLaunch) {
                    SharedPreferencesUtil.putBoolean(context, "notification_enabled", true)
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
            Text("알림 권한 필요")
        },
        text = {
            Text("알림 기능을 사용하려면 설정에서 알림 권한을 허용해주세요.")
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
                Text("설정으로 이동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}