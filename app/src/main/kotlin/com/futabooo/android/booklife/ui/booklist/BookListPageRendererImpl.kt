package com.futabooo.android.booklife.ui.booklist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.futabooo.android.booklife.data.model.BookListMenu
import javax.inject.Inject

/**
 * Phase C: make this implement `ui.main.BookListPageRenderer` and bind it in Hilt.
 */
class BookListPageRendererImpl @Inject constructor() {
    @Composable
    fun Render(menu: BookListMenu, contentPadding: PaddingValues) = BookListScreen(menu, contentPadding)
}
