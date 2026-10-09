package com.futabooo.android.booklife.ui.navigation

import android.content.Context
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.data.network.isSessionExpired
import com.futabooo.android.booklife.data.repository.SessionRepository
import com.futabooo.android.booklife.ui.common.SnackbarController
import dagger.hilt.android.ActivityRetainedLifecycle
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Central reaction to an expired bookmeter session. ViewModels call [handle] first in their catch
 * blocks and stop when it returns true: the local session state is cleared, the back stack is
 * replaced by [Login] and a snackbar explains why. Repeated calls while [Login] is already showing
 * (several requests failing at once) do nothing.
 */
@ActivityRetainedScoped
class SessionExpiryHandler internal constructor(
    private val onExpired: () -> Unit,
) {
    @Inject
    constructor(
        navigator: Navigator,
        snackbar: SnackbarController,
        session: SessionRepository,
        @ApplicationContext context: Context,
        lifecycle: ActivityRetainedLifecycle,
    ) : this(onExpired = expiryAction(navigator, snackbar, session, context, lifecycle))

    /** @return true when [throwable] was a session expiry (and has been handled). */
    fun handle(throwable: Throwable): Boolean {
        if (!throwable.isSessionExpired()) return false
        onExpired()
        return true
    }

    private companion object {
        fun expiryAction(
            navigator: Navigator,
            snackbar: SnackbarController,
            session: SessionRepository,
            context: Context,
            lifecycle: ActivityRetainedLifecycle,
        ): () -> Unit {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            lifecycle.addOnClearedListener { scope.cancel() }
            return {
                if (navigator.top != Login && navigator.top != Splash) {
                    navigator.replaceAll(Login)
                    snackbar.show(context.getString(R.string.session_expired))
                    // The cookies are dead: drop them (and the cached CSRF token) too.
                    scope.launch { session.logout() }
                }
            }
        }
    }
}
