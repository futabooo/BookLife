package com.futabooo.android.booklife.ui.main

import com.futabooo.android.booklife.analytics.Analytics
import com.futabooo.android.booklife.ui.common.SnackbarController
import com.futabooo.android.booklife.ui.navigation.EntryProviderInstaller
import com.futabooo.android.booklife.ui.navigation.Main
import com.futabooo.android.booklife.ui.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import java.util.Optional

@Module
@InstallIn(ActivityRetainedComponent::class)
object MainModule {
    @Provides
    @IntoSet
    fun provideMainEntries(
        navigator: Navigator,
        analytics: Analytics,
        snackbar: SnackbarController,
        bookListPageRenderer: Optional<BookListPageRenderer>,
    ): EntryProviderInstaller = {
        entry<Main> {
            MainScreen(
                navigator = navigator,
                analytics = analytics,
                onShowMessage = snackbar::show,
                bookListPageRenderer = bookListPageRenderer.orElse(null),
            )
        }
    }
}
