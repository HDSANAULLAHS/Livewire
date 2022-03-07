package com.livewire.app.di.module

import android.content.Context
import android.os.Build
import com.livewire.app.BuildConfig
import com.livewire.app.authentication.*
import com.livewire.app.authentication.ui.api.DataCenterApi
import com.livewire.app.authentication.ui.api.HarleyAccountApi
import com.livewire.app.authentication.ui.api.UserAccountApi
import com.livewire.app.gigya.GigyaApi
import com.livewire.app.locale.LocaleManager
import com.livewire.app.locale.LocalePreferences
import com.livewire.app.net.MoshiDateAdapter
import com.livewire.app.session.SessionTokenStore
import com.livewire.app.store.FileStorage
import com.livewire.app.store.SharedPreference
import com.livewire.app.store.appPreferences
import com.livewire.app.utils.HttpUtils
import com.livewire.app.utils.NetworkConnection
import com.squareup.moshi.Moshi
import okhttp3.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

val HEADER_CACHE_CONTROL = "Cache-Control"
val HEADER_PRAGMA = "Pragma"


fun moshi(): Moshi {
    return Moshi.Builder()
            .add(MoshiDateAdapter())
            .build()
}

fun moshiConverter(moshi: Moshi): MoshiConverterFactory {
    return MoshiConverterFactory.create(moshi).asLenient()
}

fun okhttp(cacheEnable: Boolean, conditional: Boolean, context: Context,
           network: NetworkConnection, store: SessionTokenStore): OkHttpClient {
    // Maximum size of cache (20MB)
    val maxCacheSize = 20L * 1024 * 1024

    val builder = OkHttpClient.Builder()
            .dispatcher(Dispatcher().apply { maxRequestsPerHost = 10 })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(provideAuthInterceptor(store))

    if (cacheEnable) {
        val httpCacheDirectory = File(context.cacheDir, if (conditional) "okCache" else "okCacheAll")
        val cache = Cache(httpCacheDirectory, maxCacheSize)

        builder
            .addInterceptor(provideOfflineCacheInterceptor(!conditional, network))
            .addNetworkInterceptor(provideCacheInterceptor(!conditional, network))
            .cache(cache)
    }

    /*if (BuildConfig.DEBUG) {
        builder.addNetworkInterceptor(StethoInterceptor())
        builder.addNetworkInterceptor(HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
        )
    }*/

    return builder.build()
}

private fun provideAuthInterceptor(sessionTokenStore: SessionTokenStore): Interceptor {
    val userAgent = String.format(Locale.US, "LiveWire/%s (%s %s;Android %s;%s)",
            BuildConfig.VERSION_NAME, Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE, sessionTokenStore.registrationId)

    // Create the interceptor to set the user agent and authentication header on API calls
    return Interceptor { chain ->
        val original = chain.request()

        // We need to support a non-JSON endoint for maintenance export which is CSV

        if (original.url.toString().contains("maintenance/export")) {
            val request = original.newBuilder()
                    .header("User-Agent", userAgent)
                    .header("Accept", "*/*")

            if (original.url.toString().startsWith(BuildConfig.API_URL)) {
                if (sessionTokenStore.jwt != null) {
                    request.addHeader("Authorization", "Bearer " + sessionTokenStore.jwt)
                }

            }


            request.method(original.method, original.body)

            chain.proceed(request.build())
        }
        else{

                val newUrl = original.url.newBuilder().addQueryParameter("brand", "LiveWire").build()

                val request = original.newBuilder()
                        .header("User-Agent", userAgent)
                        .header("Accept", "application/json")
                        .url(newUrl)

                if (original.url.toString().startsWith(BuildConfig.API_URL)) {
                    if (sessionTokenStore.jwt != null) {
                        request.addHeader("Authorization", "Bearer " + sessionTokenStore.jwt)
                    }
                }

                request.method(original.method, original.body)

                chain.proceed(request.build())
            }

    }
}

private fun provideCacheInterceptor(cacheAll: Boolean, network: NetworkConnection): Interceptor {
    return Interceptor { chain ->
        var request = chain.request()
        val enableOffline = cacheAll || request.header("offline-cache") != null

        if (enableOffline) {
            // Remove the header so its not send over the wire
            request = request.newBuilder().removeHeader("offline-cache").build()
        }

        val response = chain.proceed(request)
        val cacheControl = CacheControl.Builder()

        when {
            !enableOffline->
                cacheControl.noStore()
            network.isConnected ->
                cacheControl.maxAge(0, TimeUnit.SECONDS)
            else ->
                cacheControl.maxStale(7, TimeUnit.DAYS)
        }

        val cache = cacheControl.build().toString()

        response.newBuilder()
                .removeHeader(HEADER_PRAGMA)
                .removeHeader(HEADER_CACHE_CONTROL)
                .header(HEADER_CACHE_CONTROL, cache)
                .build()
    }
}

private fun provideOfflineCacheInterceptor(cacheAll: Boolean, network: NetworkConnection): Interceptor {
    return Interceptor { chain ->
        var request = chain.request()
        val enable = cacheAll || chain.request().header("offline-cache") != null

        if (enable && !network.isConnected) {
            val cacheControl = CacheControl.Builder()
                    .maxStale(7, TimeUnit.DAYS)
                    .build()

            request = request.newBuilder()
                    .removeHeader(HEADER_PRAGMA)
                    .removeHeader(HEADER_CACHE_CONTROL)
                    .cacheControl(cacheControl)
                    .build()
        }

        chain.proceed(request)
    }
}

inline fun <reified T> api(moshi: MoshiConverterFactory, client: OkHttpClient, url: String = BuildConfig.API_URL): T {
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(moshi)
        .client(client)
        .build()

    return retrofit.create(T::class.java)
}

// There are 3 okhttp clients that can be injected
// - Cache all requests (as directed by http headers). Used by image loader
// - Only cache requests with "offline-cache: 1" fake header (used by retrofit API)
// - Cache turned off (used for large one-time zip downloads)

val networkModule = module {
    single { SharedPreference(get()) }
    single { AccountFieldValidator() }
    single { LocalePreferences(get()) }
    single { LocaleManager(get(), get(), get()) }
    single { UserViewModel(get()) }
    single { FileStorage(get(), get()) }
    single { SessionTokenStore(get()) }
    single { GigyaManager(get(),get()) }
    single { GigyaApi(get(), get(), get(),get(),get(),get()) }
    single { moshi() }
    single { appPreferences(get()) }
    single { moshiConverter(get()) }
    single { NetworkConnection(get()) }
    single(named("cacheOn")) { okhttp(true, false, get(), get(), get()) }
    single(named("cacheConditional")) { okhttp(true, true, get(), get(), get()) }
    single(named("cacheOff")) { okhttp(false, false, get(), get(), get()) }
    single(named("cacheOnUtils")) { HttpUtils(get(), get(named("cacheOn"))) }
    single(named("cacheOffUtils")) { HttpUtils(get(), get(named("cacheOff"))) }
   single { api<DataCenterApi>(get(), get(named("cacheConditional")), BuildConfig.DATACENTER_API_URL) }
    single { api<HarleyAccountApi>(get(), get(named("cacheConditional")), BuildConfig.HARLEY_ACCOUNT_API_URL) }
    single { api<UserAccountApi>(get(), get(named("cacheConditional"))) }

}
