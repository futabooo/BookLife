package com.futabooo.android.booklife.screen

import com.futabooo.android.booklife.R

enum class BookListMenu constructor(val position: Int, val titleResId: Int, val key: String) {
  READ(0, R.string.book_read, "read"),
  READING(1, R.string.book_reading, "reading"),
  TO_READ(2, R.string.book_to_read, "wish"),
  QUITTED(3, R.string.book_quitted, "stacked");

  companion object {
    fun fromPosition(position: Int): BookListMenu = values().first { position == it.position }
  }
}
