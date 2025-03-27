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
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toOverriddenRecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toSingleChangeData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.ScheduleEditType
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator
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
import java.util.UUID
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

    // MARK: Original schedule related
    /**
     * 현재 달 전후 1개월 씩의 스케줄을 가져옵니다.
     * `schedules` 테이블에서 해당 기간의 데이터와
     * 해당 기간의 가장 마지막 달 이전에 존재하는 반복 일정을 조회하고,
     * 해당 기간에 존재하는 반복 일정 중 변경 사항이 있는 데이터를 `recurring_schedules` 테이블에서 조회하여
     * Calendar에 보여줄 데이터 생성하여 return
     */
//    override fun getSchedulesForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<BaseSchedule>>> {
//        val monthStrings = months.map { it.toString() }
//        val maxMonth = months.maxOrNull()?.toString() ?: YearMonth.now().toString()
//        val minMonth = months.minOrNull()?.toString() ?: YearMonth.now().toString()
//        Log.e("","repository getSchedulesForMonths")
//        return combine(
//            scheduleDao.getSchedulesForMonths(monthStrings, minMonth, maxMonth),
//            recurringScheduleDao.getRecurringSchedulesForMonths(monthStrings, minMonth, maxMonth)
//        ) { scheduleEntities, recurringEntities ->
//            // 🔹 원본 일정 변환
//            val originalSchedules = scheduleEntities.map { it.toScheduleData() }
//            // 🔹 수정된 반복 일정 변환
//            val recurringSchedules = recurringEntities.map { it.toRecurringData() }
//
//            val newScheduleMap = mutableMapOf<LocalDate, List<BaseSchedule>>()
//
//            // ✅ 기존 데이터 중에서 `monthList`에 포함되지 않는 데이터 제거
//            val validDates = months.flatMap { month -> (1..month.lengthOfMonth()).map { month.atDay(it) } }
//
//            // 🔥 새로운 데이터를 기존 scheduleMap에 추가
//            val newSchedules = originalSchedules.flatMap { schedule ->
//                val dateToIgnore = mutableSetOf<LocalDate>()
//
//                // 🔹 특정 일정의 수정된 날짜 가져오기 (`recurring_schedules`의 ID에서 날짜를 추출)
//                val modifiedRecurringEvents = recurringSchedules.filter { it.originalEventId == schedule.id }
//
//                // 🔹 수정된 일정 제외
//                val modifiedDates = modifiedRecurringEvents.map { it.originalRecurringDate }
//                dateToIgnore.addAll(modifiedDates)
//
//                // 🔹 삭제된 일정 제외
//                val deletedDates = modifiedRecurringEvents.filter { it.isDeleted }.map { it.originalRecurringDate }
//                dateToIgnore.addAll(deletedDates)
//
//                // 🔹 원본 일정의 `repeatUntil` 고려하여 반복 일정 생성
//                val originalScheduleRepeatEndDate = schedule.repeatUntil
//
//                val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(
//                    schedule.repeatType,
//                    schedule.start.date,
//                    monthList = months,
//                    dateToIgnore = dateToIgnore,
//                    repeatUntil = originalScheduleRepeatEndDate
//                )
//
//                val generatedEvents = repeatedDates.map { date -> date to generateRepeatedScheduleInstances(schedule, date) }
//
//                if (modifiedDates.contains(schedule.start.date)) {
//                    generatedEvents
//                } else {
//                    listOf(schedule.start.date to schedule) + generatedEvents
//                }
//            }
//
//            // 🔥 (2) 수정된 반복 일정 처리
//            val updatedRecurringSchedules = recurringSchedules.flatMap { recurringData ->
//                val dateToIgnore = mutableSetOf<LocalDate>()
//
//                // 🔹 특정 반복 일정의 수정된 날짜 가져오기 (`recurring_schedules`의 ID에서 날짜를 추출)
//                val modifiedRecurringEvents = recurringSchedules.filter { it.originatedFrom == recurringData.id }
//
//                // 🔹 수정된 일정 제외
//                val modifiedDates = modifiedRecurringEvents.map { it.originalRecurringDate }
//                dateToIgnore.addAll(modifiedDates)
//
//                // 🔹 삭제된 일정 제외
//                val deletedDates = modifiedRecurringEvents.filter { it.isDeleted }.map { it.originalRecurringDate }
//                dateToIgnore.addAll(deletedDates)
//
//                // 🔹 반복 일정의 `repeatUntil` 고려하여 새로운 반복 일정 생성
//                val recurringScheduleRepeatEndDate = recurringData.repeatUntil
//
//                val updatedRepeatDates = RepeatScheduleGenerator.generateRepeatedDates(
//                    recurringData.repeatType,
//                    recurringData.start.date,
//                    monthList = months,
//                    dateToIgnore = dateToIgnore, // 🔥 기존에 삭제되거나 수정된 일정 필터링
//                    repeatUntil = recurringScheduleRepeatEndDate
//                ).map { date -> date to generateRepeatedScheduleInstances(recurringData, date) }
//
//                //
//                val representOriginal = recurringData.resolveDisplayFields(
//                    scheduleOrigins = originalSchedules,        // List<ScheduleData>
//                    recurringOrigins = recurringSchedules       // List<RecurringData>
//                )
//
//                if (recurringData.isDeleted) {
//                    updatedRepeatDates
//                } else if (modifiedDates.contains(recurringData.start.date)) {
//                    updatedRepeatDates
//                } else {
//                    listOf(recurringData.start.date to representOriginal) + updatedRepeatDates
//                }
//            }
//
//            // 🔥 기존 scheduleMap과 새로운 데이터 병합
//            val updatedSchedules = (newSchedules + updatedRecurringSchedules)
//                .groupBy({ it.first }, { it.second })
//
//            updatedSchedules.forEach { (date, schedules) ->
//                newScheduleMap[date] = schedules
//            }
//
//            // ✅ 일정이 없는 날짜도 빈 리스트로 추가
//            validDates.forEach { date ->
//                newScheduleMap.putIfAbsent(date, emptyList())
//            }
//
//            // ✅ 최종 정렬 후 반환
//            newScheduleMap.toSortedMap()
//        }
//    }

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
                val dateToIgnore = recurringSchedules
                    .filter { it.originalEventId == schedule.id && (it.isDeleted || it.repeatType == RepeatType.NONE) }
                    .map { it.originalRecurringDate }
                    .toSet()

                val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(
                    schedule.repeatType,
                    schedule.start.date,
                    monthList = months,
                    dateToIgnore = dateToIgnore,
                    repeatUntil = schedule.repeatUntil
                )

                repeatedDates.forEach { date ->
                    val recurring = generateRepeatedScheduleInstances(schedule, date).copy(
                        isFirstSchedule = (date == schedule.start.date)
                    )
                    allSchedules.add(recurring)
                }
            }

            // 🔹 (2) 기존 RecurringData에서 파생된 반복들 처리 (분기 루트 기준)
            val recurringByBranch = recurringSchedules.groupBy { it.originatedFrom }

            recurringSchedules.filter { it.isFirstSchedule }.forEach { branchRoot ->
                val children = recurringByBranch[branchRoot.id].orEmpty()

                val overriddenInstances = children.filter {
                    !it.isDeleted && !it.isFirstSchedule
                }

                val dateToIgnore = children
                    .filter { it.isDeleted || it.repeatType == RepeatType.NONE }
                    .map { it.originalRecurringDate }
                    .toSet()

                val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(
                    branchRoot.repeatType,
                    branchRoot.start.date,
                    monthList = months,
                    dateToIgnore = dateToIgnore,
                    repeatUntil = branchRoot.repeatUntil
                )

                repeatedDates.forEach { date ->
                    allSchedules.add(generateRepeatedScheduleInstances(branchRoot, date))
                }

                allSchedules.addAll(overriddenInstances)
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
                val previousRepeatDate = findPreviousRepeatDateFromScheduleMap(
                    scheduleMap = scheduleMap.value,
                    currentDate = schedule.originalRecurringDate,
                    eventId = schedule.originalEventId
                )
                val newRepeatUntil = previousRepeatDate ?: schedule.start.date.minusDays(1)

                // ScheduleData에서 파생된 반복일정이면 schedules 테이블도 자름
                if (schedule.originatedFrom == schedule.originalEventId) {
                    scheduleDao.updateRepeatUntil(
                        originalEventId = schedule.originatedFrom,
                        repeatUntil = newRepeatUntil.toString()
                    )
                }

                // RecurringData 루트인 경우 recurring 테이블도 자름
                recurringScheduleDao.updateRepeatUntil(
                    eventId = schedule.originatedFrom,
                    repeatUntil = newRepeatUntil.toString()
                )

                val deletedInstance = schedule.toMarkAsDeletedData(schedule.originalRecurringDate)

                recurringScheduleDao.insertRecurringSchedule(deletedInstance.toRecurringScheduleEntity())

                val branched = schedule.toNewBranchData()
                recurringScheduleDao.insertRecurringSchedule(branched.toRecurringScheduleEntity())

                recurringScheduleDao.updateContentOnly(schedule.toRecurringScheduleEntity())
            }
            ScheduleEditType.ALL_EVENTS -> TODO()
        }
    }
//    override suspend fun updateSchedule(schedule: BaseSchedule, scheduleEditType: ScheduleEditType, isOnlyContentChanged: Boolean) {
//        when (schedule) {
//            is ScheduleData -> {
//                when (scheduleEditType){
//                    ScheduleEditType.ONLY_THIS_EVENT -> {
//                        if (schedule.repeatType == RepeatType.NONE){
//                            // 반복일정이 아니라면 schedule data update
//                            scheduleDao.insertSchedule(schedule.toScheduleEntity())
//                        } else {
//                            // 반복일정일 경우 recurring data insert
//                            recurringScheduleDao.insertRecurringSchedule(
//                                schedule
//                                    .toRecurringData(originalStartDate = schedule.originalStartDate, selectedDate = schedule.start.date)
//                                    .copy(repeatType = RepeatType.NONE, isFirstSchedule = true) // 원본 일정을 변경하여 recurring data를 생성하는 경우 -> originalRecurringDate를 원본 일정의 startDate로 설정
//                                    .toRecurringScheduleEntity()
//                            )
//                        }
//                    }
//                    ScheduleEditType.THIS_AND_FUTURE -> {
//                        if (schedule.repeatType == RepeatType.NONE) {
//                            // 반복일정이 아니라면 schedule data update
//                            scheduleDao.insertSchedule(schedule.toScheduleEntity())
//                        } else {
//                            if (isOnlyContentChanged) {
//                                scheduleDao.updateContentOnly(schedule = schedule.toScheduleEntity())
//                                recurringScheduleDao.updateContentOnly(
//                                    schedule.toRecurringData(
//                                        selectedDate = schedule.originalStartDate
//                                    ).toRecurringScheduleEntity()
//                                )
//                            } else {
//                                scheduleDao.insertSchedule(schedule.toScheduleEntity())
//                            }
//                        }
//                    }
//                    ScheduleEditType.ALL_EVENTS -> TODO()
//                }
//            }
//
//            is RecurringData -> {
//                when (scheduleEditType){
//                    ScheduleEditType.ONLY_THIS_EVENT -> {
//                        if (schedule.isFirstSchedule){
//                            recurringScheduleDao.insertRecurringSchedule(schedule.toOverriddenRecurringData(originalStartDate = schedule.originalStartDate, selectedDate = schedule.start.date, true).toRecurringScheduleEntity())
//                        } else {
//                            recurringScheduleDao.insertRecurringSchedule(schedule.copy(repeatType = RepeatType.NONE).toRecurringScheduleEntity())
//                        }
//                    }
//                    ScheduleEditType.THIS_AND_FUTURE -> {
//                        when (schedule.isFirstSchedule){
//                            true -> {
//                                when (isOnlyContentChanged){
//                                    // FIXME: isFirstSchedule을 분기의 기준으로 변경했기 때문에 수정 필요
//                                    true -> {
//                                        // 원본 일정을 수정한(대신 보여주는) recurring data에서 이후 일정 모두 변경을 선택하고 content만 변경한 경우
//                                        // original data를 수정해야 이후 일정의 content가 바뀌어 보임
//                                        // FIXME: recurring data의 originatedFrom으로 수정해야 함
//                                        //
//                                        scheduleDao.updateContentOnly(scheduleId = schedule.originatedFrom, schedule.toScheduleData().toScheduleEntity())
//                                        // 중간에 생긴 recurring data를 수정해야 이후 일정의 content가 바뀌어 보임
//                                        recurringScheduleDao.updateContentOnly(schedule.toRecurringScheduleEntity())
//                                    }
//
//                                    false -> {
//                                        // 원본 일정을 수정한(대신 보여주는) recurring data에서 이후 일정 모두 변경을 선택하고 날짜 등을 변경한 경우
//                                        // original data를 수정해야 이후 일정의 날짜가 바뀌어 보임
//                                        val scheduleData = schedule.toScheduleData().toScheduleEntity()
//                                        scheduleDao.insertSchedule(scheduleData)
//
//                                        // original data에서 나온 recurring data는 삭제
//                                        recurringScheduleDao.deleteIsFirstRecurringSchedule(schedule.originalEventId)
//                                    }
//                                }
//                            }
//
//                            false -> {
//                                val originatedFrom = schedule.originatedFrom
//                                Log.e("", "findPreviousRepeatDateFromScheduleMap 전 scheduleMap : ${_scheduleMap.value.size}")
//                                val previousRepeatDate = findPreviousRepeatDateFromScheduleMap(
//                                    scheduleMap = scheduleMap.value,
//                                    currentDate = schedule.originalRecurringDate,
//                                    eventId = schedule.originalEventId
//                                )
//                                val repeatUntil = previousRepeatDate ?: schedule.start.date.minusDays(1)
//
//                                // original의 repeatUntil을 변경
//                                // 이미 분기된 recurring data에서 유래된 것이면 영향 없음
//                                scheduleDao.updateRepeatUntil(repeatUntil = repeatUntil.toString(), originalEventId = originatedFrom)
//                                // recurring data에서 파생되어 수정되는 데이터인 경우 부모인 recurring data의 repeatUntil을 변경
//                                recurringScheduleDao.updateRepeatUntil(repeatUntil = repeatUntil.toString(), eventId = originatedFrom)
//                                // repeatUntil 이후에 존재하는 recurring data를 삭제
//                                // FIXME: 나중에 상황 보고 고려
////                                recurringScheduleDao.deleteFutureRecurringSchedule(schedule.originalEventId, repeatUntil)
//                                // 이후 일정 모두 변경은 분기를 새로 형성하는 것이라 isFirstSchedule = true로 저장
//                                recurringScheduleDao.insertRecurringSchedule(schedule.copy(isFirstSchedule = true).toRecurringScheduleEntity())
//                            }
//                        }
//                    }
//                    ScheduleEditType.ALL_EVENTS -> TODO()
//                }
//            }
//
//        }
//    }

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
                val previousRepeatDate = findPreviousRepeatDateFromScheduleMap(
                    scheduleMap = scheduleMap.value,
                    currentDate = schedule.originalRecurringDate,
                    eventId = schedule.originalEventId
                )
                val newRepeatUntil = previousRepeatDate ?: schedule.start.date.minusDays(1)

                // ScheduleData에서 파생된 반복일정이면 schedules 테이블도 자름
                if (schedule.originatedFrom == schedule.originalEventId) {
                    scheduleDao.updateRepeatUntil(
                        originalEventId = schedule.originatedFrom,
                        repeatUntil = newRepeatUntil.toString()
                    )
                }

                // RecurringData 루트인 경우 recurring 테이블도 자름
                recurringScheduleDao.updateRepeatUntil(
                    eventId = schedule.originatedFrom,
                    repeatUntil = newRepeatUntil.toString()
                )
            }
            ScheduleEditType.ALL_EVENTS -> TODO()
        }
    }
//    override suspend fun deleteSchedule(
//        schedule: BaseSchedule,
//        scheduleEditType: ScheduleEditType
//    ) {
//        when (schedule){
//            is ScheduleData -> {
//                when(scheduleEditType){
//                    ScheduleEditType.ONLY_THIS_EVENT -> {
//                        recurringScheduleDao.insertRecurringSchedule(schedule.toRecurringData(originalStartDate = schedule.originalStartDate, selectedDate = schedule.start.date).copy(isDeleted = true).toRecurringScheduleEntity())
//                    }
//                    ScheduleEditType.THIS_AND_FUTURE -> {
//                        scheduleDao.deleteSchedule(schedule.toScheduleEntity())
//                        recurringScheduleDao.deleteAllRecurringScheduleForScheduleId(schedule.id)
//                    }
//                    ScheduleEditType.ALL_EVENTS -> TODO()
//                }
//            }
//
//            is RecurringData -> {
//                when (scheduleEditType){
//                    ScheduleEditType.ONLY_THIS_EVENT -> {
//                        // 존재하지 않는 경우 repeatType = 'NONE', isDeleted = 1으로 `INSERT`
//                        recurringScheduleDao.insertRecurringScheduleIfNotExists(
//                            schedule.toRecurringScheduleEntity()
//                        )
//                        // 존재하는 경우 isDeleted = 1로 `UPDATE`
//                        // 존재하는 경우는 반복일정이 시작되는 데이터여서 repeatType = 'NONE'으로 바꾸면 안됨
//                        recurringScheduleDao.markRecurringScheduleAsDeleted(schedule.id)
//                    }
//                    ScheduleEditType.THIS_AND_FUTURE -> {
//                        when(schedule.isFirstSchedule){
//                            true -> {
//                                scheduleDao.deleteSchedule(schedule.toScheduleData().toScheduleEntity())
//                                recurringScheduleDao.deleteAllRecurringScheduleForScheduleId(schedule.id)
//                            }
//                            false -> {
//                                val previousRepeatDate = findPreviousRepeatDateFromScheduleMap(
//                                    scheduleMap = scheduleMap.value,
//                                    currentDate = schedule.originalRecurringDate,
//                                    eventId = schedule.originalEventId
//                                )
//                                val repeatUntil = previousRepeatDate ?: schedule.start.date.minusDays(1)
//                                val originatedFrom = schedule.originatedFrom
//                                // 이미 분기된 recurring data에서 유래된 것이면 영향 없음
//                                scheduleDao.updateRepeatUntil(repeatUntil = repeatUntil.toString(), originalEventId = originatedFrom)
//                                // recurring data에서 파생되어 수정되는 데이터인 경우 부모인 recurring data의 repeatUntil을 변경
//                                recurringScheduleDao.updateRepeatUntil(repeatUntil = repeatUntil.toString(), eventId = originatedFrom)
//                            }
//                        }
//                    }
//                    ScheduleEditType.ALL_EVENTS -> TODO()
//                }
//            }
//        }
//    }
    /**
     * 새로운 스케줄을 추가하거나 기존 스케줄을 갱신
     */
    override suspend fun saveSchedule(schedule: ScheduleData) {
        scheduleDao.insertSchedule(schedule.toScheduleEntity())
    }


    override suspend fun saveSingleScheduleChange(schedule: ScheduleData) {
        recurringScheduleDao.insertRecurringSchedule(
            schedule
                .toRecurringData(originalStartDate = schedule.originalStartDate, selectedDate = schedule.start.date)
                .copy(repeatType = RepeatType.NONE, isFirstSchedule = true) // 원본 일정을 변경하여 recurring data를 생성하는 경우 -> originalRecurringDate를 원본 일정의 startDate로 설정
                .toRecurringScheduleEntity()
        )
    }

    /**
     * 원본 스케쥴과 반복 스케쥴의 내용 변경
     */
    override suspend fun saveFutureScheduleChange(schedule: ScheduleData) {
        scheduleDao.insertSchedule(schedule.toScheduleEntity())
    }

    /**
     * 특정 날짜 이후의 원본 스케줄을 삭제합니다.
     * `schedules` 테이블과 `recurring_schedules` 테이블에서 모두 삭제
     */
    override suspend fun deleteFutureSchedule(schedule: ScheduleData) {
        val scheduleId = schedule.id
        val startDate = schedule.start.date.toString()
        scheduleDao.deleteFutureSchedule(scheduleId)
        recurringScheduleDao.deleteFutureRecurringSchedule(scheduleId, startDate)
    }

    override suspend fun deleteSingleSchedule(schedule: ScheduleData) {
        recurringScheduleDao.insertRecurringSchedule(schedule.toRecurringData(selectedDate = schedule.start.date).copy(isDeleted = true).toRecurringScheduleEntity())
    }

    // MARK: Recurring schedule related

    /**
     * 반복 일정의 삽입, 갱신
     */
    override suspend fun saveSingleRecurringScheduleChange(recurringData: RecurringData) {
//        if (recurringData.isFirstSchedule){
//            scheduleDao.insertSchedule(recurringData.toScheduleData().toScheduleEntity())
//        } else {
            recurringScheduleDao.insertRecurringSchedule(recurringData.copy(repeatType = RepeatType.NONE).toRecurringScheduleEntity())
//        }
    }

    /**
     * 특정 날짜의 반복 일정에서 해당 날짜 이후의 모든 반복 일정의 내용 변경
     * 원본 일정의 repeatUntil을 해당 날짜 -1로 변경 후,
     * 원본 일정의 id를 기반으로 새로운 일정 등록
     */
    override suspend fun saveFutureRecurringScheduleChange(recurringData: RecurringData) {

        if (recurringData.isFirstSchedule){
            scheduleDao.insertSchedule(recurringData.toScheduleData().toScheduleEntity())
        } else {
            // 수정한 일정의 날짜가 원래 예정된 날짜 이후인 경우에는 원본의 repeatUntil을 원래 예정된 날짜 -1까지 해야 원래 예정된 날짜에 스케쥴이 등록 안됨
            // ex) 26일에 등록된 반복 일정을 27일로 수정하는 경우 원래 예정된 날짜(26일) -1, 즉 25일로 해야 26일에 스케쥴이 등록되지 않음
            // 수정한 일정의 날짜가 원래 예정된 날짜 이전인 경우에는 원본의 repeatUntil을 수정한 날짜 -1까지 해야 예정된 날짜에 스케쥴이 등록 안됨
            // ex) 20, 22, 24, 2일마다 반복인 일정에서 26일에 등록된 반복 일정을 23일로 수정하는 경우 수정하려는 날(23일) -1, 즉 22일로 해야 24, 26일에 스케쥴이 등록되지 않음
            // 수정한 일정의 날짜가 원래 예정된 날짜의 이전인 경우
            val repeatUntil = if (recurringData.start.date.isBefore(recurringData.originalRecurringDate)){
                // repeatUntil을 수정한 날짜의 -1로 수정
                recurringData.start.date.minusDays(1).toString()
            } else if (recurringData.repeatUntil != null && recurringData.start.date.isAfter(recurringData.repeatUntil)){
                recurringData.start.date.toString()
            } else if (recurringData.start.date.isAfter(recurringData.originalRecurringDate)){
                // 수정한 일정의 날짜가 원래 예정된 날짜 이후인 경우
                // repeatUntil을 예정된 날짜의 -1로 수정
                recurringData.originalRecurringDate.minusDays(1).toString()
            } else {
                recurringData.start.date.minusDays(1).toString()
            }


            val originatedFrom = recurringData.originatedFrom

            scheduleDao.updateRepeatUntil(repeatUntil = repeatUntil, originalEventId = originatedFrom)
            recurringScheduleDao.updateRepeatUntil(repeatUntil = repeatUntil, eventId = originatedFrom)
            recurringScheduleDao.deleteFutureRecurringSchedule(recurringData.originalEventId, repeatUntil)
            recurringScheduleDao.insertRecurringSchedule(recurringData.toRecurringScheduleEntity())

        }
    }

    /**
     * 특정 날짜 이후의 반복 일정을 삭제합니다.
     * 원본 일정의 repeatUntil을 해당 날짜 -1로 변경 후,
     * 그 이후의 날에 등록되어있는 모든 반복 일정(`schedules table`) 삭제,
     * 또한 `recurring_schedules` 테이블에서도 해당 날짜 이후의 모든 반복 일정 삭제
     */
    override suspend fun deleteFutureRecurringSchedule(recurringData: RecurringData) {
//        val repeatUntil = recurringData.originalRecurringDate.minusDays(1).toString()

        // 수정한 일정의 날짜가 원래 예정된 날짜 이후인 경우에는 원본의 repeatUntil을 원래 예정된 날짜 -1까지 해야 원래 예정된 날짜에 스케쥴이 등록 안됨
        // ex) 26일에 등록된 반복 일정을 27일로 수정하는 경우 원래 예정된 날짜(26일) -1, 즉 25일로 해야 26일에 스케쥴이 등록되지 않음
        // 수정한 일정의 날짜가 원래 예정된 날짜 이전인 경우에는 원본의 repeatUntil을 수정한 날짜 -1까지 해야 예정된 날짜에 스케쥴이 등록 안됨
        // ex) 20, 22, 24, 2일마다 반복인 일정에서 26일에 등록된 반복 일정을 23일로 수정하는 경우 수정하려는 날(23일) -1, 즉 22일로 해야 24, 26일에 스케쥴이 등록되지 않음
        // 수정한 일정의 날짜가 원래 예정된 날짜의 이전인 경우
        val repeatUntil = if (recurringData.start.date.isBefore(recurringData.originalRecurringDate)){
            // repeatUntil을 수정한 날짜의 -1로 수정
            recurringData.start.date.minusDays(1).toString()
        } else {
            // 수정한 일정의 날짜가 원래 예정된 날짜 이후인 경우
            // repeatUntil을 예정된 날짜의 -1로 수정
            recurringData.originalRecurringDate.minusDays(1).toString()
        }

        val originatedFrom = recurringData.originatedFrom
        scheduleDao.updateRepeatUntil(repeatUntil = repeatUntil, originalEventId = originatedFrom)
//        scheduleDao.deleteFutureRecurringSchedule(repeatUntil = repeatUntil, originalEventId = originatedFrom)
        recurringScheduleDao.updateRepeatUntil(repeatUntil = repeatUntil, eventId = originatedFrom)
        recurringScheduleDao.deleteFutureRecurringSchedule(recurringData.id, repeatUntil)
    }

    override suspend fun deleteRecurringSchedule(recurringData: RecurringData) {
        val id = recurringData.id

        // 존재하지 않는 경우 repeatType = 'NONE', isDeleted = 1으로 `INSERT`
        recurringScheduleDao.insertRecurringScheduleIfNotExists(
            recurringData.toRecurringScheduleEntity()
        )
        // 존재하는 경우 isDeleted = 1로 `UPDATE`
        // 존재하는 경우는 반복일정이 시작되는 데이터여서 repeatType = 'NONE'으로 바꾸면 안됨
        recurringScheduleDao.markRecurringScheduleAsDeleted(id)
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

}
