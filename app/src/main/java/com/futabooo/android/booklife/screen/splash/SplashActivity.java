package com.futabooo.android.booklife.screen.splash;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.screen.login.LoginActivity;
import com.kazakago.cryptore.Cryptore;
import com.kazakago.cryptore.DecryptResult;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {

  @Inject Cryptore cryptore;
  @Inject SharedPreferences sharedPreferences;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((BookLife) getApplication()).getNetComponent().inject(this);
    // 有効期限内のcookieがあればそれを使ってそのままHome画面を開く
    if (false) {

    }

    if (sharedPreferences.contains("email") && sharedPreferences.contains("password")) {
      try {
        byte[] emailBytes = Base64.decode(sharedPreferences.getString("email", "").getBytes(), Base64.DEFAULT);
        DecryptResult emailResult = cryptore.decrypt(emailBytes, null);
        String email = new String(emailResult.getBytes());

        byte[] passwordBytes = Base64.decode(sharedPreferences.getString("password", "").getBytes(), Base64.DEFAULT);
        DecryptResult passwordResult = cryptore.decrypt(passwordBytes, null);
        String password = new String(passwordResult.getBytes());

        startActivity(LoginActivity.createIntent(SplashActivity.this, email, password));
        finish();
        return;
      } catch (IOException e) {
        e.printStackTrace();
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (UnrecoverableKeyException e) {
        e.printStackTrace();
      } catch (InvalidKeyException e) {
        e.printStackTrace();
      } catch (InvalidAlgorithmParameterException e) {
        e.printStackTrace();
      } catch (KeyStoreException e) {
        e.printStackTrace();
      }
    }

    startActivity(LoginActivity.createIntent(SplashActivity.this, "", ""));
    finish();
  }
}
