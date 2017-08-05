package com.futabooo.android.booklife

import android.support.design.widget.TabLayout
import android.view.View

enum class MainBottomMenu constructor(val position: Int,
                                      val titleId: Int,
                                      val tabLayoutVisibility: Int,
                                      val tabLayoutMode: Int) {
  HOME(0, R.string.home, View.GONE, TabLayout.MODE_FIXED),
  BOOK(1, R.string.book, View.VISIBLE, TabLayout.MODE_FIXED);

  companion object {
    fun fromPosition(position: Int): MainBottomMenu? {
      return values().firstOrNull { position == it.position }
    }
  }
}
