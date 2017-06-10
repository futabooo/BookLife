package com.futabooo.android.booklife.screen.search;

import com.google.gson.JsonObject;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface SearchService {

  @GET("/search") Observable<ResponseBody> get(@Query("keyword") String keyword);

  @GET("/search.json") Observable<JsonObject> getJson(
      @Header("X-CSRF-Token") String csrfToken,
      @Query("keyword") String keyword,
      @Query("sort") String sort,
      @Query("type") String type,
      @Query("offset") int offset,
      @Query("limit") int limit);
}
