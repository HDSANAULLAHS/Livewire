package com.livewire.app.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.livewire.app.R
import com.livewire.app.authentication.AccountDataSource
import com.livewire.app.authentication.activity.LoginActivity
import com.livewire.app.authentication.OneTapSignIn
import com.livewire.app.authentication.UserViewModel
import com.livewire.app.dashboard.DashboardActivity
import com.livewire.app.store.SharedPreference
import org.koin.android.ext.android.inject

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val userAccountDataSource: AccountDataSource by inject()
    private val oneTap: OneTapSignIn by lazy { OneTapSignIn(this) }
    private val user: UserViewModel by inject()
    companion object {
        const val TAG = "SplashActivity"
        const val SPLASH_DELAY = 1500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        val sharedPreference:SharedPreference=SharedPreference(this)
        val email = sharedPreference.getStringValue("Email")
        println(email)
        // Idle time
        Handler().postDelayed({
            if (email == null) {
                loginScreen()
            }else{
                //startMainApp()
                oneTap.handleCredentials = this::handleOneTapCredentials
            }
        }, SPLASH_DELAY)

    }
    private fun loginScreen(){
        if (isFinishing){
            return
        }
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startMainApp() {
        if (isFinishing) {
            return
        }
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun handleOneTapCredentials(username: String, password: String) {

        userAccountDataSource.login(username, password, {
            Log.i("OneTap", "Login success")
            //currentTab?.stopLoading()
            startMainApp()
        }, {
            Log.e("OneTap", "Login failed")
            //currentTab?.stopLoading()
        }, {
            Log.e("OneTap", "Change password required")
            //currentTab?.stopLoading()
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        oneTap.onActivityResult(requestCode, resultCode, data)
    }
}
