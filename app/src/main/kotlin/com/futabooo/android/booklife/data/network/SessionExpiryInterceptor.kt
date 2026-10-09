package com.futabooo.android.booklife.data.network

import okhttp3.Interceptor
import okhttp3.Response

/** Marks requests of the login / session-check flow, which must not be treated as "session expired". */
object AuthFlow {
    const val HEADER = "X-BookLife-Auth-Flow"
}

/**
 * Application interceptor (it sees the final response after redirects were followed) that throws
 * [SessionExpiredException] when a request ends at `/login` without having started there, or is
 * answered with 401. Requests carrying [AuthFlow.HEADER] bypass the check; the header is removed
 * before the request is sent.
 */
class SessionExpiryInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header(AuthFlow.HEADER) != null) {
            return chain.proceed(request.newBuilder().removeHeader(AuthFlow.HEADER).build())
        }
        val startedAtLogin = request.url.encodedPath == LOGIN_PATH
        val response = chain.proceed(request)
        val endedAtLogin = response.request.url.encodedPath == LOGIN_PATH
        if (response.code == 401 || (endedAtLogin && !startedAtLogin)) {
            response.close()
            throw SessionExpiredException()
        }
        return response
    }

    private companion object {
        const val LOGIN_PATH = "/login"
    }
}
