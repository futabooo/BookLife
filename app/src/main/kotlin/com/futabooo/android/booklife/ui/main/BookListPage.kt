package com.futabooo.android.booklife.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.futabooo.android.booklife.data.model.BookListMenu
import dagger.Module
import dagger.BindsOptionalOf
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

/**
 * Renders one bookshelf page (one `HorizontalPager` page of the Book tab).
 *
 * The page content is owned by the BookList feature (B3). This interface is the seam between the
 * Main shell and that feature:
 *
 * - The shell injects `java.util.Optional<BookListPageRenderer>` (declared with `@BindsOptionalOf`
 *   in [BookListPageRendererOptionalModule]).
 * - While nothing is bound the shell shows [PlaceholderBookListPage] (the tab title).
 * - **Phase C binding**: in the BookList package add an
 *   `@Module @InstallIn(ActivityRetainedComponent::class) abstract class ... { @Binds abstract fun bind(impl: BookListPageRendererImpl): BookListPageRenderer }`
 *   (impl has an `@Inject constructor()`); Dagger then makes the Optional present.
 *
 * [contentPadding] already contains the Scaffold insets (top bar + tabs, bottom bar); apply it as the
 * list's `contentPadding` so content scrolls under the bars edge-to-edge.
 */
interface BookListPageRenderer {
    @Composable
    fun Render(menu: BookListMenu, contentPadding: PaddingValues)
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class BookListPageRendererOptionalModule {
    @BindsOptionalOf
    abstract fun optionalBookListPageRenderer(): BookListPageRenderer
}

/** Shown until a [BookListPageRenderer] is bound. */
@Composable
fun PlaceholderBookListPage(menu: BookListMenu, contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
        Text(stringResource(menu.titleResId))
    }
}
