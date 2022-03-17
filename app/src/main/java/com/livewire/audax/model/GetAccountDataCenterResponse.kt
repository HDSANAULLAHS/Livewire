package com.livewire.audax.model

import androidx.annotation.Keep

@Keep
class GetAccountDataCenterResponse(val errorCode: Int,
                                   val hasFullAccount: Boolean,
                                   val hasLiteAccount: Boolean, val dc: String?)
