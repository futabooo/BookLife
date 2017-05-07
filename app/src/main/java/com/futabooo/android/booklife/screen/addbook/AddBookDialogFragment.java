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
import com.futabooo.android.booklife.screen.search.ActionType;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.Calendar;
import javax.inject.Inject;
import org.json.JSONObject;
import retrofit2.Retrofit;

public class AddBookDialogFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {

  private static final String EXTRA_BOOK_ASIN = "asin";

  @Inject Retrofit retrofit;

  private DialogBookAddBinding binding;

  private OnAddBookActionListener listener;

  private int year;
  private int month;
  private int dayOfMonth;

  public AddBookDialogFragment() {
  }

  public static AddBookDialogFragment newInstance(String asin) {
    AddBookDialogFragment dialogFragment = new AddBookDialogFragment();
    Bundle args = new Bundle();
    args.putString(EXTRA_BOOK_ASIN, asin);
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
    year = calendar.get(Calendar.YEAR);
    month = calendar.get(Calendar.MONTH);
    dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

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
        String type = ActionType.READ.getAddBookParam();
        String asin = getArguments().getString(EXTRA_BOOK_ASIN);
        String impressions = binding.dialogBookAddImpressions.getText().toString();
        int netabare = binding.dialogBookAddSpoiler.isChecked() ? 1 : 0;

        Observable<JSONObject> observable = retrofit.create(ActionService.class)
            .addBook(type, asin, 1, String.format("%1$02d", year), String.format("%1$02d", month),
                String.format("%1$02d", dayOfMonth), impressions, "", "", netabare, System.currentTimeMillis());
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<JSONObject>() {
              @Override public void onSubscribe(Disposable d) {

              }

              @Override public void onNext(JSONObject value) {
                listener.onRegister(value.optString("book_title"));
                dismiss();
              }

              @Override public void onError(Throwable e) {

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
    this.year = year;
    this.month = month;
    this.dayOfMonth = dayOfMonth;
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, month, dayOfMonth);
    binding.dialogBookAddReadDate.setText(DateFormat.format("yyyy/MM/dd", calendar));
  }

  public interface OnAddBookActionListener {
    void onRegister(String asin);
  }
}
