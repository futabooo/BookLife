package com.futabooo.android.booklife.di.modules;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.kazakago.cryptore.CipherAlgorithm;
import com.kazakago.cryptore.Cryptore;
import dagger.Module;
import dagger.Provides;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Singleton;
import timber.log.Timber;

@Module public class AppModule {

  Application application;

  public AppModule(Application application) {
    this.application = application;
  }

  @Provides @Singleton Application provideApplication() {
    return application;
  }

  @Provides @Singleton SharedPreferences provideSharedPreferences(Application application) {
    return PreferenceManager.getDefaultSharedPreferences(application);
  }

  @Provides @Singleton Cryptore provideCryptore(Application application) {
    Cryptore.Builder builder = new Cryptore.Builder("CIPHER_RSA", CipherAlgorithm.RSA);
    builder.setContext(application);
    try {
      return builder.build();
    } catch (IOException e) {
      Timber.e(e);
    } catch (CertificateException e) {
      Timber.e(e);
    } catch (NoSuchAlgorithmException e) {
      Timber.e(e);
    } catch (InvalidAlgorithmParameterException e) {
      Timber.e(e);
    } catch (NoSuchPaddingException e) {
      Timber.e(e);
    } catch (NoSuchProviderException e) {
      Timber.e(e);
    } catch (KeyStoreException e) {
      Timber.e(e);
    }
    return null;
  }
}
