package com.livewire.app.di.module

import com.livewire.app.authentication.AccountDataSource
import com.livewire.app.data.repository.MainRepository
import org.koin.dsl.module

val repoModule = module {
    single {
        MainRepository(get())

    }
    single {
        AccountDataSource(get(),get(),get(),get(),get(),get(),get(),get(),get())
    }
}