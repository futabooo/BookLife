package com.futabooo.android.booklife.ui.bookdetail

import android.content.Context
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.data.model.BookDetailResource
import com.futabooo.android.booklife.data.model.Review
import com.futabooo.android.booklife.data.repository.BookDetail
import com.futabooo.android.booklife.data.repository.BookDetailRepository
import com.futabooo.android.booklife.ui.common.BookActionCoordinator
import com.futabooo.android.booklife.ui.common.BookActionEvent
import com.futabooo.android.booklife.ui.common.SnackbarController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** Data seam of [BookDetailViewModel] (delegates to [BookDetailRepository]); fakeable in tests. */
interface BookDetailSource {
    suspend fun fetchDetail(bookId: Int): BookDetail
    suspend fun fetchMyReview(csrfToken: String, bookId: Int): BookDetailResource?
    suspend fun fetchReviews(csrfToken: String, bookId: Int): List<Review>
}

/** Event seam over [BookActionCoordinator.events]. */
interface BookActionEvents {
    val events: Flow<BookActionEvent>
}

/** Shows a load error to the user (snackbar). */
interface BookDetailErrorReporter {
    fun showLoadError()
}

class RepositoryBookDetailSource @Inject constructor(
    private val repository: BookDetailRepository,
) : BookDetailSource {
    override suspend fun fetchDetail(bookId: Int) = repository.fetchDetail(bookId)

    override suspend fun fetchMyReview(csrfToken: String, bookId: Int) =
        repository.fetchMyReview(csrfToken, bookId)

    override suspend fun fetchReviews(csrfToken: String, bookId: Int) =
        repository.fetchReviews(csrfToken, bookId)
}

class CoordinatorBookActionEvents @Inject constructor(
    coordinator: BookActionCoordinator,
) : BookActionEvents {
    override val events: Flow<BookActionEvent> = coordinator.events
}

class SnackbarBookDetailErrorReporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snackbar: SnackbarController,
) : BookDetailErrorReporter {
    override fun showLoadError() = snackbar.show(context.getString(R.string.load_error))
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class BookDetailBindingsModule {
    @Binds
    abstract fun bindSource(impl: RepositoryBookDetailSource): BookDetailSource

    @Binds
    abstract fun bindEvents(impl: CoordinatorBookActionEvents): BookActionEvents

    @Binds
    abstract fun bindErrorReporter(impl: SnackbarBookDetailErrorReporter): BookDetailErrorReporter
}
