package com.livewire.audax.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.livewire.audax.R
import com.livewire.audax.profile.ui.profile.ProfileFragment


class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ProfileFragment.newInstance())
                .commitNow()
        }
    }
}