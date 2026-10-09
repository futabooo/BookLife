package com.futabooo.android.booklife.ui.bookdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.model.Review
import com.futabooo.android.booklife.data.repository.BookDetail
import com.futabooo.android.booklife.ui.common.BookActionEvent
import com.futabooo.android.booklife.ui.navigation.BookDetail as BookDetailKey
import com.futabooo.android.booklife.ui.navigation.SessionExpiryHandler
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class BookDetailUiState(
    val detail: BookDetail? = null,
    /** The signed-in user's own review; null (section hidden) when there is none. */
    val myReview: Review? = null,
    val reviews: List<Review> = emptyList(),
    /** Loading the book itself failed ([detail] stays null); the screen offers a retry. */
    val error: Boolean = false,
)

@HiltViewModel(assistedFactory = BookDetailViewModel.Factory::class)
class BookDetailViewModel @AssistedInject constructor(
    @Assisted val key: BookDetailKey,
    private val source: BookDetailSource,
    actionEvents: BookActionEvents,
    private val errors: BookDetailErrorReporter,
    private val sessionExpiry: SessionExpiryHandler,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: BookDetailKey): BookDetailViewModel
    }

    private val _state = MutableStateFlow(BookDetailUiState())
    val state: StateFlow<BookDetailUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            actionEvents.events
                .filter { it.bookId == key.bookId }
                .collect { onActionEvent(it) }
        }
        retry()
    }

    /** (Re)loads the book; ignored while a load is running. */
    fun retry() {
        if (loadJob?.isActive == true) return
        _state.update { it.copy(error = false) }
        loadJob = viewModelScope.launch { load() }
    }

    private suspend fun load() {
        try {
            val detail = source.fetchDetail(key.bookId)
            _state.update { it.copy(detail = detail, error = false) }
            supervisorScope {
                launch { loadMyReview() }
                launch {
                    try {
                        val reviews = source.fetchReviews(key.bookId)
                        _state.update { it.copy(reviews = reviews) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (!sessionExpiry.handle(e)) Timber.w(e, "Failed to load reviews")
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (sessionExpiry.handle(e)) return
            _state.update { it.copy(error = true) }
            reportError(e)
        }
    }

    /**
     * A registered READ book needs one lookup of the new review; other shelves have no review. An
     * edited review is patched locally from the submitted values (no rescan of the public reviews).
     */
    private suspend fun onActionEvent(event: BookActionEvent) {
        when (event) {
            is BookActionEvent.Registered -> if (event.menu == BookListMenu.READ) loadMyReview()
            is BookActionEvent.Updated -> {
                val mine = _state.value.myReview
                if (mine != null && mine.id == event.reviewId) {
                    _state.update {
                        it.copy(
                            myReview = mine.copy(
                                content = event.review,
                                netabare = mine.netabare.copy(netabare = event.netabare),
                                readAt = event.readAt ?: mine.readAt,
                            ),
                        )
                    }
                } else {
                    loadMyReview()
                }
            }
        }
    }

    /** Secondary load: failures are logged, never shown, and leave the current state untouched. */
    private suspend fun loadMyReview() {
        try {
            val mine = source.fetchMyReview(key.bookId)?.review
            _state.update { it.copy(myReview = mine) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!sessionExpiry.handle(e)) Timber.w(e, "Failed to load my review")
        }
    }

    private fun reportError(e: Exception) {
        Timber.e(e, e.message)
        errors.showLoadError()
    }
}
