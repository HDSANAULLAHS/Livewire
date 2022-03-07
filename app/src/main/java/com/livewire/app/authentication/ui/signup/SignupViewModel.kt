package com.livewire.app.authentication.ui.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import com.livewire.app.authentication.AccountFieldValidator
import com.livewire.app.authentication.AccountDataSource
import com.livewire.app.model.User
import com.livewire.app.utils.CustomDialog
import com.livewire.app.utils.ifNonZero
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
    var postalCode = ""

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
        user.birthDay = (Calendar.DAY_OF_MONTH)
        user.birthMonth = (Calendar.MONTH) + 1 // Gigya starts from 1
        user.birthYear = (Calendar.YEAR)
        user.addressZip = postalCode
        user.emailOptIn = emailOptIn

        dataSource.createAccount(user, password, {
            success()
            Log.i(TAG, "Account Created Successfully")
        }, {
            error(it)
            Log.e(TAG, "Account Creation Error")
        })
    }
}