package com.futabooo.android.booklife.screen.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.futabooo.android.booklife.screen.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
    finish();
  }
}
