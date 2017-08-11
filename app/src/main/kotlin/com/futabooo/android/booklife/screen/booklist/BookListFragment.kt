package com.futabooo.android.booklife.screen.booklist

import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.futabooo.android.booklife.BookLife
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.FragmentBookListBinding
import com.futabooo.android.booklife.model.Resource
import com.futabooo.android.booklife.screen.BookListMenu
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

class BookListFragment : Fragment() {

  @Inject lateinit var retrofit: Retrofit
  @Inject lateinit var sharedPreferences: SharedPreferences
  lateinit var bookListMenu: BookListMenu
  private var userId: Int = 0

  lateinit var binding: FragmentBookListBinding
  lateinit var bookAdapter: BookAdapter

  companion object {

    private val EXTRA_BOOK_LIST_MENU = "book_list_menu"

    fun newInstance(bookListMenu: BookListMenu): BookListFragment {
      val args = Bundle().apply {
        putSerializable(EXTRA_BOOK_LIST_MENU, bookListMenu)
      }
      val f = BookListFragment()
      f.arguments = args
      return f
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activity.application as BookLife).netComponent.inject(this)
    userId = sharedPreferences.getInt("user_id", 0)
    bookListMenu = arguments.getSerializable(EXTRA_BOOK_LIST_MENU) as BookListMenu
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = DataBindingUtil.inflate<FragmentBookListBinding>(inflater!!, R.layout.fragment_book_list, container,
        false)
    return binding.root
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.bookList.setHasFixedSize(true)
    val layoutManager = LinearLayoutManager(activity)
    layoutManager.orientation = LinearLayoutManager.VERTICAL
    binding.bookList.layoutManager = layoutManager

    getBookList()
  }

  private fun getBookList() {
    val observable = retrofit.create(BookListService::class.java).get(userId, bookListMenu.key)
    observable.subscribeOn(Schedulers.io()).flatMap {
      val reader = BufferedReader(InputStreamReader(it.byteStream()))
      val result = reader.readLines().filter(String::isNotBlank).toList()

      val csrfToken = Jsoup.parse(result.toString()).select("meta[name=csrf-token]")[0].attr("content")
      retrofit.create(BookListService::class.java).getJson(csrfToken, userId, bookListMenu.key, "true", 0, 10)
    }.observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<JsonObject> {
      override fun onSubscribe(d: Disposable) {}

      override fun onNext(value: JsonObject) {
        val jsonArray = value.getAsJsonArray("resources")
        val resources = Gson().fromJson(jsonArray, Array<Resource>::class.java)
        bookAdapter = BookAdapter(resources, {
          startActivity(BookDetailActivity.createIntent(context, it.id))
        })
        binding.bookList.adapter = bookAdapter
      }

      override fun onError(e: Throwable) {
        Timber.e(e, e.message)
      }

      override fun onComplete() {}
    })
  }
}
