package com.futabooo.android.booklife.ui.login

import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.data.repository.LoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private var loggedIn = 0
    private val errors = mutableListOf<String>()
    private val calls = mutableListOf<Pair<String, String>>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(result: LoginResult) = LoginViewModel(
        authenticate = { email, password ->
            calls += email to password
            result
        },
        onLoggedIn = { loggedIn++ },
        showError = { errors += it },
        errorMessage = "wrong",
    )

    @Test
    fun validator_rules() {
        assertEquals(R.string.login_error_field_required, LoginValidator.emailError(""))
        assertEquals(R.string.login_error_invalid_email, LoginValidator.emailError("abc"))
        assertNull(LoginValidator.emailError("a@b"))
        assertEquals(R.string.login_error_invalid_password, LoginValidator.passwordError(""))
        assertNull(LoginValidator.passwordError("x"))
    }

    @Test
    fun emptyForm_showsErrors_andDoesNotCallRepository() {
        val vm = viewModel(LoginResult.Success)
        vm.onSignInClick()
        assertEquals(R.string.login_error_field_required, vm.uiState.emailError)
        assertEquals(R.string.login_error_invalid_password, vm.uiState.passwordError)
        assertTrue(calls.isEmpty())
        assertEquals(0, loggedIn)
    }

    @Test
    fun invalidEmail_showsError() {
        val vm = viewModel(LoginResult.Success)
        vm.onEmailChange("nope")
        vm.onPasswordChange("pw")
        vm.onSignInClick()
        assertEquals(R.string.login_error_invalid_email, vm.uiState.emailError)
        assertNull(vm.uiState.passwordError)
        assertTrue(calls.isEmpty())
    }

    @Test
    fun success_navigates() {
        val vm = viewModel(LoginResult.Success)
        vm.onEmailChange("a@b.c")
        vm.onPasswordChange("pw")
        vm.onSignInClick()
        assertEquals(listOf("a@b.c" to "pw"), calls)
        assertEquals(1, loggedIn)
        assertFalse(vm.uiState.isLoading)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun invalidCredentials_showsSnackbar() {
        val vm = viewModel(LoginResult.InvalidCredentials)
        vm.onEmailChange("a@b.c")
        vm.onPasswordChange("pw")
        vm.onSignInClick()
        assertEquals(listOf("wrong"), errors)
        assertEquals(0, loggedIn)
        assertFalse(vm.uiState.isLoading)
    }

    @Test
    fun error_showsSnackbar() {
        val vm = viewModel(LoginResult.Error(RuntimeException("boom")))
        vm.onEmailChange("a@b.c")
        vm.onPasswordChange("pw")
        vm.onSignInClick()
        assertEquals(listOf("wrong"), errors)
        assertEquals(0, loggedIn)
    }

    private fun assertTrue(value: Boolean) = org.junit.Assert.assertTrue(value)
}
