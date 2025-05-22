package com.pdevjay.calendar_with_schedule.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.pdevjay.calendar_with_schedule.data.database.dao.HolidayDao
import com.pdevjay.calendar_with_schedule.data.database.dao.RecurringScheduleDao
import com.pdevjay.calendar_with_schedule.data.database.dao.ScheduleDao
import com.pdevjay.calendar_with_schedule.data.database.ScheduleDatabase
import com.pdevjay.calendar_with_schedule.data.remote.api.DataApiService
import com.pdevjay.calendar_with_schedule.data.remote.RetrofitClient
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ScheduleDatabase {
        return Room.databaseBuilder(
            context,
            ScheduleDatabase::class.java,
            "task_database"
        )
            .setQueryCallback({ sqlQuery, bindArgs ->
                Log.e("ROOM_QUERY", "Executed Query: $sqlQuery, Args: $bindArgs") //  Log SQL queries
            }, Executors.newSingleThreadExecutor()) // Run callback in the background
            .addMigrations(ScheduleDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideScheduleDao(database: ScheduleDatabase): ScheduleDao = database.scheduleDao()
    @Provides
    fun provideRecurringScheduleDao(database: ScheduleDatabase): RecurringScheduleDao = database.recurringScheduleDao()

    @Provides
    fun provideApi(): DataApiService = RetrofitClient.apiService

    @Provides
    fun provideDao(database: ScheduleDatabase): HolidayDao = database.holidayDao()

}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository
}
