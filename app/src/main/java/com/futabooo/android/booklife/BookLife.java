package com.futabooo.android.booklife;

import android.app.Application;
import com.futabooo.android.booklife.di.components.DaggerNetComponent;
import com.futabooo.android.booklife.di.components.NetComponent;
import com.futabooo.android.booklife.di.modules.AppModule;
import com.futabooo.android.booklife.di.modules.NetModule;

public class BookLife extends Application {

  private static final String BASE_URL = "https://i.bookmeter.com";

  private NetComponent netComponent;

  @Override public void onCreate() {
    super.onCreate();

    // Dagger%COMPONENT_NAME%
    netComponent = DaggerNetComponent.builder()
        // list of modules that are part of this component need to be created here too
        .appModule(new AppModule(this)) // This also corresponds to the name of your module: %component_name%Module
        .netModule(new NetModule(BASE_URL)).build();
  }

  public NetComponent getNetComponent() {
    return netComponent;
  }
}
