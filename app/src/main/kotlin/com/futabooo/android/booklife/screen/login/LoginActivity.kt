package com.futabooo.android.booklife.screen.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.databinding.ObservableInt
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Base64
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.crashlytics.android.core.CrashlyticsCore
import com.futabooo.android.booklife.BookLife
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ActivityLoginBinding
import com.futabooo.android.booklife.extensions.applySchedulers
import com.futabooo.android.booklife.screen.MainActivity
import com.kazakago.cryptore.Cryptore
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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
import javax.inject.Inject
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Retrofit
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

  @Inject lateinit var retrofit: Retrofit
  @Inject lateinit var sharedPreferences: SharedPreferences
  @Inject lateinit var cryptore: Cryptore

  private lateinit var binding: ActivityLoginBinding
  private lateinit var loginPresenter: LoginPresenterImpl

  companion object {

    val EXTRA_EMAIL = "email"
    val EXTRA_PASSWORD = "password"

    fun createIntent(context: Context, email: String, password: String): Intent {
      val intent = Intent(context, LoginActivity::class.java).apply {
        putExtra(EXTRA_EMAIL, email)
        putExtra(EXTRA_PASSWORD, password)
      }
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (application as BookLife).netComponent.inject(this)
    binding = DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
    loginPresenter = LoginPresenterImpl()

    with(binding) {
      email.setText(intent.getStringExtra(EXTRA_EMAIL))
      password.setText(intent.getStringExtra(EXTRA_PASSWORD))
      password.setOnEditorActionListener(TextView.OnEditorActionListener { textView, id, keyEvent ->
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
          attemptLogin()
          return@OnEditorActionListener true
        }
        false
      })

      signInButton.setOnClickListener { attemptLogin() }

      forgetPassword.setOnClickListener {
        val uri = Uri.parse("https://i.bookmeter.com/account/password/tokens/new")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
      }

      signUp.setOnClickListener {
        val uri = Uri.parse("https://i.bookmeter.com/signup")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
      }
    }
  }

  /**
   * Attempts to sign in or register the account specified by the login form.
   * If there are form errors (invalid email, missing fields, etc.), the
   * errors are presented and no actual login attempt is made.
   */
  private fun attemptLogin() {
    // Reset errors.
    binding.email.error = null
    binding.password.error = null

    // Store values at the time of the login attempt.
    val email = binding.email.text.toString()
    val password = binding.password.text.toString()

    var cancel = false
    var focusView: View? = null

    // Check for a valid password, if the user entered one.
    if (!TextUtils.isEmpty(password) && !loginPresenter.isPasswordValid(password)) {
      binding.password.error = getString(R.string.login_error_invalid_password)
      focusView = binding.password
      cancel = true
    }

    // Check for a valid email address.
    if (TextUtils.isEmpty(email)) {
      binding.email.error = getString(R.string.login_error_field_required)
      focusView = binding.email
      cancel = true
    } else if (!loginPresenter.isEmailValid(email)) {
      binding.email.error = getString(R.string.login_error_invalid_email)
      focusView = binding.email
      cancel = true
    }

    if (cancel) {
      // There was an error; don't attempt login and focus the first
      // form field with an error.
      focusView?.requestFocus()
    } else {
      retrofit.create(LoginService::class.java).get()
          .flatMap {
            val reader = BufferedReader(InputStreamReader(it.byteStream()))
            val result = reader.readLines().filter(String::isNotBlank).toList()
            val authenticityToken = Jsoup.parse(result.toString()).select("form input[name=authenticity_token]").attr(
                "value")
            retrofit.create(LoginService::class.java).login(email, password, authenticityToken)
          }
          .applySchedulers()
          .doOnSubscribe { showProgress(true) }
          .doFinally { showProgress(false) }
          .subscribeBy(
              onNext = {
                val reader = BufferedReader(InputStreamReader(it.byteStream()))
                val result = reader.readLines().filter(String::isNotBlank).toList()
                val alert = Jsoup.parse(result.toString()).select("div.container li.bm-flash-item--alert").isNotEmpty()
                if (alert) {
                  CrashlyticsCore.getInstance().log("E-mail or Password is wrong")
                  Snackbar.make(binding.root, getString(R.string.error_login), Snackbar.LENGTH_SHORT).show()
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

                startActivity(MainActivity.createIntent(this@LoginActivity))
                finish()
              },
              onError = { Timber.e(it, it.message) }
          )
    }
  }

  /**
   * Shows the progress UI and hides the login form.
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2) private fun showProgress(show: Boolean) {
    // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
    // for very easy animations. If available, use these APIs to fade-in
    // the progress spinner.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
      val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

      binding.loginForm.visibility = if (show) View.GONE else View.VISIBLE
      binding.loginForm.animate()
          .setDuration(shortAnimTime.toLong())
          .alpha((if (show) 0 else 1).toFloat())
          .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
              binding.loginForm.visibility = if (show) View.GONE else View.VISIBLE
            }
          })

      binding.loginProgress.visibility = if (show) View.VISIBLE else View.GONE
      binding.loginProgress.animate()
          .setDuration(shortAnimTime.toLong())
          .alpha((if (show) 1 else 0).toFloat())
          .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
              binding.loginProgress.visibility = if (show) View.VISIBLE else View.GONE
            }
          })
    } else {
      // The ViewPropertyAnimator APIs are not available, so simply show
      // and hide the relevant UI components.
      binding.loginProgress.visibility = if (show) View.VISIBLE else View.GONE
      binding.loginForm.visibility = if (show) View.GONE else View.VISIBLE
    }
  }
}

