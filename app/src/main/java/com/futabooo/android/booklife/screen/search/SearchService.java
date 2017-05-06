package com.futabooo.android.booklife.screen.search;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchService {

  @GET("/s") Observable<ResponseBody> get(@Query("q") String query);
}
