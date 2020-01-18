package com.futabooo.android.booklife.screen.splash

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.futabooo.android.booklife.BookLife
import com.futabooo.android.booklife.screen.MainActivity
import com.futabooo.android.booklife.screen.login.LoginActivity
import com.kazakago.cryptore.Cryptore
import retrofit2.Retrofit
import javax.inject.Inject

class SplashActivity : AppCompatActivity(), SplashPresenter.Contract {

  @Inject lateinit var retrofit: Retrofit
  @Inject lateinit var cryptore: Cryptore
  @Inject lateinit var sharedPreferences: SharedPreferences

  private val splashPresenter by lazy { SplashPresenter(this, retrofit, sharedPreferences, cryptore) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (application as BookLife).netComponent.inject(this)
    splashPresenter.bind()

    when {
      splashPresenter.existLoginInfo() -> splashPresenter.attemptLogin()
      else -> openLoginActivity("","")
    }
  }

  override fun onDestroy() {
    splashPresenter.unbind()
    super.onDestroy()
  }

  override fun openLoginActivity(email:String, password:String) {
    startActivity(LoginActivity.createIntent(this@SplashActivity, email, password))
    finish()
  }

  override fun openMainActivity() {
    startActivity(MainActivity.createIntent(this@SplashActivity))
    finish()
  }
}
