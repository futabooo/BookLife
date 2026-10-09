package com.futabooo.android.booklife.ui.booklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.model.Resource
import com.futabooo.android.booklife.ui.navigation.BookDetail
import com.futabooo.android.booklife.ui.navigation.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class BookListUiState(
    val items: List<Resource> = emptyList(),
    val isLoading: Boolean = false,
    val endReached: Boolean = false,
    val error: Boolean = false,
)

/** One instance per [BookListMenu]. Loads pages of [PAGE_SIZE] and stops at the first short page. */
@HiltViewModel(assistedFactory = BookListViewModel.Factory::class)
class BookListViewModel @AssistedInject constructor(
    @Assisted private val menu: BookListMenu,
    private val source: BookListSource,
    private val navigator: Navigator,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(menu: BookListMenu): BookListViewModel
    }

    private val _state = MutableStateFlow(BookListUiState())
    val state: StateFlow<BookListUiState> = _state.asStateFlow()

    private var offset = 0

    init {
        loadMore()
    }

    /** Loads the next page unless a load is running or the end was reached. */
    fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.endReached) return
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            try {
                val page = source.fetch(menu, offset, PAGE_SIZE)
                offset += PAGE_SIZE
                _state.update {
                    it.copy(
                        items = it.items + page,
                        isLoading = false,
                        endReached = page.size < PAGE_SIZE,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, e.message)
                _state.update { it.copy(isLoading = false, error = true) }
            }
        }
    }

    fun onBookClick(resource: Resource) {
        val book = resource.book ?: return
        navigator.goTo(BookDetail(book.id, book.imageUrl))
    }

    companion object {
        const val PAGE_SIZE = 10
    }
}
