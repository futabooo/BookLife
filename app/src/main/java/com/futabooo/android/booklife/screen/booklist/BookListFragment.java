package com.futabooo.android.booklife.screen.booklist;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.FragmentBookListBinding;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.inject.Inject;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

public class BookListFragment extends Fragment {

  @Inject Retrofit retrofit;

  private FragmentBookListBinding binding;

  public BookListFragment() {
  }

  public static BookListFragment newInstance() {
    Bundle args = new Bundle();
    //args.putString(key, value);
    BookListFragment f = new BookListFragment();
    f.setArguments(args);

    return f;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((BookLife) getActivity().getApplication()).getNetComponent().inject(this);
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = DataBindingUtil.inflate(inflater, R.layout.fragment_book_list, container, false);
    return binding.getRoot();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    binding.get.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        getBookList();
      }
    });
  }

  private void getBookList() {
    Observable<ResponseBody> observable = retrofit.create(BookListService.class).get();
    observable.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<ResponseBody>() {
          @Override public void onSubscribe(Disposable d) {

          }

          @Override public void onNext(ResponseBody value) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(value.byteStream()));
            StringBuffer result = null;
            try {
              result = new StringBuffer();
              String line;
              while ((line = reader.readLine()) != null) {
                result.append(line);
              }
            } catch (IOException e) {
              e.printStackTrace();
            }

            binding.booklist.setText(result.toString());
          }

          @Override public void onError(Throwable e) {
            binding.booklist.setText(e.toString());
          }

          @Override public void onComplete() {

          }
        });
  }
}
