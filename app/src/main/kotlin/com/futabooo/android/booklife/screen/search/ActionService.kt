package com.futabooo.android.booklife.screen.search

import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
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

  @FormUrlEncoded @POST("/users/{user_id}/books/read.json") fun readUnknown(
      @Header("X-CSRF-Token") csrfToken: String,
      @Path("user_id") userId: Int,
      @Field("read_book[book_id]") bookId: Int,
      @Field("checkbox-example") unknown: String,
      @Field("read_book[review]") review: String,
      @Field("read_book[review_is_netabare]") netabare: Int): Observable<JsonObject>

  @FormUrlEncoded @PUT("/read_books/{id}.json") fun update(
      @Header("X-CSRF-Token") csrfToken: String,
      @Path("id") id: Int,
      @Field("read_book[book_id]") bookId: Int,
      @Field("read_book[read_at]") readAt: String,
      @Field("read_book[review]") review: String,
      @Field("read_book[review_is_netabare]") netabare: Int): Completable

  @FormUrlEncoded @PUT("/read_books/{id}.json") fun updateUnknown(
      @Header("X-CSRF-Token") csrfToken: String,
      @Path("id") id: Int,
      @Field("read_book[book_id]") bookId: Int,
      @Field("checkbox-example") unknown: String,
      @Field("read_book[review]") review: String,
      @Field("read_book[review_is_netabare]") netabare: Int): Completable
}
