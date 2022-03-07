package com.livewire.app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.Log
import android.content.IntentFilter
import android.net.NetworkCapabilities
import android.os.Handler


class NetworkConnection(val context: Context) {
    companion object {
        val TAG = "NetworkConnection"
    }

    private val handler = Handler()
    private val _connected = MutableLiveData<Boolean>()
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val connected: LiveData<Boolean>
        get() = _connected

    val isConnected: Boolean
        get() {
            val connection = _connected.value

            return connection ?: activeNetworkStatus
        }


    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i(TAG, "Using NetworkCallback")
            watchWithCallback()
        } else {
            Log.i(TAG, "Using broadcast receiver")
            watchWithReceiver()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun watchWithCallback() {
        connectivityManager.registerDefaultNetworkCallback(object: ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.i(TAG, "onAvailable")

                // Android seems to sometimes call this just before the network is actually available.
                // Adding a small delay to work around this.
                handler.removeCallbacks(postConnectedRunnable)
                handler.postDelayed({
                    _connected.postValue(true)
                }, 1000)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.i(TAG, "onLost")

                handler.removeCallbacks(postConnectedRunnable)
                _connected.postValue(false)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.i(TAG, "onUnavailable")

                handler.removeCallbacks(postConnectedRunnable)
                _connected.postValue(false)
            }
        })
    }

    private val postConnectedRunnable = Runnable {
        _connected.postValue(true)
    }

    private fun watchWithReceiver() {
        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        context.registerReceiver(NetworkReceiver(), intentFilter)
    }

    inner class NetworkReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            return _connected.postValue(activeNetworkStatus)
        }
    }

    @Suppress("DEPRECATION")
    private val activeNetworkStatus: Boolean
        get() = connectivityManager.activeNetworkInfo?.isConnected ?: false

    fun isNetworkWifi(context: Context): Boolean {

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> false
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    return when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> false
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return false
    }
}
