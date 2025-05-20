package com.pdevjay.calendar_with_schedule.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pdevjay.calendar_with_schedule.data.entity.HolidayDataEntity

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays")
    suspend fun getAll(): List<HolidayDataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(holidays: List<HolidayDataEntity>)
}
