package com.ijunes.mefirst.di

import android.app.Application
import androidx.room.Room
import com.ijunes.mefirst.database.MeFirstDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

fun provideDataBase(application: Application): MeFirstDatabase = Room.databaseBuilder(
    application,
    MeFirstDatabase::class.java,
    "me_first"
).build()

fun provideTodayDao(meFirstDatabase: MeFirstDatabase) = meFirstDatabase.todayDao()

fun provideEntriesDao(meFirstDatabase: MeFirstDatabase) = meFirstDatabase.entriesDao()

val databaseModule = module {
    single { provideDataBase(androidApplication()) }
    single { provideTodayDao(get()) }
    single { provideEntriesDao(get()) }
}
