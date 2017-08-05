package com.futabooo.android.booklife.screen.login

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface LoginService {
  @GET("/login") fun get(): Observable<ResponseBody>

  @FormUrlEncoded @POST("/login") fun login(
      @Field("session[email_address]") mail: String,
      @Field("session[password]") password: String,
      @Field("authenticity_token") authenticityToken: String): Observable<ResponseBody>
}
