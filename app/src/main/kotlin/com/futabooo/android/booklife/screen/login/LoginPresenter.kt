package com.futabooo.android.booklife.screen.login

interface LoginPresenter {

  fun autoLogin()

  fun isEmailValid(email: String): Boolean

  fun isPasswordValid(password: String): Boolean
}
