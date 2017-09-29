package com.futabooo.android.booklife.screen.booklist

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.futabooo.android.booklife.BookLife
import com.futabooo.android.booklife.InfiniteScrollListener
import com.futabooo.android.booklife.databinding.FragmentBookListBinding
import com.futabooo.android.booklife.extensions.observeOnUI
import com.futabooo.android.booklife.extensions.subscribeOnIO
import com.futabooo.android.booklife.model.Resource
import com.futabooo.android.booklife.screen.BookListMenu
import com.futabooo.android.booklife.screen.bookdetail.BookDetailActivity
import com.google.gson.Gson
import io.reactivex.rxkotlin.subscribeBy
import org.jsoup.Jsoup
import retrofit2.Retrofit
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject


class BookListFragment : Fragment() {

  @Inject lateinit var retrofit: Retrofit
  @Inject lateinit var sharedPreferences: SharedPreferences

  lateinit var binding: FragmentBookListBinding
  lateinit var bookAdapter: BookAdapter

  val bookListMenu by lazy { arguments.getSerializable(EXTRA_BOOK_LIST_MENU) as BookListMenu }
  val userId by lazy { sharedPreferences.getInt("user_id", 0) }

  val limit: Int = 10
  var offset: Int = 0


  companion object {

    private val EXTRA_BOOK_LIST_MENU = "book_list_menu"

    fun newInstance(bookListMenu: BookListMenu): BookListFragment =
        BookListFragment().apply {
          arguments = Bundle().apply {
            putSerializable(EXTRA_BOOK_LIST_MENU, bookListMenu)
          }
        }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activity.application as BookLife).netComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = FragmentBookListBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.bookList.setHasFixedSize(true)
    val layoutManager = LinearLayoutManager(activity).apply { orientation = LinearLayoutManager.VERTICAL }
    binding.bookList.layoutManager = layoutManager
    bookAdapter = BookAdapter(mutableListOf(), { v, book ->
      val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, v, v.transitionName)
      startActivity(BookDetailActivity.createIntent(context, book.id, book.imageUrl), options.toBundle())
    })
    binding.bookList.adapter = bookAdapter
    binding.bookList.addOnScrollListener(InfiniteScrollListener({ getBookList() }, layoutManager))

    getBookList()
  }

  private fun getBookList() {
    retrofit.create(BookListService::class.java).get(userId, bookListMenu.key)
        .flatMap {
          val reader = BufferedReader(InputStreamReader(it.byteStream()))
          val result = reader.readLines().filter(String::isNotBlank).toList()

          val csrfToken = Jsoup.parse(result.toString()).select("meta[name=csrf-token]")[0].attr("content")
          retrofit.create(BookListService::class.java).getJson(csrfToken, userId, bookListMenu.key, "true", offset,
              limit)
        }
        .subscribeOnIO
        .observeOnUI
        .doOnSubscribe { bookAdapter.showProgress(true) }
        .doFinally { bookAdapter.showProgress(false) }
        .subscribeBy(
            onNext = {
              val jsonArray = it.getAsJsonArray("resources")
              val resources = Gson().fromJson(jsonArray, Array<Resource>::class.java)
              bookAdapter.addAll(resources.toMutableList())
              offset += limit
            },
            onError = { Timber.e(it, it.message) }
        )
  }
}
