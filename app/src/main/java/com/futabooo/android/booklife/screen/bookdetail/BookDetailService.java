package com.futabooo.android.booklife.screen.bookdetail;

import com.google.gson.JsonObject;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookDetailService {

  @GET("/books/{book_id}") Observable<ResponseBody> get(@Path("book_id") int bookId);

  @GET("/books/{book_id}.json") Observable<JsonObject> getJson(
      @Header("X-CSRF-Token") String csrfToken,
      @Path("book_id") int bookId,
      @Query("offset") int offset,
      @Query("limit") int limit);
}