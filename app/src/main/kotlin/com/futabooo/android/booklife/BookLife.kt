package com.futabooo.android.booklife

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.futabooo.android.booklife.di.components.DaggerNetComponent
import com.futabooo.android.booklife.di.components.NetComponent
import com.futabooo.android.booklife.di.modules.AppModule
import com.futabooo.android.booklife.di.modules.NetModule
import io.fabric.sdk.android.Fabric
import timber.log.Timber

class BookLife : Application() {

  lateinit var netComponent: NetComponent

  companion object {
    private val BASE_URL = "https://bookmeter.com"
  }

  override fun onCreate() {
    super.onCreate()

    val crashlytics = Crashlytics.Builder()
        .core(
            CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build()
        )
        .build()
    Fabric.with(this, crashlytics)

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    } else {
      Timber.plant(CrashReportingTree())
    }

    // Dagger%COMPONENT_NAME%
    netComponent = DaggerNetComponent.builder()
        // list of modules that are part of this component need to be created here too
        .appModule(AppModule(this)) // This also corresponds to the name of your module: %component_name%Module
        .netModule(NetModule(BASE_URL))
        .build()
  }

}
