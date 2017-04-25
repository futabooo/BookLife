package com.futabooo.android.booklife.screen.bookdetail;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BookDetailService {

  @GET("/b/{book_id}") Observable<ResponseBody> get(@Path("book_id") String bookId);
}