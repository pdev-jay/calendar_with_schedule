package com.pdevjay.calendar_with_schedule.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.utils.JsonUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.net.URLEncoder
import java.time.LocalDate

enum class AlarmAction {
    REGISTER_SCHEDULE,
    REGISTER_UPDATED_SCHEDULE,
    REGISTER_SCHEDULEMAP,
    REGISTER_SCHEDULEMAP_BY_BRANCH,
    CANCEL_ALARM,
    CANCEL_ALARM_THIS_AND_FUTURE;

    companion object {
        fun fromString(value: String?): AlarmAction? {
            return entries.find { it.name == value }
        }
    }
}

enum class ScheduleType {
    SCHEDULE_DATA,
    RECURRING_DATA
}

@HiltWorker
class AlarmRegisterWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ScheduleRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val action = AlarmAction.fromString(inputData.getString("action")) ?: return Result.failure()

        when (action){
            AlarmAction.REGISTER_SCHEDULE -> {
                val type = inputData.getString("scheduleType") ?: return Result.failure()

                val scheduleJson = inputData.getString("scheduleJson") ?: return Result.failure()

                val schedule: BaseSchedule = when (ScheduleType.valueOf(type)) {
                    ScheduleType.SCHEDULE_DATA -> JsonUtils.parseScheduleDataJson(scheduleJson)
                    ScheduleType.RECURRING_DATA -> JsonUtils.parseRecurringScheduleJson(scheduleJson)
                }
                AlarmScheduler.scheduleMultipleAlarms(applicationContext, schedule)
            }
            AlarmAction.REGISTER_UPDATED_SCHEDULE -> {
                val scheduleJson = inputData.getString("scheduleJson") ?: return Result.failure()

                val schedule = JsonUtils.parseRecurringScheduleJson(scheduleJson)
                AlarmScheduler.scheduleAlarm(applicationContext, schedule)
            }
            AlarmAction.REGISTER_SCHEDULEMAP -> {
                val scheduleMap = repository.scheduleMap.value
                AlarmScheduler.scheduleAlarmsFromScheduleMap(applicationContext, scheduleMap)
            }
            AlarmAction.REGISTER_SCHEDULEMAP_BY_BRANCH -> {
                val scheduleMap = repository.scheduleMap.value
                val oldScheduleJson = inputData.getString("oldScheduleJson") ?: return Result.failure()
                val oldSchedule = JsonUtils.parseRecurringScheduleJson(oldScheduleJson)
                val newScheduleJson = inputData.getString("newScheduleJson") ?: return Result.failure()
                val newSchedule = JsonUtils.parseRecurringScheduleJson(newScheduleJson)
                newSchedule.branchId?.let {
                    AlarmScheduler.cancelThisAndFutureAlarms(applicationContext, oldSchedule, scheduleMap)
                    Log.e("AlarmLogger", "✅ WorkManager 통해 알람 취소 완료 ")
                    AlarmScheduler.scheduleAlarmsForBranchId(applicationContext, scheduleMap,
                        it
                    )
                }
            }
            AlarmAction.CANCEL_ALARM -> {
                val scheduleJson = inputData.getString("scheduleJson") ?: return Result.failure()
                val schedule = JsonUtils.parseRecurringScheduleJson(scheduleJson)
                AlarmScheduler.cancelAlarm(applicationContext, schedule)
            }
            AlarmAction.CANCEL_ALARM_THIS_AND_FUTURE -> {
                val scheduleMap = repository.scheduleMap.value
                val scheduleJson = inputData.getString("scheduleJson") ?: return Result.failure()
                val schedule = JsonUtils.parseRecurringScheduleJson(scheduleJson)
                AlarmScheduler.cancelThisAndFutureAlarms(applicationContext, schedule, scheduleMap)

            }
        }

        Log.e("AlarmLogger", "✅ WorkManager 통해 알람 등록 완료 ")
        AlarmScheduler.printAllRegisteredAlarms()
        return Result.success()
    }

    companion object {
        fun enqueueRegisterAlarmForSchedule(context: Context, schedule: BaseSchedule) {
            val json = URLEncoder.encode(JsonUtils.gson.toJson(schedule), "UTF-8")

            val type = when (schedule) {
                is RecurringData -> ScheduleType.RECURRING_DATA.name
                is ScheduleData -> ScheduleType.SCHEDULE_DATA.name
                else -> { throw IllegalArgumentException("Unknown schedule type") }
            }

            val input = workDataOf(
                "action" to AlarmAction.REGISTER_SCHEDULE.name,
                "scheduleJson" to json,
                "scheduleType" to type
            )
            enqueueWorker(context, input)
        }
        fun enqueueRegisterUpdatedAlarmForSchedule(context: Context, schedule: RecurringData) {
            val json = URLEncoder.encode(JsonUtils.gson.toJson(schedule), "UTF-8")

            val input = workDataOf(
                "action" to AlarmAction.REGISTER_UPDATED_SCHEDULE.name,
                "scheduleJson" to json,
            )
            enqueueWorker(context, input)
        }

        fun enqueueRegisterAlarmForScheduleMap(context: Context) {
            val input = workDataOf(
                "action" to AlarmAction.REGISTER_SCHEDULEMAP.name,
            )
            enqueueWorker(context, input)
        }

        fun enqueueRegisterAlarmForScheduleMapByBranch(
            context: Context,
            oldSchedule: RecurringData,
            newSchedule: RecurringData,
        ) {
            val jsonOldSchedule = URLEncoder.encode(JsonUtils.gson.toJson(oldSchedule), "UTF-8")
            val jsonNewSchedule = URLEncoder.encode(JsonUtils.gson.toJson(newSchedule), "UTF-8")
            val input = workDataOf(
                "action" to AlarmAction.REGISTER_SCHEDULEMAP_BY_BRANCH.name,
                "oldScheduleJson" to jsonOldSchedule,
                "newScheduleJson" to jsonNewSchedule,
            )
            enqueueWorker(context, input)
        }

        fun enqueueCancelAlarm(context: Context, schedule: RecurringData) {
            val json = URLEncoder.encode(JsonUtils.gson.toJson(schedule), "UTF-8")

            val input = workDataOf(
                "action" to AlarmAction.CANCEL_ALARM.name,
                "scheduleJson" to json,
            )
            enqueueWorker(context, input)
        }
        fun enqueueCancelAlarmThisAndFuture(context: Context, schedule: RecurringData) {
            val json = URLEncoder.encode(JsonUtils.gson.toJson(schedule), "UTF-8")

            val input = workDataOf(
                "action" to AlarmAction.CANCEL_ALARM_THIS_AND_FUTURE.name,
                "scheduleJson" to json,
            )
            enqueueWorker(context, input)
        }

        private fun enqueueWorker(context: Context, input: Data) {
            val work = OneTimeWorkRequestBuilder<AlarmRegisterWorker>()
                .setInputData(input)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueue(work)
        }

    }
}

