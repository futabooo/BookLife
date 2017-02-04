package com.futabooo.android.archive;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

interface LoginService {
  @Multipart @POST("/login") Call<ResponseBody> login(@Part("mail") RequestBody mail, @Part("password") RequestBody password);
}
