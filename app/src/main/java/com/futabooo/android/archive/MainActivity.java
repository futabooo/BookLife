package com.futabooo.android.archive;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import javax.inject.Inject;
import retrofit2.Retrofit;

import static com.futabooo.android.archive.R.id.response;

public class MainActivity extends AppCompatActivity {

  @Inject Retrofit retrofit;

  TextView textView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ((Archive) getApplication()).getNetComponent().inject(this);

    textView = (TextView) findViewById(response);


  }
}
