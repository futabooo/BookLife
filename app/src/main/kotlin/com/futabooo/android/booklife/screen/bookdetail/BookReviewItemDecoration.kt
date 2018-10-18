package com.futabooo.android.booklife.screen.bookdetail

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.State
import android.view.View

class BookReviewItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
  override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
    outRect.bottom = verticalSpaceHeight
    //    super.getItemOffsets(outRect, view, parent, state)
  }
}