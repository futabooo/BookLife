package com.futabooo.android.booklife.screen.login

import android.content.SharedPreferences
import android.net.Uri
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
import java.io.IOException
import java.io.InputStreamReader
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.UnrecoverableEntryException
import javax.crypto.NoSuchPaddingException

class LoginPresenter constructor(val contract: Contract,
                                 val retrofit: Retrofit,
                                 val sharedPreferences: SharedPreferences,
                                 val cryptore: Cryptore) : Presenter {
  override fun bind() {
  }

  override fun unbind() {
  }

  fun isEmailValid(email: String): Boolean = email.contains("@")

  fun isValidPassword(password: String): Boolean = password.isNotEmpty()

  fun login(email: String, password: String) {
    retrofit.create(LoginService::class.java).get()
        .flatMap {
          val reader = BufferedReader(InputStreamReader(it.byteStream()))
          val result = reader.readLines().filter(String::isNotBlank).toList()
          val token = Jsoup.parse(result.toString()).select("form input[name=authenticity_token]").attr("value")
          retrofit.create(LoginService::class.java).login(email, password, token)
        }
        .subscribeOnIO
        .observeOnUI
        .doOnSubscribe { contract.showProgress(true) }
        .doFinally { contract.showProgress(false) }
        .subscribeBy(
            onNext = {
              val reader = BufferedReader(InputStreamReader(it.byteStream()))
              val result = reader.readLines().filter(String::isNotBlank).toList()
              val alert = Jsoup.parse(result.toString()).select("div.container li.bm-flash-item--alert").isNotEmpty()
              if (alert) {
                Timber.i("E-mail or Password is wrong")
                contract.failureLogin()
                return@subscribeBy
              }

              try {
                val editor = sharedPreferences.edit()

                val emailBytes = email.toByteArray()
                val (bytes) = cryptore.encrypt(emailBytes)
                editor.putString("email", Base64.encodeToString(bytes, Base64.DEFAULT))
                editor.apply()

                val passwordBytes = password.toByteArray()
                val (bytes1) = cryptore.encrypt(passwordBytes)
                editor.putString("password", Base64.encodeToString(bytes1, Base64.DEFAULT))
                editor.apply()
              } catch (e: UnrecoverableEntryException) {
                Timber.e(e, e.message)
              } catch (e: NoSuchAlgorithmException) {
                Timber.e(e, e.message)
              } catch (e: KeyStoreException) {
                Timber.e(e, e.message)
              } catch (e: InvalidKeyException) {
                Timber.e(e, e.message)
              } catch (e: InvalidAlgorithmParameterException) {
                Timber.e(e, e.message)
              } catch (e: NoSuchPaddingException) {
                Timber.e(e, e.message)
              } catch (e: IOException) {
                Timber.e(e, e.message)
              } catch (e: NoSuchProviderException) {
                Timber.e(e, e.message)
              }

              contract.successLogin()
            },
            onError = { Timber.e(it, it.message) }
        )
  }

  fun onSignInClick() = contract.attemptLogin()

  fun onForgetPasswordClick() =
      contract.openBrowser(Uri.parse("https://i.bookmeter.com/account/password/tokens/new"))

  fun onSignUpClick() =
      contract.openBrowser(Uri.parse("https://i.bookmeter.com/signup"))


  interface Contract {
    fun openBrowser(uri: Uri)
    fun attemptLogin()
    fun successLogin()
    fun failureLogin()
    fun showProgress(show: Boolean)
  }
}
