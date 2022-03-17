package com.livewire.audax.model

import androidx.annotation.Keep

@Keep
class UpdateLoginIDRequest(val apiKey: String,
                           val UID: String,
                           val UIDSignature: String,
                           val signatureTimestamp: String)
