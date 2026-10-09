package com.futabooo.android.booklife.ui.licenses

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.ui.theme.BookLifeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LicensesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsTitle() {
        composeRule.setContent { BookLifeTheme { LicensesContent(onBack = {}) {} } }
        val title = ApplicationProvider.getApplicationContext<Context>().getString(R.string.licenses)
        composeRule.onNodeWithText(title).assertExists()
    }
}
