package com.pdevjay.calendar_with_schedule.notification

import android.content.Context
import android.util.Log
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.utils.JsonUtils
import java.time.LocalDate

object AlarmPersistenceUtils {

    private const val PREF_NAME = "alarm_prefs"
    private const val KEY_SCHEDULE_MAP = "schedule_map"

    fun persistScheduleMap(
        context: Context,
        scheduleMap: Map<LocalDate, List<RecurringData>>
    ) {
        try {
            val today = LocalDate.now()
            val until = today.plusDays(30)


            val json = JsonUtils.gson.toJson(scheduleMap.filterKeys { it in today..until })
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SCHEDULE_MAP, json)
                .apply()
            Log.i("AlarmPersistence", "✅ scheduleMap 저장 완료 (${scheduleMap.size}일)")
        } catch (e: Exception) {
            Log.e("AlarmPersistence", "❌ scheduleMap 저장 실패", e)
        }
    }

    fun loadPersistedScheduleMap(context: Context): Map<LocalDate, List<RecurringData>> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SCHEDULE_MAP, null) ?: return emptyMap()

        return try {
            JsonUtils.parseScheduleMapJson(json)
        } catch (e: Exception) {
            Log.e("AlarmPersistence", "❌ scheduleMap 불러오기 실패", e)
            emptyMap()
        }
    }

    fun clearPersistedScheduleMap(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_SCHEDULE_MAP)
            .apply()
        Log.i("AlarmPersistence", "🧹 저장된 scheduleMap 초기화 완료")
    }
}
