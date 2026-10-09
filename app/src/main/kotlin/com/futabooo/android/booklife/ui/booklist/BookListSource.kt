package com.futabooo.android.booklife.ui.booklist

import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.model.Resource
import com.futabooo.android.booklife.data.repository.BookListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Inject

/** Page source of a bookshelf; a seam so that [BookListViewModel] can be tested with a fake. */
interface BookListSource {
    suspend fun fetch(menu: BookListMenu, offset: Int, limit: Int): List<Resource>
}

class RepositoryBookListSource @Inject constructor(
    private val repository: BookListRepository,
) : BookListSource {
    override suspend fun fetch(menu: BookListMenu, offset: Int, limit: Int): List<Resource> =
        repository.fetch(menu, offset, limit)
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class BookListBindingsModule {
    @Binds
    abstract fun bindBookListSource(impl: RepositoryBookListSource): BookListSource
}
