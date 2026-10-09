package com.futabooo.android.booklife.ui.common

import android.content.Context
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.repository.BookActionRepository
import com.futabooo.android.booklife.data.repository.SessionRepository
import com.futabooo.android.booklife.ui.navigation.Navigator
import com.futabooo.android.booklife.ui.navigation.ReadBookDialog
import dagger.hilt.android.ActivityRetainedLifecycle
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/** Emitted after a book was successfully registered / its review updated. */
sealed interface BookActionEvent {
    val bookId: Int

    data class Registered(override val bookId: Int) : BookActionEvent
    data class Updated(override val bookId: Int) : BookActionEvent
}

/**
 * Port of the old `onBottomSheetAction` logic, shared by BookDetail and Search. Inject it where
 * needed (it is `@ActivityRetainedScoped`; screens normally do not call it directly, see below).
 *
 * Flow:
 *  1. A screen's "+" button does `navigator.goTo(RegisterBook(bookId))` -> bottom sheet.
 *  2. The sheet calls [onMenuSelected]: READ opens the `ReadBookDialog` (with a fresh CSRF token),
 *     the other shelves call `BookActionRepository.addBook` and show the `book_registered` snackbar
 *     (`error_review_update` on failure).
 *  3. The dialog calls [submitReadBook] / [updateReadBook].
 *  4. Successful actions are published on [events] so that screens can refresh
 *     (e.g. BookDetail reloads "my review").
 *
 * To edit an existing review a screen navigates to `ReadBookDialog(bookId, csrf, reviewId, ...)`
 * directly.
 */
@ActivityRetainedScoped
class BookActionCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val actions: BookActionRepository,
    private val session: SessionRepository,
    private val navigator: Navigator,
    private val snackbar: SnackbarController,
    lifecycle: ActivityRetainedLifecycle,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _events = MutableSharedFlow<BookActionEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<BookActionEvent> = _events.asSharedFlow()

    init {
        lifecycle.addOnClearedListener { scope.cancel() }
    }

    /** The user picked [menu] in the register sheet for [bookId]. */
    fun onMenuSelected(menu: BookListMenu, bookId: Int) {
        scope.launch {
            runAction {
                val csrf = actions.fetchCsrfToken()
                when (menu) {
                    BookListMenu.READ -> navigator.goTo(ReadBookDialog(bookId = bookId, csrfToken = csrf))
                    else -> {
                        actions.addBook(csrf, session.resolveUserId(), menu, bookId)
                        onRegistered(bookId)
                    }
                }
            }
        }
    }

    /** Confirm of the dialog opened for a new read book. */
    fun submitReadBook(
        csrfToken: String,
        bookId: Int,
        review: String,
        readAt: String,
        netabare: Boolean,
    ) {
        scope.launch {
            runAction {
                actions.addReadBook(csrfToken, session.resolveUserId(), bookId, readAt, review, netabare)
                navigator.goBack()
                onRegistered(bookId)
            }
        }
    }

    /** Confirm of the dialog opened to edit review [reviewId]. */
    fun updateReadBook(
        csrfToken: String,
        reviewId: Int,
        bookId: Int,
        review: String,
        readAt: String,
        netabare: Boolean,
    ) {
        scope.launch {
            runAction {
                actions.updateReadBook(csrfToken, reviewId, bookId, readAt, review, netabare)
                navigator.goBack()
                snackbar.show(context.getString(R.string.book_update, bookId.toString()))
                _events.emit(BookActionEvent.Updated(bookId))
            }
        }
    }

    private suspend fun onRegistered(bookId: Int) {
        snackbar.show(context.getString(R.string.book_registered, bookId.toString()))
        _events.emit(BookActionEvent.Registered(bookId))
    }

    private suspend fun runAction(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, e.message)
            snackbar.show(context.getString(R.string.error_review_update))
        }
    }
}
