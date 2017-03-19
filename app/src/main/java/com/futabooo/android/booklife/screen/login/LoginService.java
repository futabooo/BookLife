package com.futabooo.android.booklife.screen.login;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

interface LoginService {
  @FormUrlEncoded @POST("/login") Observable<ResponseBody> login(@Field("mail") String mail,
      @Field("password") String password);
}
