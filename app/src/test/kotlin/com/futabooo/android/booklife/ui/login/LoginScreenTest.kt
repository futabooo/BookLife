package com.futabooo.android.booklife.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.ui.theme.BookLifeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(id: Int) = ApplicationProvider.getApplicationContext<Context>().getString(id)

    @Test
    fun submittingEmptyForm_showsRequiredError() {
        val vm = LoginViewModel({ _, _ -> error("not called") }, {}, {}, "")
        composeRule.setContent {
            BookLifeTheme {
                LoginContent(
                    state = vm.uiState,
                    onEmailChange = vm::onEmailChange,
                    onPasswordChange = vm::onPasswordChange,
                    onSignIn = vm::onSignInClick,
                    onForgetPassword = {},
                    onSignUp = {},
                )
            }
        }
        composeRule.onNodeWithText(string(R.string.login_sign_in)).performClick()
        composeRule.onNodeWithText(string(R.string.login_error_field_required)).assertExists()
        composeRule.onNodeWithText(string(R.string.login_error_invalid_password)).assertExists()
    }

    @Test
    fun loading_hidesForm() {
        composeRule.setContent {
            BookLifeTheme { LoginContent(LoginUiState(isLoading = true), {}, {}, {}, {}, {}) }
        }
        composeRule.onNodeWithText(string(R.string.login_sign_in)).assertDoesNotExist()
    }
}
