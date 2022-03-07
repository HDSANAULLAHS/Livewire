package com.livewire.app.authentication

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.livewire.app.model.*
import com.livewire.app.session.SessionTokenStore
import com.livewire.app.utils.SingleLiveEvent
import java.util.*

open class UserViewModel(private val session: SessionTokenStore) : ViewModel() {
    private val _isLoggedIn = MutableLiveData<Boolean>()
    private val _profile = MutableLiveData<User>()
    val authenticated = SingleLiveEvent<Boolean>()

    init {
        _isLoggedIn.value = session.isUserLoggedIn
    }

    open val isLoggedIn: LiveData<Boolean>
        get() = _isLoggedIn

    open val profile: LiveData<User>
        get() = _profile


    val userEmail: String?
        get() = profile.value?.email


    fun updateUser(action: (User) -> Unit) {
        val user = _profile.value

        if (user != null) {
            action(user)

            setUser(user)
        }
    }
    fun setUser(user: User, new: Boolean = false) {
        _profile.value = user

        if (new) {
            authenticated.postValue(true)
        }
    }

    fun setIsLoggedIn(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun clear() {
        _isLoggedIn.postValue(null)
        _profile.postValue(null)

    }

}

