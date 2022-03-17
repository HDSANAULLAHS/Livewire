package com.livewire.audax.di.module

import com.livewire.audax.authentication.AccountDataSource
import com.livewire.audax.data.repository.MainRepository
import org.koin.dsl.module

val repoModule = module {
    single {
        MainRepository(get())

    }
    single {
        AccountDataSource(get(),get(),get(),get(),get(),get(),get())
    }
}