package com.futabooo.android.booklife.screen.search

import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SearchService {

  @GET("/search") fun get(@Query("keyword") keyword: String): Observable<ResponseBody>

  @GET("/search.json") fun getJson(
      @Header("X-CSRF-Token") csrfToken: String,
      @Query("keyword") keyword: String,
      @Query("sort") sort: String,
      @Query("type") type: String,
      @Query("offset") offset: Int,
      @Query("limit") limit: Int): Observable<JsonObject>
}
