package com.pdevjay.calendar_with_schedule.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pdevjay.calendar_with_schedule.data.entity.HolidayDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays")
    suspend fun getAll(): List<HolidayDataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(holidays: List<HolidayDataEntity>)

    @Query("""
    SELECT * FROM holidays
    WHERE 
        strftime('%Y-%m', date) IN (:months)
""")
    fun getHolidaysForMonths(months: List<String>): Flow<List<HolidayDataEntity>>
}
