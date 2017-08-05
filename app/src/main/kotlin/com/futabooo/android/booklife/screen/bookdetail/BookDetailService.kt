package com.futabooo.android.booklife.screen.bookdetail

import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface BookDetailService {

  @GET("/books/{book_id}") fun get(@Path("book_id") bookId: Int): Observable<ResponseBody>

  @GET("/books/{book_id}.json") fun getJson(
      @Header("X-CSRF-Token") csrfToken: String,
      @Path("book_id") bookId: Int,
      @Query("offset") offset: Int,
      @Query("limit") limit: Int): Observable<JsonObject>
}