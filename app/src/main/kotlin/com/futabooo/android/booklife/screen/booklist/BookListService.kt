package com.futabooo.android.booklife.screen.booklist

import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface BookListService {

  @GET("/users/{user_id}/books/{book_list_menu}") fun get(
      @Path("user_id") userId: Int,
      @Path("book_list_menu") bookListMenu: String): Observable<ResponseBody>

  @GET("/users/{user_id}/books/{book_list_menu}.json") fun getJson(
      @Header("X-CSRF-Token") csrfToken: String,
      @Path("user_id") userId: Int,
      @Path("book_list_menu") bookListMenu: String,
      @Query("attach_review") attach_review: String,
      @Query("offset") offset: Int,
      @Query("limit") limit: Int): Observable<JsonObject>
}
