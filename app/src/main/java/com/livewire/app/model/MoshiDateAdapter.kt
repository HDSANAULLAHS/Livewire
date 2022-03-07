package com.livewire.app.net

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.joda.time.DateTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.getOrSet

class MoshiDateAdapter {

    private val _dateTimeFormat = ThreadLocal<SimpleDateFormat>()
    private val _dateOnlyFormat = ThreadLocal<SimpleDateFormat>()

    private val dateTimeFormat: SimpleDateFormat
        get() = _dateTimeFormat.getOrSet { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.US) }

    private val dateOnlyFormat: SimpleDateFormat
        get() = _dateOnlyFormat.getOrSet { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    @FromJson
    @Throws(ParseException::class)
    internal fun fromJson(date: String?): Date? {
        var dateToParse = date ?: return null

        if (dateToParse.endsWith("Z")) {
            dateToParse = dateToParse.replace("Z", "+0000")
        }

        return when {
            dateToParse.isBlank() -> null
            dateToParse.contains(":") -> dateTimeFormat.parse(dateToParse)
            else -> dateOnlyFormat.parse(dateToParse)
        }
    }

    @ToJson
    internal fun toJson(date: Date?): String? {
        return when (date) {
            null -> null
            else -> dateTimeFormat.format(date)
        }
    }

}
