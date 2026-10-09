package com.futabooo.android.booklife.ui.splash

import com.futabooo.android.booklife.ui.navigation.EntryProviderInstaller
import com.futabooo.android.booklife.ui.navigation.Splash
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object SplashModule {
    @Provides
    @IntoSet
    fun provideSplashEntries(): EntryProviderInstaller = {
        entry<Splash> { SplashScreen() }
    }
}
