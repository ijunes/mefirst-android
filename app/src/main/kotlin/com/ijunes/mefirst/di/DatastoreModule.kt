package com.ijunes.mefirst.di

import com.ijunes.mefirst.main.AppModeViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { AppModeViewModel(androidApplication(), get()) }
}
