package com.futabooo.android.booklife.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futabooo.android.booklife.data.repository.SessionRepository
import com.futabooo.android.booklife.ui.navigation.Login
import com.futabooo.android.booklife.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel internal constructor(
    private val logout: suspend () -> Unit,
    private val onSignedOut: () -> Unit,
) : ViewModel() {

    @Inject
    constructor(sessionRepository: SessionRepository, navigator: Navigator) : this(
        logout = sessionRepository::logout,
        onSignedOut = { navigator.replaceAll(Login) },
    )

    fun signOut() {
        viewModelScope.launch {
            logout()
            onSignedOut()
        }
    }
}
