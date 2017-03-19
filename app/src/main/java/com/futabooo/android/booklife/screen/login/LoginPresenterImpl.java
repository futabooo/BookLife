package com.futabooo.android.booklife.screen.login;

public class LoginPresenterImpl implements LoginPresenter {
  @Override public void autoLogin() {

  }

  @Override public boolean isEmailValid(String email) {
    return email.contains("@");
  }

  @Override public boolean isPasswordValid(String password) {
    return password.length() > 0;
  }
}
