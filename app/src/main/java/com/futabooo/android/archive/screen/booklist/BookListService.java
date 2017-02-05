package com.futabooo.android.archive.screen.booklist;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;

public interface BookListService {

  @GET("/booklist") Observable<ResponseBody> get();
}
