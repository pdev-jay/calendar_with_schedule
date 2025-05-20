package com.pdevjay.calendar_with_schedule.data.remote

import com.pdevjay.calendar_with_schedule.screens.calendar.data.HolidayData
import com.pdevjay.calendar_with_schedule.screens.settings.data.VersionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DataApiService {
    @GET("get_updated_holidays")
    suspend fun getUpdatedHolidays(@Query("since") updatedAfter: String): List<HolidayData>

    @GET("get_app_version")
    suspend fun getAppVersion(): VersionResponse
}
