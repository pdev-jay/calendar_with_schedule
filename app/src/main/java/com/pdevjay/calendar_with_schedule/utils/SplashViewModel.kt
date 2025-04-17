package com.pdevjay.calendar_with_schedule.utils

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {
    var isSplashRunning = mutableStateOf(true)

    init {
        viewModelScope.launch {
            delay(800L) // 최소 스플래시 유지 시간

            scheduleRepository.isScheduleMapReady
                .filter { it } // true일 때만
                .first() // 한 번만 받음
                .let {
                    isSplashRunning.value = false
                }
        }
    }
    fun finishSplash() {
        isSplashRunning.value = false
    }
}
