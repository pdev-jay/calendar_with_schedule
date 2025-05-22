package com.pdevjay.calendar_with_schedule.core.utils.extensions

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.pdevjay.calendar_with_schedule.BuildConfig
import com.pdevjay.calendar_with_schedule.R
import com.pdevjay.calendar_with_schedule.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AppVersionUtils {

    fun checkAppVersion(activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getAppVersion()
                val latest = response.version
                val appUrl = response.appUrl
                val current = BuildConfig.VERSION_NAME
                val contents = response.contents
                Log.e("AppVersionUtil", "response : $response / current : $current / latest : $latest")

                if (isVersionOlder(current, latest)) {
                    withContext(Dispatchers.Main) {
                        showUpdateDialog(activity, latest, contents, appUrl)
                    }
                }
            } catch (e: Exception) {
                Log.e("AppVersionUtil", "버전 확인 실패: ${e.message}")
            }
        }
    }

    private fun isVersionOlder(current: String, latest: String): Boolean {
        return compareVersions(current, latest) < 0
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".")
        val parts2 = v2.split(".")
        val maxLength = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLength) {
            val p1 = parts1.getOrNull(i)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
            val p2 = parts2.getOrNull(i)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
            if (p1 != p2) return p1 - p2
        }
        return 0
    }

    private fun showUpdateDialog(activity: Activity, latestVersion: String, contents: String, appUrl: String? = null) {
        val context = activity.applicationContext
        val message = if (contents.isNotBlank()) {
            "최신 버전: $latestVersion\n\n$contents"
        } else {
            "최신 버전 ($latestVersion)으로 앱을 업데이트해주세요."
        }

        AlertDialog.Builder(activity)
            .setTitle(context.getString(R.string.need_update))
            .setMessage(message)
            .setPositiveButton(context.getString(R.string.update)) { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(appUrl)
                }
                activity.startActivity(intent)
            }
            .setNegativeButton(context.getString(android.R.string.cancel)) { dialog, _ ->
                dialog.dismiss() // 다이얼로그만 닫기
            }
            .setCancelable(false)
            .show()
    }
}
