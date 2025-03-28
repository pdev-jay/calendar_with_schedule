package com.pdevjay.calendar_with_schedule.data.repository

import android.util.Log
import com.pdevjay.calendar_with_schedule.data.database.RecurringScheduleDao
import com.pdevjay.calendar_with_schedule.data.database.ScheduleDao
import com.pdevjay.calendar_with_schedule.data.entity.toRecurringData
import com.pdevjay.calendar_with_schedule.data.entity.toScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.resolveDisplayFieldsFromBranch
import com.pdevjay.calendar_with_schedule.screens.schedule.data.resolveDisplayOnly
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toMarkAsDeletedData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toNewBranchData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toSingleChangeData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.ScheduleEditType
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator.generateRepeatedDatesWithIndex
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator.generateRepeatedScheduleInstances
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val recurringScheduleDao: RecurringScheduleDao // 🔥 추가
) : ScheduleRepository {

    private val _scheduleMap = MutableStateFlow<Map<LocalDate, List<RecurringData>>>(emptyMap())
    override val scheduleMap: StateFlow<Map<LocalDate, List<RecurringData>>> = _scheduleMap

    private val _currentMonths = MutableStateFlow<List<YearMonth>>(emptyList()) // 🔹 현재 조회 중인 월 리스트
    val currentMonths: StateFlow<List<YearMonth>> = _currentMonths.asStateFlow()

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        Log.e("ScheduleRepository", "🔥 Created! hash=${this.hashCode()}")

        repositoryScope.launch {
            combine(
                scheduleDao.getSchedulesForMonths(
                    _currentMonths.value.map { it.toString() },
                    _currentMonths.value.minOrNull()?.toString() ?: YearMonth.now().toString(),
                    _currentMonths.value.maxOrNull()?.toString() ?: YearMonth.now().toString()
                ),
                recurringScheduleDao.getRecurringSchedulesForMonths(
                    _currentMonths.value.map { it.toString() },
                    _currentMonths.value.minOrNull()?.toString() ?: YearMonth.now().toString(),
                    _currentMonths.value.maxOrNull()?.toString() ?: YearMonth.now().toString()
                ),
                _currentMonths
            ) { _, _, months ->
                months
            }.distinctUntilChanged()
                .collectLatest { months ->
                    Log.e("ScheduleRepository", "📌 _currentMonths 변경 감지됨: ${months}")

                    if (months.isNotEmpty()) {
                        getSchedulesForMonths(months)
//                            .distinctUntilChanged()
                            .collect { newScheduleMap ->
                                _scheduleMap.value = newScheduleMap
                                Log.e("ScheduleRepository", "✅ scheduleMap 자동 업데이트됨: ${newScheduleMap.keys}")
                            }
                    }
                }
        }

    }

    override suspend fun loadSchedulesForMonths(months: List<YearMonth>) {
        _currentMonths.value = months // 🔥 `currentMonths` 를 갱신하면 자동으로 `scheduleMap` 업데이트됨
    }

    override fun getSchedulesForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<RecurringData>>> {
        val monthStrings = months.map { it.toString() }
        val maxMonth = months.maxOrNull()?.toString() ?: YearMonth.now().toString()
        val minMonth = months.minOrNull()?.toString() ?: YearMonth.now().toString()

        return combine(
            scheduleDao.getSchedulesForMonths(monthStrings, minMonth, maxMonth),
            recurringScheduleDao.getRecurringSchedulesForMonths(monthStrings, minMonth, maxMonth)
        ) { scheduleEntities, recurringEntities ->

            val originalSchedules = scheduleEntities.map { it.toScheduleData() }
            val recurringSchedules = recurringEntities.map { it.toRecurringData() }

            val allSchedules = mutableListOf<RecurringData>()

            // 🔹 (1) ScheduleData → RecurringData 변환 후 반복 인스턴스 생성
            originalSchedules.forEach { schedule ->
                val indicesToIgnore = recurringSchedules
                    .filter { it.branchId == schedule.branchId && (it.isDeleted || it.repeatType == RepeatType.NONE) }
                    .mapNotNull { it.repeatIndex }
                    .toSet()

                val indexedDates = generateRepeatedDatesWithIndex(
                    repeatType = schedule.repeatType,
                    startDate = schedule.start.date,
                    monthList = months,
                    indicesToIgnore = indicesToIgnore,
                    repeatUntil = schedule.repeatUntil
                )

                indexedDates.forEach { (index, date) ->
                    allSchedules.add(
                        generateRepeatedScheduleInstances(schedule, date, index)
                    )
                }
            }

            // 🔹 (2) 기존 RecurringData에서 파생된 반복들 처리 (분기 루트 기준)
            recurringSchedules.filter { it.isFirstSchedule }.forEach { branchRoot ->
                val children = recurringSchedules.filter { it.branchId == branchRoot.branchId }

                val indicesToIgnore = children
                    .filter { it.isDeleted || it.repeatType == RepeatType.NONE }
                    .mapNotNull { it.repeatIndex }
                    .toSet()


                val indexedDates = RepeatScheduleGenerator.generateRepeatedDatesWithIndex(
                    branchRoot.repeatType,
                    branchRoot.start.date,
                    monthList = months,
                    indicesToIgnore = indicesToIgnore,
                    repeatUntil = branchRoot.repeatUntil
                )

                indexedDates.forEach { (index, date) ->
                    allSchedules.add(
                        generateRepeatedScheduleInstances(branchRoot, date, index)
                    )
                }

            }

            // 🔹 (3) 단일 오버라이드 일정 (branch 없이 단독 저장된)
            allSchedules.addAll(
                recurringSchedules.filter {
                    it.repeatType == RepeatType.NONE && !it.isDeleted && !it.isFirstSchedule
                }
            )

            // 🔹 (4) resolveDisplayOnly() 로 반복 정보 덮어쓰기
            val branchRoots = allSchedules.filter { it.isFirstSchedule }.associateBy { it.branchId }

            val resolvedSchedules = allSchedules.map { item ->
                if (item.repeatType == RepeatType.NONE && !item.isFirstSchedule) {
                    val root = branchRoots[item.branchId]
                    if (root != null) item.resolveDisplayOnly(root) else item
                } else item
            }

            // 🔹 (5) 삭제 제외, 날짜 기준 정리
            val filtered = resolvedSchedules
                .filter { !it.isDeleted }
                .groupBy { it.start.date }
                .mapValues { it.value.sortedBy { item -> item.start.time } }

            // 🔹 (6) 빈 날짜 처리
            val validDates = months.flatMap { month -> (1..month.lengthOfMonth()).map { month.atDay(it) } }
            val result = validDates.associateWith { date -> filtered[date].orEmpty() }

            result.toSortedMap()
        }
    }


    override suspend fun updateSchedule(schedule: RecurringData, scheduleEditType: ScheduleEditType, isOnlyContentChanged: Boolean) {

        when (scheduleEditType){
            ScheduleEditType.ONLY_THIS_EVENT -> {
                // update 시도
                val row = recurringScheduleDao.markRecurringScheduleAsDeleted(schedule.id)
                // update 실패 시 isDeleted = true로 insert
                if (row == 0){
                    val deleted = schedule.toMarkAsDeletedData(schedule.originalRecurringDate)
                    recurringScheduleDao.insertRecurringSchedule(deleted.toRecurringScheduleEntity())
                }

                // 새롭게 수정되는 데이터 insert
                val overridden = schedule.toSingleChangeData()
                recurringScheduleDao.insertRecurringSchedule(overridden.toRecurringScheduleEntity())
            }
            ScheduleEditType.THIS_AND_FUTURE -> {
//                val previousRepeatDate = findPreviousRepeatDateFromScheduleMap(
//                    scheduleMap = scheduleMap.value,
//                    currentDate = schedule.originalRecurringDate,
//                    eventId = schedule.originalEventId
//                )
                val previousRepeatDate = findPreviousRepeatDateFromScheduleMapByIndex(
                    scheduleMap = scheduleMap.value,
                    currentIndex = schedule.repeatIndex ?: 0,
                    branchId = schedule.branchId ?: return
                )
                val newRepeatUntil = previousRepeatDate ?: schedule.start.date.minusDays(1)

                // ScheduleData에서 파생된 반복일정이면 schedules 테이블도 자름
                if (schedule.originatedFrom == schedule.originalEventId) {
                    scheduleDao.updateRepeatUntil(
                        branchId = schedule.branchId,
                        repeatUntil = newRepeatUntil.toString()
                    )
                }

                // RecurringData 루트인 경우 recurring 테이블도 자름
                recurringScheduleDao.updateRepeatUntil(
                    branchId = schedule.branchId,
                    repeatUntil = newRepeatUntil.toString()
                )

                val branched = if (schedule.isFirstSchedule) { schedule } else { schedule.toNewBranchData() }
                recurringScheduleDao.insertRecurringSchedule(branched.toRecurringScheduleEntity())

                if (isOnlyContentChanged){
                    recurringScheduleDao.updateContentOnly(schedule.toRecurringScheduleEntity())
                }
            }
            ScheduleEditType.ALL_EVENTS -> TODO()
        }
    }

    override suspend fun deleteSchedule(
        schedule: RecurringData,
        scheduleEditType: ScheduleEditType
    ) {
        when (scheduleEditType){
            ScheduleEditType.ONLY_THIS_EVENT -> {
                // update 시도
                val row = recurringScheduleDao.markRecurringScheduleAsDeleted(schedule.id)
                // update 실패 시 isDeleted = true로 insert
                if (row == 0){
                    val deleted = schedule.toMarkAsDeletedData(schedule.originalRecurringDate)
                    recurringScheduleDao.insertRecurringSchedule(deleted.toRecurringScheduleEntity())
                }
            }
            ScheduleEditType.THIS_AND_FUTURE -> {
                val previousRepeatDate = findPreviousRepeatDateFromScheduleMapByIndex(
                    scheduleMap = scheduleMap.value,
                    currentIndex = schedule.repeatIndex ?: 0,
                    branchId = schedule.branchId ?: return
                )
                val newRepeatUntil = previousRepeatDate ?: schedule.start.date.minusDays(1)

                // ScheduleData에서 파생된 반복일정이면 schedules 테이블도 자름
                if (schedule.originatedFrom == schedule.originalEventId) {
                    scheduleDao.updateRepeatUntil(
                        branchId = schedule.branchId ?: return,
                        repeatUntil = newRepeatUntil.toString()
                    )
                }

                // RecurringData 루트인 경우 recurring 테이블도 자름
                recurringScheduleDao.updateRepeatUntil(
                    branchId = schedule.branchId ?: return,
                    repeatUntil = newRepeatUntil.toString()
                )

                // 해당 recurring data의 original event id를 가진 모든 recurring data를 기준 날짜 이후로 삭제
                recurringScheduleDao.deleteThisAndFutureRecurringData(schedule.originalEventId, schedule.start.date)
            }
            ScheduleEditType.ALL_EVENTS -> TODO()
        }
    }

    /**
     * 새로운 스케줄을 추가하거나 기존 스케줄을 갱신
     */
    override suspend fun saveSchedule(schedule: ScheduleData) {
        scheduleDao.insertSchedule(schedule.toScheduleEntity())
    }

    private fun findPreviousRepeatDateFromScheduleMap(
        scheduleMap: Map<LocalDate, List<BaseSchedule>>,
        currentDate: LocalDate,
        eventId: String
    ): LocalDate? {
        return scheduleMap
            .filterKeys { it.isBefore(currentDate) }
            .toSortedMap()
            .entries
            .reversed()
            .firstOrNull { (_, schedules) ->
                schedules.any { schedule ->
                    (schedule is RecurringData && schedule.originalEventId == eventId) ||
                            (schedule is ScheduleData && schedule.id == eventId)
                }
            }
            ?.key
    }
    private fun findPreviousRepeatDateFromScheduleMapByIndex(
        scheduleMap: Map<LocalDate, List<BaseSchedule>>,
        currentIndex: Int,
        branchId: String
    ): LocalDate? {
        return scheduleMap.values
            .flatten()
            .filterIsInstance<RecurringData>()
            .filter { it.branchId == branchId && (it.repeatIndex) < currentIndex }
            .maxByOrNull { it.repeatIndex ?: Int.MIN_VALUE }
            ?.start?.date
    }

}
