package com.futabooo.android.booklife.screen.search;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.ActivitySearchBinding;
import com.futabooo.android.booklife.screen.bookdetail.BookDetailActivity;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import retrofit2.Retrofit;

public class SearchActivity extends AppCompatActivity {

  @Inject Retrofit retrofit;

  private ActivitySearchBinding binding;

  private SearchResultAdapter resultAdapter;

  public static Intent createIntent(Context context) {
    return new Intent(context, SearchActivity.class);
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((BookLife) getApplication()).getNetComponent().inject(this);

    binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
    setSupportActionBar(binding.activitySearchToolbar);

    binding.activitySearchResultList.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    binding.activitySearchToolbar.inflateMenu(R.menu.activity_search_menu);
    SearchView searchView =
        (SearchView) binding.activitySearchToolbar.getMenu().findItem(R.id.menu_search).getActionView();
    searchView.setIconified(false);
    searchView.setQueryHint(getString(R.string.search_hint));
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String query) {
        searchBooks(query);
        return false;
      }

      @Override public boolean onQueryTextChange(String newText) {
        return false;
      }
    });
    return super.onCreateOptionsMenu(menu);
  }

  private void searchBooks(String query) {
    Observable<ResponseBody> observable = retrofit.create(SearchService.class).get(query);
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

            Elements bookList = Jsoup.parse(result.toString()).select("div.book_list_box div.book");
            resultAdapter = new SearchResultAdapter(SearchActivity.this, bookList);
            binding.activitySearchResultList.setAdapter(resultAdapter);
            resultAdapter.setOnCardClickListener(new SearchResultAdapter.OnCardClickListener() {
              @Override public void onCardClick(SearchResultAdapter adapter, int position, Element book) {
                String path = book.select("div.book_list_detail a").attr("href");
                Intent intent = BookDetailActivity.createIntent(SearchActivity.this, path.substring(3));
                startActivity(intent);
              }

              @Override public void onRegisterClick(SearchResultAdapter adapter, int position, Element book) {
                BookRegisterBottomSheetDialogFragment dialogFragment = BookRegisterBottomSheetDialogFragment.newInstance();
                dialogFragment.show(getSupportFragmentManager(), "bottom_sheet");
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
