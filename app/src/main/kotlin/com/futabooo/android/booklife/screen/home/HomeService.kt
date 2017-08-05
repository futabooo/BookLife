package com.futabooo.android.booklife.screen.home

import com.google.gson.JsonObject

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

internal interface HomeService {
  @GET("/home") fun home(): Observable<ResponseBody>

  @GET("/home.json") fun getJson(
      @Header("X-CSRF-Token") csrfToken: String,
      @Query("offset") offset: Int,
      @Query("limit") limit: Int): Observable<JsonObject>
}
