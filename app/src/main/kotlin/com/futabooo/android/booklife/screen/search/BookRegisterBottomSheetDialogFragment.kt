package com.futabooo.android.booklife.screen.search

import android.app.Dialog
import android.content.Context
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.view.LayoutInflater
import android.view.View
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ComponentBottomSheetBinding
import com.futabooo.android.booklife.screen.BookListMenu

class BookRegisterBottomSheetDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

  private lateinit var binding: ComponentBottomSheetBinding

  private lateinit var listener: OnBottomSheetActionListener

  companion object {

    private val EXTRA_BOOK_ID = "book_id"

    fun newInstance(bookId: Int): BookRegisterBottomSheetDialogFragment {
      val dialogFragment = BookRegisterBottomSheetDialogFragment()
      val args = Bundle()
      args.putInt(EXTRA_BOOK_ID, bookId)
      dialogFragment.arguments = args
      return dialogFragment
    }
  }

  private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
    override fun onStateChanged(bottomSheet: View, newState: Int) {
      if (newState == BottomSheetBehavior.STATE_HIDDEN) {
        dismiss()
      }
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {

    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    if (context !is OnBottomSheetActionListener) {
      throw ClassCastException("activity must implements OnBottomSheetActionListener")
    }
    this.listener = context
  }

  override fun setupDialog(dialog: Dialog, style: Int) {
    super.setupDialog(dialog, style)
    binding = DataBindingUtil.inflate<ComponentBottomSheetBinding>(LayoutInflater.from(context),
        R.layout.component_bottom_sheet, null, false)
    with(binding) {
      dialog.setContentView(root)

      val layoutParams = (root.parent as View).layoutParams as CoordinatorLayout.LayoutParams
      val behavior = layoutParams.behavior
      if (behavior != null && behavior is BottomSheetBehavior<*>) {
        behavior.setBottomSheetCallback(bottomSheetCallback)
      }

      bottomSheetReading.setOnClickListener(this@BookRegisterBottomSheetDialogFragment)
      bottomSheetToRead.setOnClickListener(this@BookRegisterBottomSheetDialogFragment)
      bottomSheetRead.setOnClickListener(this@BookRegisterBottomSheetDialogFragment)
      bottomSheetQuitted.setOnClickListener(this@BookRegisterBottomSheetDialogFragment)
    }
  }

  override fun onClick(v: View) {
    val bookId = arguments!!.getInt(EXTRA_BOOK_ID)
    when (v.id) {
      R.id.bottom_sheet_reading -> listener.onBottomSheetAction(BookListMenu.READING, bookId)
      R.id.bottom_sheet_to_read -> listener.onBottomSheetAction(BookListMenu.TO_READ, bookId)
      R.id.bottom_sheet_read -> listener.onBottomSheetAction(BookListMenu.READ, bookId)
      R.id.bottom_sheet_quitted -> listener.onBottomSheetAction(BookListMenu.QUITTED, bookId)
      else -> {
      }
    }
    dismiss()
  }

  interface OnBottomSheetActionListener {
    fun onBottomSheetAction(bookListMenu: BookListMenu, bookId: Int)
  }
}
