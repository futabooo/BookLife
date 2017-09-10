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
import com.futabooo.android.booklife.InfiniteScrollListener
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ActivitySearchBinding
import com.futabooo.android.booklife.extensions.observeOnUI
import com.futabooo.android.booklife.extensions.subscribeOnIO
import com.futabooo.android.booklife.model.SearchResultResource
import com.futabooo.android.booklife.screen.BookListMenu
import com.futabooo.android.booklife.screen.addbook.AddBookDialogFragment
import com.futabooo.android.booklife.screen.bookdetail.BookDetailActivity
import com.google.gson.Gson
import io.reactivex.rxkotlin.subscribeBy
import org.jsoup.Jsoup
import retrofit2.Retrofit
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class SearchActivity : AppCompatActivity(), BookRegisterBottomSheetDialogFragment.OnBottomSheetActionListener, AddBookDialogFragment.OnAddBookActionListener {

  @Inject lateinit var retrofit: Retrofit
  @Inject lateinit var sharedPreferences: SharedPreferences

  private lateinit var binding: ActivitySearchBinding
  private lateinit var resultAdapter: SearchResultAdapter

  val userId by lazy { sharedPreferences.getInt("user_id", 0) }
  private lateinit var csrfToken: String
  private lateinit var keyword: String
  val limit: Int = 20
  var offset: Int = 0

  companion object {
    private val EXTRA_ISBN = "isbn"

    fun createIntent(context: Context, isbn: String = "") =
        Intent(context, SearchActivity::class.java).apply {
          putExtra(EXTRA_ISBN, isbn)
        }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (application as BookLife).netComponent.inject(this)

    binding = DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_search).apply {
      setSupportActionBar(activitySearchToolbar)
      supportActionBar?.setDisplayShowTitleEnabled(false)
      supportActionBar?.setDisplayHomeAsUpEnabled(true)

      val layoutManager = LinearLayoutManager(this@SearchActivity)
      activitySearchResultList.layoutManager = layoutManager
      resultAdapter = SearchResultAdapter(mutableListOf(),
          { view, bookId ->
            when (view.id) {
              R.id.search_result_item -> {
                startActivity(BookDetailActivity.createIntent(this@SearchActivity, bookId))
              }
              R.id.search_result_book_action -> {
                BookRegisterBottomSheetDialogFragment.newInstance(bookId).show(supportFragmentManager, "bottom_sheet")
              }
            }
          })
      activitySearchResultList.adapter = resultAdapter
      activitySearchResultList.addOnScrollListener(InfiniteScrollListener({ searchBooks(keyword) }, layoutManager))
    }

    intent.getStringExtra(EXTRA_ISBN).let { searchBooks(it) }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    binding.activitySearchToolbar.inflateMenu(R.menu.activity_search_menu)
    binding.activitySearchToolbar.menu.findItem(R.id.menu_search).actionView.also {
      it as SearchView
      it.isIconified = false
      it.queryHint = getString(R.string.search_hint)
      it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
          it.clearFocus()
          keyword = query
          searchBooks(keyword)
          return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
          return false
        }
      })
    }
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId

    when (id) {
      android.R.id.home -> {
        finish()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun searchBooks(keyword: String) {
    retrofit.create(SearchService::class.java).get(keyword)
        .flatMap {
          val reader = BufferedReader(InputStreamReader(it.byteStream()))
          val result = reader.readLines().filter(String::isNotBlank).toList()
          csrfToken = Jsoup.parse(result.toString()).select("meta[name=csrf-token]")[0].attr("content")
          retrofit.create(SearchService::class.java).getJson(csrfToken, keyword, "recommended", "japanese", offset,
              limit)
        }
        .subscribeOnIO
        .observeOnUI
        .doOnSubscribe { resultAdapter.showProgress(true) }
        .doFinally { resultAdapter.showProgress(false) }
        .subscribeBy(
            onNext = {
              val jsonArray = it.getAsJsonArray("resources")
              val resources = Gson().fromJson(jsonArray, Array<SearchResultResource>::class.java)
              resultAdapter.addAll(resources.toMutableList())
              offset += limit
            },
            onError = { Timber.e(it, it.message) }
        )
  }

  override fun onBottomSheetAction(bookListMenu: BookListMenu, bookId: Int) {
    when (bookListMenu) {
      BookListMenu.READ -> {
        AddBookDialogFragment.newInstance(csrfToken, userId, bookId).show(supportFragmentManager, "add_book_dialog")
      }
      BookListMenu.READING, BookListMenu.TO_READ, BookListMenu.QUITTED -> {
        retrofit.create(ActionService::class.java).addBook(csrfToken, userId, bookListMenu.key, bookId)
            .subscribeOnIO
            .observeOnUI
            .subscribeBy(
                onError = { Timber.e(it, it.message) }
            )
      }
    }
  }

  override fun onRegister(bookId: Int) {
    Snackbar.make(findViewById(android.R.id.content), getString(R.string.book_registered, bookId.toString()),
        Snackbar.LENGTH_LONG).show()
  }

  override fun onUpdate(bookId: Int) {
    Snackbar.make(findViewById(android.R.id.content), getString(R.string.book_update, bookId.toString()),
        Snackbar.LENGTH_LONG).show()
  }

  override fun onError() {
    Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_review_update),
        Snackbar.LENGTH_LONG).show()
  }
}
