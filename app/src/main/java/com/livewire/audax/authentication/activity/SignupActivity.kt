package com.livewire.audax.authentication.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.livewire.audax.R
import com.livewire.audax.authentication.ui.signup.SignupFragment

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SignupFragment.newInstance())
                .commitNow()
        }

    }

}