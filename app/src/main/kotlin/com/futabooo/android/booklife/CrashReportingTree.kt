package com.futabooo.android.booklife

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Forwards logs to Firebase Crashlytics. Does nothing when Firebase is not configured (the build
 * has no google-services.json), so the app never crashes because of it.
 */
class CrashReportingTree(context: Context) : Timber.Tree() {

    private val crashlytics: FirebaseCrashlytics? = try {
        if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseCrashlytics.getInstance() else null
    } catch (e: IllegalStateException) {
        null
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val crashlytics = crashlytics ?: return
        if (priority < Log.INFO) return
        crashlytics.log("${tag ?: "BookLife"}: $message")
        if (t != null && priority >= Log.WARN) {
            crashlytics.recordException(t)
        }
    }
}
