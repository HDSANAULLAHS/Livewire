package com.livewire.audax.authentication.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.livewire.audax.R
import com.livewire.audax.authentication.ui.resetpassword.ResetPasswordFragment

class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset_password_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ResetPasswordFragment.newInstance())
                .commitNow()
        }
    }
}