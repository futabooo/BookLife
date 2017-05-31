package com.futabooo.android.booklife.screen.login;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

interface LoginService {
  @GET("/login") Observable<ResponseBody> get();

  @FormUrlEncoded @POST("/login") Observable<ResponseBody> login(@Field("session[email_address]") String mail,
      @Field("session[password]") String password,
      @Field("authenticity_token") String authenticityToken);
}
