package com.futabooo.android.booklife.ui.login

import com.futabooo.android.booklife.ui.navigation.EntryProviderInstaller
import com.futabooo.android.booklife.ui.navigation.Login
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object LoginModule {
    @Provides
    @IntoSet
    fun provideLoginEntries(): EntryProviderInstaller = {
        entry<Login> { LoginScreen() }
    }
}
