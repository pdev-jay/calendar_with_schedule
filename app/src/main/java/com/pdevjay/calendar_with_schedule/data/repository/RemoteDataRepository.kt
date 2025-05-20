package com.pdevjay.calendar_with_schedule.data.repository

import android.content.Context
import com.pdevjay.calendar_with_schedule.data.database.HolidayDao
import com.pdevjay.calendar_with_schedule.data.remote.DataApiService
import com.pdevjay.calendar_with_schedule.screens.calendar.data.HolidayData
import com.pdevjay.calendar_with_schedule.screens.calendar.data.toEntity
import com.pdevjay.calendar_with_schedule.utils.SharedPreferencesUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataRepository @Inject constructor(
    private val api: DataApiService,
    private val dao: HolidayDao,
    @ApplicationContext private val context: Context,
    ) {

    suspend fun refreshHolidays() {
        val lastSync = SharedPreferencesUtil.getString(context, SharedPreferencesUtil.KEY_HOLIDAY_SYNC, "2000-01-01T00:00:00Z") ?: "2000-01-01T00:00:00Z"

        val holidays = api.getUpdatedHolidays(lastSync)

        if (holidays.isNotEmpty()) {
            dao.insertAll(holidays.map { it.toEntity() })

            val latestUpdateTime = holidays.maxOfOrNull { it.updatedAt } ?: lastSync
            SharedPreferencesUtil.putString(context, SharedPreferencesUtil.KEY_HOLIDAY_SYNC, latestUpdateTime)
        }

    }

    suspend fun getLocalHolidays(): List<HolidayData> {
        return dao.getAll().map { it.toModel() }
    }
}
