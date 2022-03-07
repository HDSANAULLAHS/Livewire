package com.livewire.app.model

import android.util.Log
import com.livewire.app.model.BaseResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseRemoteDataSource {
    /**
     * This parses the OLD api response format. May or may not contain an "error" object - if it does, call failed
     */
    fun <T : com.livewire.app.model.Response> Call<T>.call(success: (T) -> Unit, error: (String) -> Unit) {
        this.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>?, t: Throwable) {
                error(t.message ?: "Unknown Error")
                Log.d("JSON Debugging", t.message ?: "Unknown Error")

            }

            override fun onResponse(call: Call<T>?, response: Response<T>) {
                if (response.isSuccessful) {
                    var resp = response.body()
                    if (resp != null) {
                        if (resp.error != null) {
                            val errorBody = resp.error

                            if (errorBody?.code == "4000" || errorBody?.code == "1" || errorBody?.code == "0")
                            {
                                success(resp)
                            }
                            else {
                                error(resp.error?.exceptionCause ?: "Unknown Error")
                            }
                          //  success(resp)
                        } else {
                            success(resp)
                        }
                    }
                    else
                    {
                        error(response.errorBody()?.string() ?: "Unknown Error")
                    }
                } else {
                    error(response.errorBody()?.string() ?: "Unknown Error")
                }
            }
        })
    }

    /**
     * This is the NEW api response format. Response contains "error" and "data" object. If the error.code
     * is nonzero, or data.responseType != "SUCCESS", the call failed.
     */
    fun <T : BaseResponse.Data, U : BaseResponse<T>> Call<U>.call2(success: (T) -> Unit, error: (Int) -> Unit) {
        this.enqueue(object : Callback<U> {
            override fun onFailure(call: Call<U>?, t: Throwable) {
                error(-1)
            }

            override fun onResponse(call: Call<U>?, response: Response<U>) {
                if (response.isSuccessful) {
                    val resp = response.body()

                    if (resp != null) {
                        val data = resp.data

                        if (data != null && data.responseType == "SUCCESS") {
                            success(data)
                        } else {
                            error(resp.error?.code ?: -1)
                        }
                    }
                } else {
                    error(response.code())
                }
            }
        })
    }

    /**
     * This is used to call an API and return content as string.
     */
    fun <T: ResponseBody> Call<T>.string(success: (String) -> Unit, error: (String) -> Unit) {
        this.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>?, t: Throwable) {
                error(t.message ?: "Unknown Error")
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    success(response.body()?.string() ?: "")
                }
            }
        })
    }

    /**
     * This is used to call an API returning any JSON object and does not try to parse errors.
     */
    fun <T> Call<T>.json(success: (T) -> Unit, error: (String) -> Unit) {
        this.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>?, t: Throwable) {
                error(t.message ?: "Unknown Error")
            }

            override fun onResponse(call: Call<T>?, response: Response<T>) {
                if (response.isSuccessful) {
                    val result = response.body()

                    if (result != null) {
                        success(result)
                    } else {
                        error("Null response")
                    }
                } else {
                    error(response.code().toString())
                }
            }
        })
    }

    /**
     * This is the helper to use with API calls that return NO response (not even EmptyResponse)
     */
    fun Call<Void>.emptyCall(success: () -> Unit, error: (Int) -> Unit) {
        this.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>?, t: Throwable) {
                error(-1)
            }

            override fun onResponse(call: Call<Void>?, response: Response<Void>) {
                if (response.isSuccessful) {
                    success()
                } else {
                    error(response.code())
                }
            }
        })
    }
}
