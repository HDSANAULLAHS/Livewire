package com.livewire.audax.homescreen

import android.content.Intent
import android.content.res.Configuration
import android.graphics.ColorSpace
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.livewire.audax.R
import com.livewire.audax.homescreen.adapter.CustomSpinnerAdapter
import com.livewire.audax.homescreen.model.SpinnerModel
import com.livewire.audax.profile.ProfileActivity
import com.unity3d.player.UnityPlayer
import kotlinx.android.synthetic.main.activity_dashboard.*


class HomeScreenActivity : AppCompatActivity() {
lateinit var mUnityPlayer : UnityPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        /*val modelList :List<SpinnerModel> = readData()
        val customSpinnerAdapter = CustomSpinnerAdapter(this, modelList)
        spnr_bike_name.adapter = customSpinnerAdapter*/

        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 630)
        mUnityPlayer = UnityPlayer(this)
        fl_3d_bike.addView(mUnityPlayer, 0, layoutParams)
        mUnityPlayer.requestFocus()
        //mUnityPlayer.resume()
        mUnityPlayer.windowFocusChanged(true)


        rl_user_profile.setOnClickListener {

            //mUnityPlayer.quit()
            //mUnityPlayer.destroy()
            try {
                profile()
            }catch (exception: IllegalStateException){
                profile()
            }

        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    private fun profile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    override fun onNewIntent(var1: Intent) {
        super.onNewIntent(var1)
        this.intent = var1
        mUnityPlayer.newIntent(var1)
    }

    override fun onDestroy() {
        mUnityPlayer.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        mUnityPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        mUnityPlayer.resume()
    }

    override fun onStart() {
        super.onStart()
        mUnityPlayer.resume()
    }

    override fun onStop() {
        super.onStop()
        mUnityPlayer.pause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mUnityPlayer.lowMemory()
    }

    override fun onTrimMemory(var1: Int) {
        super.onTrimMemory(var1)
        if (var1 == 15) {
            mUnityPlayer.lowMemory()
        }
    }

    override fun onConfigurationChanged(var1: Configuration) {
        super.onConfigurationChanged(var1)
        mUnityPlayer.configurationChanged(var1)
    }

    /*private fun readData(): List<SpinnerModel> {

        val bufferReader = applicationContext.assets.open("api.json").bufferedReader()
        val json_str = bufferReader.use {
            it.readText()
        }

        val gson =Gson()
        val modelList : List<SpinnerModel> = gson.fromJson(json_str, Array<SpinnerModel>::class.java).toList()
        return modelList
    }*/

}