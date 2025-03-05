package com.pdevjay.calendar_with_schedule.di

import android.content.Context
import androidx.room.Room
import com.pdevjay.calendar_with_schedule.data.db.TaskDao
import com.pdevjay.calendar_with_schedule.data.db.TaskDatabase
import com.pdevjay.calendar_with_schedule.data.repository.TaskRepository
import com.pdevjay.calendar_with_schedule.data.repository.TaskRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TaskDatabase {
        return Room.databaseBuilder(
            context,
            TaskDatabase::class.java,
            "task_database"
        ).build()
    }

    @Provides
    fun provideTaskDao(database: TaskDatabase): TaskDao = database.taskDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
}
