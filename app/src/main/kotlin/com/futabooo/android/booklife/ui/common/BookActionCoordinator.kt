package com.futabooo.android.booklife.ui.common

import android.content.Context
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.repository.BookActionRepository
import com.futabooo.android.booklife.data.repository.SessionRepository
import com.futabooo.android.booklife.ui.navigation.Navigator
import com.futabooo.android.booklife.ui.navigation.SessionExpiryHandler
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/** Emitted after a book was successfully registered / its review updated. */
sealed interface BookActionEvent {
    val bookId: Int

    /** [menu] is the shelf the book was added to (only READ creates a review). */
    data class Registered(override val bookId: Int, val menu: BookListMenu) : BookActionEvent

    /**
     * The review [reviewId] was updated; carries the submitted values so that screens can patch their
     * copy instead of refetching. [readAt] is null when the date was left untouched.
     */
    data class Updated(
        override val bookId: Int,
        val reviewId: Int,
        val review: String,
        val readAt: String?,
        val netabare: Boolean,
    ) : BookActionEvent
}

/**
 * Port of the old `onBottomSheetAction` logic, shared by BookDetail and Search. Inject it where
 * needed (it is `@ActivityRetainedScoped`; screens normally do not call it directly, see below).
 *
 * Flow:
 *  1. A screen's "+" button does `navigator.goTo(RegisterBook(bookId))` -> bottom sheet.
 *  2. The sheet calls [onMenuSelected]: READ opens the `ReadBookDialog`, the other shelves call
 *     `BookActionRepository.addBook` and show the `book_registered` snackbar
 *     (`error_review_update` on failure).
 *  3. The dialog calls [submitReadBook] / [updateReadBook].
 *  4. Successful actions are published on [events] so that screens can refresh
 *     (e.g. BookDetail reloads "my review").
 *
 * Only one action runs at a time ([inFlight]); further calls while one is running are ignored, which
 * makes a double tap on the dialog's button harmless. A failed dialog submit is also exposed as
 * [dialogError] so the dialog can show it next to the buttons.
 *
 * To edit an existing review a screen navigates to `ReadBookDialog(bookId, reviewId, ...)` directly.
 */
@ActivityRetainedScoped
class BookActionCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val actions: BookActionRepository,
    private val session: SessionRepository,
    private val navigator: Navigator,
    private val snackbar: SnackbarController,
    private val sessionExpiry: SessionExpiryHandler,
    lifecycle: ActivityRetainedLifecycle,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _events = MutableSharedFlow<BookActionEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<BookActionEvent> = _events.asSharedFlow()

    private val _inFlight = MutableStateFlow(false)

    /** True while a registration / update request is running. */
    val inFlight: StateFlow<Boolean> = _inFlight.asStateFlow()

    private val _dialogError = MutableStateFlow(false)

    /** True after the last dialog submit failed (until [clearDialogError] or the next action). */
    val dialogError: StateFlow<Boolean> = _dialogError.asStateFlow()

    init {
        lifecycle.addOnClearedListener { scope.cancel() }
    }

    fun clearDialogError() {
        _dialogError.value = false
    }

    /** The user picked [menu] in the register sheet for [bookId]. */
    fun onMenuSelected(menu: BookListMenu, bookId: Int) {
        if (menu == BookListMenu.READ) {
            clearDialogError()
            navigator.goTo(ReadBookDialog(bookId = bookId))
            return
        }
        launchAction(fromDialog = false) {
            actions.addBook(session.resolveUserId(), menu, bookId)
            onRegistered(bookId, menu)
        }
    }

    /** Confirm of the dialog opened for a new read book. */
    fun submitReadBook(
        bookId: Int,
        review: String,
        readAt: String,
        netabare: Boolean,
    ) {
        launchAction(fromDialog = true) {
            actions.addReadBook(session.resolveUserId(), bookId, readAt, review, netabare)
            navigator.goBack()
            onRegistered(bookId, BookListMenu.READ)
        }
    }

    /** Confirm of the dialog opened to edit review [reviewId]; a null [readAt] keeps the stored date. */
    fun updateReadBook(
        reviewId: Int,
        bookId: Int,
        review: String,
        readAt: String?,
        netabare: Boolean,
    ) {
        launchAction(fromDialog = true) {
            actions.updateReadBook(reviewId, bookId, readAt, review, netabare)
            navigator.goBack()
            snackbar.show(context.getString(R.string.book_update, bookId.toString()))
            _events.emit(BookActionEvent.Updated(bookId, reviewId, review, readAt, netabare))
        }
    }

    private suspend fun onRegistered(bookId: Int, menu: BookListMenu) {
        snackbar.show(context.getString(R.string.book_registered, bookId.toString()))
        _events.emit(BookActionEvent.Registered(bookId, menu))
    }

    private fun launchAction(fromDialog: Boolean, block: suspend () -> Unit) {
        if (!_inFlight.compareAndSet(false, true)) return
        _dialogError.value = false
        scope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!sessionExpiry.handle(e)) {
                    Timber.e(e, e.message)
                    if (fromDialog) _dialogError.value = true
                    snackbar.show(context.getString(R.string.error_review_update))
                }
            } finally {
                _inFlight.value = false
            }
        }
    }
}
