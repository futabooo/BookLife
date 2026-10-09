package com.futabooo.android.booklife.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futabooo.android.booklife.data.repository.SessionRepository
import com.futabooo.android.booklife.ui.navigation.Login
import com.futabooo.android.booklife.ui.navigation.Main
import com.futabooo.android.booklife.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber

/** Checks the stored session once (on creation) and routes to Main or Login. */
@HiltViewModel
class SplashViewModel internal constructor(
    isLoggedIn: suspend () -> Boolean,
    onLoggedIn: () -> Unit,
    onLoggedOut: () -> Unit,
) : ViewModel() {

    @Inject
    constructor(sessionRepository: SessionRepository, navigator: Navigator) : this(
        isLoggedIn = sessionRepository::isLoggedIn,
        onLoggedIn = { navigator.replaceAll(Main) },
        onLoggedOut = { navigator.replaceAll(Login) },
    )

    init {
        viewModelScope.launch {
            val loggedIn = try {
                isLoggedIn()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "session check failed")
                false
            }
            if (loggedIn) onLoggedIn() else onLoggedOut()
        }
    }
}
