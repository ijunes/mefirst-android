package com.ijunes.mefirst.settings.di

import com.ijunes.mefirst.settings.backup.BackupManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val settingsModule = module {
    single { BackupManager(androidContext(), get()) }
}
