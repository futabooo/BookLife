package com.futabooo.android.booklife.ui.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.futabooo.android.booklife.data.model.Author
import com.futabooo.android.booklife.data.model.Book
import com.futabooo.android.booklife.data.model.SearchResultContents
import com.futabooo.android.booklife.data.model.SearchResultResource
import com.futabooo.android.booklife.ui.theme.BookLifeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SearchScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private fun result(id: Int, status: String) = SearchResultResource(
        contents = SearchResultContents(Book(id = id, title = "Title $id", author = Author(name = "Author $id"))),
        statusText = status,
    )

    @Test
    fun rendersTitlesReadMarkAndAddButton() {
        var registered: Int? = null
        composeRule.setContent {
            BookLifeTheme {
                SearchContent(
                    state = SearchUiState(
                        query = "x",
                        items = listOf(result(1, "READ_MARK"), result(2, "")),
                        searched = true,
                    ),
                    autoFocus = false,
                    onBack = {},
                    onQueryChange = {},
                    onSubmit = {},
                    onLoadMore = {},
                    onOpenBook = {},
                    onRegister = { registered = it.id },
                )
            }
        }

        composeRule.onNodeWithText("Title 1").assertIsDisplayed()
        composeRule.onNodeWithText("Title 2").assertIsDisplayed()
        composeRule.onNodeWithText("READ_MARK").assertIsDisplayed()
        composeRule.onNodeWithTag("search_add_1").assertIsDisplayed().performClick()
        assertEquals(1, registered)
    }
}
