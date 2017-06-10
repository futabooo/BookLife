package com.futabooo.android.booklife.screen.search;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.ComponentBottomSheetBinding;
import com.futabooo.android.booklife.screen.BookListMenu;

public class BookRegisterBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

  private static final String EXTRA_BOOK_ID = "book_id";

  private ComponentBottomSheetBinding binding;

  private OnBottomSheetActionListener listener;

  private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
    @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
      if (newState == BottomSheetBehavior.STATE_HIDDEN) {
        dismiss();
      }
    }

    @Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {

    }
  };

  public BookRegisterBottomSheetDialogFragment() {

  }

  public static BookRegisterBottomSheetDialogFragment newInstance(int bookId) {
    BookRegisterBottomSheetDialogFragment dialogFragment = new BookRegisterBottomSheetDialogFragment();
    Bundle args = new Bundle();
    args.putInt(EXTRA_BOOK_ID, bookId);
    dialogFragment.setArguments(args);
    return dialogFragment;
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof OnBottomSheetActionListener == false) {
      throw new ClassCastException("activity must implements OnBottomSheetActionListener");
    }
    this.listener = (OnBottomSheetActionListener) context;
  }

  @Override public void setupDialog(Dialog dialog, int style) {
    super.setupDialog(dialog, style);
    binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.component_bottom_sheet, null, false);
    dialog.setContentView(binding.getRoot());

    CoordinatorLayout.LayoutParams layoutParams =
        (CoordinatorLayout.LayoutParams) ((View) binding.getRoot().getParent()).getLayoutParams();
    CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
    if (behavior != null && behavior instanceof BottomSheetBehavior) {
      ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetCallback);
    }

    binding.bottomSheetReading.setOnClickListener(this);
    binding.bottomSheetToRead.setOnClickListener(this);
    binding.bottomSheetRead.setOnClickListener(this);
    binding.bottomSheetQuitted.setOnClickListener(this);
  }

  @Override public void onClick(View v) {
    int bookId = getArguments().getInt(EXTRA_BOOK_ID);
    switch (v.getId()) {
      case R.id.bottom_sheet_reading:
        listener.onBottomSheetAction(BookListMenu.READING, bookId);
        break;
      case R.id.bottom_sheet_to_read:
        listener.onBottomSheetAction(BookListMenu.TO_READ, bookId);
        break;
      case R.id.bottom_sheet_read:
        listener.onBottomSheetAction(BookListMenu.READ, bookId);
        break;
      case R.id.bottom_sheet_quitted:
        listener.onBottomSheetAction(BookListMenu.QUITTED, bookId);
        break;
    }
    dismiss();
  }

  public interface OnBottomSheetActionListener {
    void onBottomSheetAction(BookListMenu bookListMenu, int bookId);
  }
}
