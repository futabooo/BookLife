package com.futabooo.android.booklife.data.network

import java.io.IOException

/**
 * The bookmeter session is gone: a request ended on the login page (or was answered with 401), or an
 * HTML page turned out to be the login form. Handled centrally by `SessionExpiryHandler`.
 */
class SessionExpiredException : IOException("bookmeter session expired")

/** True when this throwable (or one of its causes) is a [SessionExpiredException]. */
fun Throwable.isSessionExpired(): Boolean =
    generateSequence(this) { it.cause }.any { it is SessionExpiredException }
