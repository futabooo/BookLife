package com.futabooo.android.booklife.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futabooo.android.booklife.data.repository.HomeRepository
import com.futabooo.android.booklife.data.repository.HomeStats
import com.futabooo.android.booklife.ui.navigation.SessionExpiryHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/** Seam over [HomeRepository] (a concrete class) so the ViewModel can be unit tested with a fake. */
interface HomeStatsLoader {
    suspend fun load(): HomeStats
}

class RepositoryHomeStatsLoader @Inject constructor(
    private val repository: HomeRepository,
) : HomeStatsLoader {
    override suspend fun load(): HomeStats = repository.fetchHomeStats()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {
    @Binds
    abstract fun bindHomeStatsLoader(impl: RepositoryHomeStatsLoader): HomeStatsLoader
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val stats: HomeStats) : HomeUiState
    data object Error : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val loader: HomeStatsLoader,
    private val sessionExpiry: SessionExpiryHandler,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = HomeUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                HomeUiState.Success(loader.load())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!sessionExpiry.handle(e)) Timber.e(e, "failed to load home stats")
                HomeUiState.Error
            }
        }
    }
}
