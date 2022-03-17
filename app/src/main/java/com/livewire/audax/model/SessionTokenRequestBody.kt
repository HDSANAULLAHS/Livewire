package com.livewire.audax.model

import androidx.annotation.Keep

@Keep
class SessionTokenRequestBody(val UID: String, val apiKey: String, val secret: String)
