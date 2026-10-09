package com.futabooo.android.booklife.ui.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import dagger.hilt.android.ActivityRetainedLifecycle
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * App-wide snackbar. `AppNavDisplay` hosts [hostState] above all screens, so any class can call
 * [show] without owning a Scaffold (e.g. after a bottom sheet / dialog was dismissed).
 */
@ActivityRetainedScoped
class SnackbarController @Inject constructor(lifecycle: ActivityRetainedLifecycle) {

    val hostState = SnackbarHostState()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        lifecycle.addOnClearedListener { scope.cancel() }
    }

    /** Shows [message] (long duration); a message already showing is replaced. */
    fun show(message: String) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(message, duration = SnackbarDuration.Long)
        }
    }
}
