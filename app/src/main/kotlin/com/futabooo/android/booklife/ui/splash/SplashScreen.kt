package com.futabooo.android.booklife.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.futabooo.android.booklife.ui.theme.BookLifeColors
import com.futabooo.android.booklife.ui.theme.BookLifeTheme

/**
 * Blank white screen; the system splash (core-splashscreen) shows the logo. Obtaining the
 * ViewModel starts the one-time session check.
 */
@Composable
fun SplashScreen(@Suppress("UNUSED_PARAMETER") viewModel: SplashViewModel = hiltViewModel()) {
    SplashContent()
}

@Composable
internal fun SplashContent() {
    Box(modifier = Modifier.fillMaxSize().background(BookLifeColors.primary))
}

@Preview(showBackground = true)
@Composable
private fun SplashPreview() {
    BookLifeTheme { SplashContent() }
}
