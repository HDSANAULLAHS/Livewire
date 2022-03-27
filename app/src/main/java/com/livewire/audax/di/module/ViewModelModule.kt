package com.livewire.audax.di.module

import com.livewire.app.profile.ui.edit.EditProfileViewModel
import com.livewire.audax.authentication.ui.login.LoginViewModel
import com.livewire.audax.authentication.ui.resetpassword.ResetPasswordViewModel
import com.livewire.audax.authentication.ui.signup.SignupViewModel
import com.livewire.audax.profile.ui.changepassword.ChangePasswordViewModel
import com.livewire.audax.profile.ui.profile.ProfileViewModel
import com.livewire.audax.splash.ui.launchar.LauncharViewModel
import com.livewire.audax.ui.main.viewmodel.MainViewModel
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
    viewModel { EditProfileViewModel(get(), get(), get()) }
    viewModel { ProfileViewModel(get(),get(),get()) }
    viewModel { LauncharViewModel(get()) }
    viewModel { ChangePasswordViewModel(get()) }
}