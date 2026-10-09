package com.futabooo.android.booklife.ui.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.futabooo.android.booklife.analytics.Analytics
import com.futabooo.android.booklife.ui.navigation.Navigator
import com.futabooo.android.booklife.ui.theme.BookLifeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MainScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent() {
        composeRule.setContent {
            BookLifeTheme {
                MainScreen(
                    navigator = Navigator(),
                    analytics = object : Analytics {
                        override fun logEvent(name: String) = Unit
                    },
                    onShowMessage = {},
                    bookListPageRenderer = null,
                    homeContent = { androidx.compose.material3.Text("home body") },
                )
            }
        }
    }

    @Test
    fun bookBottomItem_showsFourTabs() {
        setContent()
        composeRule.onNodeWithTag("book_tabs").assertDoesNotExist()
        composeRule.onNodeWithTag("nav_book").performClick()
        composeRule.onNodeWithTag("book_tabs").assertIsDisplayed()
        listOf("read", "reading", "wish", "stacked").forEach {
            composeRule.onNodeWithTag("book_tab_$it").assertIsDisplayed()
        }
    }

    @Test
    fun fab_showsThreeArcButtons() {
        setContent()
        composeRule.onNodeWithTag("arc_search").assertDoesNotExist()
        composeRule.onNodeWithTag("fab").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("arc_barcode_scan").assertIsDisplayed()
        composeRule.onNodeWithTag("arc_search").assertIsDisplayed()
        composeRule.onNodeWithTag("arc_record_voice").assertIsDisplayed()
    }
}
