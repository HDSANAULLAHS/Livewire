package com.livewire.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import com.livewire.app.authentication.GigyaManager
import com.livewire.app.di.module.MviewModelodule
import com.livewire.app.di.module.appModule
import com.livewire.app.di.module.networkModule
import com.livewire.app.di.module.repoModule
import com.livewire.app.session.SessionTokenStore
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class App : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {
    private val gigya: GigyaManager by inject()
    private val session: SessionTokenStore by inject()


    companion object {
        val TAG = "appApplication"
    }

    override fun onCreate() {
        super<Application>.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(appModule, repoModule, MviewModelodule,networkModule))
        }

        gigya.initialize(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onActivityStarted(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityResumed(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityPaused(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityStopped(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(activity: Activity) {
        TODO("Not yet implemented")
    }
}
