package com.livewire.audax.model

import androidx.annotation.Keep
import com.livewire.audax.gigya.GigyaApi

@Keep
class User(var uID: String = "",
           var firstName: String = "",
           var middleName: String = "",
           var lastName: String = "",
           var photoURL: String = "",
           var birthDay: Int = 0,
           var birthMonth: Int = 0,
           var birthYear: Int = 0,
           var email: String = "",
           var preferredDealerID: String = "",
           var preferredDealerName: String = "",
           var hogMemberID: String = "",
           var addressLine1: String = "",
           var addressLine2: String = "",
           var addressLine3: String = "",
           var addressZip: String = "",
           var addressCity: String = "",
           var addressState: String = "",
           var addressCountry: String = "",
           var uidSignature: String = "",
           var signatureTimestamp: String = "",
           var mobilePhoneNumber: String = "",
           var emailOptIn: Boolean = false,
           var bikesOwned: List<GigyaApi.GigyaBike>? = null) {

    val firstLastName: String
        get() = "$firstName $lastName"

    val fullNameSingleLine: String
        get() = if (middleName.isEmpty()) "$firstName $lastName" else "$firstName $middleName $lastName"

    val fullNameMultiLine: String
        get() = if (middleName.isEmpty()) "$firstName\n$lastName" else "$firstName\n$middleName\n$lastName"

    val firstNameLastInitial: String
        get() = if (lastName.isNotEmpty()) "$firstName ${lastName.substring(0, 1)}." else firstName

}
