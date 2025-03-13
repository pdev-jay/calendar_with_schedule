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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val recurringScheduleDao: RecurringScheduleDao // 🔥 추가
) : ScheduleRepository {

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
    override fun getSchedulesForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<BaseSchedule>>> {
        val monthStrings = months.map { it.toString() }
        val maxMonth = months.maxOrNull()?.toString() ?: YearMonth.now().toString()
        val minMonth = months.minOrNull()?.toString() ?: YearMonth.now().toString()
        return combine(
            scheduleDao.getSchedulesForMonths(monthStrings, minMonth,  maxMonth),
            recurringScheduleDao.getRecurringSchedulesForMonths(monthStrings)
        ) { scheduleEntities, recurringEntities ->
            // 원본 일정 변환
            val originalSchedules = scheduleEntities.map { it.toScheduleData() }
            // 수정된 반복 일정 변환
            val recurringSchedules = recurringEntities.map { it.toRecurringData() }

            // 일정 리스트에 recurringSchedules 적용
            val updatedSchedules = originalSchedules.flatMap { schedule ->
                val dateToIgnore = mutableListOf<LocalDate>()

                // 원본 일정 추가

                // 원본 일정에 대한 수정 여부
                val originalDataModified = recurringEntities.map{ it.originalEventId == schedule.id && it.originalRecurringDate == schedule.start.date }.isNotEmpty()

                // 원본 데이터의 날짜
                if (!originalDataModified) {
                    dateToIgnore.add(schedule.start.date)
                }

//                dateToIgnore.add(schedule.start.date)

                // 🔹 특정 일정의 수정된 날짜 가져오기 (`recurring_schedules`의 ID에서 날짜를 추출)
                val modifiedRecurringEvents = recurringSchedules.filter { it.originalEventId == schedule.id }
                val modifiedDates = modifiedRecurringEvents.map{ it.originalRecurringDate }

                // 🔹 `recurring_schedules`에 있는 모든 날짜를 `dateToIgnore` 리스트에 추가
                dateToIgnore.addAll(modifiedDates)

                // 🔹 삭제된 일정은 반복 일정에서 제외
                val deletedDates = modifiedRecurringEvents
                    .filter { it.isDeleted }
                    .map { it.originalRecurringDate }

                dateToIgnore.addAll(deletedDates)

                // 🔹 수정된 일정 추가 (삭제된 일정 제외)
                val modifiedEvents = modifiedRecurringEvents
                    .filterNot { it.isDeleted }
                    .map { modifiedEvent ->
                        modifiedEvent.start.date to modifiedEvent // 🔹 수정된 일정 추가
                    }

                // 🔹 `repeatUntil`을 고려하여 반복 일정 생성 범위 제한
                val repeatEndDate = schedule.repeatUntil ?: months.max().atEndOfMonth()

                // 수정되지 않은 반복 일정 생성 (수정된 일정 및 삭제된 일정 제외)
                val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(
                    schedule.repeatType,
                    schedule.start.date,
                    monthList = months,
                    dateToIgnore = dateToIgnore,
                    repeatUntil = repeatEndDate // 🔹 repeatUntil 반영
                )
                val filteredDates = repeatedDates.filter { date ->
                    months.any { month -> YearMonth.from(date) == month }
                }
                val generatedEvents = filteredDates.map { date -> date to generateRepeatedScheduleInstances(schedule, date) }

                // 수정된 일정 + 생성된 일정 반환
                if (originalDataModified){
                    modifiedEvents + generatedEvents
                } else {
                    listOf(schedule.start.date to schedule) + modifiedEvents + generatedEvents
                }
            }

            // 날짜 기준으로 그룹화하여 반환
            updatedSchedules.groupBy({ it.first }, { it.second })
        }
    }

    /**
     * 새로운 스케줄을 추가하거나 기존 스케줄을 갱신
     */
    override suspend fun saveSchedule(schedule: ScheduleData) {
        scheduleDao.insertSchedule(schedule.toScheduleEntity())
    }


    override suspend fun saveSingleScheduleChange(schedule: ScheduleData) {
        TODO("Not yet implemented")
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

    // FIXME: 아직 구현 안함
    override suspend fun deleteSingleSchedule(schedule: ScheduleData) {

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
        } else {
            // 수정한 일정의 날짜가 원래 예정된 날짜 이후인 경우
            // repeatUntil을 예정된 날짜의 -1로 수정
            recurringData.originalRecurringDate.minusDays(1).toString()
        }
        val originalEventId = recurringData.originalEventId
        scheduleDao.updateRepeatUntil(repeatUntil = repeatUntil, originalEventId = originalEventId)
//        scheduleDao.deleteFutureRecurringSchedule(repeatUntil = repeatUntil, originalEventId = originalEventId)
        // recurring schedule을 기반으로 새로운 일정 등록
        scheduleDao.insertSchedule(recurringData.toScheduleData().toScheduleEntity())
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

        val originalEventId = recurringData.originalEventId
        scheduleDao.updateRepeatUntil(repeatUntil = repeatUntil, originalEventId = originalEventId)
        scheduleDao.deleteFutureRecurringSchedule(repeatUntil = repeatUntil, originalEventId = originalEventId)
        recurringScheduleDao.deleteFutureRecurringSchedule(recurringData.id, repeatUntil)
    }
}
