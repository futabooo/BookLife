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

  @Provides @Singleton fun provideApplication(): Application = application

  @Provides @Singleton fun provideSharedPreferences(application: Application): SharedPreferences =
      PreferenceManager.getDefaultSharedPreferences(application)

  @Provides @Singleton fun provideCryptore(application: Application): Cryptore {
    val builder = Cryptore.Builder("CIPHER_RSA", CipherAlgorithm.RSA)
    builder.context = application
    try {
      return builder.build()
    } catch (e: IOException) {
      Timber.e(e)
    } catch (e: CertificateException) {
      Timber.e(e)
    } catch (e: NoSuchAlgorithmException) {
      Timber.e(e)
    } catch (e: InvalidAlgorithmParameterException) {
      Timber.e(e)
    } catch (e: NoSuchPaddingException) {
      Timber.e(e)
    } catch (e: NoSuchProviderException) {
      Timber.e(e)
    } catch (e: KeyStoreException) {
      Timber.e(e)
    }

    return builder.build()
  }
}
