package com.livewire.audax.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.livewire.app.profile.ui.edit.EditProfileFragment
import com.livewire.audax.R

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, EditProfileFragment.newInstance())
                .commitNow()
        }
    }
}