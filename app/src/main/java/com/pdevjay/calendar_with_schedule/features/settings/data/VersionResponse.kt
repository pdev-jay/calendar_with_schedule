package com.pdevjay.calendar_with_schedule.features.settings.data

import com.google.gson.annotations.SerializedName

data class VersionResponse(
    @SerializedName("version")
    val version: String,

    @SerializedName("contents")
    val contents: String,

    @SerializedName("app_url")
    val appUrl: String
)
