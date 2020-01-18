package com.futabooo.android.booklife.screen.home

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.futabooo.android.booklife.BookLife
import com.futabooo.android.booklife.databinding.FragmentHomeBinding
import com.futabooo.android.booklife.model.HomeResource
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import retrofit2.Retrofit
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class HomeFragment : Fragment() {

  @Inject lateinit var retrofit: Retrofit
  @Inject lateinit var sharedPreferences: SharedPreferences

  lateinit var binding: FragmentHomeBinding

  lateinit var page: String
  lateinit var volume: String
  lateinit var pageParDay: String

  companion object {

    fun newInstance(): HomeFragment = HomeFragment()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activity?.application as BookLife).netComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentHomeBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    val date = Date()
    val dateFormat = SimpleDateFormat("yyyy/MM")
    binding.fragmentHomeDate.text = dateFormat.format(date)

    retrofit.create(HomeService::class.java)
        .home()
        .subscribeOn(Schedulers.io())
        .flatMap {
          val reader = BufferedReader(InputStreamReader(it.byteStream()))
          val result = reader.readLines()
              .filter(String::isNotBlank)
              .toList()

          val thisVolume = Jsoup.parse(result.toString())
              .select("div.stats__thismonth ul.thismonth__list li.list__item span.item__number")
          page = thisVolume[0].text()
          volume = thisVolume[1].text()
          pageParDay = thisVolume[2].text()

          val href = Jsoup.parse(result.toString())
              .select("div.bm-block-side__content dl.user-profiles dt.user-profiles__avatar a")
              .attr("href")
          if (href.isNotBlank() && !sharedPreferences.contains("user_id")) {
            val userId = Integer.parseInt(href.substring(7))
            val editor = sharedPreferences.edit()
            editor.putInt("user_id", userId)
            editor.apply()
          }

          val csrfToken = Jsoup.parse(result.toString()).select("meta[name=csrf-token]")[0].attr("content")

          retrofit.create(HomeService::class.java)
              .getJson(csrfToken, 0, 10)
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onNext = {
              // user_idが保存されていない場合は取得して保存する
              if (!sharedPreferences.contains("user_id")) {
                val jsonArray = it.getAsJsonArray("resources")
                val resources = Gson().fromJson(jsonArray, Array<HomeResource>::class.java)
                val userId = resources[0].user.id
                val editor = sharedPreferences.edit()
                editor.putInt("user_id", userId)
                editor.apply()
              }

              binding.readingPageCurrentMonth.text = page
              binding.readingVolumeCurrentMonth.text = volume
              binding.readingPageParDayCurrentMonth.text = pageParDay
            },
            onError = {
              Timber.e(it, it.message)
            }
        )
  }
}
