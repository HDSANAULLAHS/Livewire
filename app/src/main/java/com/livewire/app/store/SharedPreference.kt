package com.livewire.app.store

import android.content.Context
import android.content.SharedPreferences

class SharedPreference(val context: Context) {

    private val PREF_NAME = "livewire"
    private var sharedPreference = context.getSharedPreferences(PREF_NAME, 0)

    fun save (KEY_NAME: String, text:String){
        val editor: SharedPreferences.Editor = sharedPreference!!.edit()
        editor.putString(KEY_NAME, text)
        editor.commit()
    }

    fun save(KEY_NAME: String, value: Int){
        val editor : SharedPreferences.Editor = sharedPreference!!.edit()
        editor.putInt(KEY_NAME, value)
        editor.commit()
    }
    fun save(KEY_NAME: String, status: Boolean){
        val editor: SharedPreferences.Editor = sharedPreference!!.edit()
        editor.putBoolean(KEY_NAME, status)
        editor.commit()
    }
    fun getStringValue(KEY_NAME: String):String?{
        return sharedPreference!!.getString(KEY_NAME, null)
    }
    fun getIntValue(KEY_NAME: String): Int{
        return sharedPreference!!.getInt(KEY_NAME, 0)
    }
    fun getBooleanValue(KEY_NAME: String, defaultValue: Boolean):Boolean{
        return sharedPreference!!.getBoolean(KEY_NAME, defaultValue)
    }
    fun clearSharedPreference(){
        val editor : SharedPreferences.Editor = sharedPreference!!.edit()
        editor.clear()
        editor.commit()
    }
    fun removeValue(KEY_NAME: String){
        val editor : SharedPreferences.Editor = sharedPreference!!.edit()
        editor.remove(KEY_NAME)
        editor.commit()
    }
}
