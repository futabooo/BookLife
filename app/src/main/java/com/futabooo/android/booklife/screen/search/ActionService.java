package com.futabooo.android.booklife.screen.search;

import com.google.gson.JsonObject;
import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ActionService {

  @FormUrlEncoded @POST("/users/{user_id}/books/{book_list_menu}") Observable<JsonObject> addBook(
      @Header("X-CSRF-Token") String csrfToken,
      @Path("user_id") String userId,
      @Path("book_list_menu") String bookListMenu,
      @Field("book[book_id]") int bookId);

  @FormUrlEncoded @POST("/users/{user_id}/books/read.json") Observable<JsonObject> read(
      @Header("X-CSRF-Token") String csrfToken,
      @Path("user_id") String userId,
      @Field("read_book[book_id]") int bookId,
      @Field("read_book[read_at]") String readAt,
      @Field("read_book[review]") String review,
      @Field("read_book[review_is_netabare]") int netabare);
}
