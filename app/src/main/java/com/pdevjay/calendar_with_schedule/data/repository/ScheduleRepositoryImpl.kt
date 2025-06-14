package com.pdevjay.calendar_with_schedule.data.repository

import android.util.Log
import com.pdevjay.calendar_with_schedule.core.utils.helpers.RepeatScheduleGenerator.generateRepeatedDatesWithIndex
import com.pdevjay.calendar_with_schedule.core.utils.helpers.RepeatScheduleGenerator.generateRepeatedScheduleInstances
import com.pdevjay.calendar_with_schedule.data.database.dao.HolidayDao
import com.pdevjay.calendar_with_schedule.data.database.dao.RecurringScheduleDao
import com.pdevjay.calendar_with_schedule.data.database.dao.ScheduleDao
import com.pdevjay.calendar_with_schedule.data.entity.toRecurringData
import com.pdevjay.calendar_with_schedule.data.entity.toScheduleData
import com.pdevjay.calendar_with_schedule.features.calendar.data.HolidayData
import com.pdevjay.calendar_with_schedule.features.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.features.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.features.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.features.schedule.data.rangeTo
import com.pdevjay.calendar_with_schedule.features.schedule.data.resolveDisplayOnly
import com.pdevjay.calendar_with_schedule.features.schedule.data.toMarkAsDeletedData
import com.pdevjay.calendar_with_schedule.features.schedule.data.toNewBranchData
import com.pdevjay.calendar_with_schedule.features.schedule.data.toRecurringData
import com.pdevjay.calendar_with_schedule.features.schedule.data.toRecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.features.schedule.data.toScheduleData
import com.pdevjay.calendar_with_schedule.features.schedule.data.toScheduleEntity
import com.pdevjay.calendar_with_schedule.features.schedule.data.toSingleChangeData
import com.pdevjay.calendar_with_schedule.features.schedule.enums.RepeatType
import com.pdevjay.calendar_with_schedule.features.schedule.enums.ScheduleEditType
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val recurringScheduleDao: RecurringScheduleDao,
    private val holidayDao: HolidayDao,
) : ScheduleRepository {

    private val _scheduleMap = MutableStateFlow<Map<LocalDate, List<RecurringData>>>(emptyMap())
    override val scheduleMap: StateFlow<Map<LocalDate, List<RecurringData>>> = _scheduleMap

    private val _holidayMap = MutableStateFlow<Map<LocalDate, List<HolidayData>>>(emptyMap())
    override val holidayMap: StateFlow<Map<LocalDate, List<HolidayData>>> = _holidayMap

    private val _isScheduleMapReady = MutableStateFlow(false)
    override val isScheduleMapReady: StateFlow<Boolean> = _isScheduleMapReady

    val monthsToLoad = (-6..6).map {
        YearMonth.now().plusMonths(it.toLong())
    }

    override val scheduleMapFlowForWorker: Flow<Map<LocalDate, List<RecurringData>>> =
        getSchedulesForMonths(monthsToLoad)
            .filter { it.isNotEmpty() }


    private val _currentMonths = MutableStateFlow<List<YearMonth>>(monthsToLoad) //  현재 조회 중인 월 리스트
    val currentMonths: StateFlow<List<YearMonth>> = _currentMonths.asStateFlow()

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        Log.e("ScheduleRepository", " Created! hash=${this.hashCode()}")

        repositoryScope.launch {
            _currentMonths.collectLatest { months ->
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
                    holidayDao.getHolidaysForMonths(_currentMonths.value.map { it.toString() }),
                ) { scheduleEntities, recurringEntities, holidayEntities ->
                    Log.e("viemodel_repository", "months 업데이트 됨 : ${months}")
                    val originalSchedules = scheduleEntities.map { it.toScheduleData() }
                    val recurringSchedules = recurringEntities.map { it.toRecurringData() }
                    val holidays = holidayEntities.map { it.toModel() }
                    buildScheduleMap(originalSchedules, recurringSchedules, holidays, months)
                }
                    .distinctUntilChanged()
                    .collectLatest { result ->
                        _scheduleMap.value = result.first
                        _holidayMap.value = result.second
                        _isScheduleMapReady.value = true
                        Log.e("viemodel_repository", "scheduleMap 자동 업데이트됨: ${result.first.keys}")
                        Log.e("viemodel_repository", "_holidayMap 자동 업데이트됨: ${_holidayMap.value}")
                    }
            }
        }

    }

    private fun buildScheduleMap(
        originalSchedules: List<ScheduleData>,
        recurringSchedules: List<RecurringData>,
        holidays: List<HolidayData>,
        months: List<YearMonth>
    ): Pair<Map<LocalDate, List<RecurringData>>, Map<LocalDate, List<HolidayData>>> {
        val allSchedules = mutableListOf<RecurringData>()
        val branchRoots = mutableMapOf<String?, RecurringData>()

        inflateRecurringData(
            ScheduleTarget.Original(originalSchedules),
            recurringSchedules,
            months,
            allSchedules,
            branchRoots
        )
        inflateRecurringData(
            ScheduleTarget.Branch(recurringSchedules),
            recurringSchedules,
            months,
            allSchedules,
            branchRoots,
            true
        )

        val resolvedSchedules = allSchedules.map { item ->
            if (item.repeatType == RepeatType.NONE && !item.isFirstSchedule) {
                val root = branchRoots[item.branchId]
                if (root != null) item.resolveDisplayOnly(root) else item
            } else item
        }

        val expanded = resolvedSchedules
            .filter { !it.isDeleted }
            .flatMap { item ->
                val startDate = item.start.date
                val endDate = item.end.date
                if (startDate == endDate) listOf(startDate to item)
                else startDate.rangeTo(endDate).map { it to item }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.sortedBy { item -> item.start.time } }

        val validDates =
            months.flatMap { month -> (1..month.lengthOfMonth()).map { month.atDay(it) } }

        // holidays
        val holidayMap = holidays
            .groupBy { LocalDate.parse(it.date) }
            .filterKeys { it in validDates }

        return Pair(
            validDates.associateWith { date -> expanded[date].orEmpty() }.toSortedMap(),
            holidayMap
        )
    }

//    init {
//        Log.e("ScheduleRepository", " Created! hash=${this.hashCode()}")
//
//        repositoryScope.launch {
//            combine(
//                scheduleDao.getSchedulesForMonths(
//                    _currentMonths.value.map { it.toString() },
//                    _currentMonths.value.minOrNull()?.toString() ?: YearMonth.now().toString(),
//                    _currentMonths.value.maxOrNull()?.toString() ?: YearMonth.now().toString()
//                ).distinctUntilChanged(),
//                recurringScheduleDao.getRecurringSchedulesForMonths(
//                    _currentMonths.value.map { it.toString() },
//                    _currentMonths.value.minOrNull()?.toString() ?: YearMonth.now().toString(),
//                    _currentMonths.value.maxOrNull()?.toString() ?: YearMonth.now().toString()
//                ).distinctUntilChanged(),
//                _currentMonths
//            ) { _, _, months ->
//                months
//            }.distinctUntilChanged()
//                .collectLatest { months ->
//                    Log.e("viemodel", "📌 _currentMonths 변경 감지됨: ${months}")
//
//                    if (months.isNotEmpty()) {
//                        getSchedulesForMonths(months)
//                            .distinctUntilChanged()
//                            .filter { it.isNotEmpty() }
//                            .collectLatest { newScheduleMap ->
//                                _scheduleMap.value = newScheduleMap
//                                _isScheduleMapReady.value = true
//                                Log.e("viemodel_repository", " scheduleMap 자동 업데이트됨: ${newScheduleMap.keys}")
//                            }
//                    }
//                }
//        }
//
//    }

    override suspend fun loadSchedulesForMonths(months: List<YearMonth>) {
        _currentMonths.value = months //  `currentMonths` 를 갱신하면 자동으로 `scheduleMap` 업데이트됨
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
            val branchRoots: MutableMap<String?, RecurringData> = mutableMapOf()

            inflateRecurringData(
                ScheduleTarget.Original(originalSchedules),
                recurringSchedules,
                months,
                allSchedules,
                branchRoots
            )
            inflateRecurringData(
                ScheduleTarget.Branch(recurringSchedules),
                recurringSchedules,
                months,
                allSchedules,
                branchRoots,
                true
            )

            val resolvedSchedules = allSchedules.map { item ->
                if (item.repeatType == RepeatType.NONE && !item.isFirstSchedule) {
                    val root = branchRoots[item.branchId]
                    if (root != null) item.resolveDisplayOnly(root) else item
                } else {
                    item
                }
            }

            //  (5) 삭제 제외, 날짜 기준 정리 (여러 날짜에 걸친 일정 고려)
            val expanded = resolvedSchedules
                .filter { !it.isDeleted }
                .flatMap { item ->
                    val startDate = item.start.date
                    val endDate = item.end.date

                    if (startDate == endDate) {
                        listOf(startDate to item)
                    } else {
                        startDate.rangeTo(endDate).map { it to item } //  날짜 범위 전체에 매핑
                    }
                }
                .groupBy({ it.first }, { it.second })
                .mapValues { it.value.sortedBy { item -> item.start.time } }


//  (6) 빈 날짜 처리
            val validDates =
                months.flatMap { month -> (1..month.lengthOfMonth()).map { month.atDay(it) } }
            val result = validDates.associateWith { date -> expanded[date].orEmpty() }

            return@combine result.toSortedMap()
        }
    }

    override suspend fun updateSchedule(
        schedule: RecurringData,
        scheduleEditType: ScheduleEditType,
        isOnlyContentChanged: Boolean
    ) {

        when (scheduleEditType) {
            ScheduleEditType.ONLY_THIS_EVENT -> {
                // 반복 일정이 아닌 경우
                if (schedule.branchId == null) {
                    scheduleDao.insertSchedule(schedule.toScheduleData().toScheduleEntity())
                } else {
                    // 반복 일정의 첫번째 일정이 업데이트 되는 경우
                    if (schedule.isFirstSchedule) {
                        // 첫번째 일정이 수정될 때
                        if (schedule.repeatType == RepeatType.NONE) {
                            recurringScheduleDao.update(schedule.toRecurringScheduleEntity())
                        } else {
                            val overridden = schedule.toSingleChangeData(needNewId = true)
                                .copy(isFirstSchedule = false)
                            recurringScheduleDao.insertRecurringSchedule(overridden.toRecurringScheduleEntity())
                        }

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
                if (schedule.isFirstSchedule) {
                    recurringScheduleDao.insertRecurringSchedule(schedule.toRecurringScheduleEntity())
                } else {
                    // 새로운 branch를 만드는 경우
                    // 단일 수정 일정에서 시작하는 경우 단일 수정 일정을 삭제
                    recurringScheduleDao.markRecurringScheduleAsDeleted(schedule.id)
                    // 새로운 branch를 생성
                    recurringScheduleDao.insertRecurringSchedule(
                        schedule.toNewBranchData().toRecurringScheduleEntity()
                    )

                }
                if (isOnlyContentChanged) {
                    recurringScheduleDao.updateContentOnly(schedule.toRecurringScheduleEntity())
                }

                recurringScheduleDao.deleteThisAndFutureRecurringData(
                    schedule.originalEventId,
                    schedule.start.date
                )

            }

            ScheduleEditType.ALL_EVENTS -> TODO()
        }
    }

    override suspend fun deleteSchedule(
        schedule: RecurringData,
        scheduleEditType: ScheduleEditType
    ) {
        when (scheduleEditType) {
            ScheduleEditType.ONLY_THIS_EVENT -> {
                if (schedule.branchId == null) {
                    scheduleDao.deleteScheduleById(schedule.originatedFrom)
                } else {
                    if (schedule.repeatType == RepeatType.NONE) {
                        recurringScheduleDao.markRecurringScheduleAsDeleted(schedule.id)
                    } else {
                        val overridden = schedule.toSingleChangeData(needNewId = true)
                        val deleted = overridden.toMarkAsDeletedData()
                        recurringScheduleDao.insertRecurringSchedule(deleted.toRecurringScheduleEntity())
                    }
                }
            }

            ScheduleEditType.THIS_AND_FUTURE -> {
                if (schedule.isFirstSchedule) {
                    val alreadyExists =
                        scheduleDao.countByBranchId(schedule.branchId.toString()) > 0
                    if (alreadyExists) {
                        scheduleDao.deleteScheduleByBranchId(schedule.branchId.toString())
                    }
                    // 해당 recurring data의 original event id를 가진 모든 recurring data를 기준 날짜 이후로 삭제
                    recurringScheduleDao.deleteThisAndFutureRecurringData(
                        schedule.originalEventId,
                        schedule.start.date
                    )
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
                    recurringScheduleDao.deleteThisAndFutureRecurringData(
                        schedule.originalEventId,
                        schedule.start.date
                    )
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

    sealed class ScheduleTarget {
        class Original(val list: List<ScheduleData>) : ScheduleTarget()
        class Branch(val list: List<RecurringData>) : ScheduleTarget()
    }

    private fun inflateRecurringData(
        target: ScheduleTarget,
        recurringSchedules: List<RecurringData>,
        months: List<YearMonth>,
        allSchedules: MutableList<RecurringData>,
        branchRoots: MutableMap<String?, RecurringData>,
        addSingleRecurringEvents: Boolean = false
    ) {
        val filtered = when (target) {
            is ScheduleTarget.Original -> target.list
            is ScheduleTarget.Branch -> target.list.filter { it.isFirstSchedule }
        }

        filtered.forEach { schedule ->
            when (schedule) {
                is ScheduleData -> {
                    if (schedule.branchId != null) {
                        branchRoots[schedule.branchId] = schedule.toRecurringData(
                            selectedDate = schedule.start.date,
                            repeatIndex = 1
                        )
//                        branchRoots.add(schedule.toRecurringData(selectedDate = schedule.start.date, repeatIndex = 1))
                    }
                }

                is RecurringData -> {
                    if (schedule.isFirstSchedule) {
                        branchRoots[schedule.branchId] = schedule
//                        branchRoots.add(schedule)
                    }
                }
            }
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

        if (addSingleRecurringEvents) {
            allSchedules.addAll(
                recurringSchedules.filter {
                    it.repeatType == RepeatType.NONE &&
                            !it.isDeleted &&
                            (it.repeatUntil == null || it.start.date <= it.repeatUntil)
                }
            )
        }
    }
}

