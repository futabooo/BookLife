package com.futabooo.android.booklife.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futabooo.android.booklife.data.repository.SessionRepository
import com.futabooo.android.booklife.data.repository.SessionState
import com.futabooo.android.booklife.ui.navigation.Login
import com.futabooo.android.booklife.ui.navigation.Main
import com.futabooo.android.booklife.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Checks the stored session once (on creation) and routes: logged in -> Main, logged out -> Login,
 * unknown (offline, server trouble) -> Main when session cookies exist (so an offline user keeps
 * their session; an expired one is caught by the first request), else Login.
 */
@HiltViewModel
class SplashViewModel internal constructor(
    checkSession: suspend () -> SessionState,
    hasSessionCookie: () -> Boolean,
    onLoggedIn: () -> Unit,
    onLoggedOut: () -> Unit,
) : ViewModel() {

    @Inject
    constructor(sessionRepository: SessionRepository, navigator: Navigator) : this(
        checkSession = sessionRepository::checkSession,
        hasSessionCookie = sessionRepository::hasSessionCookie,
        onLoggedIn = { navigator.replaceAll(Main) },
        onLoggedOut = { navigator.replaceAll(Login) },
    )

    init {
        viewModelScope.launch {
            val state = try {
                checkSession()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "session check failed")
                SessionState.Unknown(e)
            }
            val enterApp = when (state) {
                SessionState.LoggedIn -> true
                SessionState.LoggedOut -> false
                is SessionState.Unknown -> hasSessionCookie()
            }
            if (enterApp) onLoggedIn() else onLoggedOut()
        }
    }
}
