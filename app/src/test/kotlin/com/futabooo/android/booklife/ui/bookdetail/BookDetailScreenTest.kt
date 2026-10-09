package com.futabooo.android.booklife.ui.bookdetail

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.futabooo.android.booklife.data.model.Netabare
import com.futabooo.android.booklife.data.model.Review
import com.futabooo.android.booklife.data.model.User
import com.futabooo.android.booklife.data.repository.BookDetail
import com.futabooo.android.booklife.ui.theme.BookLifeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BookDetailScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun spoilerBadgeOnlyForNetabareReviews() {
        val state = BookDetailUiState(
            detail = BookDetail("Title X", "Author", "", ""),
            reviews = listOf(
                Review(id = 1, content = "spoiler review", netabare = Netabare(netabare = true), user = User(name = "A")),
                Review(id = 2, content = "clean review", user = User(name = "B")),
            ),
        )
        composeRule.setContent {
            BookLifeTheme {
                BookDetailContent(
                    state = state,
                    imageUrl = "",
                    onBack = {},
                    onAmazon = {},
                    onAdd = {},
                    onEditReview = {},
                )
            }
        }
        composeRule.onNodeWithText("spoiler review").assertExists()
        composeRule.onNodeWithText("clean review").assertExists()
        composeRule.onAllNodesWithText("Spoilers").assertCountEquals(1)
    }
}
