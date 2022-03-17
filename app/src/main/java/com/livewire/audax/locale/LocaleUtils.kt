package com.livewire.audax.locale

import android.content.Context

open class LocaleUtils(val context: Context, val localeManager: LocaleManager) {}

    /*fun formatWindSpeed(context: Context?, weather: WeatherData?): String {
        context ?: return ""
        weather ?: return ""

        // Weather data is already in the correct unit
        // but units from dark sky is based on te temp system

        val measurementSystem = localeManager.getCurrentMeasurementSystem()
        val temperatureDisplayType = localeManager.getCurrentTemperatureDisplayType()


        return when {
            temperatureDisplayType == TemperatureDisplayType.FAHRENHEIT && measurementSystem == MeasurementSystem.METRIC
                -> context.getString(R.string.weather_wind_mph, weather.windSpeedRounded)

            temperatureDisplayType == TemperatureDisplayType.CELSIUS && measurementSystem == MeasurementSystem.IMPERIAL
            -> context.getString(R.string.weather_wind_mph, weather.windSpeedRounded)

            // Convert KM/H to MPH
            // TODO we shouldnt have math like this in here... use geo functions
            temperatureDisplayType == TemperatureDisplayType.CELSIUS && measurementSystem == MeasurementSystem.IMPERIAL
                -> context.getString(R.string.weather_wind_kmph, weather.windSpeedRounded * (1 / Geo.KILOMETRES_PER_MILE))

            // Convert MPH to KM/H
            // TODO we shouldnt have math like this in here... use geo functions
            temperatureDisplayType == TemperatureDisplayType.FAHRENHEIT && measurementSystem == MeasurementSystem.METRIC
            -> context.getString(R.string.weather_wind_kmph, weather.windSpeedRounded * Geo.KILOMETRES_PER_MILE)

            else -> context.getString(R.string.weather_wind_mph, weather.windSpeedRounded)
        }
    }*/

   /* fun formatHDWindSpeed(context: Context?, windSpeedRounded: Int?): String {
        context ?: return ""
        windSpeedRounded ?: return ""

        // Weather data is already in the correct unit
        // but units from dark sky is based on te temp system

        val measurementSystem = localeManager.getCurrentMeasurementSystem()
        val temperatureDisplayType = localeManager.getCurrentTemperatureDisplayType()


        *//*return when {
            temperatureDisplayType == TemperatureDisplayType.FAHRENHEIT && measurementSystem == MeasurementSystem.METRIC
            -> context.getString(R.string.weather_wind_mph, windSpeedRounded)

            temperatureDisplayType == TemperatureDisplayType.CELSIUS && measurementSystem == MeasurementSystem.IMPERIAL
            -> context.getString(R.string.weather_wind_mph,windSpeedRounded)

            // Convert KM/H to MPH
            // TODO we shouldnt have math like this in here... use geo functions
            temperatureDisplayType == TemperatureDisplayType.CELSIUS && measurementSystem == MeasurementSystem.IMPERIAL
            -> context.getString(R.string.weather_wind_kmph, windSpeedRounded * (1 / Geo.KILOMETRES_PER_MILE))

            // Convert MPH to KM/H
            // TODO we shouldnt have math like this in here... use geo functions
            temperatureDisplayType == TemperatureDisplayType.FAHRENHEIT && measurementSystem == MeasurementSystem.METRIC
            -> context.getString(R.string.weather_wind_kmph, windSpeedRounded * Geo.KILOMETRES_PER_MILE)

            else -> context.getString(R.string.weather_wind_mph, windSpeedRounded)
        }
    }*//*

    *//*fun formatSpeed(rideLength: Double, rideDuration: Int): String {
        if (rideDuration == 0) {
            return formatSpeed(0)
        }

        // TODO we shouldnt have math like this in here... use geo functions
        return when (localeManager.getCurrentMeasurementSystem()) {
            MeasurementSystem.METRIC -> formatSpeed((rideLength * Geo.KILOMETRES_PER_MILE / (rideDuration / 60.0)).roundToInt())
            MeasurementSystem.IMPERIAL -> formatSpeed((rideLength / (rideDuration / 60.0)).roundToInt())
        }
    }

    fun formatSpeed(speed: Int): String {
        return when (localeManager.getCurrentMeasurementSystem()) {
            MeasurementSystem.METRIC -> context.getString(R.string.kmph, speed)
            MeasurementSystem.IMPERIAL -> context.getString(R.string.mph, speed)
        }
    }*//*

    *//**
     * Format value as 3.5 FT
     *//*
   *//* fun formatDistance(miles: Double, abbreviate: Boolean = true): String {

        val (value, units) = formatDistanceParts(miles, abbreviate)
        return "$value $units"
    }

    *//**//**
     * Format value as two seperate strings, like "3.5" and "FT" with no spaces or plurals
     *//**//*
    fun formatDistanceParts(miles: Double, abbreviate: Boolean = true): Pair<String, String> {

        return when (localeManager.getCurrentMeasurementSystem()) {
            MeasurementSystem.METRIC -> formatDistancePartsMetric(miles, abbreviate)
            MeasurementSystem.IMPERIAL -> formatDistancePartsImperial(miles, abbreviate)
        }

    }*//*

    *//**
     * Format value
     * this method does not return feet when miles is requested
     *//*
    *//*fun formatDistanceSimpleFormat(miles: Float): String {

        return when (localeManager.getCurrentMeasurementSystem()) {
            MeasurementSystem.METRIC -> formatDistanceKilometresOnly(miles)
            MeasurementSystem.IMPERIAL -> formatDistanceMilesOnly(miles)
        }
    }

    fun formatDistanceAway(miles: Float): String {
        val distanceText = formatDistanceSimpleFormat(miles)
        val away = context.getString(R.string.distance_away).toLowerCase()

        return "$distanceText $away"
    }*//*

    *//*fun formatDistanceWithin(value: Int): String {
        val text = when (localeManager.getCurrentMeasurementSystem()) {
            MeasurementSystem.METRIC -> R.string.within_km
            MeasurementSystem.IMPERIAL -> R.string.within_miles
        }

        return context.getString(text, value)
    }*//*

    fun ensureLocaleDistanceUnits(miles: Double): Double {
        return when (localeManager.getCurrentMeasurementSystem()) {
            MeasurementSystem.METRIC -> geo.milesToKm(miles)
            MeasurementSystem.IMPERIAL -> miles
        }
    }

    fun ensureMiles(value: Double): Double {
        return when (localeManager.getCurrentMeasurementSystem()) {
            MeasurementSystem.METRIC -> geo.kmToMiles(value)
            MeasurementSystem.IMPERIAL -> value
        }
    }

    fun unitsFormat() : String {
        return when (localeManager.getCurrentMeasurementSystem()) {
            MeasurementSystem.METRIC -> "KM"
            MeasurementSystem.IMPERIAL -> "MI"
        }
    }

    *//*fun getLastUpdated(): String {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.ROOT).parse(BuildConfig.BUILD_TIME) ?: return ""
        return dateFormat("MMMM d, yyyy").format(date)
    }*//*

   *//* private fun formatDistancePartsImperial(miles: Double, abbreviate: Boolean = true): Pair<String, String> {

        val feet = Math.round(miles * Geo.FEET_PER_MILE).toInt()

        val feetUnits = context.getString(if (abbreviate) R.string.feet_abbrev else R.string.feet)
        val milesUnits = context.getString(if (abbreviate) R.string.miles_abbrev else R.string.miles)

        return when {
            feet < 100 ->
                // Round feet to nearest 10
                Pair(((feet + 5) / 10 * 10).toString(), feetUnits)
            feet < 501 ->
                // Round feet to nearest 100
                Pair(((feet + 50) / 100 * 100).toString(), feetUnits)
            miles < 10 ->
                // Round miles to nearest tenth
                Pair((Math.round(miles * 10.0) / 10.0).toString(), milesUnits)
            else ->
                // Round miles to nearest whole
                Pair(Math.round(miles).toString(), milesUnits)
        }
    }

    private fun formatDistancePartsMetric(miles: Double, abbreviate: Boolean = true): Pair<String, String> {
        val metres = geo.milesToMeters(miles).roundToInt()
        val kilometres = (metres / 1000.0).roundToInt()
        val metresUnits = context.getString(if (abbreviate) R.string.metres_abbrev else R.string.metres)
        val kilometresUnits = context.getString(if (abbreviate) R.string.kilometres_abbrev else R.string.kilometres)

        return when {
            metres < 100 ->
                // Round feet to nearest 10
                Pair(((metres + 5) / 10 * 10).toString(), metresUnits)
            metres < 501 ->
                // Round feet to nearest 100
                Pair(((metres + 50) / 100 * 100).toString(), metresUnits)
            kilometres < 10 ->
                // Round miles to nearest tenth
                Pair((Math.round(kilometres * 10.0) / 10.0).toString(), kilometresUnits)
            else ->
                // Round miles to nearest whole
                Pair(kilometres.toString(), kilometresUnits)
        }

    }

    fun formatDistanceMilesOnly(miles: Float): String {

        if (localeManager.getCurrentMeasurementSystem() == MeasurementSystem.METRIC) {
            return formatDistanceKilometresOnly(miles)
        }

        val units = context.getString(R.string.miles_abbrev_lower)

        return when {
            miles >= 100 ->
                // Round to nearest mile
                "${Math.round(miles)} $units"
            else ->
                // Round miles to nearest tenth
                "${Math.round(miles * 10f) / 10f} $units"
        }
    }

    private fun formatDistanceKilometresOnly(miles: Float): String {
        val kilometres = geo.milesToKm(miles.toDouble())
        val units = context.getString(R.string.kilometres_abbrev_lower)

        return when {
            kilometres >= 100 ->
                // Round to nearest mile
                "${Math.round(kilometres)} $units"
            else ->
                // Round miles to nearest tenth
                "${Math.round(kilometres * 10f) / 10f} $units"
        }
    }

    fun formatValueWithUnit(value: ValueWithUnit?): String? {
        value ?: return null

        val number = value.Value?.roundToInt() ?: 0
        val unit = when (value.UnitOfMeasure?.toUpperCase()) {
            "KG" -> context.getString(R.string.kg)
            "LB" -> context.getString(R.string.lbs)
            else -> value.UnitOfMeasure
        }

        return "$number $unit"
    }
*//*
   *//* fun formatValueWithUnit(value: ValueWithUnitDisplay?): String? {
        value ?: return null

        return "${value.value} ${value.label.toUpperCase()}"
    }

    private fun datePattern(pattern: String): String {
        val locale = localeManager.getCurrentRegion().locale
        return DateFormat.getBestDateTimePattern(locale, pattern)
    }*//*

   *//* fun dateFormat(pattern: String): SimpleDateFormat {
        val language = localeManager.getCurrentLanguage().locale
        return SimpleDateFormat(datePattern(pattern), language)
    }*//*

    @SuppressLint("SimpleDateFormat")
    fun timeFormat(forceNoAMPM: Boolean = false): SimpleDateFormat {
        return when {
            // 24 hour time
            localeManager.use24HTime -> SimpleDateFormat("H:mm")

            // Otherwise sometimes we show AM/PM in a seperate field
            forceNoAMPM -> SimpleDateFormat("h:mm")

            // Else
            else -> SimpleDateFormat("h:mm a")
        }
    }

    *//*fun formatDate(pattern: String, date: Date): String {
        return dateFormat(pattern).format(date)
    }*//*

   *//* fun formatTimeOfDay(timeOfDay: TimeOfDay, alwaysShowMinutes: Boolean = false): String {
        val formatHourOnly = if (localeManager.use24HTime) {
                dateFormat("H")
            } else {
                dateFormat("ha")
            }

        val formatHourMinute = if (localeManager.use24HTime) {
                dateFormat("H:mm")
            } else {
                dateFormat("h:mmaa")
            }

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, timeOfDay.hour)
        cal.set(Calendar.MINUTE, timeOfDay.minute)

        return if (localeManager.use24HTime) {
            formatHourMinute.format(cal.time)
        } else {
            if (timeOfDay.minute == 0 && !alwaysShowMinutes) {
                formatHourOnly.format(cal.time)
            } else {
                formatHourMinute.format(cal.time)
            }
        }
    }

    fun formatTimeOfDayRange(from: TimeOfDay, to: TimeOfDay, alwaysShowMinutes: Boolean = false): String {
        return "${formatTimeOfDay(from, alwaysShowMinutes)} - ${formatTimeOfDay(to, alwaysShowMinutes)}"
    }
*//*
    fun parseFloat(value: String): Float {
        return try {
            localeManager.decimalFormatterForLocale.parse(value)?.toFloat() ?: 0f
        } catch (ex: Exception) {
            ex.printStackTrace()
            0f
        }
    }

    *//*fun getTextAlwaysEnglish(context: Context, @StringRes text: Int): String {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(Language.ENGLISH_USA.locale)
        }

        return context.createConfigurationContext(configuration).getString(text)
    }*//*
        return
}
*/