package com.livewire.app.di.module

import com.livewire.app.authentication.ui.login.LoginViewModel
import com.livewire.app.authentication.ui.resetpassword.ResetPasswordViewModel
import com.livewire.app.authentication.ui.signup.SignupViewModel
import com.livewire.app.ui.main.viewmodel.MainViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val MviewModelodule = module {
    viewModel {
        MainViewModel(get(),get())
    }
    viewModel {
        SignupViewModel(get(),get())
    }
    viewModel {
        LoginViewModel(get(),get())
    }
    viewModel { ResetPasswordViewModel(get(),get()) }
}