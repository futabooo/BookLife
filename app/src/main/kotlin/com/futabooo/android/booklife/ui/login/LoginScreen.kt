package com.futabooo.android.booklife.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.ui.theme.BookLifeColors
import com.futabooo.android.booklife.ui.theme.BookLifeSpacing
import com.futabooo.android.booklife.ui.theme.BookLifeTheme

private const val FORGET_PASSWORD_URL = "https://i.bookmeter.com/account/password/tokens/new"
private const val SIGN_UP_URL = "https://i.bookmeter.com/signup"

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val context = LocalContext.current
    LoginContent(
        state = viewModel.uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignIn = viewModel::onSignInClick,
        onForgetPassword = { openUrl(context, FORGET_PASSWORD_URL) },
        onSignUp = { openUrl(context, SIGN_UP_URL) },
    )
}

@Composable
internal fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onForgetPassword: () -> Unit,
    onSignUp: () -> Unit,
) {
    // safeDrawingPadding includes the IME, so the form stays above the keyboard.
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = dimensionResource(R.dimen.logo_margin_top))
                .size(width = 240.dp, height = 30.dp),
        )
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = BookLifeSpacing.XLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.login_prompt_email)) },
                    isError = state.emailError != null,
                    supportingText = state.emailError?.let { { Text(stringResource(it)) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.login_prompt_password)) },
                    isError = state.passwordError != null,
                    supportingText = state.passwordError?.let { { Text(stringResource(it)) } },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSignIn() }),
                )
                Button(
                    onClick = onSignIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp, start = BookLifeSpacing.Large, end = BookLifeSpacing.Large),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BookLifeColors.accent,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(stringResource(R.string.login_sign_in))
                }
                Text(
                    text = stringResource(R.string.login_forget_password),
                    color = BookLifeColors.secondaryText,
                    modifier = Modifier
                        .padding(top = BookLifeSpacing.Large, bottom = 64.dp)
                        .clickable(onClick = onForgetPassword),
                )
                Row(horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = stringResource(R.string.login_create_account),
                        color = BookLifeColors.secondaryText,
                        modifier = Modifier.padding(top = BookLifeSpacing.Medium),
                    )
                    Spacer(Modifier.width(BookLifeSpacing.Small))
                    Text(
                        text = stringResource(R.string.login_sign_up),
                        color = BookLifeColors.secondaryText,
                        modifier = Modifier
                            .padding(top = BookLifeSpacing.Medium)
                            .clickable(onClick = onSignUp),
                    )
                }
                Spacer(Modifier.height(BookLifeSpacing.Large))
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun LoginPreview() {
    BookLifeTheme {
        LoginContent(LoginUiState(email = "a@example.com"), {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun LoginLoadingPreview() {
    BookLifeTheme {
        LoginContent(LoginUiState(isLoading = true), {}, {}, {}, {}, {})
    }
}
