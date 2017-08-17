package com.futabooo.android.booklife.screen.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.futabooo.android.booklife.BookLife
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ActivityLoginBinding
import com.futabooo.android.booklife.screen.MainActivity
import com.kazakago.cryptore.Cryptore
import javax.inject.Inject
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity(), LoginPresenter.Contract {

  @Inject lateinit var retrofit: Retrofit
  @Inject lateinit var sharedPreferences: SharedPreferences
  @Inject lateinit var cryptore: Cryptore

  private val binding by lazy { DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login) }
  private val loginPresenter by lazy { LoginPresenter(this, retrofit, sharedPreferences, cryptore) }

  companion object {

    val EXTRA_EMAIL = "email"
    val EXTRA_PASSWORD = "password"

    fun createIntent(context: Context, email: String, password: String) =
        Intent(context, LoginActivity::class.java).apply {
          putExtra(EXTRA_EMAIL, email)
          putExtra(EXTRA_PASSWORD, password)
        }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (application as BookLife).netComponent.inject(this)

    binding.apply {
      presenter = loginPresenter
      email.setText(intent.getStringExtra(EXTRA_EMAIL))
      password.setText(intent.getStringExtra(EXTRA_PASSWORD))
      password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
          attemptLogin()
          return@OnEditorActionListener true
        }
        false
      })
    }
    loginPresenter.bind()
  }

  override fun onDestroy() {
    loginPresenter.unbind()
    super.onDestroy()
  }

  override fun successLogin() {
    startActivity(MainActivity.createIntent(this@LoginActivity))
    finish()
  }

  override fun failureLogin() {
    Snackbar.make(binding.root, getString(R.string.error_login), Snackbar.LENGTH_SHORT).show()
  }

  override fun openBrowser(uri: Uri) {
    startActivity(Intent(Intent.ACTION_VIEW, uri))
  }

  override fun attemptLogin() {
    getSystemService(Context.INPUT_METHOD_SERVICE).also {
      it as InputMethodManager
      it.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    // Reset errors.
    binding.email.error = null
    binding.password.error = null

    // Store values at the time of the login attempt.
    val email = binding.email.text.toString()
    val password = binding.password.text.toString()

    var cancel = false
    var focusView: View? = null

    // Check for a valid password, if the user entered one.
    if (!loginPresenter.isValidPassword(password)) {
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
      loginPresenter.login(email, password)
    }
  }

  override fun showProgress(show: Boolean) {
    // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
    // for very easy animations. If available, use these APIs to fade-in
    // the progress spinner.
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
  }
}

