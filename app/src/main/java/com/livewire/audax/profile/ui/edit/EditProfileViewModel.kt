package com.livewire.audax.profile.ui.edit

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.livewire.audax.R
import com.livewire.audax.authentication.AccountDataSource
import com.livewire.audax.authentication.AccountFieldValidator
import com.livewire.audax.authentication.UserViewModel
import com.livewire.audax.model.User
import com.livewire.audax.utils.SingleLiveEvent
import java.util.*

class EditProfileViewModel (val dataSource: AccountDataSource,
                            val userViewModel: UserViewModel,
                            val validator: AccountFieldValidator): ViewModel() {
    // TODO: Implement the ViewModel
    companion object {
        private val TAG = "EditProfileViewModel"
    }

    var first = ""
    var middle = ""
    var last = ""
    var addressName = ""
    var addressOne = ""
    var addressTwo = ""
    var zipCode = ""
    var city = ""
    var state = ""
    var country = ""
    var emailOptIn = false
    var mobilePhoneNumber = ""
    val birthDate = MutableLiveData<Calendar>()

    private val user: User?
        get() = userViewModel.profile.value

    init {
        user?.let {
            first = it.firstName
            last = it.lastName
            middle = it.middleName
            addressOne = it.addressLine1
            addressTwo = it.addressLine2
            zipCode = it.addressZip
            city = it.addressCity
            state = it.addressState
            country = it.addressCountry
            emailOptIn = it.emailOptIn
            mobilePhoneNumber = it.mobilePhoneNumber
        }
    }

    val canEditEmail: Boolean
        get() = userViewModel.profile.value?.uidSignature != null && userViewModel.profile.value?.signatureTimestamp != null

    private val _isAddressDataChanged = MutableLiveData<Boolean>()
    val isAddressDataChanged: LiveData<Boolean>
        get() = _isAddressDataChanged

    private val _navigateAddressScreenBackEvent = SingleLiveEvent<Unit>()
    val navigateAddressScreenBackEvent: LiveData<Unit>
        get() = _navigateAddressScreenBackEvent

    private val _confirmToDiscardAddressChanges = SingleLiveEvent<Unit>()
    val confirmDiscardAddressChanges: LiveData<Unit>
        get() = _confirmToDiscardAddressChanges

    fun onNavigateBackAddressScreenAction() {
        if (isAddressDataChanged.value == true) {
            _confirmToDiscardAddressChanges.call()
        } else {
            _navigateAddressScreenBackEvent.call()
        }
    }

    fun onDiscardAddressChangesConfirmed() {
        _isAddressDataChanged.value = false
        _navigateAddressScreenBackEvent.call()
    }

    fun onAddressDataChanged() {
        _isAddressDataChanged.value = true
    }

    fun onAddressUpdatedSuccessfully() {
        _isAddressDataChanged.value = false
        _navigateAddressScreenBackEvent.call()
    }

    fun uploadProfilePicture(uri: Uri, success: (String) -> Unit, error: (Int) -> Unit) {
        dataSource.uploadPicture(uri, {
            updateLocalPhotoUrl(uri.toString())
            success(uri.toString())
        }, {
            error(R.string.cant_upload_picture)
        })
    }

    fun removeProfilePicture(success: () -> Unit, error: (Int) -> Unit) {
        dataSource.removePicture({
            updateLocalPhotoUrl("")
            success()
        }, {
            error(R.string.cant_remove_picture)
        })
    }

    private fun updateLocalPhotoUrl(url: String) {
        userViewModel.updateUser { it.photoURL = url }
    }

    /*private fun validateAddress(): Int {
        validator.validateName(first, last).ifNonZero { return it }
        validator.validatePostalCode(zipCode).ifNonZero { return it }
        if (addressOne.isBlank()) return R.string.missing_address
        if (city.isBlank()) return R.string.missing_city
        if (state.isBlank()) return R.string.missing_state
        if (mobilePhoneNumber.isNotBlank() && mobilePhoneNumber.any { it.isLetter() }) return R.string.invalid_mobile_number
        validator.validateBirthday(birthDate.value).ifNonZero { return it }

        return 0
    }
*/
    fun updateProfile(success: () -> Unit, error: (Int) -> Unit) {
        /*validate().ifNonZero {
            error(it)
            return
        }*/

        val user = userViewModel.profile.value ?: return
        val birthDate = this.birthDate.value ?: return

        user.firstName = first
        user.middleName = middle
        user.addressLine1 = addressOne
        user.addressLine2 = addressTwo
        user.addressCity = city
        user.lastName = last
        user.addressZip = zipCode
        user.emailOptIn = emailOptIn
        user.addressState = state
        user.addressCountry = "US"
        user.mobilePhoneNumber = mobilePhoneNumber

        user.birthDay = birthDate.get(Calendar.DAY_OF_MONTH)
        user.birthMonth = birthDate.get(Calendar.MONTH) + 1
        user.birthYear = birthDate.get(Calendar.YEAR)

        dataSource.updateProfile(user, {
            Log.i(TAG, "User Profile Update Success")

            success()
        }, {
            Log.e(TAG, "User Profile Update $it")

            error(it)
        })
    }

    fun updateAddress(success: () -> Unit, error: (Int) -> Unit) {

        /*validateAddress().ifNonZero {
            error(it)
            return
        }*/

        val user = userViewModel.profile.value!!
        val birthDate = this.birthDate.value!!

        user.firstName = first
        user.middleName = middle
        user.addressLine1 = addressOne
        user.addressLine2 = addressTwo
        user.addressCity = city
        user.lastName = last
        user.addressZip = zipCode
        user.emailOptIn = emailOptIn
        user.addressState = state
        user.addressCountry = "US"
        user.mobilePhoneNumber = mobilePhoneNumber

        user.birthDay = birthDate.get(Calendar.DAY_OF_MONTH)
        user.birthMonth = birthDate.get(Calendar.MONTH) + 1
        user.birthYear = birthDate.get(Calendar.YEAR)

        dataSource.updateProfile(user, {
            success()
        }, {
            error(it)
        })
    }


}