package com.pdevjay.calendar_with_schedule.data.repository

import android.content.Context
import android.util.Log
import com.pdevjay.calendar_with_schedule.BuildConfig
import com.pdevjay.calendar_with_schedule.data.database.RecurringScheduleDao
import com.pdevjay.calendar_with_schedule.data.database.ScheduleDao
import com.pdevjay.calendar_with_schedule.data.entity.toRecurringData
import com.pdevjay.calendar_with_schedule.data.entity.toScheduleData
import com.pdevjay.calendar_with_schedule.notification.AlarmScheduler
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.resolveDisplayFieldsFromBranch
import com.pdevjay.calendar_with_schedule.screens.schedule.data.resolveDisplayOnly
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toMarkAsDeletedData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toNewBranchData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toSingleChangeData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.ScheduleEditType
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator.generateRepeatedDates
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator.generateRepeatedDatesWithIndex
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator.generateRepeatedScheduleInstances
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import com.pdevjay.calendar_with_schedule.screens.schedule.data.rangeTo
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleData
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val recurringScheduleDao: RecurringScheduleDao, // 🔥 추가
    @ApplicationContext private val context: Context
) : ScheduleRepository {

    private val _scheduleMap = MutableStateFlow<Map<LocalDate, List<RecurringData>>>(emptyMap())
    override val scheduleMap: StateFlow<Map<LocalDate, List<RecurringData>>> = _scheduleMap
    private val _scheduleMapForDebug = MutableStateFlow<Map<LocalDate, List<RecurringData>>>(emptyMap())
    override val scheduleMapForDebug: StateFlow<Map<LocalDate, List<RecurringData>>> = _scheduleMapForDebug

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
                ).distinctUntilChanged(),
                recurringScheduleDao.getRecurringSchedulesForMonths(
                    _currentMonths.value.map { it.toString() },
                    _currentMonths.value.minOrNull()?.toString() ?: YearMonth.now().toString(),
                    _currentMonths.value.maxOrNull()?.toString() ?: YearMonth.now().toString()
                ).distinctUntilChanged(),
                _currentMonths
            ) { _, _, months ->
                months
            }.distinctUntilChanged()
                .collectLatest { months ->
                    Log.e("viemodel", "📌 _currentMonths 변경 감지됨: ${months}")

                    if (months.isNotEmpty()) {
                        getSchedulesForMonths(months)
                            .distinctUntilChanged()
                            .collectLatest { newScheduleMap ->
                                _scheduleMap.value = newScheduleMap
                                Log.e("viemodel_repository", "✅ scheduleMap 자동 업데이트됨: ${newScheduleMap.keys}")
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
         Log.e("viemodel_repository", "inside getSchedulesForMonths")

            val originalSchedules = scheduleEntities.map { it.toScheduleData() }
            val recurringSchedules = recurringEntities.map { it.toRecurringData() }

            val allSchedules = mutableListOf<RecurringData>()

            val allSchedulesForDebug = mutableListOf<RecurringData>()

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

                // ✅ 디버그용: 삭제 인덱스 무시하고 전체 생성
                if (BuildConfig.DEBUG) {
                    val debugIndexedDates = generateRepeatedDatesWithIndex(
                        repeatType = schedule.repeatType,
                        startDate = schedule.start.date,
                        monthList = months,
                        indicesToIgnore = emptySet(), // 🔥 무시!
                        repeatUntil = schedule.repeatUntil
                    )

                    debugIndexedDates.forEach { (index, date) ->
                        val instance = generateRepeatedScheduleInstances(schedule, date, index)
                        allSchedulesForDebug.add(instance)
                    }
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

                if (BuildConfig.DEBUG) {
                    val debugIndexedDates = RepeatScheduleGenerator.generateRepeatedDatesWithIndex(
                        branchRoot.repeatType,
                        branchRoot.start.date,
                        monthList = months,
                        indicesToIgnore = emptySet(), // 🔥 무시!
                        repeatUntil = branchRoot.repeatUntil
                    )

                    debugIndexedDates.forEach { (index, date) ->
                        val instance = generateRepeatedScheduleInstances(branchRoot, date, index)
                        allSchedulesForDebug.add(instance)
                    }
                }
            }

            // 🔹 (3) 단일 오버라이드 일정 (branch 없이 단독 저장된)
            allSchedules.addAll(
                recurringSchedules.filter {
                    it.repeatType == RepeatType.NONE &&
                            !it.isDeleted &&
                            (it.repeatUntil == null || it.start.date <= it.repeatUntil)
                }
            )

            allSchedulesForDebug.addAll(
                recurringSchedules.filter {
//                    it.repeatType == RepeatType.NONE &&
                            (it.repeatUntil == null || it.start.date <= it.repeatUntil)
                }
            )

            // 🔹 (4) resolveDisplayOnly() 로 반복 정보 덮어쓰기
            val branchRoots = allSchedules.filter { it.isFirstSchedule }.associateBy { it.branchId }
            val branchRootsForDebug = allSchedulesForDebug.filter { it.isFirstSchedule }.associateBy { it.branchId }

            val resolvedSchedules = allSchedules.map { item ->
                if (item.repeatType == RepeatType.NONE && !item.isFirstSchedule) {
                    val root = branchRoots[item.branchId]
                    if (root != null) item.resolveDisplayOnly(root) else item
                } else item
            }
            val resolvedSchedulesForDebug = allSchedulesForDebug.map { item ->
                if (item.repeatType == RepeatType.NONE && !item.isFirstSchedule) {
                    val root = branchRootsForDebug[item.branchId]
                    if (root != null) item.resolveDisplayOnly(root) else item
                } else item
            }

            // 🔹 (5) 삭제 제외, 날짜 기준 정리 (여러 날짜에 걸친 일정 고려)
            val expanded = resolvedSchedules
                .filter { !it.isDeleted }
                .flatMap { item ->
                    val startDate = item.start.date
                    val endDate = item.end.date

                    if (startDate == endDate) {
                        listOf(startDate to item)
                    } else {
                        startDate.rangeTo(endDate).map { it to item } // 🔥 날짜 범위 전체에 매핑
                    }
                }
                .groupBy({ it.first }, { it.second })
                .mapValues { it.value.sortedBy { item -> item.start.time } }

            val expandedDebug = resolvedSchedulesForDebug
                .flatMap { item ->
                    val startDate = item.start.date
                    val endDate = item.end.date
                    if (startDate == endDate) listOf(startDate to item)
                    else startDate.rangeTo(endDate).map { it to item }
                }
                .groupBy({ it.first }, { it.second })
                .mapValues { it.value.sortedBy { it.start.time } }


// 🔹 (6) 빈 날짜 처리
            val validDates = months.flatMap { month -> (1..month.lengthOfMonth()).map { month.atDay(it) } }
            val result = validDates.associateWith { date -> expanded[date].orEmpty() }

            if (BuildConfig.DEBUG) {
                val debugScheduleMap = validDates.associateWith { date -> expandedDebug[date].orEmpty() }
                _scheduleMapForDebug.value = debugScheduleMap
                // 필요하다면 여기에 알람 로그 추가도 가능:
//                 AlarmScheduler.logRegisteredAlarms(context, debugScheduleMap)
            }

            return@combine result.toSortedMap()
        }
    }


    override suspend fun updateSchedule(schedule: RecurringData, scheduleEditType: ScheduleEditType, isOnlyContentChanged: Boolean) {

        when (scheduleEditType){
            ScheduleEditType.ONLY_THIS_EVENT -> {
                // 반복 일정이 아닌 경우
                if (schedule.branchId == null){
                    scheduleDao.insertSchedule(schedule.toScheduleData().toScheduleEntity())
                }  else {
                    // 반복 일정의 첫번째 일정이 업데이트 되는 경우
                    if (schedule.isFirstSchedule){
                        // 첫번째 일정이 수정될 때
                        if (schedule.repeatType == RepeatType.NONE){
//                            val alreadyExists = recurringScheduleDao.countById(schedule.id) > 0
                            recurringScheduleDao.update(schedule.toRecurringScheduleEntity())
                        } else {
//                            recurringScheduleDao.markRecurringScheduleAsDeleted(schedule.id)
                            val overridden = schedule.toSingleChangeData(needNewId = true)
                            recurringScheduleDao.insertRecurringSchedule(overridden.toRecurringScheduleEntity())
                        }

//                        if (alreadyExists){
//                        } else {
//                            recurringScheduleDao.markRecurringScheduleAsDeleted(schedule.id)
//                            val overridden = schedule.toSingleChangeData(needNewId = true)
//                            recurringScheduleDao.insertRecurringSchedule(overridden.toRecurringScheduleEntity())
//                        }
                    } else {
                        // 반복 일정의 중간 일정이 업데이트 되는 경우
                        val overridden = schedule.toSingleChangeData(needNewId = false)
                        recurringScheduleDao.insertRecurringSchedule(overridden.toRecurringScheduleEntity())
                    }
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
                        branchId = schedule.branchId,
                        repeatUntil = newRepeatUntil.toString()
                    )
                }

                // RecurringData 루트인 경우 recurring 테이블도 자름
                recurringScheduleDao.updateRepeatUntil(
                    branchId = schedule.branchId,
                    repeatUntil = newRepeatUntil.toString()
                )
                // branch의 root인 경우 새로운 branch를 생성하지 않고 기존 branch를 업데이트
                if (schedule.isFirstSchedule){
                    recurringScheduleDao.insertRecurringSchedule(schedule.toRecurringScheduleEntity())
                } else {
                    // 새로운 branch를 만드는 경우
                    // 단일 수정 일정에서 시작하는 경우 단일 수정 일정을 삭제
                    recurringScheduleDao.markRecurringScheduleAsDeleted(schedule.id)
                    // 새로운 branch를 생성
                    recurringScheduleDao.insertRecurringSchedule(schedule.toNewBranchData().toRecurringScheduleEntity())

                }
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
                if (schedule.branchId == null){
                    scheduleDao.deleteScheduleById(schedule.originatedFrom)
                } else {
                    if (schedule.repeatType == RepeatType.NONE){
                        recurringScheduleDao.markRecurringScheduleAsDeleted(schedule.id)
                    } else {
                        val overridden = schedule.toSingleChangeData(needNewId = true)
                        val deleted = overridden.toMarkAsDeletedData()
                        recurringScheduleDao.insertRecurringSchedule(deleted.toRecurringScheduleEntity())
                    }
//                    val alreadyExists = recurringScheduleDao.countById(schedule.id) > 0
//
//                    if (alreadyExists){
//                        recurringScheduleDao.markRecurringScheduleAsDeleted(schedule.id)
//                    } else {
//                    }
                }
            }
            ScheduleEditType.THIS_AND_FUTURE -> {
                if (schedule.isFirstSchedule){
                    val alreadyExists = scheduleDao.countByBranchId(schedule.branchId.toString()) > 0
                    if (alreadyExists){
                        scheduleDao.deleteScheduleByBranchId(schedule.branchId.toString())
                    }
                    // 해당 recurring data의 original event id를 가진 모든 recurring data를 기준 날짜 이후로 삭제
                    recurringScheduleDao.deleteThisAndFutureRecurringData(schedule.originalEventId, schedule.start.date)
                } else {
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
        val candidates = scheduleMap.values
            .flatten()
            .filterIsInstance<RecurringData>()
            .filter { it.branchId == branchId && it.repeatIndex != null }

        // 1. 이전 repeatIndex 중 가장 큰 값의 날짜 반환 (currentIndex보다 작은 repeatIndex)
        val previous = candidates
            .filter { it.repeatIndex!! < currentIndex }
            .maxByOrNull { it.repeatIndex!! }
            ?.start?.date

        if (previous != null) return previous

        // 2. 동일한 repeatIndex가 존재하면 → 해당 날짜 - 1일 반환
        val sameIndexDate = candidates
            .find { it.repeatIndex == currentIndex }
            ?.start?.date

        return sameIndexDate?.minusDays(1)
    }

}
