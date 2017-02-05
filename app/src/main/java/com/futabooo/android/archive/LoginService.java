package com.futabooo.android.archive;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

interface LoginService {
  @FormUrlEncoded @POST("/login") Call<ResponseBody> login(@Field("mail") String mail, @Field("password") String password);
}
