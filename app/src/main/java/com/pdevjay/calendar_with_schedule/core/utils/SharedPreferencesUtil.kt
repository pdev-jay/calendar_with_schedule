package com.pdevjay.calendar_with_schedule.core.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesUtil {
    private const val PREF_NAME = "my_prefs"
    const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
    const val KEY_FIRST_LAUNCH = "first_launch"
    const val KEY_SHOW_LUNAR_DATE = "show_lunar_date"
    const val KEY_HOLIDAY_SYNC = "holiday_sync"
    const val KEY_FIRST_HOLIDAY_SYNC = "holiday_sync_first"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun putString(context: Context, key: String, value: String?) {
        getPrefs(context).edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String, defaultValue: String? = null): String? {
        return getPrefs(context).getString(key, defaultValue)
    }

    fun putBoolean(context: Context, key: String, value: Boolean) {
        getPrefs(context).edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return getPrefs(context).getBoolean(key, defaultValue)
    }

    fun putInt(context: Context, key: String, value: Int) {
        getPrefs(context).edit().putInt(key, value).apply()
    }

    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        return getPrefs(context).getInt(key, defaultValue)
    }

    fun remove(context: Context, key: String) {
        getPrefs(context).edit().remove(key).apply()
    }

    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
