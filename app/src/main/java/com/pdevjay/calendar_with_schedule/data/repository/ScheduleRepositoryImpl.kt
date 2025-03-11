package com.pdevjay.calendar_with_schedule.data.repository

import android.util.Log
import com.pdevjay.calendar_with_schedule.data.database.ScheduleDao
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.toScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatOption
import com.pdevjay.calendar_with_schedule.utils.RepeatScheduleGenerator
import com.pdevjay.calendar_with_schedule.utils.RepeatType
import kotlinx.coroutines.flow.Flow
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

    override fun getSchedulesForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<ScheduleData>>> {
        val monthStrings = months.map { it.toString() }
        val maxMonth = months.maxOrNull()?.toString() ?: YearMonth.now().toString()

        return scheduleDao.getSchedulesForMonths(monthStrings, maxMonth)
            .map { scheduleEntities ->
                scheduleEntities.map { it.toScheduleData() }
                    .flatMap { schedule ->
                        val repeatedDates = RepeatScheduleGenerator.generateRepeatedDates(schedule.repeatType, schedule.start.date, monthList = months)
                        val filteredDates = repeatedDates.filter { date ->
                            months.any { month -> YearMonth.from(date) == month }
                        }
                        filteredDates.map { date -> date to schedule }
                    }
                    .groupBy({ it.first }, { it.second })
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



    private fun generateDateRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        return generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it <= endDate }
            .toList()
    }
}
