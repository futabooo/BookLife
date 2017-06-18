package com.futabooo.android.booklife.screen.search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.ActivitySearchBinding;
import com.futabooo.android.booklife.model.Book;
import com.futabooo.android.booklife.model.SearchResultResource;
import com.futabooo.android.booklife.screen.BookListMenu;
import com.futabooo.android.booklife.screen.addbook.AddBookDialogFragment;
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
import timber.log.Timber;

public class SearchActivity extends AppCompatActivity
    implements BookRegisterBottomSheetDialogFragment.OnBottomSheetActionListener,
    AddBookDialogFragment.OnAddBookActionListener {

  @Inject Retrofit retrofit;
  @Inject SharedPreferences sharedPreferences;

  private String userId;
  private String csrfToken;

  private ActivitySearchBinding binding;

  private SearchResultAdapter resultAdapter;

  public static Intent createIntent(Context context) {
    return new Intent(context, SearchActivity.class);
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((BookLife) getApplication()).getNetComponent().inject(this);
    userId = sharedPreferences.getString("user_id","");

    binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
    setSupportActionBar(binding.activitySearchToolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    binding.activitySearchResultList.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    binding.activitySearchToolbar.inflateMenu(R.menu.activity_search_menu);
    final SearchView searchView =
        (SearchView) binding.activitySearchToolbar.getMenu().findItem(R.id.menu_search).getActionView();
    searchView.setIconified(false);
    searchView.setQueryHint(getString(R.string.search_hint));
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String query){
        searchView.clearFocus();
        searchBooks(query);
        return false;
      }

      @Override public boolean onQueryTextChange(String newText) {
        return false;
      }
    });
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void searchBooks(final String keyword) {
    Observable<ResponseBody> observable = retrofit.create(SearchService.class).get(keyword);
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
          Timber.e(e);
        }

        csrfToken = Jsoup.parse(result.toString()).select("meta[name=csrf-token]").get(0).attr("content");
        return retrofit.create(SearchService.class).getJson(csrfToken, keyword, "recommended", "japanese", 0, 20);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<JsonObject>() {
      @Override public void onSubscribe(Disposable d) {
      }

      @Override public void onNext(JsonObject value) {
        JsonArray jsonArray = value.getAsJsonArray("resources");
        Gson gson = new Gson();
        SearchResultResource[] resources = gson.fromJson(jsonArray, SearchResultResource[].class);
        resultAdapter = new SearchResultAdapter(SearchActivity.this, resources);
        binding.activitySearchResultList.setAdapter(resultAdapter);
        resultAdapter.setOnCardClickListener(new SearchResultAdapter.OnCardClickListener() {
          @Override public void onCardClick(SearchResultAdapter adapter, int position, Book book) {
            Intent intent = BookDetailActivity.createIntent(SearchActivity.this, book.getId());
            startActivity(intent);
          }

          @Override public void onRegisterClick(int bookId) {
            BookRegisterBottomSheetDialogFragment dialogFragment =
                BookRegisterBottomSheetDialogFragment.newInstance(bookId);
            dialogFragment.show(getSupportFragmentManager(), "bottom_sheet");
          }
        });
      }

      @Override public void onError(Throwable e) {
        Timber.e(e);
      }

      @Override public void onComplete() {
      }
    });
  }

  @Override public void onBottomSheetAction(BookListMenu bookListMenu, int bookId) {
    switch (bookListMenu) {
      case READ:
        //startActivity(BookEditActivity.createIntent(SearchActivity.this, asin));
        AddBookDialogFragment dialogFragment = AddBookDialogFragment.newInstance(csrfToken, userId, bookId);
        dialogFragment.show(getSupportFragmentManager(), "add_book_dialog");
        break;
      case READING:
      case TO_READ:
      case QUITTED:
        Observable<JsonObject> observable =
            retrofit.create(ActionService.class).addBook(csrfToken, userId, bookListMenu.getKey(), bookId);
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<JsonObject>() {
              @Override public void onSubscribe(Disposable d) {
              }

              @Override public void onNext(JsonObject value) {
              }

              @Override public void onError(Throwable e) {
                Timber.e(e);
              }

              @Override public void onComplete() {
              }
            });
    }
  }

  @Override public void onRegister(int bookId) {
    Snackbar.make(findViewById(android.R.id.content), getString(R.string.book_registered, String.valueOf(bookId)),
        Snackbar.LENGTH_LONG).show();
  }
}
