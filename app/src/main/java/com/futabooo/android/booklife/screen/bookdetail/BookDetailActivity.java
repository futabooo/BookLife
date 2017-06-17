package com.futabooo.android.booklife.screen.bookdetail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import com.bumptech.glide.Glide;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.ActivityBookDetailBinding;
import com.futabooo.android.booklife.model.BookDetailResource;
import com.futabooo.android.booklife.model.Review;
import com.futabooo.android.booklife.screen.BookListMenu;
import com.futabooo.android.booklife.screen.addbook.AddBookDialogFragment;
import com.futabooo.android.booklife.screen.search.ActionService;
import com.futabooo.android.booklife.screen.search.BookRegisterBottomSheetDialogFragment;
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

public class BookDetailActivity extends AppCompatActivity
    implements BookRegisterBottomSheetDialogFragment.OnBottomSheetActionListener,
    AddBookDialogFragment.OnAddBookActionListener {

  private static final String EXTRA_BOOK_ID = "book_id";

  @Inject Retrofit retrofit;
  @Inject SharedPreferences sharedPreferences;

  private ActivityBookDetailBinding binding;

  private String userId;
  private String csrfToken;
  private String title;
  private String author;
  private String thumbnail;
  private String url;

  public static Intent createIntent(Context context, int bookId) {
    Intent intent = new Intent(context, BookDetailActivity.class);
    intent.putExtra(EXTRA_BOOK_ID, bookId);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((BookLife) getApplication()).getNetComponent().inject(this);
    userId = sharedPreferences.getString("user_id", "");
    binding = DataBindingUtil.setContentView(this, R.layout.activity_book_detail);

    setSupportActionBar(binding.bookDetailToolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    binding.bookDetailToolbar.setTitle("");

    final int bookId = getIntent().getExtras().getInt(EXTRA_BOOK_ID);
    Observable<ResponseBody> observable = retrofit.create(BookDetailService.class).get(bookId);
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

        title = Jsoup.parse(result.toString()).select("h2.bm-headline span.bm-headline__text").get(0).text();
        author = Jsoup.parse(result.toString()).select("div.books_show__details ul li").get(0).text();
        thumbnail = Jsoup.parse(result.toString())
            .select("div.books_show__details__cover a.books_show__details__cover__link img")
            .attr("src");
        url = Jsoup.parse(result.toString())
            .select("div.books_show__details__cover a.books_show__details__cover__link")
            .attr("href");

        csrfToken = Jsoup.parse(result.toString()).select("meta[name=csrf-token]").get(0).attr("content");
        return retrofit.create(BookDetailService.class).getJson(csrfToken, bookId, 0, 1000);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<JsonObject>() {
      @Override public void onSubscribe(Disposable d) {
      }

      @Override public void onNext(JsonObject value) {
        binding.bookDetailToolbar.setTitle(title);
        binding.bookDetailBookAuthor.setText(author);
        Glide.with(BookDetailActivity.this).load(thumbnail).into(binding.bookDetailBookThumbnail);
        binding.bookDetailBuy.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
          }
        });

        binding.bookDetailAdd.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            BookRegisterBottomSheetDialogFragment dialogFragment =
                BookRegisterBottomSheetDialogFragment.newInstance(bookId);
            dialogFragment.show(getSupportFragmentManager(), "bottom_sheet");
          }
        });

        JsonArray jsonArray = value.getAsJsonArray("resources");
        if (jsonArray.size() == 0) {
          return;
        }

        Gson gson = new Gson();
        BookDetailResource resource = gson.fromJson(jsonArray, BookDetailResource[].class)[0];
        Review review = resource.getReview();
        if(review != null){
          binding.bookDetailImpression.setText(review.getContent());
        }
      }

      @Override public void onError(Throwable e) {
        Timber.e(e);
      }

      @Override public void onComplete() {
      }
    });
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

  @Override public void onBottomSheetAction(BookListMenu bookListMenu, int bookId) {
    switch (bookListMenu) {
      case READ:
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
