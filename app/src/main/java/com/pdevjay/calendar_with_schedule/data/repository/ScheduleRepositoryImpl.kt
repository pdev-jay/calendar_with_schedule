package com.pdevjay.calendar_with_schedule.data.repository

import android.text.TextUtils.split
import android.util.Log
import com.pdevjay.calendar_with_schedule.data.database.ScheduleDao
import com.pdevjay.calendar_with_schedule.data.entity.RecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toRecurringScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatOption
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

    override fun getAllSchedules(): Flow<List<ScheduleData>> {
        return scheduleDao.getAllSchedules()
            .map { scheduleEntities -> scheduleEntities.map { it.toScheduleData() } }
    }

//    override fun getSchedulesForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<ScheduleData>>> {
//        val monthStrings = months.map { it.toString() }
//        val maxMonth = months.maxOrNull()?.toString() ?: YearMonth.now().toString()
//
//        return scheduleDao.getSchedulesForMonths(monthStrings, maxMonth)
//            .map { scheduleEntities ->
//                scheduleEntities.map { it.toScheduleData() }
//                    .flatMap { schedule ->
//                        val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(schedule.repeatType, schedule.start.date, monthList = months)
//                        val filteredDates = repeatedDates.filter { date ->
//                            months.any { month -> YearMonth.from(date) == month }
//                        }
//                        filteredDates.map { date -> date to schedule }
//                    }
//                    .groupBy({ it.first }, { it.second })
//            }
//    }

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
                val modifiedDates = mutableListOf<LocalDate>()

                // 특정 일정의 수정된 날짜 가져오기
                val modifiedRecurringEvents = recurringSchedules.filter { it.originalEventId == schedule.id }

                // 삭제된 일정도 `dateToIgnore`에 추가하여 반복 생성되지 않도록 설정
                val deletedDates = modifiedRecurringEvents.filter { it.isDeleted }.map { it.start.date }
                modifiedDates.addAll(deletedDates)

                // 수정된 일정만 추가 (삭제된 일정은 포함하지 않음)
                val modifiedEvents = modifiedRecurringEvents
                    .filterNot { it.isDeleted } // 삭제된 일정 제거
                    .map { modifiedEvent ->
                        modifiedDates.add(modifiedEvent.start.date) // 반복 일정에서 제외해야 하는 날짜 추가
                        modifiedEvent.start.date to modifiedEvent.toScheduleData(schedule) // 수정된 일정 추가
                    }

                // 수정되지 않은 반복 일정 생성 (수정된 일정 및 삭제된 일정 제외)
                val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(
                    schedule.repeatType,
                    schedule.start.date,
                    monthList = months,
                    dateToIgnore = modifiedDates
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

    override suspend fun getSchedulesForDate(date: LocalDate): Flow<List<ScheduleData>> {
        return scheduleDao.getSchedulesForDate(date.toString())
            .map { scheduleEntities ->
                scheduleEntities.map { it.toScheduleData() }
                    .filter { schedule ->
                        (schedule.repeatType == RepeatType.NONE && schedule.start.date == date) ||
                                RepeatScheduleGenerator.generateRepeatedDates(schedule.repeatType, schedule.start.date, selectedDate = date).contains(date)
                    }
            }
    }

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
