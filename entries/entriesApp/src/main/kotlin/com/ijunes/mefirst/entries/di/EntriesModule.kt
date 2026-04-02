package com.ijunes.mefirst.entries.di

import com.ijunes.entries.data.EntriesRepository
import com.ijunes.entries.presentation.EntriesScreenProvider
import com.ijunes.entries.presentation.EntriesViewModel
import com.ijunes.mefirst.entries.presentation.EntriesScreenProviderImpl
import com.ijunes.mefirst.entries.presentation.EntriesScreenViewModelImpl
import com.ijunes.mefirst.entries.repository.EntriesRepositoryImpl
import com.ijunes.mefirst.common.state.ModeStateHolder
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val entriesModule = module {
    factory<EntriesRepository> { EntriesRepositoryImpl(get()) }
    viewModel<EntriesViewModel> { EntriesScreenViewModelImpl(get(), get<ModeStateHolder>()) }
    single<EntriesScreenProvider> { EntriesScreenProviderImpl() }
}
