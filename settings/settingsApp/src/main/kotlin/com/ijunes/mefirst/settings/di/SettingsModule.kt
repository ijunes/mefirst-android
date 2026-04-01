package com.ijunes.mefirst.settings.di

import com.ijunes.mefirst.settings.alarm.AlarmScheduler
import com.ijunes.mefirst.settings.alarm.MidnightAlarmScheduler
import com.ijunes.mefirst.settings.backup.BackupManager
import com.ijunes.mefirst.settings.presentation.SettingsScreenProvider
import com.ijunes.mefirst.settings.presentation.SettingsScreenProviderImpl
import com.ijunes.mefirst.settings.presentation.SettingsViewModel
import com.ijunes.mefirst.settings.presentation.SettingsViewModelImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    single<AlarmScheduler> { MidnightAlarmScheduler }
    single { BackupManager(androidContext(), get()) }
    viewModel<SettingsViewModel> { SettingsViewModelImpl(androidApplication()) }
    single<SettingsScreenProvider> { SettingsScreenProviderImpl() }
}
