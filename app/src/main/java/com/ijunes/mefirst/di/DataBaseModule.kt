package com.ijunes.mefirst.di

import android.app.Application
import androidx.room.Room
import com.ijunes.mefirst.common.data.database.MIGRATION_1_2
import com.ijunes.mefirst.common.data.database.MeFirstDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

fun provideDataBase(application: Application): MeFirstDatabase = Room.databaseBuilder(
    application,
    MeFirstDatabase::class.java,
    "me_first"
).addMigrations(MIGRATION_1_2).fallbackToDestructiveMigration(false).build()

fun provideTodayDao(meFirstDatabase: MeFirstDatabase) = meFirstDatabase.todayDao()

fun provideEntriesDao(meFirstDatabase: MeFirstDatabase) = meFirstDatabase.entriesDao()

fun provideWorkTodayDao(meFirstDatabase: MeFirstDatabase) = meFirstDatabase.workTodayDao()

fun provideWorkEntriesDao(meFirstDatabase: MeFirstDatabase) = meFirstDatabase.workEntriesDao()

val databaseModule = module {
    single { provideDataBase(androidApplication()) }
    single { provideTodayDao(get()) }
    single { provideEntriesDao(get()) }
    single { provideWorkTodayDao(get()) }
    single { provideWorkEntriesDao(get()) }
}
