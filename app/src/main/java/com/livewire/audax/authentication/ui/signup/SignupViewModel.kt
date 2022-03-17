package com.livewire.audax.authentication.ui.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import com.livewire.audax.authentication.AccountFieldValidator
import com.livewire.audax.authentication.AccountDataSource
import com.livewire.audax.model.User
import com.livewire.audax.utils.ifNonZero
import java.util.*

class SignupViewModel constructor(private val dataSource: AccountDataSource,
                                  val validator: AccountFieldValidator) : ViewModel() {
    // TODO: Implement the ViewModel

    companion object {
        private const val TAG = "CreateAccountViewModel"
    }

    var email = ""
    var password = ""
    var firstName = ""
    var lastName = ""
    var emailOptIn = false

    private fun validator(): Int {
        validator.validateEmail(email).ifNonZero { return it }
        validator.validatePassword(password, password).ifNonZero { return it }
        validator.validateName(firstName, lastName).ifNonZero { return it }

        return 0
    }

    fun createAccount(success: () -> Unit, error: (Int) -> Unit) {
        validator().ifNonZero {
            error(it)
            return
        }

//text value bind with model class
        val user = User()
        user.email = email
        user.firstName = firstName
        user.lastName = lastName

        dataSource.createAccount(user, password, {
            success()
            Log.i(TAG, "Account Created Successfully")
        }, {
            error(it)
            Log.e(TAG, "Account Creation Error")
        })
    }
}