package com.livewire.audax.profile.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.livewire.audax.R
import com.livewire.audax.profile.ui.changepassword.ChangePasswordFragment

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_password_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ChangePasswordFragment.newInstance())
                .commitNow()
        }
    }
}