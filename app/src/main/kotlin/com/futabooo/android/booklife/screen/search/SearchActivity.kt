package com.futabooo.android.booklife.screen.search

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.futabooo.android.booklife.BookLife
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ActivitySearchBinding
import com.futabooo.android.booklife.model.SearchResultResource
import com.futabooo.android.booklife.screen.BookListMenu
import com.futabooo.android.booklife.screen.addbook.AddBookDialogFragment
import com.futabooo.android.booklife.screen.bookdetail.BookDetailActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import org.jsoup.Jsoup
import retrofit2.Retrofit
import timber.log.Timber

class SearchActivity : AppCompatActivity(), BookRegisterBottomSheetDialogFragment.OnBottomSheetActionListener, AddBookDialogFragment.OnAddBookActionListener {

  @Inject lateinit var retrofit: Retrofit
  @Inject lateinit var sharedPreferences: SharedPreferences

  private var userId: Int = 0
  private lateinit var csrfToken: String

  private lateinit var binding: ActivitySearchBinding
  private lateinit var resultAdapter: SearchResultAdapter

  companion object {
    fun createIntent(context: Context): Intent = Intent(context, SearchActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (application as BookLife).netComponent.inject(this)
    userId = sharedPreferences.getInt("user_id", 0)

    binding = DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_search)
    with(binding) {
      setSupportActionBar(activitySearchToolbar)
      supportActionBar?.setDisplayShowTitleEnabled(false)
      supportActionBar?.setDisplayHomeAsUpEnabled(true)

      activitySearchResultList.layoutManager = LinearLayoutManager(this@SearchActivity)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    binding.activitySearchToolbar.inflateMenu(R.menu.activity_search_menu)
    val searchView = binding.activitySearchToolbar.menu.findItem(R.id.menu_search).actionView as SearchView
    searchView.isIconified = false
    searchView.queryHint = getString(R.string.search_hint)
    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String): Boolean {
        searchView.clearFocus()
        searchBooks(query)
        return false
      }

      override fun onQueryTextChange(newText: String): Boolean {
        return false
      }
    })
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId

    when (id) {
      android.R.id.home -> {
        finish()
        return true
      }
      else -> {
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun searchBooks(keyword: String) {
    val observable = retrofit.create(SearchService::class.java).get(keyword)
    observable.subscribeOn(Schedulers.io()).flatMap {
      val reader = BufferedReader(InputStreamReader(it.byteStream()))
      val result = reader.readLines().filter(String::isNotBlank).toList()
      csrfToken = Jsoup.parse(result.toString()).select("meta[name=csrf-token]")[0].attr("content")
      retrofit.create(SearchService::class.java).getJson(csrfToken, keyword, "recommended", "japanese", 0, 20)
    }.observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<JsonObject> {
      override fun onSubscribe(d: Disposable) {}

      override fun onNext(value: JsonObject) {
        val jsonArray = value.getAsJsonArray("resources")
        val gson = Gson()
        val resources = gson.fromJson(jsonArray, Array<SearchResultResource>::class.java)
        resultAdapter = SearchResultAdapter(resources,
            { view, bookId ->
              when (view.id) {
                R.id.search_result_item -> {
                  startActivity(BookDetailActivity.createIntent(this@SearchActivity, bookId))
                }
                R.id.search_result_book_action -> {
                  BookRegisterBottomSheetDialogFragment.newInstance(bookId).show(supportFragmentManager, "bottom_sheet")
                }
                else -> {
                }
              }
            })
        binding.activitySearchResultList.adapter = resultAdapter
      }

      override fun onError(e: Throwable) {
        Timber.e(e, e.message)
      }

      override fun onComplete() {}
    })
  }

  override fun onBottomSheetAction(bookListMenu: BookListMenu, bookId: Int) {
    when (bookListMenu) {
      BookListMenu.READ -> {
        //startActivity(BookEditActivity.createIntent(SearchActivity.this, asin));
        val dialogFragment = AddBookDialogFragment.newInstance(csrfToken, userId, bookId)
        dialogFragment.show(supportFragmentManager, "add_book_dialog")
      }
      BookListMenu.READING, BookListMenu.TO_READ, BookListMenu.QUITTED -> {
        val observable = retrofit.create(ActionService::class.java).addBook(csrfToken, userId, bookListMenu.key,
            bookId)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<JsonObject> {
              override fun onSubscribe(d: Disposable) {}

              override fun onNext(value: JsonObject) {}

              override fun onError(e: Throwable) {
                Timber.e(e, e.message)
              }

              override fun onComplete() {}
            })
      }
    }
  }

  override fun onRegister(bookId: Int) {
    Snackbar.make(findViewById(android.R.id.content), getString(R.string.book_registered, bookId.toString()),
        Snackbar.LENGTH_LONG).show()
  }
}
