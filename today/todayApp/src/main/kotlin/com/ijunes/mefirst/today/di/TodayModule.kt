package com.ijunes.mefirst.today.di

import com.ijunes.today.data.TodayRepository
import com.ijunes.today.data.WorkTodayRepository
import com.ijunes.today.presentation.TodayScreenProvider
import com.ijunes.today.presentation.TodayViewModel
import com.ijunes.mefirst.today.presentation.TodayScreenProviderImpl
import com.ijunes.mefirst.today.presentation.TodayScreenViewModelImpl
import com.ijunes.mefirst.today.repository.TodayRepositoryImpl
import com.ijunes.mefirst.today.repository.WorkTodayRepositoryImpl
import com.ijunes.mefirst.common.state.ModeStateHolder
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val todayModule = module {
    factory<TodayRepository> { TodayRepositoryImpl(get()) }
    factory<WorkTodayRepository> { WorkTodayRepositoryImpl(get()) }
    viewModel<TodayViewModel> { TodayScreenViewModelImpl(androidApplication(), get(), get(), get<ModeStateHolder>()) }
    single<TodayScreenProvider> { TodayScreenProviderImpl() }
}
