package com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.notification.AlarmRegisterWorker
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.ScheduleEditType
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.states.ScheduleState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val _scheduleMap = MutableStateFlow<Map<LocalDate, List<RecurringData>>>(emptyMap())

    init {
//        scheduleRepository.scheduleMap
//            .onEach { newScheduleMap ->
//                Log.e("viemodel", "✅ scheduleMap 자동 업데이트됨 from: ${Thread.currentThread().name}")
//
//                _scheduleMap.value = newScheduleMap
//                Log.e("viemodel", "newScheduleMap 갱싱 ${newScheduleMap.size}")
//            }
//            .launchIn(viewModelScope)
//
    }
    fun processIntent(intent: ScheduleIntent) {
        viewModelScope.launch {
            when (intent) {
                is ScheduleIntent.AddSchedule -> {
                    scheduleRepository.saveSchedule(intent.schedule)
                    AlarmRegisterWorker.enqueueRegisterAlarmForSchedule(context, intent.schedule)

                }

                is ScheduleIntent.UpdateSchedule -> {
                    scheduleRepository.updateSchedule(intent.newSchedule, intent.editType, intent.isOnlyContentChanged)
                    when (intent.editType) {
                        ScheduleEditType.ONLY_THIS_EVENT -> {
                            AlarmRegisterWorker.enqueueRegisterUpdatedAlarmForSchedule(context, intent.newSchedule)
                        }
                        ScheduleEditType.THIS_AND_FUTURE -> {
                            AlarmRegisterWorker.enqueueRegisterAlarmForScheduleMapByBranch(context, intent.oldSchedule, intent.newSchedule)
                        }

                        ScheduleEditType.ALL_EVENTS -> TODO()
                    }
                }

                is ScheduleIntent.DeleteSchedule -> {
                    scheduleRepository.deleteSchedule(intent.schedule, intent.editType)

                    when (intent.editType) {
                        ScheduleEditType.ONLY_THIS_EVENT -> {
                            AlarmRegisterWorker.enqueueCancelAlarm(context, intent.schedule)
                        }
                        ScheduleEditType.THIS_AND_FUTURE -> {
                            AlarmRegisterWorker.enqueueCancelAlarmThisAndFuture(context, intent.schedule)
                        }
                        ScheduleEditType.ALL_EVENTS -> {

                        }
                    }
                }
            }
        }
    }
}
