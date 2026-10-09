package com.futabooo.android.booklife.ui.bookdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futabooo.android.booklife.data.model.Review
import com.futabooo.android.booklife.data.repository.BookDetail
import com.futabooo.android.booklife.ui.navigation.BookDetail as BookDetailKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
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
)

@HiltViewModel(assistedFactory = BookDetailViewModel.Factory::class)
class BookDetailViewModel @AssistedInject constructor(
    @Assisted val key: BookDetailKey,
    private val source: BookDetailSource,
    actionEvents: BookActionEvents,
    private val errors: BookDetailErrorReporter,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: BookDetailKey): BookDetailViewModel
    }

    private val _state = MutableStateFlow(BookDetailUiState())
    val state: StateFlow<BookDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            actionEvents.events
                .filter { it.bookId == key.bookId }
                .collect { reloadMyReview() }
        }
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        try {
            val detail = source.fetchDetail(key.bookId)
            _state.update { it.copy(detail = detail) }
            coroutineScope {
                launch {
                    val mine = source.fetchMyReview(detail.csrfToken, key.bookId)?.review
                    _state.update { it.copy(myReview = mine) }
                }
                launch {
                    val reviews = source.fetchReviews(detail.csrfToken, key.bookId)
                    _state.update { it.copy(reviews = reviews) }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reportError(e)
        }
    }

    private suspend fun reloadMyReview() {
        val csrf = _state.value.detail?.csrfToken ?: return
        try {
            val mine = source.fetchMyReview(csrf, key.bookId)?.review
            _state.update { it.copy(myReview = mine) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reportError(e)
        }
    }

    private fun reportError(e: Exception) {
        Timber.e(e, e.message)
        errors.showLoadError()
    }
}
