package com.pdevjay.calendar_with_schedule.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import java.lang.reflect.Type
import java.net.URLDecoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


object JsonUtils {
//    val gson: Gson = GsonBuilder()
//        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
//        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
//        .create()

    // 지연 초기화된 Gson 인스턴스
    val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            .create()
    }

    fun parseRecurringScheduleJson(scheduleJson: String): RecurringData {
        return gson.fromJson(URLDecoder.decode(scheduleJson, "UTF-8"), RecurringData::class.java)
    }

    fun parseScheduleDataJson(scheduleJson: String): ScheduleData {
        return gson.fromJson(URLDecoder.decode(scheduleJson, "UTF-8"), ScheduleData::class.java)
    }
}

//  LocalDate Adapter
class LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(formatter) ?: "")
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDate {
        return json?.asString?.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it, formatter) } ?: LocalDate.now()
    }
}

//  LocalTime Adapter
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

class RecurringDataAdapter : JsonSerializer<RecurringData>, JsonDeserializer<RecurringData> {
    override fun serialize(src: RecurringData, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return context.serialize(src)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RecurringData {
        return context.deserialize(json, RecurringData::class.java)
    }
}

class ScheduleDataAdapter : JsonSerializer<ScheduleData>, JsonDeserializer<ScheduleData> {
    override fun serialize(src: ScheduleData, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return context.serialize(src)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ScheduleData {
        return context.deserialize(json, ScheduleData::class.java)
    }
}


class ScheduleMapAdapter : JsonSerializer<Map<LocalDate, List<RecurringData>>>,
    JsonDeserializer<Map<LocalDate, List<RecurringData>>> {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun serialize(
        src: Map<LocalDate, List<RecurringData>>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        src?.forEach { (key, value) ->
            val keyString = key.format(dateFormatter)
            jsonObject.add(keyString, context.serialize(value))
        }
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): Map<LocalDate, List<RecurringData>> {
        val result = mutableMapOf<LocalDate, List<RecurringData>>()
        val jsonObject = json?.asJsonObject ?: return result

        for ((key, value) in jsonObject.entrySet()) {
            val date = LocalDate.parse(key, dateFormatter)
            val listType = object : TypeToken<List<RecurringData>>() {}.type
            val schedules: List<RecurringData> = context.deserialize(value, listType)
            result[date] = schedules
        }

        return result
    }
}
