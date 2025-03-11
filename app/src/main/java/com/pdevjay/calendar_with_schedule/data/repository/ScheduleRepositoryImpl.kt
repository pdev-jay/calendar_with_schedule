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
