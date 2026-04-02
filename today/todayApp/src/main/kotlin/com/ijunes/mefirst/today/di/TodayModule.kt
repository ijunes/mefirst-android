package com.ijunes.mefirst.today.di

import com.ijunes.mefirst.today.recording.AudioRecordingManager
import com.ijunes.today.data.TodayRepository
import com.ijunes.today.presentation.TodayScreenProvider
import com.ijunes.today.presentation.TodayViewModel
import com.ijunes.mefirst.today.presentation.TodayScreenProviderImpl
import com.ijunes.mefirst.today.presentation.TodayScreenViewModelImpl
import com.ijunes.mefirst.today.repository.TodayRepositoryImpl
import com.ijunes.mefirst.common.state.ModeStateHolder
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val todayModule = module {
    factory<TodayRepository> { TodayRepositoryImpl(get()) }
    factory { AudioRecordingManager(androidApplication()) }
    viewModel<TodayViewModel> {
        TodayScreenViewModelImpl(androidApplication(), get(), get<ModeStateHolder>(), get())
    }
    single<TodayScreenProvider> { TodayScreenProviderImpl() }
}
