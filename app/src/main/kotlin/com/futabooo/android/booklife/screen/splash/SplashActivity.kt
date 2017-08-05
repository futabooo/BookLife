package com.futabooo.android.booklife.screen.splash

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import com.futabooo.android.booklife.BookLife
import com.futabooo.android.booklife.screen.login.LoginActivity
import com.kazakago.cryptore.Cryptore
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import javax.inject.Inject
import timber.log.Timber

class SplashActivity : AppCompatActivity() {

  @Inject lateinit var cryptore: Cryptore
  @Inject lateinit var sharedPreferences: SharedPreferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (application as BookLife).netComponent.inject(this)

    if (sharedPreferences.contains("email") && sharedPreferences.contains("password")) {
      try {
        val emailBytes = Base64.decode(sharedPreferences.getString("email", "")?.toByteArray(), Base64.DEFAULT)
        val (bytes) = cryptore.decrypt(emailBytes, null)
        val email = String(bytes!!)

        val passwordBytes = Base64.decode(sharedPreferences.getString("password", "")?.toByteArray(), Base64.DEFAULT)
        val (bytes1) = cryptore.decrypt(passwordBytes, null)
        val password = String(bytes1!!)

        startActivity(LoginActivity.createIntent(this@SplashActivity, email, password))
        finish()
        return
      } catch (e: IOException) {
        Timber.e(e)
      } catch (e: NoSuchAlgorithmException) {
        Timber.e(e)
      } catch (e: UnrecoverableKeyException) {
        Timber.e(e)
      } catch (e: InvalidKeyException) {
        Timber.e(e)
      } catch (e: InvalidAlgorithmParameterException) {
        Timber.e(e)
      } catch (e: KeyStoreException) {
        Timber.e(e)
      }

    }

    startActivity(LoginActivity.createIntent(this@SplashActivity, "", ""))
    finish()
  }
}
