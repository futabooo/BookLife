package com.futabooo.android.booklife.ui.login

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.data.repository.LoginResult
import com.futabooo.android.booklife.data.repository.SessionRepository
import com.futabooo.android.booklife.ui.common.SnackbarController
import com.futabooo.android.booklife.ui.navigation.Main
import com.futabooo.android.booklife.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class LoginViewModel internal constructor(
    private val authenticate: suspend (String, String) -> LoginResult,
    private val onLoggedIn: () -> Unit,
    private val showError: (String) -> Unit,
    private val errorMessage: String,
) : ViewModel() {

    @Inject
    constructor(
        sessionRepository: SessionRepository,
        navigator: Navigator,
        snackbar: SnackbarController,
        @ApplicationContext context: Context,
    ) : this(
        authenticate = sessionRepository::login,
        onLoggedIn = { navigator.replaceAll(Main) },
        showError = snackbar::show,
        errorMessage = context.getString(R.string.error_login),
    )

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, emailError = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, passwordError = null)
    }

    fun onSignInClick() {
        val current = uiState
        if (current.isLoading) return
        val emailError = LoginValidator.emailError(current.email)
        val passwordError = LoginValidator.passwordError(current.password)
        if (emailError != null || passwordError != null) {
            uiState = current.copy(emailError = emailError, passwordError = passwordError)
            return
        }
        uiState = current.copy(emailError = null, passwordError = null, isLoading = true)
        viewModelScope.launch {
            val result = authenticate(current.email, current.password)
            uiState = uiState.copy(isLoading = false)
            when (result) {
                LoginResult.Success -> onLoggedIn()
                LoginResult.InvalidCredentials -> showError(errorMessage)
                is LoginResult.Error -> {
                    Timber.e(result.throwable, "login failed")
                    showError(errorMessage)
                }
            }
        }
    }
}
