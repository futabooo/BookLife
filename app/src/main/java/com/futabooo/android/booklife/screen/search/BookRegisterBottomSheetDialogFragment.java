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

public class BookRegisterBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

  private static final String EXTRA_BOOK_ASIN = "asin";

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

  public static BookRegisterBottomSheetDialogFragment newInstance(String asin) {
    BookRegisterBottomSheetDialogFragment dialogFragment = new BookRegisterBottomSheetDialogFragment();
    Bundle args = new Bundle();
    args.putString(EXTRA_BOOK_ASIN, asin);
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
    String asin = getArguments().getString(EXTRA_BOOK_ASIN);
    switch (v.getId()) {
      case R.id.bottom_sheet_reading:
        listener.onBottomSheetAction(ActionType.READING, asin);
        break;
      case R.id.bottom_sheet_to_read:
        listener.onBottomSheetAction(ActionType.TO_READ, asin);
        break;
      case R.id.bottom_sheet_read:
        listener.onBottomSheetAction(ActionType.READ, asin);
        break;
      case R.id.bottom_sheet_quitted:
        listener.onBottomSheetAction(ActionType.QUITTED, asin);
        break;
    }
    dismiss();
  }

  public interface OnBottomSheetActionListener {
    void onBottomSheetAction(ActionType actionType, String asin);
  }
}
