package com.futabooo.android.booklife.ui.booklist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.ui.main.BookListPageRenderer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import javax.inject.Inject

class BookListPageRendererImpl @Inject constructor() : BookListPageRenderer {
    @Composable
    override fun Render(menu: BookListMenu, contentPadding: PaddingValues) = BookListScreen(menu, contentPadding = contentPadding)
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class BookListPageRendererModule {
    @Binds
    abstract fun bind(impl: BookListPageRendererImpl): BookListPageRenderer
}
