package com.livewire.audax.utils

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.content.Context
import android.os.Build
import android.os.Environment
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import com.livewire.audax.R
import java.text.DecimalFormat
import java.io.File


object Utils {

    private val formatterCommasRounded = DecimalFormat("#,###")
    private val formatterCommasDecimal = DecimalFormat("#,###.##")
    private const val MINUTES_PER_DAY = 24 * 60

    /**
     * Formatting
     */

    fun formatAddress(house: String?, street: String?, city: String?, state: String?, countryCode: String?, postalCode: String?, singleLine: Boolean = false): String? {
        val address = when {
            house.isNullOrBlank() && street.isNullOrBlank() -> null
            house.isNullOrBlank() -> street
            street.isNullOrBlank() -> house
            else -> "$house $street"
        }

        return formatAddress(address, city, state, countryCode, postalCode, singleLine)
    }

    fun formatAddress(address: String?, city: String?, stateCode: String?, countryCode: String?, zipCode: String?, singleLine: Boolean = false): String? {
        val formattedAddress = formatAddressCityState(address, city, stateCode, singleLine)

        val countryZip = when {
            countryCode.isNullOrBlank() -> zipCode
            zipCode.isNullOrBlank() -> countryCode
            else -> "$zipCode, $countryCode"
        }

        return when {
            formattedAddress.isNullOrBlank() -> countryZip
            zipCode.isNullOrBlank() -> formattedAddress
            else -> "$formattedAddress $countryZip"
        }
    }


    fun formatAddressCityStateWithLocationDescription(locationDescription: String?, address: String?, city: String?, stateCode: String?, singleLine: Boolean = false): String? {
        val addressCityState = formatAddressCityState(address, city, stateCode, singleLine)

        return when {
            locationDescription.isNullOrBlank() -> addressCityState
            addressCityState.isNullOrBlank() -> locationDescription
            else -> locationDescription + (if (singleLine) ", " else "\n") + addressCityState
        }
    }

    fun formatAddressCityState(address: String?, city: String?, stateCode: String?, singleLine: Boolean = false): String? {
        val cityState = formatCityState(city, stateCode)

        return when {
            address.isNullOrBlank() -> cityState
            cityState.isNullOrBlank() -> address
            else -> address + (if (singleLine) ", " else "\n") + cityState
        }
    }

    fun formatCityState(city: String?, stateCode: String?): String? {
        return when {
            city.isNullOrBlank() && stateCode.isNullOrBlank() -> null
            city.isNullOrBlank() -> stateCode
            stateCode.isNullOrBlank() -> city
            else -> "$city, $stateCode"
        }
    }

    fun formatCityStateZip(city: String?, stateCode: String?, zip: String?): String? {
        val zipText = if (zip == null) "" else " $zip"
        return "${formatCityState(city, stateCode)}$zipText"
    }


    /***
     * Split duration in separate parts: 3d7hr, or 5hr3min, or 2min
     */
    data class DurationParts(val value1: Int, @StringRes val unit1: Int, val value2: Int?, @StringRes val unit2: Int?)


    /***
     * Format duration for ride record: HH:MM:SS
     */
    fun formatDuration(context: Context?, seconds: Int): String {
        context ?: return ""

        if (seconds < 0) {
            return context.getString(R.string.time_span_none)
        }

        val minutes = (seconds / 60)
        val hours = (minutes / 60)

        return context.getString(R.string.time_span, hours, minutes % 60, seconds % 60)
    }

    /**
     * Format duration like "3H 32MIN"
     */
    /*fun formatDurationWithLabels(context: Context?, minutes: Int, compact: Boolean = false): String {
        context ?: return ""

        val hourLabel = if (compact) R.string.hr_abbrev else R.string.hr_abbrev_long
        val spacer = if (compact) "" else " "
        //val parts = splitDuration(minutes * 60, hourLabel, false) ?: return ""

        return if (parts.value2 == null || parts.unit2 == null) {
            "${parts.value1}$spacer${context.getString(parts.unit1).toLowerCase()}"
        } else {
            "${parts.value1}$spacer${context.getString(parts.unit1).toLowerCase()} ${parts.value2}$spacer${context.getString(parts.unit2).toLowerCase()}"
        }
    }*/

    fun formatChallengePoints(points: Int): String {
        return formatterCommasRounded.format(points)
    }

    fun formatChallengeCheckins(checkins: Int): String {
        return formatterCommasRounded.format(checkins)
    }

    fun getAppVersion(context: Context?) = context?.packageManager
            ?.getPackageInfo(context.packageName, 0)
            ?.versionName
            ?: "unknown"

    fun getAppVersionAndCode(context: Context?): String {
        val info = context?.packageManager?.getPackageInfo(context.packageName, 0) ?: return ""

        return "${info.versionName} (${info.versionCode})"
    }

    /**
     * Handles dynamically setting the app bar transparency and elevation on different API levels
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setViewElevated21(view: View, elevation: Float) {
        val stateListAnimator = StateListAnimator()
        stateListAnimator.addState(IntArray(0), ObjectAnimator.ofFloat(view, "elevation", elevation))
        view.stateListAnimator = stateListAnimator
    }

    private fun setViewElevation(view: View, elevation: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            setViewElevated21(view, elevation)
        } else {
            ViewCompat.setElevation(view, elevation)
        }
    }

    fun makeEmailPrivate(email: String): String {
        val at = email.indexOf('@')
        if (at < 0) {
            return email
        }

        var max = email.indexOf('.', at)
        if (max < 0) {
            max = email.length - 1
        }

        val left = email.substring(0, Math.min(3, at))
        val right = email.substring(at, Math.min(max, at + 4))

        return "$left...$right..."
    }

    fun getCustomInputFilter(allowCharacters: Boolean, allowDigits: Boolean, allowSpaceChar: Boolean): InputFilter {
        return object : InputFilter {
            override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
                var keepOriginal = true
                val sb = StringBuilder(end - start)
                for (i in start until end) {
                    val c = source[i]
                    if (isCharAllowed(c)) {
                        sb.append(c)
                    } else {
                        keepOriginal = false
                    }
                }
                if (keepOriginal) {
                    return null
                } else {
                    if (source is Spanned) {
                        val sp = SpannableString(sb)
                        TextUtils.copySpansFrom(source, start, sb.length, null, sp, 0)
                        return sp
                    } else {
                        return sb
                    }
                }
            }

            private fun isCharAllowed(c: Char): Boolean {
                if (Character.isLetter(c) && allowCharacters) {
                    return true
                }
                if (Character.isDigit(c) && allowDigits) {
                    return true
                }
                return Character.isSpaceChar(c) && allowSpaceChar
            }
        }
    }





    fun getBadgeCountText(count: Int): String {
        return when {
            count < 99 -> count.toString()
            else -> "99+"
        }
    }



    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / context.resources.displayMetrics.density
    }

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    fun getImageFile(context: Context, filename: String): File {
        var cacheDir: File? = context.cacheDir

        if (isExternalStorageWritable()) {
            cacheDir = context.externalCacheDir
        }

        val dir = File(cacheDir, "HDIMAGES")
        if (!dir.exists()) dir.mkdirs()


  //      var file = context.getDir("Images", Context.MODE_PRIVATE)
        var file = File(dir, filename)

        // If the file doesn't exist it could be in our local file store

        if (!file.exists()) {
            val directory = context.cacheDir
            file = File(directory, filename)
        }

        return file
    }
}
