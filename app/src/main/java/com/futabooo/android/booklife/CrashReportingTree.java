package com.futabooo.android.booklife;

import android.util.Log;
import com.crashlytics.android.Crashlytics;
import timber.log.Timber;

public class CrashReportingTree extends Timber.Tree {

  @Override protected void log(int priority, String tag, String message, Throwable t) {
    if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
      Crashlytics.log(priority, tag, message);
      return;
    }

    if (t != null) {
      if (priority == Log.ERROR) {
        Crashlytics.logException(t);
      } else if (priority == Log.WARN) {
        Crashlytics.logException(new Exception(message));
      }
    }
  }
}
