package com.livewire.app.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.livewire.app.R
import com.livewire.app.authentication.ui.resetpassword.ResetPasswordFragment

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