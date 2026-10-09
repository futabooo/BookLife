package com.futabooo.android.booklife.data.model

import androidx.annotation.StringRes
import com.futabooo.android.booklife.R

/** The four bookshelves of bookmeter. [key] is the path segment used by the API. */
enum class BookListMenu(val position: Int, @StringRes val titleResId: Int, val key: String) {
    READ(0, R.string.book_read, "read"),
    READING(1, R.string.book_reading, "reading"),
    TO_READ(2, R.string.book_to_read, "wish"),
    QUITTED(3, R.string.book_quitted, "stacked"),
    ;

    companion object {
        fun fromPosition(position: Int): BookListMenu = entries.first { it.position == position }
    }
}
