package com.futabooo.android.booklife.screen.search;

import io.reactivex.Observable;
import org.json.JSONObject;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ActionService {

  @FormUrlEncoded @POST("/action/add_book_quick.php") Observable<JSONObject> addBook(
      @Field("type") String type,
      @Field("asin") String asin,
      @Query("t") long unixTime);

  @FormUrlEncoded @POST("/action/add_book_quick.php") Observable<JSONObject> addBook(
      @Field("type") String type,
      @Field("asin") String asin,
      @Field("from_dialog") int from,
      @Field("read_date_y") String year,
      @Field("read_date_m") String month,
      @Field("read_date_d") String day,
      @Field("comment") String impressions,
      @Field("category") String category,
      @Field("category_pre") String categoryPre,
      @Field("netabare") int netabare,
      @Query("t") long unixTime);

  @FormUrlEncoded @POST("/b") Observable<JSONObject> updateBook(
      @Field("asin") String asin,
      @Field("edit_no") String type,
      @Field("read_date_y") int year,
      @Field("read_date_m") int month,
      @Field("read_date_d") int day,
      @Field("comment") String impressions);
}
