package com.livewire.app.model

import androidx.annotation.Keep

@Keep
open class Response(val error: Response.Error? = null) {

    @Keep
    class Error(val code: String?, val exceptionCause: String?, val description: String?)
}

