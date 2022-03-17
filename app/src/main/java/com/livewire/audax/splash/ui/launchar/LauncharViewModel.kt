package com.livewire.audax.splash.ui.launchar

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.livewire.audax.R
import com.livewire.audax.authentication.AccountDataSource

class LauncharViewModel (private val dataSource: AccountDataSource
) : ViewModel() {
    // TODO: Implement the ViewModel


    sealed class LoginResult {
        object Success : LoginResult()
        class Failure(@StringRes val message: Int) : LoginResult()
        class ChangePasswordRequired(val regToken: String) : LoginResult()
    }

    // Cant use SingleLiveEvent as there are now multiple instances of fragment possible in app
    fun login(email: String, password: String, completion: (LoginResult) -> Unit) {

        dataSource.login(email, password, {
            Log.i(TAG, "Login Success")
            completion(LoginResult.Success)
        }, {
            Log.e(TAG, "Error Logging in")
            completion(LoginResult.Failure(it))
        }, {
            completion(LoginResult.ChangePasswordRequired(it))
        })
    }

    companion object {
        private val TAG = "LauncharViewModel"
    }
}