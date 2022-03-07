package com.livewire.app.store

import android.content.Context
import android.util.Log
import com.livewire.app.utils.AESUtils
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import okio.Okio
import java.io.File

class FileStorage(val context: Context,
                  val moshi: Moshi) {
    companion object {
        const val TAG = "FileStorage"
    }

    inline fun <reified T> writeToCache(dirname: String, filename: String, value: T) {
        writeToFile(context.cacheDir, dirname, filename, value, T::class.java)
    }

    inline fun <reified T> readFromCache(dirname: String, filename: String): T? {
        return readFromFile(context.cacheDir, dirname, filename, T::class.java)
    }

    fun deleteFromCache(dirname: String, filename: String) {
        deleteFile(context.cacheDir, dirname, filename)
    }

    fun <T> writeToFile(parent: File, dirname: String, filename: String, value: T, type: Class<T>) {
        val directory = File(parent, dirname)
        val file = File(directory, filename)
        val adapter = moshi.adapter<T>(type)

        directory.mkdirs()

        try {
            val str = adapter.toJson(value)
            val encrypted = AESUtils.encrypt(str)
            file.writeText(encrypted)
            Log.i(TAG, "Wrote file $filename")
        } catch (ex: Exception) {
            Log.w(TAG, ex.message ?: "")
        }
    }

    fun <T> readFromFile(parent: File, dirname: String, filename: String, type: Class<T>): T? {
        val directory = File(parent, dirname)
        val file = File(directory, filename)
        val adapter = moshi.adapter<T>(type)

        return try {
            val buffer = file.readText()
            adapter.fromJson(AESUtils.decrypt(buffer))

        } catch (ex: Exception) {
            Log.w(TAG, ex.message ?: "")

            null
        }
    }

    fun deleteFile(parent: File, dirname: String, filename: String) {
        val directory = File(parent, dirname)
        val file = File(directory, filename)

        try {
            file.delete()

            Log.i(TAG, "Deleted file $filename")
        } catch (ex: Exception) {
            Log.w(TAG, ex.message ?: "")
        }
    }
}
