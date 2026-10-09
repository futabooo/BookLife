package com.futabooo.android.booklife.ui.settings

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.ui.theme.BookLifeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(id: Int) = ApplicationProvider.getApplicationContext<Context>().getString(id)

    @Test
    fun showsVersionRow() {
        composeRule.setContent {
            BookLifeTheme { SettingsContent("9.9.9", {}, {}, {}, {}) }
        }
        composeRule.onNodeWithText(string(R.string.version)).assertExists()
        composeRule.onNodeWithText("9.9.9").assertExists()
    }

    @Test
    fun signOut_requiresConfirmation() {
        var signedOut = 0
        composeRule.setContent {
            BookLifeTheme { SettingsContent("1", {}, {}, {}, { signedOut++ }) }
        }
        composeRule.onNodeWithText(string(R.string.sign_out)).performClick()
        assertEquals(0, signedOut)
        composeRule.onNodeWithText(string(R.string.sign_out_confirm)).assertExists()
        // The dialog's confirm button is the second "Sign out" node.
        composeRule.onAllNodesWithText(string(R.string.sign_out)).onLast().performClick()
        assertEquals(1, signedOut)
    }
}
