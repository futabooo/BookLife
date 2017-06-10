package com.futabooo.android.booklife.screen.booklist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.FragmentBookListBinding;
import com.futabooo.android.booklife.model.Book;
import com.futabooo.android.booklife.model.Resource;
import com.futabooo.android.booklife.screen.BookListMenu;
import com.futabooo.android.booklife.screen.bookdetail.BookDetailActivity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.inject.Inject;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import retrofit2.Retrofit;

public class BookListFragment extends Fragment {

  @Inject Retrofit retrofit;
  @Inject SharedPreferences sharedPreferences;

  private static final String EXTRA_BOOK_LIST_MENU = "book_list_menu";
  private BookListMenu bookListMenu;
  private String userId;

  private FragmentBookListBinding binding;
  private BookAdapter bookAdapter;

  public BookListFragment() {
  }

  public static BookListFragment newInstance(@NonNull BookListMenu bookListMenu) {
    Bundle args = new Bundle();
    args.putSerializable(EXTRA_BOOK_LIST_MENU, bookListMenu);
    BookListFragment f = new BookListFragment();
    f.setArguments(args);
    return f;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((BookLife) getActivity().getApplication()).getNetComponent().inject(this);
    userId = sharedPreferences.getString("user_id","");
    bookListMenu = (BookListMenu) getArguments().getSerializable(EXTRA_BOOK_LIST_MENU);
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = DataBindingUtil.inflate(inflater, R.layout.fragment_book_list, container, false);
    return binding.getRoot();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    binding.bookList.setHasFixedSize(true);
    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    binding.bookList.setLayoutManager(layoutManager);

    getBookList();
  }

  private void getBookList() {
    Observable<ResponseBody> observable = retrofit.create(BookListService.class).get(userId, bookListMenu.getKey());
    observable.subscribeOn(Schedulers.io()).flatMap(new Function<ResponseBody, ObservableSource<JsonObject>>() {
      @Override public ObservableSource<JsonObject> apply(ResponseBody responseBody) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
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

        String csrfToken = Jsoup.parse(result.toString()).select("meta[name=csrf-token]").get(0).attr("content");
        return retrofit.create(BookListService.class)
            .getJson(csrfToken, userId, bookListMenu.getKey(), "true", "0", "10");
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<JsonObject>() {
      @Override public void onSubscribe(Disposable d) {
      }

      @Override public void onNext(JsonObject value) {
        JsonArray jsonArray = value.getAsJsonArray("resources");
        Gson gson = new Gson();
        Resource[] resources = gson.fromJson(jsonArray, Resource[].class);
        bookAdapter = new BookAdapter(getContext(), resources);
        binding.bookList.setAdapter(bookAdapter);
        bookAdapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
          @Override public void onItemClick(BookAdapter adapter, int position, Book book) {
            String path = book.getPath();
            Intent intent = BookDetailActivity.createIntent(getContext(), path.substring(3));
            startActivity(intent);
          }
        });
      }

      @Override public void onError(Throwable e) {
      }

      @Override public void onComplete() {
      }
    });
  }
}
