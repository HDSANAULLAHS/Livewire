package com.livewire.app.authentication.ui.login

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.livewire.app.R
import com.livewire.app.authentication.AccountDataSource
import com.livewire.app.authentication.AccountFieldValidator

class LoginViewModel (private val dataSource:AccountDataSource,
                      private val validator: AccountFieldValidator) : ViewModel() {
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

    fun checkPassword(email: String, password: String, success: () -> Unit, error: (Int) -> Unit) {
        val validationError = validateCredentials(email, password)
        if (validationError != 0) {
            error(validationError)
            return
        }
        dataSource.checkPassword(email, password, {
            success()

            Log.i(TAG, "Check password success")
        }, {
            error(it)

            Log.e(TAG, "Check password failed")
        })
    }

    private fun validateCredentials(email: String, password: String): Int {
        val error = validator.validateEmail(email)
        if (error != 0) {
            return error
        }

        if (password.isEmpty()) {
            return R.string.missing_password
        }

        return 0
    }

    companion object {
        private val TAG = "LoginViewModel"
    }
}