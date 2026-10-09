package com.futabooo.android.booklife.ui.search

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.futabooo.android.booklife.ui.common.SnackbarController
import com.futabooo.android.booklife.ui.navigation.EntryProviderInstaller
import com.futabooo.android.booklife.ui.navigation.Navigator
import com.futabooo.android.booklife.ui.navigation.Search
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object SearchModule {

    @Provides
    fun provideSearchSource(impl: RepositorySearchSource): SearchSource = impl

    @Provides
    @IntoSet
    fun provideSearchEntries(
        navigator: Navigator,
        snackbar: SnackbarController,
    ): EntryProviderInstaller = {
        entry<Search> { key ->
            val viewModel = hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                creationCallback = { factory -> factory.create(key) },
            )
            SearchScreen(
                key = key,
                navigator = navigator,
                snackbar = snackbar,
                viewModel = viewModel,
            )
        }
    }
}
