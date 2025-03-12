package com.pdevjay.calendar_with_schedule.data.repository

import com.pdevjay.calendar_with_schedule.data.database.ScheduleDao
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
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    // MARK: Original schedule related
    override fun getAllSchedules(): Flow<List<ScheduleData>> {
        return scheduleDao.getAllSchedules()
            .map { scheduleEntities -> scheduleEntities.map { it.toScheduleData() } }
    }

    override suspend fun getSchedulesForDate(date: LocalDate): Flow<List<ScheduleData>> {
        return combine(
            scheduleDao.getSchedulesForDate(date.toString()),  // 원본 일정 가져오기
            scheduleDao.getRecurringScheduleChangesForDate(date.toString()) // 특정 날짜에서 수정된 반복 일정 가져오기
        ) { scheduleEntities, recurringEntities ->
            val schedules = scheduleEntities.map { it.toScheduleData() }
            val recurringSchedules = recurringEntities.map { it.toRecurringData() }

            schedules.flatMap { schedule ->
                // 반복 일정이 없는 경우 (단일 일정 또는 시작 날짜가 `date`와 일치하는 일정)
                if ((schedule.repeatType == RepeatType.NONE || schedule.repeatRule.isNullOrEmpty()) ||
                    (schedule.repeatType != RepeatType.NONE && schedule.start.date == date)) {
                    listOf(schedule) // 그대로 반환
                } else {
                    // `recurringSchedules`에서 해당 일정이 수정되었는지 확인
                    val modifiedRecurringEvent = recurringSchedules.firstOrNull {
                        it.originalEventId == schedule.id && it.start.date == date
                    }

                    if (modifiedRecurringEvent != null) {
                        if (modifiedRecurringEvent.isDeleted) {
                            emptyList() // 해당 날짜에서 삭제된 일정이면 반환하지 않음
                        } else {
                            listOf(modifiedRecurringEvent.toScheduleData(schedule)) // 수정된 일정 반영
                        }
                    } else {
                        // 수정되지 않은 반복 일정 생성
                        val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(
                            schedule.repeatType,  // 반복 유형 (DAILY, WEEKLY 등)
                            schedule.start.date,  // 반복 일정의 시작 날짜
                            monthList = null,     // 특정 월 리스트 사용 안 함
                            selectedDate = date   // 특정 날짜에 해당하는 일정만 생성
                        )

                        // 반복 일정 생성
                        repeatedDates.map { selectedDate -> generateRepeatedScheduleInstances(schedule, selectedDate) }
                    }
                }
            }
        }
    }

    override fun getSchedulesForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<ScheduleData>>> {
        val monthStrings = months.map { it.toString() }
        val maxMonth = months.maxOrNull()?.toString() ?: YearMonth.now().toString()

        return combine(
            scheduleDao.getSchedulesForMonths(monthStrings, maxMonth),
            scheduleDao.getRecurringSchedulesForMonths(monthStrings)
        ) { scheduleEntities, recurringEntities ->
            // 원본 일정 변환
            val originalSchedules = scheduleEntities.map { it.toScheduleData() }
            // 수정된 반복 일정 변환
            val recurringSchedules = recurringEntities.map { it.toRecurringData() }

            // 일정 리스트에 recurringSchedules 적용
            val updatedSchedules = originalSchedules.flatMap { schedule ->
                val dateToIgnore = mutableListOf<LocalDate>()

                // 🔹 특정 일정의 수정된 날짜 가져오기 (`recurring_schedules`의 ID에서 날짜를 추출)
                val modifiedRecurringEvents = recurringSchedules.filter { it.originalEventId == schedule.id }
                val modifiedDates = modifiedRecurringEvents.mapNotNull { it.id.split("_").lastOrNull()?.let { date -> LocalDate.parse(date) } }

                // 🔹 `recurring_schedules`에 있는 모든 날짜를 `dateToIgnore` 리스트에 추가
                dateToIgnore.addAll(modifiedDates)

                // 🔹 삭제된 일정은 반복 일정에서 제외
                val deletedDates = modifiedRecurringEvents
                    .filter { it.isDeleted }
                    .mapNotNull { it.id.split("_")
                        .lastOrNull()?.let { date -> LocalDate.parse(date) } }

                dateToIgnore.addAll(deletedDates)

                // 🔹 수정된 일정 추가 (삭제된 일정 제외)
                val modifiedEvents = modifiedRecurringEvents
                    .filterNot { it.isDeleted }
                    .map { modifiedEvent ->
                        modifiedEvent.start.date to modifiedEvent.toScheduleData(schedule) // 🔹 수정된 일정 추가
                    }

                // 수정되지 않은 반복 일정 생성 (수정된 일정 및 삭제된 일정 제외)
                val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(
                    schedule.repeatType,
                    schedule.start.date,
                    monthList = months,
                    dateToIgnore = dateToIgnore
                )
                val filteredDates = repeatedDates.filter { date ->
                    months.any { month -> YearMonth.from(date) == month }
                }
                val generatedEvents = filteredDates.map { date -> date to generateRepeatedScheduleInstances(schedule, date) }

                // 수정된 일정 + 생성된 일정 반환
                modifiedEvents + generatedEvents
            }

            // 날짜 기준으로 그룹화하여 반환
            updatedSchedules.groupBy({ it.first }, { it.second })
        }
    }

    override suspend fun saveSchedule(schedule: ScheduleData) {
        scheduleDao.insertSchedule(schedule.toScheduleEntity())
    }

    override suspend fun deleteSchedule(schedule: ScheduleData) {
        scheduleDao.deleteSchedule(schedule.toScheduleEntity())
    }



    // MARK: Recurring schedule related
    override suspend fun getRecurringSchedulesForDate(date: LocalDate): Flow<List<RecurringData>> {
        return scheduleDao.getRecurringScheduleChangesForDate(date.toString())
            .map { recurringScheduleEntities ->
                recurringScheduleEntities.map { it.toRecurringData() }
            }
    }



    override suspend fun saveRecurringScheduleChange(recurringData: RecurringData) {
        scheduleDao.insertRecurringSchedule(recurringData.toRecurringScheduleEntity())
    }

    override suspend fun markRecurringScheduleDeleted(recurringData: RecurringData) {
        scheduleDao.insertRecurringSchedule(recurringData.toRecurringScheduleEntity())
    }
}
