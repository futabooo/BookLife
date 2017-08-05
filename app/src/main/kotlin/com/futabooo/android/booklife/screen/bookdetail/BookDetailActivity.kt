package com.futabooo.android.booklife.screen.bookdetail

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.futabooo.android.booklife.BookLife
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ActivityBookDetailBinding
import com.futabooo.android.booklife.model.BookDetailResource
import com.futabooo.android.booklife.model.Review
import com.futabooo.android.booklife.screen.BookListMenu
import com.futabooo.android.booklife.screen.addbook.AddBookDialogFragment
import com.futabooo.android.booklife.screen.search.ActionService
import com.futabooo.android.booklife.screen.search.BookRegisterBottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Retrofit
import timber.log.Timber

class BookDetailActivity : AppCompatActivity(), BookRegisterBottomSheetDialogFragment.OnBottomSheetActionListener, AddBookDialogFragment.OnAddBookActionListener {

  @Inject lateinit var retrofit: Retrofit
  @Inject lateinit var sharedPreferences: SharedPreferences

  lateinit var binding: ActivityBookDetailBinding

  private var userId: Int = 0
  lateinit var csrfToken: String
  lateinit var title: String
  lateinit var author: String
  lateinit var thumbnail: String
  lateinit var url: String

  companion object {

    private val EXTRA_BOOK_ID = "book_id"

    fun createIntent(context: Context, bookId: Int): Intent {
      val intent = Intent(context, BookDetailActivity::class.java).apply {
        putExtra(EXTRA_BOOK_ID, bookId)
      }
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (application as BookLife).netComponent.inject(this)
    userId = sharedPreferences.getInt("user_id", 0)
    binding = DataBindingUtil.setContentView<ActivityBookDetailBinding>(this, R.layout.activity_book_detail)

    setSupportActionBar(binding.bookDetailToolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    binding.bookDetailToolbar.title = ""

    val bookId = intent.extras.getInt(EXTRA_BOOK_ID)
    val observable = retrofit.create(BookDetailService::class.java).get(bookId)
    observable.subscribeOn(Schedulers.io()).flatMap {
      val reader = BufferedReader(InputStreamReader(it.byteStream()))
      val result = reader.readLines().filter(String::isNotBlank).toList()

      title = Jsoup.parse(result!!.toString()).select("h2.bm-headline span.bm-headline__text")[0].text()
      author = Jsoup.parse(result.toString()).select("div.books_show__details ul li")[0].text()
      thumbnail = Jsoup.parse(result.toString())
          .select("div.books_show__details__cover a.books_show__details__cover__link img")
          .attr("src")
      url = Jsoup.parse(result.toString())
          .select("div.books_show__details__cover a.books_show__details__cover__link")
          .attr("href")

      csrfToken = Jsoup.parse(result.toString()).select("meta[name=csrf-token]")[0].attr("content")
      retrofit.create(BookDetailService::class.java).getJson(csrfToken, bookId, 0, 1000)
    }.observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<JsonObject> {
      override fun onSubscribe(d: Disposable) {}

      override fun onNext(value: JsonObject) {
        binding.bookDetailToolbar.title = title
        binding.bookDetailBookAuthor.text = author
        Glide.with(this@BookDetailActivity).load(thumbnail).into(binding.bookDetailBookThumbnail)
        binding.bookDetailBuy.setOnClickListener {
          val uri = Uri.parse(url)
          val intent = Intent(Intent.ACTION_VIEW, uri)
          startActivity(intent)
        }

        binding.bookDetailAdd.setOnClickListener {
          val dialogFragment = BookRegisterBottomSheetDialogFragment.newInstance(bookId)
          dialogFragment.show(supportFragmentManager, "bottom_sheet")
        }

        val jsonArray = value.getAsJsonArray("resources")
        if (jsonArray.size() == 0) {
          return
        }

        val gson = Gson()
        val resource = gson.fromJson(jsonArray, Array<BookDetailResource>::class.java)[0]
        val review = resource.review
        if (review != null) {
          binding.bookDetailImpression.text = review.content
        }
      }

      override fun onError(e: Throwable) {
        Timber.e(e)
      }

      override fun onComplete() {}
    })
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
      BookListMenu.READING, BookListMenu.TO_READ, BookListMenu.QUITTED -> {
        val observable = retrofit.create(ActionService::class.java).addBook(csrfToken, userId, bookListMenu.key,
            bookId)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<JsonObject> {
              override fun onSubscribe(d: Disposable) {}

              override fun onNext(value: JsonObject) {}

              override fun onError(e: Throwable) {
                Timber.e(e)
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
