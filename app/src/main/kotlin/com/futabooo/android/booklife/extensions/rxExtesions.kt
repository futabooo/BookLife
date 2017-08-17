package com.futabooo.android.booklife.extensions

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

val <T> Observable<T>.subscribeOnIO: Observable<T>
  get() = subscribeOn(Schedulers.io())

val <T> Observable<T>.observeOnUI: Observable<T>
  get() = observeOn(AndroidSchedulers.mainThread())