package com.livewire.app.dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.livewire.app.R

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
    }
}