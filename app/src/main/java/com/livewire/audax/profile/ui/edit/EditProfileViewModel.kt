package com.livewire.app.profile.ui.edit

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
    var last = ""

    private val user: User?
        get() = userViewModel.profile.value

    init {
        user?.let {
            first = it.firstName
            last = it.lastName
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

        val user = userViewModel.profile.value ?: return

        user.firstName = first
        user.lastName = last

        dataSource.updateProfile(user, {
            Log.i(TAG, "User Profile Update Success")

            success()
        }, {
            Log.e(TAG, "User Profile Update $it")

            error(it)
        })
    }

   /* fun updateAddress(success: () -> Unit, error: (Int) -> Unit) {

        *//*validateAddress().ifNonZero {
            error(it)
            return
        }*//*

        val user = userViewModel.profile.value!!

        user.firstName = first
        user.lastName = last


        dataSource.updateProfile(user, {
            success()
        }, {
            error(it)
        })
    }
*/

}