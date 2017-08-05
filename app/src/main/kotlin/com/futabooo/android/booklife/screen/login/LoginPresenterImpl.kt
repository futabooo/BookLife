package com.futabooo.android.booklife.screen.login

class LoginPresenterImpl : LoginPresenter {
  override fun autoLogin() {

  }

  override fun isEmailValid(email: String): Boolean = email.contains("@")

  override fun isPasswordValid(password: String): Boolean = password.isNotEmpty()
}
