package com.futabooo.android.booklife.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futabooo.android.booklife.data.model.SearchResultResource
import com.futabooo.android.booklife.data.repository.SearchRepository
import com.futabooo.android.booklife.ui.navigation.Search
import com.futabooo.android.booklife.ui.navigation.SessionExpiryHandler
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/** Data access of the search screen (kept behind an interface so the ViewModel is easy to test). */
interface SearchSource {
    suspend fun search(keyword: String, offset: Int, limit: Int): List<SearchResultResource>
}

class RepositorySearchSource @Inject constructor(
    private val repository: SearchRepository,
) : SearchSource {
    override suspend fun search(keyword: String, offset: Int, limit: Int): List<SearchResultResource> =
        repository.search(keyword, offset, limit).items
}

data class SearchUiState(
    val query: String = "",
    val items: List<SearchResultResource> = emptyList(),
    val isLoading: Boolean = false,
    val endReached: Boolean = false,
    /** True once a search returned successfully, so an empty list means "no results". */
    val searched: Boolean = false,
)

@HiltViewModel(assistedFactory = SearchViewModel.Factory::class)
class SearchViewModel @AssistedInject constructor(
    @Assisted key: Search,
    private val source: SearchSource,
    private val sessionExpiry: SessionExpiryHandler,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: Search): SearchViewModel
    }

    private val _state = MutableStateFlow(SearchUiState(query = key.isbn.orEmpty()))
    val state: StateFlow<SearchUiState> = _state

    private val _errors = Channel<Throwable>(Channel.BUFFERED)

    /** One-shot search failures (shown as a snackbar by the screen). */
    val errors = _errors.receiveAsFlow()

    private var keyword = ""
    private var offset = 0
    private var job: Job? = null

    init {
        if (!key.isbn.isNullOrEmpty()) submit()
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    /** Starts a new search for the current query: clears results and resets the offset. */
    fun submit() {
        val query = _state.value.query
        if (query.isBlank()) return
        job?.cancel()
        keyword = query
        offset = 0
        _state.update { it.copy(items = emptyList(), isLoading = false, endReached = false, searched = false) }
        load()
    }

    /** Loads the next page; no-op while loading, after the last page, or before any search. */
    fun loadMore() {
        val s = _state.value
        if (s.isLoading || s.endReached || !s.searched) return
        load()
    }

    private fun load() {
        _state.update { it.copy(isLoading = true) }
        job = viewModelScope.launch {
            try {
                val page = source.search(keyword, offset, PAGE_SIZE)
                offset += PAGE_SIZE
                _state.update {
                    it.copy(
                        items = it.items + page,
                        isLoading = false,
                        endReached = page.size < PAGE_SIZE,
                        searched = true,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                if (!sessionExpiry.handle(e)) {
                    Timber.e(e, e.message)
                    _errors.trySend(e)
                }
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
