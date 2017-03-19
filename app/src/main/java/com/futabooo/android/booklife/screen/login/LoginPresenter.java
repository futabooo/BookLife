package com.futabooo.android.booklife.screen.login;

public interface LoginPresenter {

  void autoLogin();

  boolean isEmailValid(String email);

  boolean isPasswordValid(String password);
}
