package com.pdevjay.calendar_with_schedule.data.repository

import com.pdevjay.calendar_with_schedule.data.database.RecurringScheduleDao
import com.pdevjay.calendar_with_schedule.data.database.ScheduleDao
import com.pdevjay.calendar_with_schedule.data.entity.toRecurringData
import com.pdevjay.calendar_with_schedule.data.entity.toScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleEntity
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator.generateRepeatedScheduleInstances
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.min

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val recurringScheduleDao: RecurringScheduleDao // 🔥 추가
) : ScheduleRepository {

    private val _scheduleMap = MutableStateFlow<Map<LocalDate, List<BaseSchedule>>>(emptyMap())
    override val scheduleMap: StateFlow<Map<LocalDate, List<BaseSchedule>>> = _scheduleMap

    override suspend fun loadSchedulesForMonths(months: List<YearMonth>) {
        getSchedulesForMonths(months).collect { newScheduleMap ->
            _scheduleMap.value = newScheduleMap
        }
    }

    // MARK: Original schedule related
    override fun getAllSchedules(): Flow<List<ScheduleData>> {
        return scheduleDao.getAllSchedules()
            .map { scheduleEntities -> scheduleEntities.map { it.toScheduleData() } }
    }

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
//        return combine(
//            scheduleDao.getSchedulesForMonths(monthStrings, minMonth,  maxMonth),
//            recurringScheduleDao.getRecurringSchedulesForMonths(monthStrings)
//        ) { scheduleEntities, recurringEntities ->
//            // 원본 일정 변환
//            val originalSchedules = scheduleEntities.map { it.toScheduleData() }
//            // 수정된 반복 일정 변환
//            val recurringSchedules = recurringEntities.map { it.toRecurringData() }
//
//            // 일정 리스트에 recurringSchedules 적용
//            val updatedSchedules = originalSchedules.flatMap { schedule ->
//                val dateToIgnore = mutableListOf<LocalDate>()
//
//                // 원본 일정 추가
//
//                // 원본 일정에 대한 수정 여부
//                val originalDataModified = recurringEntities.map{ it.originalEventId == schedule.id && it.originalRecurringDate == schedule.start.date }.isNotEmpty()
//
//                // 원본 데이터의 날짜
//                if (!originalDataModified) {
//                    dateToIgnore.add(schedule.start.date)
//                }
//
////                dateToIgnore.add(schedule.start.date)
//
//                // 🔹 특정 일정의 수정된 날짜 가져오기 (`recurring_schedules`의 ID에서 날짜를 추출)
//                val modifiedRecurringEvents = recurringSchedules.filter { it.originalEventId == schedule.id }
//                val modifiedDates = modifiedRecurringEvents.map{ it.originalRecurringDate }
//
//                // 🔹 `recurring_schedules`에 있는 모든 날짜를 `dateToIgnore` 리스트에 추가
//                dateToIgnore.addAll(modifiedDates)
//
//                // 🔹 삭제된 일정은 반복 일정에서 제외
//                val deletedDates = modifiedRecurringEvents
//                    .filter { it.isDeleted }
//                    .map { it.originalRecurringDate }
//
//                dateToIgnore.addAll(deletedDates)
//
//                // 🔹 수정된 일정 추가 (삭제된 일정 제외)
//                val modifiedEvents = modifiedRecurringEvents
//                    .filterNot { it.isDeleted }
//                    .map { modifiedEvent ->
//                        modifiedEvent.start.date to modifiedEvent // 🔹 수정된 일정 추가
//                    }
//
//                // 🔹 `repeatUntil`을 고려하여 반복 일정 생성 범위 제한
//                val repeatEndDate = schedule.repeatUntil ?: months.max().atEndOfMonth()
//
//                // 수정되지 않은 반복 일정 생성 (수정된 일정 및 삭제된 일정 제외)
//                val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(
//                    schedule.repeatType,
//                    schedule.start.date,
//                    monthList = months,
//                    dateToIgnore = dateToIgnore,
//                    repeatUntil = repeatEndDate // 🔹 repeatUntil 반영
//                )
//                val filteredDates = repeatedDates.filter { date ->
//                    months.any { month -> YearMonth.from(date) == month }
//                }
//                val generatedEvents = filteredDates.map { date -> date to generateRepeatedScheduleInstances(schedule, date) }
//
//                // 수정된 일정 + 생성된 일정 반환
//                if (originalDataModified){
//                    modifiedEvents + generatedEvents
//                } else {
//                    listOf(schedule.start.date to schedule) + modifiedEvents + generatedEvents
//                }
//            }
//
//            // 날짜 기준으로 그룹화하여 반환
//            updatedSchedules.groupBy({ it.first }, { it.second })
//        }
//    }
//    override fun getSchedulesForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<BaseSchedule>>> {
//        val monthStrings = months.map { it.toString() }
//        val maxMonth = months.maxOrNull()?.toString() ?: YearMonth.now().toString()
//        val minMonth = months.minOrNull()?.toString() ?: YearMonth.now().toString()
//
//        return combine(
//            scheduleDao.getSchedulesForMonths(monthStrings, minMonth, maxMonth),
//            recurringScheduleDao.getRecurringSchedulesForMonths(monthStrings, minMonth, maxMonth)
//        ) { scheduleEntities, recurringEntities ->
//            // 🔹 원본 일정 변환
//            val originalSchedules = scheduleEntities.map { it.toScheduleData() }
//            // 🔹 수정된 반복 일정 변환
//            val recurringSchedules = recurringEntities.map { it.toRecurringData() }
//
//            // 🔥 업데이트된 일정 리스트 생성
//            val updatedSchedules = originalSchedules.flatMap { schedule ->
//                val dateToIgnore = mutableSetOf<LocalDate>()
//
//                // 🔹 특정 일정의 수정된 날짜 가져오기 (`recurring_schedules`의 ID에서 날짜를 추출)
//                val modifiedRecurringEvents = recurringSchedules.filter { it.originalEventId == schedule.id }
//
//                // 수정된 일정은 `dateToIgnore` 리스트에 추가
//                // 🔹 `recurring_schedules`에 있는 모든 날짜를 `dateToIgnore` 리스트에 추가
//                val modifiedDates = modifiedRecurringEvents.map { it.originalRecurringDate }
//                dateToIgnore.addAll(modifiedDates)
//
//                // 🔹 삭제된 일정 dateToIgnore 리스트에 추가
//                val deletedDates = modifiedRecurringEvents
//                    .filter { it.isDeleted }
//                    .map { it.originalRecurringDate }
//                dateToIgnore.addAll(deletedDates)
//
//                // 🔹 수정된 일정 추가 (삭제된 일정 제외)
//                val modifiedEvents = modifiedRecurringEvents
//                    .filterNot { it.isDeleted }
//                    .map { modifiedEvent ->
//                        modifiedEvent.start.date to modifiedEvent // 🔹 수정된 일정 추가
//                    }
//
//                // 🔹 original schedule의 `repeatUntil`을 고려하여 반복 일정 생성 범위 제한
//                val originalScheduleRepeatEndDate = schedule.repeatUntil
//
//                // 🔹 수정되지 않은 반복 일정 생성 (수정된 일정 및 삭제된 일정 제외)
//                val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(
//                    schedule.repeatType,
//                    schedule.start.date,
//                    monthList = months,
//                    dateToIgnore = dateToIgnore,
//                    repeatUntil = originalScheduleRepeatEndDate // 🔹 repeatUntil 반영
//                )
//
//                // 해당 original schedule의 수정된 반복일정(recurring data)의 정보로 새로운 반복 일정 생성
//                val updatedRecurringSchedules = recurringSchedules.flatMap { recurringData ->
//                    val recurringScheduleRepeatEndDate = recurringData.repeatUntil
//                    val updatedRepeatDates = RepeatScheduleGenerator.generateRepeatedDates(
//                        recurringData.repeatType,
//                        recurringData.start.date,
//                        monthList = months,
//                        repeatUntil = recurringScheduleRepeatEndDate
//                    ).map { date -> date to generateRepeatedScheduleInstances(recurringData, date) }
//
//                    updatedRepeatDates
//                }
//
//                val generatedEvents = repeatedDates.map { date -> date to generateRepeatedScheduleInstances(schedule, date) }
//
//                // 🔥 원본 일정이 수정된 경우 반영
//                val originalDataModified = modifiedRecurringEvents.any { it.originalEventId == schedule.id && it.originalRecurringDate == schedule.start.date }
//                if (originalDataModified) {
//                    modifiedEvents + generatedEvents + updatedRecurringSchedules
//                } else {
//                    listOf(schedule.start.date to schedule) + modifiedEvents + generatedEvents + updatedRecurringSchedules
//                }
//            }
//
//            // 날짜 기준으로 그룹화하여 반환
//            updatedSchedules.groupBy({ it.first }, { it.second })
//        }
//    }
    override fun getSchedulesForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<BaseSchedule>>> {
        val monthStrings = months.map { it.toString() }
        val maxMonth = months.maxOrNull()?.toString() ?: YearMonth.now().toString()
        val minMonth = months.minOrNull()?.toString() ?: YearMonth.now().toString()

        return combine(
            scheduleDao.getSchedulesForMonths(monthStrings, minMonth, maxMonth),
            recurringScheduleDao.getRecurringSchedulesForMonths(monthStrings, minMonth, maxMonth)
        ) { scheduleEntities, recurringEntities ->
            // 🔹 원본 일정 변환
            val originalSchedules = scheduleEntities.map { it.toScheduleData() }
            // 🔹 수정된 반복 일정 변환
            val recurringSchedules = recurringEntities.map { it.toRecurringData() }

            // 🔥 (1) 원본 일정에서 반복 일정 생성
            val generatedOriginalSchedules = originalSchedules.flatMap { schedule ->
                val dateToIgnore = mutableSetOf<LocalDate>()

                // 🔹 특정 일정의 수정된 날짜 가져오기 (`recurring_schedules`의 ID에서 날짜를 추출)
                val modifiedRecurringEvents = recurringSchedules.filter { it.originalEventId == schedule.id }

                // 🔹 수정된 일정 제외
                val modifiedDates = modifiedRecurringEvents.map { it.originalRecurringDate }
                dateToIgnore.addAll(modifiedDates)

                // 🔹 삭제된 일정 제외
                val deletedDates = modifiedRecurringEvents.filter { it.isDeleted }.map { it.originalRecurringDate }
                dateToIgnore.addAll(deletedDates)

                // 🔹 원본 일정의 `repeatUntil` 고려하여 반복 일정 생성
                val originalScheduleRepeatEndDate = schedule.repeatUntil

                val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(
                    schedule.repeatType,
                    schedule.start.date,
                    monthList = months,
                    dateToIgnore = dateToIgnore,
                    repeatUntil = originalScheduleRepeatEndDate
                )

                val generatedEvents = repeatedDates.map { date -> date to generateRepeatedScheduleInstances(schedule, date) }

                if (modifiedDates.contains(schedule.start.date)){
                    generatedEvents
                } else {

                    listOf(schedule.start.date to schedule) + generatedEvents
                }
            }

            // 🔥 (2) 수정된 반복 일정 처리 (`dateToIgnore` 적용)
            val generatedRecurringSchedules = recurringSchedules
                .flatMap { recurringData ->
                val dateToIgnore = mutableSetOf<LocalDate>()

                // 🔹 특정 반복 일정의 수정된 날짜 가져오기 (`recurring_schedules`의 ID에서 날짜를 추출)
                val modifiedRecurringEvents = recurringSchedules.filter { it.originatedFrom == recurringData.id }

                // 🔹 수정된 일정 제외
                val modifiedDates = modifiedRecurringEvents.map { it.originalRecurringDate }
                dateToIgnore.addAll(modifiedDates)

                // 🔹 삭제된 일정 제외
                val deletedDates = modifiedRecurringEvents.filter { it.isDeleted }.map { it.originalRecurringDate }
                dateToIgnore.addAll(deletedDates)

                // 🔹 반복 일정의 `repeatUntil` 고려하여 새로운 반복 일정 생성
                val recurringScheduleRepeatEndDate = recurringData.repeatUntil

                val updatedRepeatDates = RepeatScheduleGenerator.generateRepeatedDates(
                    recurringData.repeatType,
                    recurringData.start.date,
                    monthList = months,
                    dateToIgnore = dateToIgnore, // 🔥 기존에 삭제되거나 수정된 일정 필터링
                    repeatUntil = recurringScheduleRepeatEndDate
                ).map { date -> date to generateRepeatedScheduleInstances(recurringData, date) }

                if (recurringData.isDeleted){
                    updatedRepeatDates
                } else {
                    listOf(recurringData.start.date to recurringData) + updatedRepeatDates
                }
            }


            // 🔥 (3) 두 개의 리스트를 합쳐서 반환
            val updatedSchedules = generatedOriginalSchedules + generatedRecurringSchedules

            // 🔹 날짜 기준으로 그룹화하여 반환
            val groupedSchedules = updatedSchedules.groupBy({ it.first }, { it.second }).toMutableMap()

            // ✅ 일정이 없는 날짜도 포함하기 위한 처리
            val allDays = months.flatMap { month ->
                (1..month.lengthOfMonth()).map { day ->
                    month.atDay(day)
                }
            }

            allDays.forEach { date ->
                groupedSchedules.putIfAbsent(date, emptyList()) // 일정이 없는 날짜는 빈 리스트 추가
            }

            groupedSchedules.toSortedMap() // 🔹 날짜 기준 정렬 후 반환
        }
    }

    /**
     * 새로운 스케줄을 추가하거나 기존 스케줄을 갱신
     */
    override suspend fun saveSchedule(schedule: ScheduleData) {
        scheduleDao.insertSchedule(schedule.toScheduleEntity())
    }


    override suspend fun saveSingleScheduleChange(schedule: ScheduleData) {
        recurringScheduleDao.insertRecurringSchedule(schedule.toRecurringData(schedule.start.date).copy(repeatType = RepeatType.NONE).toRecurringScheduleEntity())

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
        scheduleDao.deleteFutureSchedule(scheduleId, startDate)
        recurringScheduleDao.deleteFutureRecurringSchedule(scheduleId, startDate)
    }

    override suspend fun deleteSingleSchedule(schedule: ScheduleData) {
        recurringScheduleDao.insertRecurringSchedule(schedule.toRecurringData(schedule.start.date).copy(isDeleted = true).toRecurringScheduleEntity())
    }

    // MARK: Recurring schedule related

    /**
     * 반복 일정의 삽입, 갱신
     */
    override suspend fun saveSingleRecurringScheduleChange(recurringData: RecurringData) {
        recurringScheduleDao.insertRecurringSchedule(recurringData.toRecurringScheduleEntity())
    }

    /**
     * 특정 날짜의 반복 일정에서 해당 날짜 이후의 모든 반복 일정의 내용 변경
     * 원본 일정의 repeatUntil을 해당 날짜 -1로 변경 후,
     * 원본 일정의 id를 기반으로 새로운 일정 등록
     */
    override suspend fun saveFutureRecurringScheduleChange(recurringData: RecurringData) {
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

        // original schedule의 repeatUntil을 업데이트 하고
        // 업데이트된 repeatUntil을 기반으로 recurring schedule 삭제
//        scheduleDao.deleteFutureRecurringSchedule(repeatUntil = repeatUntil, originalEventId = originalEventId)
        // recurring schedule을 기반으로 새로운 일정 등록
//        scheduleDao.updateRepeatUntil(repeatUntil = repeatUntil, originalEventId = originalEventId)
//        // 업데이트된 repeatUntil을 기반으로 recurring schedule 삭제
//        scheduleDao.deleteFutureRecurringSchedule(repeatUntil = repeatUntil, originalEventId = originalEventId)
//        // recurring schedule을 기반으로 새로운 일정 등록
//        scheduleDao.insertSchedule(
//            recurringData
//                .toScheduleData()
//                .toScheduleEntity()
//        )
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
        scheduleDao.deleteFutureRecurringSchedule(repeatUntil = repeatUntil, originalEventId = originatedFrom)
        recurringScheduleDao.updateRepeatUntil(repeatUntil = repeatUntil, eventId = originatedFrom)
        recurringScheduleDao.deleteFutureRecurringSchedule(recurringData.id, repeatUntil)
    }

    override suspend fun deleteRecurringSchedule(recurringData: RecurringData) {
        val id = recurringData.id
        val originalEventId = recurringData.originalEventId
        val originalRecurringDate = recurringData.originalRecurringDate
        val originatedFrom = recurringData.originatedFrom
        val startDate = recurringData.start
        val endDate = recurringData.end
        val title = recurringData.title
        val location = recurringData.location
        val isAllDay = recurringData.isAllDay
        val repeatUntil = recurringData.repeatUntil
        val repeatRule = recurringData.repeatRule
        val alarmOption = recurringData.alarmOption
        val isOriginalSchedule = recurringData.isOriginalSchedule
        val originalRepeatUntil = recurringData.originalRepeatUntil

        // 존재하지 않는 경우 repeatType = 'NONE', isDeleted = 1으로 `INSERT`
        recurringScheduleDao.insertRecurringScheduleIfNotExists(
            id, originalEventId, originalRecurringDate, originatedFrom, startDate, endDate, title,
            location, isAllDay, repeatUntil, repeatRule, alarmOption, isOriginalSchedule, originalRepeatUntil
        )
        // 존재하는 경우 isDeleted = 1로 `UPDATE`
        // 존재하는 경우는 반복일정이 시작되는 데이터여서 repeatType = 'NONE'으로 바꾸면 안됨
        recurringScheduleDao.markRecurringScheduleAsDeleted(id)
    }
}
