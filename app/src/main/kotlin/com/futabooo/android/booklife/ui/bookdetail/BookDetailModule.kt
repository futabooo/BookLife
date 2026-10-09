package com.futabooo.android.booklife.ui.bookdetail

import com.futabooo.android.booklife.ui.navigation.BookDetail
import com.futabooo.android.booklife.ui.navigation.EntryProviderInstaller
import com.futabooo.android.booklife.ui.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object BookDetailModule {
    @Provides
    @IntoSet
    fun provideBookDetailEntries(navigator: Navigator): EntryProviderInstaller = {
        entry<BookDetail> { key -> BookDetailScreen(key = key, navigator = navigator) }
    }
}
