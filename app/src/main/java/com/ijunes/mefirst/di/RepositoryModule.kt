package com.ijunes.mefirst.di

import com.ijunes.mefirst.settings.backup.BackupManager
import com.ijunes.mefirst.entries.repository.EntriesRepositoryImpl
import com.ijunes.mefirst.common.state.ModeStateHolder
import com.ijunes.mefirst.common.state.OnboardingStateHolder
import com.ijunes.mefirst.common.state.SettingsStateHolder
import com.ijunes.mefirst.today.data.repository.TodayRepositoryImpl
import com.ijunes.mefirst.entries.repository.WorkEntriesRepositoryImpl
import com.ijunes.mefirst.today.data.repository.WorkTodayRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    factory { TodayRepositoryImpl(get()) }
    factory { EntriesRepositoryImpl(get()) }
    factory { WorkTodayRepositoryImpl(get()) }
    factory { WorkEntriesRepositoryImpl(get()) }
    single { ModeStateHolder(androidContext()) }
    single { OnboardingStateHolder(androidContext()) }
    single { SettingsStateHolder(androidContext()) }
    single { BackupManager(androidContext(), get()) }
}
