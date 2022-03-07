package com.livewire.app.model

import androidx.annotation.Keep

@Keep
open class BaseResponse<out T: BaseResponse.Data>(val data: T? = null, val error: BaseResponse.Error? = null) {
    @Keep
    open class Data(val responseType: String? = null)

    @Keep
    class Error(val code: Int, val exceptionCause: String, val description: String)
}

