package com.pdevjay.calendar_with_schedule.data.repository

import android.util.Log
import com.pdevjay.calendar_with_schedule.data.database.ScheduleDao
import com.pdevjay.calendar_with_schedule.data.entity.toScheduleData
import com.pdevjay.calendar_with_schedule.data.entity.toScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatOption
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

    override suspend fun getSchedulesForDate(date: LocalDate): Flow<List<ScheduleData>> {
        return scheduleDao.getAllSchedules()
            .map { scheduleEntities ->
                scheduleEntities.map { it.toScheduleData() }
                    .filter { schedule ->
                        if (schedule.repeatOption == RepeatOption.NONE) {
                            schedule.start.date == date
                        } else {
                            generateRepeatedDates(schedule).contains(date)
                        }
                    }
            }
    }

    override fun getSchedulesForMonths(months: List<YearMonth>): Flow<Map<LocalDate, List<ScheduleData>>> {
        val monthStrings = months.map { it.toString() } // Convert YearMonth to "YYYY-MM" format
        Log.e("DB_DEBUG", "Querying for months: $monthStrings") // ✅ Debugging

        return scheduleDao.getSchedulesForMonths(monthStrings) // Pass List<String> instead of List<YearMonth>
            .map { scheduleEntities ->
                Log.e("DB_DEBUG", "Fetched ${scheduleEntities.size} tasks") // ✅ Debugging
                scheduleEntities.map { it.toScheduleData() }
                    .filter { schedule ->
                        val scheduleStartMonth = YearMonth.from(schedule.start.date)
                        val scheduleEndMonth = YearMonth.from(schedule.end.date)
                        months.any { it == scheduleStartMonth || it == scheduleEndMonth }
                    }
                    .flatMap { schedule ->
                        val dateRange = generateDateRange(schedule.start.date, schedule.end.date)
                        dateRange.map { date -> date to schedule }
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

    private fun generateRepeatedDates(event: ScheduleData, maxYears: Int = 10): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = event.start.date
        val now = LocalDate.now()

        repeat(maxYears) {
            if (currentDate.isAfter(now.minusYears(1))) {
                dates.add(currentDate)
            }
            currentDate = when (event.repeatOption) {
                RepeatOption.DAILY -> currentDate.plusDays(1)
                RepeatOption.WEEKLY -> currentDate.plusWeeks(1)
                RepeatOption.BIWEEKLY -> currentDate.plusWeeks(2)
                RepeatOption.MONTHLY -> currentDate.plusMonths(1)
                RepeatOption.YEARLY -> currentDate.plusYears(1)
                else -> return dates
            }
        }
        return dates
    }

    private fun generateDateRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        return generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it <= endDate }
            .toList()
    }
}
