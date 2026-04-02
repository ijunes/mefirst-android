package com.ijunes.mefirst.di

import android.app.Application
import androidx.room.Room
import com.ijunes.mefirst.database.MIGRATION_1_2
import com.ijunes.mefirst.database.MIGRATION_2_3
import com.ijunes.mefirst.database.MeFirstDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

fun provideDataBase(application: Application): MeFirstDatabase = Room.databaseBuilder(
    application,
    MeFirstDatabase::class.java,
    "me_first"
).addMigrations(MIGRATION_1_2, MIGRATION_2_3).fallbackToDestructiveMigration(false).build()

fun provideTodayDao(meFirstDatabase: MeFirstDatabase) = meFirstDatabase.todayDao()

fun provideEntriesDao(meFirstDatabase: MeFirstDatabase) = meFirstDatabase.entriesDao()

val databaseModule = module {
    single { provideDataBase(androidApplication()) }
    single { provideTodayDao(get()) }
    single { provideEntriesDao(get()) }
}
