package com.futabooo.android.booklife.ui.login

import androidx.annotation.StringRes
import com.futabooo.android.booklife.R

/** Same rules as the old `LoginActivity.attemptLogin`. Returns an error string resource or null. */
object LoginValidator {
    @StringRes
    fun emailError(email: String): Int? = when {
        email.isEmpty() -> R.string.login_error_field_required
        !email.contains("@") -> R.string.login_error_invalid_email
        else -> null
    }

    @StringRes
    fun passwordError(password: String): Int? =
        if (password.isEmpty()) R.string.login_error_invalid_password else null
}
