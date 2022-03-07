package com.livewire.app.authentication

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

class OneTapSignIn(val activity: Activity) {
    companion object {
        const val TAG = "OneTapSignIn"
        const val REQ_ONE_TAP = 987
    }

    private var temporarilyDisabled = false
    private val client = Identity.getSignInClient(activity)

    var handleCredentials: (username: String, password: String) -> Unit = { _, _ -> }

    fun display() {
        if (temporarilyDisabled) {
            return
        }

        val signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setAutoSelectEnabled(false)
                .build()

        client.beginSignIn(signInRequest)
                .addOnSuccessListener(activity) { result ->
                    try {
                        ActivityCompat.startIntentSenderForResult(activity,
                                result.pendingIntent.intentSender, REQ_ONE_TAP,
                                null, 0, 0, 0, null)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(activity) { e ->
                    // No saved credentials found. Launch the One Tap sign-up flow, or
                    // do nothing and continue presenting the signed-out UI.
                    Log.d(TAG, e.localizedMessage)
                }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = client.getSignInCredentialFromIntent(data)
                    val username = credential.id
                    val password = credential.password
                    when {
                        password != null -> {
                            Log.d(TAG, "Got password.")
                            handleCredentials(username, password)
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token or password!")
                        }
                    }
                } catch (e: ApiException) {
                    Log.d(TAG, "ApiException!")

                    if (e.statusCode == CommonStatusCodes.CANCELED) {
                        temporarilyDisabled = true
                    }
                }
            }
        }
    }
}