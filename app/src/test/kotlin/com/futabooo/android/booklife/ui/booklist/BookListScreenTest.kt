package com.futabooo.android.booklife.ui.booklist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.futabooo.android.booklife.data.model.Book
import com.futabooo.android.booklife.data.model.Resource
import com.futabooo.android.booklife.ui.theme.BookLifeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BookListScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun rendersTitles() {
        val items = listOf(
            Resource(id = 1, book = Book(id = 1, title = "First book")),
            Resource(id = 2, book = Book(id = 2, title = "Second book")),
        )
        composeRule.setContent {
            BookLifeTheme {
                BookListContent(
                    state = BookListUiState(items = items, endReached = true),
                    onLoadMore = {},
                    onBookClick = {},
                    contentPadding = PaddingValues(),
                )
            }
        }
        composeRule.onNodeWithText("First book").assertExists()
        composeRule.onNodeWithText("Second book").assertExists()
    }
}
