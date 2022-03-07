package com.livewire.app.model

import androidx.annotation.Keep

@Keep
class SessionTokenRequestResponse(val jwt: String?) : Response()
