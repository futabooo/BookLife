package com.futabooo.android.booklife.screen.home;

import com.google.gson.JsonObject;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

interface HomeService {
  @GET("/home") Observable<ResponseBody> home();

  @GET("/home.json") Observable<JsonObject> getJson(
      @Header("X-CSRF-Token") String csrfToken,
      @Query("offset") int offset,
      @Query("limit") int limit);
}
