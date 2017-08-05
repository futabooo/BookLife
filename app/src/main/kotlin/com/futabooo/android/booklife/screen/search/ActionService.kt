package com.futabooo.android.booklife.screen.search

import com.google.gson.JsonObject
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ActionService {

  @FormUrlEncoded @POST("/users/{user_id}/books/{book_list_menu}") fun addBook(
      @Header("X-CSRF-Token") csrfToken: String,
      @Path("user_id") userId: Int,
      @Path("book_list_menu") bookListMenu: String,
      @Field("book[book_id]") bookId: Int): Observable<JsonObject>

  @FormUrlEncoded @POST("/users/{user_id}/books/read.json") fun read(
      @Header("X-CSRF-Token") csrfToken: String,
      @Path("user_id") userId: Int,
      @Field("read_book[book_id]") bookId: Int,
      @Field("read_book[read_at]") readAt: String,
      @Field("read_book[review]") review: String,
      @Field("read_book[review_is_netabare]") netabare: Int): Observable<JsonObject>
}
