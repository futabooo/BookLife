package com.futabooo.android.booklife.screen.booklist;

import com.google.gson.JsonObject;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookListService {

  @GET("/users/{user_id}/books/{book_list_menu}") Observable<ResponseBody> get(
      @Path("user_id") int userId,
      @Path("book_list_menu") String bookListMenu);

  @GET("/users/{user_id}/books/{book_list_menu}.json") Observable<JsonObject> getJson(
      @Header("X-CSRF-Token") String csrfToken,
      @Path("user_id") int userId,
      @Path("book_list_menu") String bookListMenu,
      @Query("attach_review") String attach_review,
      @Query("offset") int offset,
      @Query("limit") int limit);
}
