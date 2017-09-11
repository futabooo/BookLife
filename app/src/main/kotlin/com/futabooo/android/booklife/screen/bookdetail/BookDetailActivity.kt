package com.futabooo.android.booklife.screen.bookdetail

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.futabooo.android.booklife.BookLife
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ActivityBookDetailBinding
import com.futabooo.android.booklife.extensions.observeOnUI
import com.futabooo.android.booklife.extensions.subscribeOnIO
import com.futabooo.android.booklife.model.BookDetailResource
import com.futabooo.android.booklife.model.Review
import com.futabooo.android.booklife.screen.BookListMenu
import com.futabooo.android.booklife.screen.addbook.AddBookDialogFragment
import com.futabooo.android.booklife.screen.search.ActionService
import com.futabooo.android.booklife.screen.search.BookRegisterBottomSheetDialogFragment
import com.google.gson.Gson
import io.reactivex.rxkotlin.subscribeBy
import org.jsoup.Jsoup
import retrofit2.Retrofit
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class BookDetailActivity : AppCompatActivity(), BookRegisterBottomSheetDialogFragment.OnBottomSheetActionListener, AddBookDialogFragment.OnAddBookActionListener {

  @Inject lateinit var retrofit: Retrofit
  @Inject lateinit var sharedPreferences: SharedPreferences

  private val binding by lazy {
    DataBindingUtil.setContentView<ActivityBookDetailBinding>(this, R.layout.activity_book_detail)
  }
  private val userId by lazy { sharedPreferences.getInt("user_id", 0) }

  lateinit var csrfToken: String
  lateinit var title: String
  lateinit var author: String
  lateinit var thumbnail: String
  lateinit var url: String

  companion object {

    private val EXTRA_BOOK_ID = "book_id"

    fun createIntent(context: Context, bookId: Int) =
        Intent(context, BookDetailActivity::class.java).apply { putExtra(EXTRA_BOOK_ID, bookId) }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (application as BookLife).netComponent.inject(this)

    binding.apply {
      setSupportActionBar(bookDetailToolbar)
      supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setHomeAsUpIndicator(R.drawable.ic_arrow_left)
        title = ""
      }

      val bookId = intent.extras.getInt(EXTRA_BOOK_ID)
      retrofit.create(BookDetailService::class.java).get(bookId)
          .subscribeOnIO
          .map {
            val reader = BufferedReader(InputStreamReader(it.byteStream()))
            val result = reader.readLines().filter(String::isNotBlank).toList()

            title = Jsoup.parse(result.toString()).select("h2.bm-headline span.bm-headline__text")[0].text()
            author = Jsoup.parse(result.toString()).select("div.books_show__details ul li")[0].text()
            thumbnail = Jsoup.parse(result.toString())
                .select("div.books_show__details__cover a.books_show__details__cover__link img")
                .attr("src")
            url = Jsoup.parse(result.toString())
                .select("div.books_show__details__cover a.books_show__details__cover__link")
                .attr("href")

            csrfToken = Jsoup.parse(result.toString()).select("meta[name=csrf-token]")[0].attr("content")
          }
          .observeOnUI
          .subscribeBy(
              onNext = {
                getJson(csrfToken, bookId)
                getReview(csrfToken, bookId)
              },
              onError = { Timber.e(it, it.message) }
          )
    }
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

  override fun onBottomSheetAction(bookListMenu: BookListMenu, bookId: Int) {
    when (bookListMenu) {
      BookListMenu.READ -> {
        val dialogFragment = AddBookDialogFragment.newInstance(csrfToken, userId, bookId)
        dialogFragment.show(supportFragmentManager, "add_book_dialog")
      }
      BookListMenu.READING,
      BookListMenu.TO_READ,
      BookListMenu.QUITTED -> {
        retrofit.create(ActionService::class.java).addBook(csrfToken, userId, bookListMenu.key, bookId)
            .subscribeOnIO
            .observeOnUI
            .subscribeBy(
                onNext = { onRegister(bookId) },
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

  private fun getJson(csrfToken: String, bookId: Int) {
    binding.apply {
      retrofit.create(BookDetailService::class.java).getJson(csrfToken, bookId, 0, 1000)
          .subscribeOnIO
          .observeOnUI
          .subscribeBy(
              onNext = {
                bookDetailToolbar.title = title
                bookDetailBookTitle.text = title
                bookDetailBookAuthor.text = author
                Glide.with(this@BookDetailActivity).load(thumbnail).into(bookDetailBookThumbnail)
                bookDetailBuy.setOnClickListener {
                  val uri = Uri.parse(url)
                  val intent = Intent(Intent.ACTION_VIEW, uri)
                  startActivity(intent)
                }

                bookDetailAdd.setOnClickListener {
                  val dialogFragment = BookRegisterBottomSheetDialogFragment.newInstance(bookId)
                  dialogFragment.show(supportFragmentManager, "bottom_sheet")
                }

                val jsonArray = it.getAsJsonArray("resources")
                if (jsonArray.size() == 0) {
                  return@subscribeBy
                }

                val resource = Gson().fromJson(jsonArray, Array<BookDetailResource>::class.java)[0]
                resource.review.let {

                  bookDetailImpression.text = it?.content
                  bookDetailImpressionTitle.visibility = View.VISIBLE
                  bookDetailImpression.visibility = View.VISIBLE
                  bookDetailImpressionEdit.visibility = View.VISIBLE
                  bookDetailImpressionSeparator.visibility = View.VISIBLE

                  bookDetailImpressionEdit.setOnClickListener { _ ->
                    val dialogFragment = AddBookDialogFragment.newInstance(csrfToken, userId, bookId, it)
                    dialogFragment.show(supportFragmentManager, "add_book_dialog")
                  }
                }
              },
              onError = { Timber.e(it, it.message) }
          )
    }
  }

  private fun getReview(csrfToken: String, bookId: Int) {
    binding.apply {
      retrofit.create(BookDetailService::class.java).getReviews(csrfToken, bookId, "none", 0, 20)
          .subscribeOnIO
          .observeOnUI
          .subscribeBy(
              onNext = {
                val jsonArray = it.getAsJsonArray("resources")
                if (jsonArray.size() == 0) {
                  return@subscribeBy
                }
                val resources = Gson().fromJson(jsonArray, Array<Review>::class.java)
                val reviewAdapter = BookReviewAdapter(resources.toMutableList())
                bookDetailReviews.layoutManager = LinearLayoutManager(this@BookDetailActivity)
                bookDetailReviews.adapter = reviewAdapter
                bookDetailReviews.addItemDecoration(
                    BookReviewItemDecoration((30 * getResources().displayMetrics.density).toInt()))
                bookDetailReviews.isNestedScrollingEnabled = false
              },
              onError = { Timber.e(it, it.message) }
          )
    }
  }
}
