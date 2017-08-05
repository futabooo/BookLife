package com.futabooo.android.booklife

import android.util.Log
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class LoggingInterceptor : Interceptor {

  companion object {
    private val TAG = LoggingInterceptor::class.java.simpleName
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    val t1 = System.nanoTime()
    Log.i(TAG, String.format("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()))

    val response = chain.proceed(request)

    val t2 = System.nanoTime()
    Log.i(TAG, String.format("Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6,
        response.headers()))

    return response
  }
}
