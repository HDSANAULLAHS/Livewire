package com.livewire.app.store

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.livewire.app.BuildConfig

class appPreferences(context: Context) {
    private var preferences: SharedPreferences

    private val PREFS_NAME = "version_1"
    private val KEY_FLAVOR = "flavor"
    private val GIGYA_UUID = "gigya_uuid"
    private val RIDE_INDICATOR_ICON = "ride_indicator_icon"
    private val LOGGED_MAP_UPDATE = "logged_map_update"
    private val HOLLANDER_BIKE_LAST_LATITUDE = "hollander_bike_last_latitude"
    private val HOLLANDER_BIKE_LAST_LONGITUDE = "hollander_bike_last_longitude"
    private val IDENTITY_LAST_VERIFIED = "identity_last_verified"
    private val LAMOTTA_FEATURE_KEY="lamotta_feature_key"
    private val LAMOTTA_TILE_SERVER_LOCAL="lamotta_tile_server_local"
    private val PROFILE_HAS_BIKES="profile_has_bikes"
    private val AMP_LOG_TIMESTAMP="amp_log_timestamp"

    init {
        this.preferences = context.getSharedPreferences(PREFS_NAME, 0)
    }

    var flavor: String?
        get() = preferences.getString(KEY_FLAVOR, null)
        set(value) { preferences.edit { putString(KEY_FLAVOR, value) }}

    var gigyaUUID: String?
        get() = preferences.getString(GIGYA_UUID, null)
        set(value) { preferences.edit { putString(GIGYA_UUID, value) }}

    var rideIndicatorIcon: Int
        get() = preferences.getInt(RIDE_INDICATOR_ICON, 0)
        set(value) { preferences.edit { putInt(RIDE_INDICATOR_ICON, value) }}

    var hollanderLastLatitude: Float
        get() = preferences.getFloat(HOLLANDER_BIKE_LAST_LATITUDE, 0f)
        set(value) { preferences.edit { putFloat(HOLLANDER_BIKE_LAST_LATITUDE, value) }}

    var hollanderLastLongitude: Float
        get() = preferences.getFloat(HOLLANDER_BIKE_LAST_LONGITUDE, 0f)
        set(value) { preferences.edit { putFloat(HOLLANDER_BIKE_LAST_LONGITUDE, value) }}

    var lastMapUpdateVersionLogged: String?
        get() = preferences.getString(LOGGED_MAP_UPDATE, null)
        set(value) { preferences.edit { putString(LOGGED_MAP_UPDATE, value) }}

    private var identityLastVerified: Long
        get() = preferences.getLong(IDENTITY_LAST_VERIFIED, 0L)
        set(value) { preferences.edit { putLong(IDENTITY_LAST_VERIFIED, value) }}

    var isLamottaFeature:Boolean
    get()=preferences.getBoolean(LAMOTTA_FEATURE_KEY,BuildConfig.LAMOTTA_FEATURE)
    set(value) { preferences.edit { putBoolean(LAMOTTA_FEATURE_KEY,value) } }

    var isLocalTileServer:Boolean
        get()=preferences.getBoolean(LAMOTTA_TILE_SERVER_LOCAL,false)
        set(value) { preferences.edit { putBoolean(LAMOTTA_TILE_SERVER_LOCAL,value) } }

    var profileHasBikes:Boolean
        get()=preferences.getBoolean(PROFILE_HAS_BIKES,false)
        set(value) { preferences.edit { putBoolean(PROFILE_HAS_BIKES,value) } }

    fun identityVerified() {
        identityLastVerified = System.currentTimeMillis()
    }

    val haveRecentlyVerifiedIdentity: Boolean
        get() = (System.currentTimeMillis() - identityLastVerified) < (30 * 60 * 1000)

    var ampLogTimestamp: String?
        get() = preferences.getString(AMP_LOG_TIMESTAMP, null)
        set(value) { preferences.edit { putString(AMP_LOG_TIMESTAMP, value) }}

    fun clear() {
        preferences.edit { clear() }
    }
}

