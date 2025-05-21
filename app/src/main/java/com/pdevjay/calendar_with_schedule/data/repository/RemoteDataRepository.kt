package com.pdevjay.calendar_with_schedule.data.repository

import android.content.Context
import android.util.Log
import com.pdevjay.calendar_with_schedule.data.database.HolidayDao
import com.pdevjay.calendar_with_schedule.data.remote.DataApiService
import com.pdevjay.calendar_with_schedule.screens.calendar.data.HolidayData
import com.pdevjay.calendar_with_schedule.screens.calendar.data.toEntity
import com.pdevjay.calendar_with_schedule.utils.SharedPreferencesUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataRepository @Inject constructor(
    private val api: DataApiService,
    private val dao: HolidayDao,
    @ApplicationContext private val context: Context,
    ) {

    suspend fun refreshHolidays() {
        val lastSync = SharedPreferencesUtil.getString(
            context,
            SharedPreferencesUtil.KEY_HOLIDAY_SYNC,
            "2000-01-01T00:00:00Z"
        ) ?: "2000-01-01T00:00:00Z"

        Log.d("HolidaySync", "🔍 lastSync = $lastSync")

        val holidays = api.getUpdatedHolidays(lastSync)

        Log.d("HolidaySync", "📦 조회된 holiday 수: ${holidays.size}")
//        holidays.forEach {
//            Log.d("HolidaySync", "📄 holiday: date=${it.date}, name=${it.name}, updatedAt=${it.updatedAt}")
//        }

        if (holidays.isNotEmpty()) {
            dao.insertAll(holidays.map { it.toEntity() })

            val latestUpdateTime = holidays.maxOfOrNull { it.updatedAt } ?: lastSync
            Log.d("HolidaySync", "✅ size = ${holidays.size}")
            Log.d("HolidaySync", "✅ 최신 updatedAt = $latestUpdateTime")

            SharedPreferencesUtil.putString(
                context,
                SharedPreferencesUtil.KEY_HOLIDAY_SYNC,
                latestUpdateTime
            )
        } else {
            Log.d("HolidaySync", "🔕 업데이트할 holiday 없음.")
        }
    }

    suspend fun getLocalHolidays(): List<HolidayData> {
        return dao.getAll().map { it.toModel() }
    }

//    suspend fun getHolidaysForMonths(months: List<String>): List<HolidayData> {
//        return dao.getHolidaysForMonths(months).map { it.toModel() }
//    }
}
