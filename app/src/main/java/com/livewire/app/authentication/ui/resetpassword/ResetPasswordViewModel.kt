package com.livewire.app.authentication.ui.resetpassword

import android.util.Log
import androidx.lifecycle.ViewModel
import com.livewire.app.authentication.AccountDataSource
import com.livewire.app.authentication.AccountFieldValidator
import com.livewire.app.utils.ifNonZero

class ResetPasswordViewModel (private val dataSource: AccountDataSource,
                                private val validator: AccountFieldValidator): ViewModel() {
    // TODO: Implement the ViewModel

    companion object {
        private val TAG = "ResetPasswordViewModel"
    }

    fun resetPassword(email: String, success: () -> Unit, failed: (Int, Boolean) -> Unit) {
        validator.validateEmail(email).ifNonZero {
            failed(it, false)
            return
        }

        dataSource.resetPassword(email, {
            Log.i(TAG, "Password Reset Success")

            success()
        }, {
            Log.e(TAG, "Error Resetting Password")

            failed(it, true)
        })
    }
}