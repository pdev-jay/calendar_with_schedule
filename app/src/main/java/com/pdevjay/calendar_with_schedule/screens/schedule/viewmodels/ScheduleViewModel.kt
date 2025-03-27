package com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.notification.AlarmScheduler
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.ScheduleEditType
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.states.ScheduleState
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleState())
    val state: StateFlow<ScheduleState> = _state

    fun processIntent(intent: ScheduleIntent) {
        viewModelScope.launch {
            when (intent) {
                is ScheduleIntent.AddSchedule -> {
                    scheduleRepository.saveSchedule(intent.schedule)
                    AlarmScheduler.scheduleAlarm(context, intent.schedule)  // 알람 예약
                }
                is ScheduleIntent.UpdateFutureSchedule -> {
                    scheduleRepository.saveFutureScheduleChange(intent.schedule)
                    AlarmScheduler.scheduleAlarm(context, intent.schedule)  // 알람 예약
                }
                is ScheduleIntent.UpdateSingleSchedule -> {
                    scheduleRepository.saveSingleScheduleChange(intent.schedule)
                    AlarmScheduler.scheduleAlarm(context, intent.schedule)  // 알람 예약
                }
                is ScheduleIntent.DeleteFutureSchedule -> {
                    scheduleRepository.deleteFutureSchedule(intent.schedule)
                }
                is ScheduleIntent.DeleteSingleSchedule -> {
                    scheduleRepository.deleteSingleSchedule(intent.schedule)
                }

                is ScheduleIntent.UpdateFutureRecurringSchedule -> {
                    scheduleRepository.saveFutureRecurringScheduleChange(intent.schedule)
                    AlarmScheduler.scheduleAlarm(context, intent.schedule)  // 알람 예약
                }
                is ScheduleIntent.UpdateSingleRecurringSchedule -> {
                    scheduleRepository.saveSingleRecurringScheduleChange(intent.schedule)
                    AlarmScheduler.scheduleAlarm(context, intent.schedule)  // 알람 예약
                }

                is ScheduleIntent.DeleteFutureRecurringSchedule -> {
                    scheduleRepository.deleteFutureRecurringSchedule(intent.schedule)
                }

                is ScheduleIntent.DeleteSingleRecurringSchedule -> {
                    scheduleRepository.deleteRecurringSchedule(intent.schedule)
                }

                is ScheduleIntent.UpdateSchedule -> {
                    scheduleRepository.updateSchedule(intent.schedule, intent.editType, intent.isOnlyContentChanged)
//                    when (intent.schedule){
//                        is ScheduleData -> {
//                            when (intent.editType){
//                                ScheduleEditType.ONLY_THIS_EVENT -> scheduleRepository.saveSingleScheduleChange(intent.schedule)
//                                ScheduleEditType.THIS_AND_FUTURE -> scheduleRepository.saveFutureScheduleChange(intent.schedule)
//                                ScheduleEditType.ALL_EVENTS -> TODO()
//                            }
//                        }
//
//                        is RecurringData -> {
//                            when (intent.editType){
//                                ScheduleEditType.ONLY_THIS_EVENT -> scheduleRepository.saveSingleRecurringScheduleChange(intent.schedule.copy(repeatType = RepeatType.NONE))
//                                ScheduleEditType.THIS_AND_FUTURE -> scheduleRepository.saveFutureRecurringScheduleChange(intent.schedule)
//                                ScheduleEditType.ALL_EVENTS -> TODO()
//                            }
//                        }
//                    }
                    AlarmScheduler.scheduleAlarm(context, intent.schedule)  // 알람 예약
                }

                is ScheduleIntent.DeleteSchedule -> {
                    scheduleRepository.deleteSchedule(intent.schedule, intent.editType)
//                    when (intent.editType) {
//                        ScheduleEditType.ONLY_THIS_EVENT -> {
//                            when (intent.schedule) {
//                                is ScheduleData -> scheduleRepository.deleteSingleSchedule(intent.schedule)
//                                is RecurringData -> scheduleRepository.deleteRecurringSchedule(
//                                    intent.schedule
//                                )
//                            }
//                        }
//
//                        ScheduleEditType.THIS_AND_FUTURE -> {
//                            when (intent.schedule) {
//                                is ScheduleData -> scheduleRepository.deleteFutureSchedule(intent.schedule)
//                                is RecurringData -> scheduleRepository.deleteFutureRecurringSchedule(
//                                    intent.schedule
//                                )
//                            }
//                        }

//                        ScheduleEditType.ALL_EVENTS -> TODO()
//                    }
                }
            }
        }
    }
}
