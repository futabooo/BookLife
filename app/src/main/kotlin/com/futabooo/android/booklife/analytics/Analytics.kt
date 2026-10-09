package com.futabooo.android.booklife.analytics

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/** Minimal analytics facade (inject it; tests can supply a fake). */
interface Analytics {
    fun logEvent(name: String)
}

/** Firebase-backed implementation; a no-op when Firebase is not configured. */
@Singleton
class FirebaseAnalyticsImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : Analytics {

    private val firebase: FirebaseAnalytics? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseAnalytics.getInstance(context) else null
        } catch (e: IllegalStateException) {
            null
        }
    }

    override fun logEvent(name: String) {
        Timber.d("analytics event: %s", name)
        firebase?.logEvent(name, null)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    abstract fun bindAnalytics(impl: FirebaseAnalyticsImpl): Analytics
}
