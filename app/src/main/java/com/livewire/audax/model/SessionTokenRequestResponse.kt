package com.livewire.audax.model

import androidx.annotation.Keep

@Keep
class SessionTokenRequestResponse(val jwt: String?) : Response()
