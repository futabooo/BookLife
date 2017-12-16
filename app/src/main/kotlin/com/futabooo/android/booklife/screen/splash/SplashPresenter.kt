package com.futabooo.android.booklife.screen.splash

import android.content.SharedPreferences
import android.util.Base64
import com.futabooo.android.booklife.extensions.observeOnUI
import com.futabooo.android.booklife.extensions.subscribeOnIO
import com.futabooo.android.booklife.screen.Presenter
import com.kazakago.cryptore.Cryptore
import io.reactivex.rxkotlin.subscribeBy
import org.jsoup.Jsoup
import retrofit2.Retrofit
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader

class SplashPresenter constructor(val contract: Contract,
                                  val retrofit: Retrofit,
                                  val sharedPreferences: SharedPreferences,
                                  val cryptore: Cryptore) : Presenter {
  override fun bind() {
  }

  override fun unbind() {
  }

  fun existLoginInfo() = sharedPreferences.contains("email") && sharedPreferences.contains("password")

  fun attemptLogin() {
    val email = getEmail()
    val password = getPassword()

    if (email.isBlank() or password.isBlank())
      contract.openLoginActivity("", "")
    else {
      retrofit.create(SplashService::class.java).get()
          .flatMap {
            val reader = BufferedReader(InputStreamReader(it.byteStream()))
            val result = reader.readLines().filter(String::isNotBlank).toList()
            val token = Jsoup.parse(result.toString()).select("form input[name=authenticity_token]").attr("value")
            retrofit.create(SplashService::class.java).login(email, password, token)
          }
          .subscribeOnIO
          .observeOnUI
          .subscribeBy(
              onNext = {
                val reader = BufferedReader(InputStreamReader(it.byteStream()))
                val result = reader.readLines().filter(String::isNotBlank).toList()
                val alert = Jsoup.parse(result.toString()).select("div.container li.bm-flash-item--alert").isNotEmpty()
                if (alert) {
                  Timber.i("failure auto login")
                  contract.openLoginActivity(email, password)
                  return@subscribeBy
                }
                contract.openMainActivity()
              },
              onError = {
                contract.openLoginActivity(email, password)
                Timber.e(it, it.message)
              }
          )
    }
  }


  fun getEmail() =
      try {
        String(cryptore.decrypt(Base64.decode(sharedPreferences.getString("email", "")?.toByteArray(), Base64.DEFAULT),
            null).bytes)
      } catch (e: Exception) {
        Timber.d("failure decrypt email")
        ""
      }

  fun getPassword() =
      try {
        String(
            cryptore.decrypt(Base64.decode(sharedPreferences.getString("password", "")?.toByteArray(), Base64.DEFAULT),
                null).bytes)
      } catch (e: Exception) {
        Timber.d("failure decrypt password")
        ""
      }

  interface Contract {
    fun openLoginActivity(email: String, password: String)
    fun openMainActivity()
  }
}