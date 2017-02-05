package com.futabooo.android.archive.screen.booklist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.futabooo.android.archive.Archive;
import com.futabooo.android.archive.R;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.inject.Inject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BookListActivity extends AppCompatActivity {

  @Inject Retrofit retrofit;

  Button button;
  TextView textView;

  public static Intent createIntent(Context context) {
    return new Intent(context, BookListActivity.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_book_list);
    button = (Button) findViewById(R.id.get);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        getBookList();
      }
    });
    textView = (TextView) findViewById(R.id.booklist);

    ((Archive) getApplication()).getNetComponent().inject(this);
  }

  private void getBookList() {
    Call<ResponseBody> call = retrofit.create(BookListService.class).get();
    call.enqueue(new Callback<ResponseBody>() {
      @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
        StringBuffer result = null;
        try {
          result = new StringBuffer();
          String line;
          while ((line = reader.readLine()) != null) {
            result.append(line);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }

        textView.setText(result.toString());
      }

      @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
        textView.setText(t.toString());
      }
    });
  }
}
