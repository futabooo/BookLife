package com.futabooo.android.booklife.di.modules

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.kazakago.cryptore.CipherAlgorithm
import com.kazakago.cryptore.Cryptore
import dagger.Module
import dagger.Provides
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.cert.CertificateException
import javax.crypto.NoSuchPaddingException
import javax.inject.Singleton
import timber.log.Timber

@Module class AppModule(val application: Application) {

  @Singleton @Provides fun provideApplication(): Application = application

  @Singleton @Provides fun provideSharedPreferences(application: Application): SharedPreferences =
      PreferenceManager.getDefaultSharedPreferences(application)

  @Singleton @Provides fun provideCryptore(application: Application): Cryptore {
    val builder = Cryptore.Builder("CIPHER_RSA", CipherAlgorithm.RSA)
    builder.context = application
    try {
      return builder.build()
    } catch (e: IOException) {
      Timber.e(e, e.message)
    } catch (e: CertificateException) {
      Timber.e(e, e.message)
    } catch (e: NoSuchAlgorithmException) {
      Timber.e(e, e.message)
    } catch (e: InvalidAlgorithmParameterException) {
      Timber.e(e, e.message)
    } catch (e: NoSuchPaddingException) {
      Timber.e(e, e.message)
    } catch (e: NoSuchProviderException) {
      Timber.e(e, e.message)
    } catch (e: KeyStoreException) {
      Timber.e(e, e.message)
    }

    return builder.build()
  }
}
