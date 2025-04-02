package com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.BuildConfig
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.notification.AlarmReceiver
import com.pdevjay.calendar_with_schedule.notification.AlarmRegisterWorker
import com.pdevjay.calendar_with_schedule.notification.AlarmScheduler
import com.pdevjay.calendar_with_schedule.notification.AlarmScheduler.getAlarmRequestCode
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.ScheduleEditType
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.states.ScheduleState
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleState())
    val state: StateFlow<ScheduleState> = _state
    private val _scheduleMap = MutableStateFlow<Map<LocalDate, List<BaseSchedule>>>(emptyMap())

    init {
        scheduleRepository.scheduleMap
            .onEach { newScheduleMap ->
                Log.e("viemodel", "✅ scheduleMap 자동 업데이트됨 from: ${Thread.currentThread().name}")

                _scheduleMap.value = newScheduleMap
                Log.e("viemodel", "newScheduleMap 갱싱 ${newScheduleMap.size}")
            }
            .launchIn(viewModelScope)

        scheduleRepository.scheduleMapForDebug
            .filter { it.isNotEmpty() }
            .onEach { scheduleMap ->
                AlarmScheduler.logRegisteredAlarms(context, scheduleMap)
            }
            .launchIn(viewModelScope)
    }
    fun processIntent(intent: ScheduleIntent) {
        viewModelScope.launch {
            when (intent) {
                is ScheduleIntent.AddSchedule -> {
                    scheduleRepository.saveSchedule(intent.schedule)
//                    AlarmScheduler.scheduleAlarm(context, intent.schedule)  // 알람 예약
                    AlarmScheduler.scheduleMultipleAlarms(context, intent.schedule)  // 알람 예약

                }

                is ScheduleIntent.UpdateSchedule -> {
                    scheduleRepository.updateSchedule(intent.newSchedule, intent.editType, intent.isOnlyContentChanged)
                    when (intent.editType) {
                        ScheduleEditType.ONLY_THIS_EVENT -> {
                            AlarmScheduler.cancelAlarm(context, intent.oldSchedule)
                            AlarmScheduler.scheduleAlarm(context, intent.newSchedule)  // 알람 예약
                        }
                        ScheduleEditType.THIS_AND_FUTURE -> {
                            Log.e("viemodel", "ScheduleIntent.UpdateSchedule 호출")
                            AlarmScheduler.cancelThisAndFutureAlarms(context, intent.oldSchedule, _scheduleMap.value)
//                            AlarmScheduler.scheduleMultipleAlarms(context, intent.newSchedule)  // 알람 예약
                            AlarmRegisterWorker.enqueue(context, intent.newSchedule)

                        }

                        ScheduleEditType.ALL_EVENTS -> TODO()
                    }
//                    AlarmScheduler.scheduleMultipleAlarms(context, intent.newSchedule)  // 알람 예약
                }

                is ScheduleIntent.DeleteSchedule -> {
                    scheduleRepository.deleteSchedule(intent.schedule, intent.editType)

                    when (intent.editType) {
                        ScheduleEditType.ONLY_THIS_EVENT -> {
                            AlarmScheduler.cancelAlarm(context, intent.schedule)
                        }
                        ScheduleEditType.THIS_AND_FUTURE -> {
                            // update/delete 후에 바로 알람 취소 작업을 하려면
                            viewModelScope.launch {
                                delay(100) // 약간의 delay로 최신화 시간 확보
                                val latestMap = scheduleRepository.scheduleMap.value
                                AlarmScheduler.cancelThisAndFutureAlarms(context, intent.schedule, latestMap)
                            }
                        }
                        ScheduleEditType.ALL_EVENTS -> {

                        }
                    }
                }
            }
        }
    }
}
