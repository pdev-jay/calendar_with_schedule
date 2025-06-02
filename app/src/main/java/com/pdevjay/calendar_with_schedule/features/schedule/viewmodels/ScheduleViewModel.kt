package com.pdevjay.calendar_with_schedule.features.schedule.viewmodels

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.works.AlarmRegisterWorker
import com.pdevjay.calendar_with_schedule.features.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.features.schedule.enums.AlarmOption
import com.pdevjay.calendar_with_schedule.features.schedule.enums.ScheduleEditType
import com.pdevjay.calendar_with_schedule.features.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.features.schedule.states.ScheduleState
import com.pdevjay.calendar_with_schedule.features.widget.SchedyWidget
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

    fun processIntent(intent: ScheduleIntent) {
        viewModelScope.launch {
            try {
                when (intent) {
                    is ScheduleIntent.AddSchedule -> {

                        scheduleRepository.saveSchedule(intent.schedule)

                        if (intent.schedule.alarmOption != AlarmOption.NONE) {
                            AlarmRegisterWorker.enqueueRegisterAlarmForSchedule(
                                context,
                                intent.schedule
                            )
                        }
                    }

                    is ScheduleIntent.UpdateSchedule -> {

                        scheduleRepository.updateSchedule(
                            intent.newSchedule,
                            intent.editType,
                            intent.isOnlyContentChanged
                        )

                        if (intent.newSchedule.alarmOption != AlarmOption.NONE) {
                            when (intent.editType) {
                                ScheduleEditType.ONLY_THIS_EVENT -> {
                                    AlarmRegisterWorker.enqueueRegisterUpdatedAlarmForSchedule(
                                        context,
                                        intent.newSchedule
                                    )
                                }

                                ScheduleEditType.THIS_AND_FUTURE -> {
                                    AlarmRegisterWorker.enqueueRegisterAlarmForScheduleMapByBranch(
                                        context,
                                        intent.oldSchedule,
                                        intent.newSchedule
                                    )
                                }

                                ScheduleEditType.ALL_EVENTS -> TODO()
                            }
                        }
                    }

                    is ScheduleIntent.DeleteSchedule -> {

                        scheduleRepository.deleteSchedule(intent.schedule, intent.editType)

                        when (intent.editType) {
                            ScheduleEditType.ONLY_THIS_EVENT -> {
                                AlarmRegisterWorker.enqueueCancelAlarm(context, intent.schedule)
                            }

                            ScheduleEditType.THIS_AND_FUTURE -> {
                                AlarmRegisterWorker.enqueueCancelAlarmThisAndFuture(
                                    context,
                                    intent.schedule
                                )
                            }

                            ScheduleEditType.ALL_EVENTS -> {

                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ScheduleVM", "processIntent 중 오류: ${e.message}", e)
            } finally {
                Log.e("widget", "before SchedyWidget().updateAll")
//                SchedyWidget().updateAll(context)
                Log.e("widget", "after SchedyWidget().updateAll")
            }
        }
    }
}
