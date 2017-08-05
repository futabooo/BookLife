package com.futabooo.android.booklife

import java.io.IOException
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class HostSelectionInterceptor : Interceptor {
  @Volatile private var scheme: String? = null

  fun setScheme(scheme: String) {
    this.scheme = scheme
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    var request = chain.request()
    val scheme = this.scheme
    if (scheme != null) {
      val newUrl = request.url().newBuilder().scheme(scheme).build()
      request = request.newBuilder().url(newUrl).build()
    }
    return chain.proceed(request)
  }
}
