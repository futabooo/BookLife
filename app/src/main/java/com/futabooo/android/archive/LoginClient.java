package com.futabooo.android.archive;

import retrofit2.Call;
import retrofit2.http.GET;

public class LoginClient {

  public

  interface LoginService{
    @GET("/login")
    Call<String> login();
  }

}
