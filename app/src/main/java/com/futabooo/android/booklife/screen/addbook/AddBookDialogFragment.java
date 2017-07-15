package com.futabooo.android.booklife.screen.addbook;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.DialogBookAddBinding;
import com.futabooo.android.booklife.screen.search.ActionService;
import com.google.gson.JsonObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.Calendar;
import javax.inject.Inject;
import retrofit2.Retrofit;
import retrofit2.http.Header;
import timber.log.Timber;

public class AddBookDialogFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {

  private static final String EXTRA_CSRF_TOKEN = "csrf_token";
  private static final String EXTRA_BOOK_USER_ID = "user_id";
  private static final String EXTRA_BOOK_ID= "book_id";

  @Inject Retrofit retrofit;

  private DialogBookAddBinding binding;

  private OnAddBookActionListener listener;

  private String readAt;

  public AddBookDialogFragment() {
  }

  public static AddBookDialogFragment newInstance(String csrfToken, int userID, int bookId) {
    AddBookDialogFragment dialogFragment = new AddBookDialogFragment();
    Bundle args = new Bundle();
    args.putString(EXTRA_CSRF_TOKEN, csrfToken);
    args.putInt(EXTRA_BOOK_USER_ID, userID);
    args.putInt(EXTRA_BOOK_ID, bookId);
    dialogFragment.setArguments(args);
    return dialogFragment;
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof OnAddBookActionListener == false) {
      throw new ClassCastException("activity must implements OnAddBookActionListener");
    }
    this.listener = (OnAddBookActionListener) context;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((BookLife) getActivity().getApplication()).getNetComponent().inject(this);
  }

  @Override public void setupDialog(Dialog dialog, int style) {
    super.setupDialog(dialog, style);
    binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_book_add, null, false);
    dialog.setContentView(binding.getRoot());

    final Calendar calendar = Calendar.getInstance();
    readAt = DateFormat.format("yyyy/M/d", calendar).toString();
    final int year = calendar.get(Calendar.YEAR);
    final int month = calendar.get(Calendar.MONTH);
    final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

    binding.dialogBookAddReadDate.setText(DateFormat.format("yyyy/MM/dd", calendar));

    binding.dialogBookAddReadDate.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        DatePickerDialog datePickerDialog =
            new DatePickerDialog(getContext(), AddBookDialogFragment.this, year, month, dayOfMonth);
        datePickerDialog.show();
      }
    });

    binding.dialogBookAddNegative.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        dismiss();
      }
    });

    binding.dialogBookAddPositive.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        String csrfToken = getArguments().getString(EXTRA_CSRF_TOKEN);
        int userId = getArguments().getInt(EXTRA_BOOK_USER_ID);
        final int bookId = getArguments().getInt(EXTRA_BOOK_ID);
        String impressions = binding.dialogBookAddImpressions.getText().toString();
        int netabare = binding.dialogBookAddSpoiler.isChecked() ? 1 : 0;

        Observable<JsonObject> observable = retrofit.create(ActionService.class)
            .read(csrfToken, userId, bookId, readAt, impressions, netabare);
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<JsonObject>() {
              @Override public void onSubscribe(Disposable d) {

              }

              @Override public void onNext(JsonObject value) {
                listener.onRegister(bookId);
                dismiss();
              }

              @Override public void onError(Throwable e) {
                Timber.e(e);
              }

              @Override public void onComplete() {

              }
            });
      }
    });
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Dialog dialog = getDialog();
    WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
    DisplayMetrics metrics = getResources().getDisplayMetrics();
    lp.width = metrics.widthPixels;
    dialog.getWindow().setAttributes(lp);
  }

  @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, month, dayOfMonth);
    readAt = DateFormat.format("yyyy/M/d", calendar).toString();
    binding.dialogBookAddReadDate.setText(DateFormat.format("yyyy/MM/dd", calendar));
  }

  public interface OnAddBookActionListener {
    void onRegister(int bookId);
  }
}
