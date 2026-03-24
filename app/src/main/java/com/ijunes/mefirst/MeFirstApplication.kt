package com.ijunes.mefirst

import android.app.Application
import com.ijunes.mefirst.settings.alarm.MidnightAlarmScheduler
import com.ijunes.mefirst.common.state.SettingsStateHolder
import com.ijunes.mefirst.di.databaseModule
import com.ijunes.mefirst.di.repositoryModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MeFirstApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MeFirstApplication)
            androidLogger()
            modules(databaseModule, repositoryModule)
        }
        val settings: SettingsStateHolder = get()
        MidnightAlarmScheduler.schedule(this, settings.flushHour.value, settings.flushMinute.value)
    }
}