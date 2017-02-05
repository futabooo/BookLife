package com.futabooo.android.archive.screen.booklist;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface BookListService {

  @GET("/booklist") Call<ResponseBody> get();
}
