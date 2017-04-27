package com.futabooo.android.booklife.screen.bookdetail;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import com.bumptech.glide.Glide;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.ActivityBookDetailBinding;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import retrofit2.Retrofit;

public class BookDetailActivity extends AppCompatActivity {

  private static final String EXTRA_BOOK_PATH = "book_path";

  @Inject Retrofit retrofit;

  private ActivityBookDetailBinding binding;

  public static Intent createIntent(Context context, String bookId) {
    Intent intent = new Intent(context, BookDetailActivity.class);
    intent.putExtra(EXTRA_BOOK_PATH, bookId);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((BookLife) getApplication()).getNetComponent().inject(this);

    binding = DataBindingUtil.setContentView(this, R.layout.activity_book_detail);

    setSupportActionBar(binding.bookDetailToolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    binding.bookDetailToolbar.setTitle("");

    String bookId = getIntent().getExtras().getString(EXTRA_BOOK_PATH);
    Log.d(this.getClass().getName(), "onItemClick: " + bookId);

    Observable<ResponseBody> observable = retrofit.create(BookDetailService.class).get(bookId);
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

            Document document = Jsoup.parse(result.toString());
            String title = document.select("h1").text();
            String author = document.select("div.default_box_body_link span").text();
            String thumbnail = document.select("div.book_detail_thumb img").first().absUrl("src");
            final String url = document.select("div.button_amazon_kindle a").attr("href");

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
          }

          @Override public void onError(Throwable e) {

          }

          @Override public void onComplete() {

          }
        });
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
