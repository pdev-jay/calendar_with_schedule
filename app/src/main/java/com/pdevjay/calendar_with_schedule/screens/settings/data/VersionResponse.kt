package com.pdevjay.calendar_with_schedule.screens.settings.data

import com.google.gson.annotations.SerializedName

data class VersionResponse(
    @SerializedName("version")
    val version: String,

    @SerializedName("app_url")
    val appUrl: String
)
