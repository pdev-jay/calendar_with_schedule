package com.pdevjay.calendar_with_schedule.features.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.features.schedule.GroupContainer
import com.pdevjay.calendar_with_schedule.features.schedule.SwitchSelector
import com.pdevjay.calendar_with_schedule.core.utils.extensions.NotificationPermissionDeniedDialog
import com.pdevjay.calendar_with_schedule.core.utils.extensions.PermissionUtils
import com.pdevjay.calendar_with_schedule.core.utils.SharedPreferencesUtil
import com.pdevjay.calendar_with_schedule.core.utils.extensions.AppVersionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
){
    val context = LocalContext.current

    val notificationEnabled = SharedPreferencesUtil.getBoolean(context, SharedPreferencesUtil.KEY_NOTIFICATION_ENABLED, false)
    val notificationPermissionGranted = PermissionUtils.hasNotificationPermission(context)
    var isNotificationEnabled by remember { mutableStateOf(notificationEnabled && notificationPermissionGranted) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val isShowLunarDate = SharedPreferencesUtil.getBoolean(context, SharedPreferencesUtil.KEY_SHOW_LUNAR_DATE, false)
    var showLunarDate by remember { mutableStateOf(isShowLunarDate) }

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.setting)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close"
                            )
                        }
                    }
                )
        }
    ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GroupContainer {
                    SwitchSelector(
                        label = stringResource(R.string.notification),
                        option = isNotificationEnabled,
                        onSwitch = {
                            if (PermissionUtils.hasNotificationPermission(context)) {
                                isNotificationEnabled = it
                            } else {
                                isNotificationEnabled = false
                                showPermissionDialog = true
                            }
                            SharedPreferencesUtil.putBoolean(context, SharedPreferencesUtil.KEY_NOTIFICATION_ENABLED, it)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                GroupContainer {
                    SwitchSelector(
                        label = stringResource(R.string.show_lunar_date),
                        option = showLunarDate,
                        onSwitch = {
                            showLunarDate = it
                            SharedPreferencesUtil.putBoolean(context, SharedPreferencesUtil.KEY_SHOW_LUNAR_DATE, it)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                GroupContainer {
                    SwitchSelector(
                        label = stringResource(R.string.show_lunar_date),
                        option = showLunarDate,
                        onSwitch = {
                            showLunarDate = it
                            SharedPreferencesUtil.putBoolean(context, SharedPreferencesUtil.KEY_SHOW_LUNAR_DATE, it)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                GroupContainer {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(text = stringResource(R.string.inquiry), color = MaterialTheme.colorScheme.onSurfaceVariant,fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = {
                            sendFeedbackEmail(context)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "Send email"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(text = "Schedy", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "v${AppVersionUtils.currentAppVersion}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        if (showPermissionDialog) {
            NotificationPermissionDeniedDialog(onDismiss = { showPermissionDialog = false })
        }
    }
}

fun sendFeedbackEmail(context: Context) {
    val packageManager = context.packageManager
    val packageName = context.packageName

    val appVersion = try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toString()
        }
        "$versionName ($versionCode)"
    } catch (e: Exception) {
        "Unknown"
    }

    val deviceInfoFormatted = context.getString(
        R.string.email_body_template,
        appVersion,
        Build.MODEL,
        Build.MANUFACTURER,
        Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT
    )

    val recipient = context.getString(R.string.email_recipient)
    val subject = context.getString(R.string.email_subject)
    val body = deviceInfoFormatted

    val uri = Uri.parse("mailto:$recipient?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
    val intent = Intent(Intent.ACTION_SENDTO, uri)

    if (intent.resolveActivity(packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, context.getString(R.string.no_email_apps), Toast.LENGTH_SHORT).show()
    }
}

