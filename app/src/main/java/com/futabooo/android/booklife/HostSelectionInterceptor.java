package com.futabooo.android.booklife;

import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HostSelectionInterceptor implements Interceptor {

  private volatile String scheme;

  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    String scheme = this.scheme;
    if (scheme != null) {
      HttpUrl newUrl = request.url().newBuilder().scheme(scheme).build();
      request = request.newBuilder().url(newUrl).build();
    }

    return chain.proceed(request);
  }
}
