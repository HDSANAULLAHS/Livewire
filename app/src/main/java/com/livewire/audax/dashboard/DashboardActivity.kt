package com.livewire.audax.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.livewire.audax.R
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerActivity


class DashboardActivity : AppCompatActivity() {
    protected var mUnityPlayer: UnityPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val intent = Intent(this, UnityPlayerActivity::class.java)
        startActivity(intent)


        /*setContentView(mUnityPlayer)
        mUnityPlayer!!.requestFocus()*/
    }
}