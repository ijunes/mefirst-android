package com.ijunes.mefirst.entries.di

import com.ijunes.mefirst.entries.repository.EntriesRepositoryImpl
import com.ijunes.mefirst.entries.repository.WorkEntriesRepositoryImpl
import org.koin.dsl.module

val entriesModule = module {
    factory { EntriesRepositoryImpl(get()) }
    factory { WorkEntriesRepositoryImpl(get()) }
}
