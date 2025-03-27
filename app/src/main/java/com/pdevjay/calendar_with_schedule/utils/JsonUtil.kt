package com.pdevjay.calendar_with_schedule.utils

import com.google.gson.*
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import java.lang.reflect.Type
import java.net.URLDecoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


object JsonUtils {
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(BaseSchedule::class.java, BaseScheduleTypeAdapter()) // ✅ 커스텀 TypeAdapter 등록
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
        .create()

    fun parseScheduleJson(scheduleJson: String): RecurringData {
        return gson.fromJson(URLDecoder.decode(scheduleJson, "UTF-8"), RecurringData::class.java)
    }
}

// 🔹 LocalDate Adapter
class LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(formatter) ?: "")
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDate {
        return json?.asString?.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it, formatter) } ?: LocalDate.now()
    }
}

// 🔹 LocalTime Adapter
class LocalTimeAdapter : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

    override fun serialize(src: LocalTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(formatter) ?: "")
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalTime {
        return json?.asString?.takeIf { it.isNotEmpty() }?.let { LocalTime.parse(it, formatter) } ?: LocalTime.MIN
    }
}

class BaseScheduleTypeAdapter : JsonDeserializer<BaseSchedule>, JsonSerializer<BaseSchedule> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): BaseSchedule {
        val jsonObject = json.asJsonObject

        return if (jsonObject.has("originalEventId")) { // `RecurringData`의 필드 확인
            context.deserialize(json, RecurringData::class.java)
        } else {
            context.deserialize(json, ScheduleData::class.java)
        }
    }

    override fun serialize(src: BaseSchedule, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return context.serialize(src)
    }
}
