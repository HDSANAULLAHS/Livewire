package com.livewire.audax.profile.ui.changepassword

import android.util.Log
import androidx.lifecycle.ViewModel
import com.livewire.audax.authentication.AccountDataSource

class ChangePasswordViewModel(private val dataSource: AccountDataSource) : ViewModel() {
    companion object {
        private const val TAG = "ChangePasswordViewModel"
    }

    var currentPassword = ""
    var newPassword = ""


    fun updatePassword(success: () -> Unit, error: (Int) -> Unit) {

        dataSource.updatePassword(currentPassword, newPassword, {
            Log.i(TAG, "Password Update Success")

            success()
        }, {
            Log.e(TAG, "Password Update Error")

            error(it)
        })
    }
}
