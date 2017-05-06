package com.futabooo.android.booklife.screen.search;

import io.reactivex.Observable;
import org.json.JSONObject;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ActionService {

  @FormUrlEncoded @POST("/action/add_book_quick.php") Observable<JSONObject> addBook(@Field("type") String type,
      @Field("asin") String asin, @Query("t") long unixTime);
}
