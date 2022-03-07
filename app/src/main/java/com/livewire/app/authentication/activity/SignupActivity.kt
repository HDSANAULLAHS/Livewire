package com.livewire.app.authentication.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.livewire.app.R
import com.livewire.app.authentication.ui.signup.SignupFragment
import org.koin.android.ext.android.inject

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