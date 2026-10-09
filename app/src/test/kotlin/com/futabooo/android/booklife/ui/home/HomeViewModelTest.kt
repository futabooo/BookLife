package com.futabooo.android.booklife.ui.home

import com.futabooo.android.booklife.data.repository.HomeStats
import com.futabooo.android.booklife.ui.navigation.SessionExpiryHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private class FakeLoader(private val results: MutableList<Result<HomeStats>>) : HomeStatsLoader {
        override suspend fun load(): HomeStats = results.removeAt(0).getOrThrow()
    }

    private val stats = HomeStats(pages = "1000", volumes = "20", pagesPerDay = "33")

    @Test
    fun loadsStats_loadingThenSuccess() = runTest(dispatcher) {
        val vm = HomeViewModel(FakeLoader(mutableListOf(Result.success(stats))), SessionExpiryHandler(onExpired = {}))
        assertEquals(HomeUiState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(HomeUiState.Success(stats), vm.state.value)
    }

    @Test
    fun loadFailure_loadingThenError_retrySucceeds() = runTest(dispatcher) {
        val vm = HomeViewModel(
            FakeLoader(mutableListOf(Result.failure(RuntimeException("boom")), Result.success(stats))),
            SessionExpiryHandler(onExpired = {}),
        )
        assertEquals(HomeUiState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(HomeUiState.Error, vm.state.value)
        vm.retry()
        assertEquals(HomeUiState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(HomeUiState.Success(stats), vm.state.value)
    }

    @Test
    fun sessionExpiry_isHandledCentrally() = runTest(dispatcher) {
        var expired = 0
        val vm = HomeViewModel(
            FakeLoader(mutableListOf(Result.failure(com.futabooo.android.booklife.data.network.SessionExpiredException()))),
            SessionExpiryHandler(onExpired = { expired++ }),
        )
        advanceUntilIdle()
        assertEquals(1, expired)
        assertEquals(HomeUiState.Error, vm.state.value)
    }
}
