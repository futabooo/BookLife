package com.futabooo.android.booklife.screen.bookdetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.futabooo.android.booklife.R;

public class BookDetailActivity extends AppCompatActivity {

  private static final String EXTRA_BOOK_PATH = "book_path";

  public static Intent createIntent(Context context, String bookPath) {
    Intent intent = new Intent(context, BookDetailActivity.class);
    intent.putExtra(EXTRA_BOOK_PATH, bookPath);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_book_detail);

    String bookPath = getIntent().getExtras().getString(EXTRA_BOOK_PATH);
    Log.d(this.getClass().getName(), "onItemClick: " + bookPath);
  }
}
