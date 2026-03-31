package com.ijunes.mefirst.di

import com.ijunes.mefirst.common.state.ModeStateHolder
import com.ijunes.mefirst.common.state.OnboardingStateHolder
import com.ijunes.mefirst.common.state.SettingsStateHolder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { ModeStateHolder(androidContext()) }
    single { OnboardingStateHolder(androidContext()) }
    single { SettingsStateHolder(androidContext()) }

}
