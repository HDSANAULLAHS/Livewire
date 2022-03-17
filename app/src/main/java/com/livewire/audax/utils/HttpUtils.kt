package com.livewire.audax.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.math.roundToInt

class HttpUtils(val context: Context, val okhttp: OkHttpClient) {
    private var thread: Thread? = null
    private val preferences = context.getSharedPreferences("coin_prefs2", Context.MODE_PRIVATE)

    companion object {
        private val TAG = "HttpUtils"

        /**
         * Zip URLs verified that extracted files are up to date (etag matches)
         */
        private val verifiedUrls = mutableListOf<String>()
    }

    @UiThread
    fun downloadAndExtractZip(url: String, ready: (List<Uri>, Int) -> Unit, error: (String) -> Unit, progress: (Int) -> Unit) {
        val hash = url.hashCode().toString()
        val zipFile = File(context.cacheDir, "$hash.zip")
        val dir = File(context.cacheDir, "frames_$hash")

        Log.i(TAG, "Load $url to file $zipFile to dir $dir")

        // Download on thread
        thread = Thread {
            downloadAndExtractZip(url, zipFile, dir, ready, error, progress)
        }.apply { start() }
    }

    @WorkerThread
    private fun downloadAndExtractZip(url: String, zipFile: File, dir: File, ready: (List<Uri>, Int) -> Unit, error: (String) -> Unit, progress: (Int) -> Unit) {
        // All kinds of things can go wrong here
        try {
            // If already downloaded, start now
            if (downloadZipRequired(url, dir, zipFile)) {
                Log.i(TAG, "Downloading archive")
                val etag = downloadUrl(url, zipFile, progress)

                Log.i(TAG, "Extracting files")
                extractZip(zipFile, dir)

                Log.i(TAG, "Delete zip")
                zipFile.delete()
                setExtractedFiles(zipFile.name, etag)
                verifiedUrls.add(url)
            }

            // Load files and start
            prepareAndStart(dir, ready)
        } catch (ex: Exception) {
            ex.printStackTrace()
            setExtractedFiles(zipFile.name, "")

            error(ex.message ?: "Failed to download and extract")
        }
    }

    @WorkerThread
    private fun downloadZipRequired(url: String, framesDir: File, zipFile: File): Boolean {
        // Check if dont have the images extracted already
        val extractedEtag = getExtractedFiles(zipFile.name)
        if (!framesDir.exists() || extractedEtag == "") {
            Log.i(TAG, "Frames not downloaded yet")
            return true
        }

        // Dont recheck header if already done
        if (verifiedUrls.contains(url)) {
            Log.i(TAG, "Already verified etag")
            return false
        }

        // Check headers to see if there is a newer zip file on server
        val serverETag = getEtag(url)
        if (serverETag == null) {
            // Offline?
            Log.i(TAG, "Couldnt get etag, continuing")
            return false
        }

        if (extractedEtag != serverETag) {
            Log.i(TAG, "Zip file etag does not match")
            return true
        }

        // Good
        Log.i(TAG, "Verified etag OK")
        verifiedUrls.add(url)
        return false
    }

    @WorkerThread
    private fun setExtractedFiles(key: String, etag: String) {
        preferences.edit().putString(key, etag).apply()
    }

    @WorkerThread
    private fun getExtractedFiles(key: String): String {
        return preferences.getString(key, "") ?: ""
    }

    @WorkerThread
    private fun prepareAndStart(dir: File, ready: (List<Uri>, Int) -> Unit) {
        // Prebuild list of URIs for imageView
        val frames = dir.listFiles()
            .sortedBy { it.nameWithoutExtension }
            .map { Uri.fromFile(it) }

        if (frames.isEmpty()) {
            return
        }

        // Callback
        val size = getImageSize(frames[0])
        ready(frames, size)
    }

    @WorkerThread
    fun downloadUrl(url: String, target: File, progress: (Int) -> Unit): String {
        val request = Request.Builder().url(url).build()

        val response = okhttp.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalArgumentException("Failed to download url")
        }

        val etag = response.header("ETag") ?: ""
        val length = response.body?.contentLength() ?: -1
        copyStreamWithProgress(response.body!!.byteStream(), target, length, progress)
        return etag
    }

    @WorkerThread
    fun getEtag(url: String): String? {
        try {
            val request = Request.Builder().url(url).head().build()

            okhttp.newCall(request).execute().use {
                if (!it.isSuccessful) {
                    return null
                }

                return it.header("ETag")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()

            return null
        }
    }

    @WorkerThread
    private fun copyStream(stream: InputStream, target: File) {
        stream.use { input ->
            target.outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
        }
    }

    @WorkerThread
    private fun copyStreamWithProgress(stream: InputStream, target: File, length: Long, progress: (Int) -> Unit) {
        stream.use { input ->
            target.outputStream().use { fileOut ->
                copyToWithProgress(input, fileOut, length, progress)
            }
        }
    }

    @WorkerThread
    fun extractZip(zipFile: File, dir: File) {
        data class ZipIO (val entry: ZipEntry, val output: File)

        if (!dir.exists()) {
            dir.mkdirs()
        }

        val zip = ZipFile(zipFile)
        zip.use {
            it.entries()
                .asSequence()
                .map { ZipIO(it, File(dir, it.name.split("/").last())) }
                .filter { !it.entry.isDirectory && !it.output.exists() }
                .forEach { (entry, output) ->
                    copyStream(zip.getInputStream(entry), output)
                }
        }
    }

    @WorkerThread
    fun getImageSize(uri: Uri): Int {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true

            BitmapFactory.decodeFile(uri.path, options)
            options.outWidth
        } catch (ex: Exception) {
            ex.printStackTrace()

            -1
        }
    }

    @WorkerThread
    private fun copyToWithProgress(input: InputStream, out: OutputStream, length: Long, progress: (Int) -> Unit): Long {
        var bytesCopied: Long = 0
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytes = input.read(buffer)
        while (bytes >= 0) {
            out.write(buffer, 0, bytes)
            bytesCopied += bytes
            progress((bytesCopied.toFloat() / length.toFloat() * 100f).roundToInt())
            bytes = input.read(buffer)
        }
        return bytesCopied
    }

    fun cancel() {
        thread?.interrupt()
        thread = null
    }
}

