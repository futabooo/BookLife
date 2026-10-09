package com.futabooo.android.booklife.ui.booklist

import com.futabooo.android.booklife.data.model.Book
import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.model.Resource
import com.futabooo.android.booklife.ui.navigation.Navigator
import com.futabooo.android.booklife.ui.navigation.SessionExpiryHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookListViewModelTest {

    private class FakeSource(private val total: Int) : BookListSource {
        val offsets = mutableListOf<Int>()
        var gate: CompletableDeferred<Unit>? = null
        var failNext = false

        override suspend fun fetch(menu: BookListMenu, offset: Int, limit: Int): List<Resource> {
            offsets += offset
            gate?.await()
            if (failNext) {
                failNext = false
                error("boom")
            }
            return (offset until minOf(offset + limit, total)).map {
                Resource(id = it, book = Book(id = it, title = "book $it"))
            }
        }
    }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads first page on init and increments offset by 10`() {
        val source = FakeSource(total = 35)
        val vm = BookListViewModel(BookListMenu.READ, source, Navigator(), SessionExpiryHandler(onExpired = {}))
        assertEquals(10, vm.state.value.items.size)
        vm.loadMore()
        vm.loadMore()
        assertEquals(listOf(0, 10, 20), source.offsets)
        assertEquals(30, vm.state.value.items.size)
        assertFalse(vm.state.value.endReached)
    }

    @Test
    fun `stops at a short page`() {
        val source = FakeSource(total = 15)
        val vm = BookListViewModel(BookListMenu.READING, source, Navigator(), SessionExpiryHandler(onExpired = {}))
        vm.loadMore()
        assertTrue(vm.state.value.endReached)
        assertEquals(15, vm.state.value.items.size)
        vm.loadMore()
        assertEquals(listOf(0, 10), source.offsets)
    }

    @Test
    fun `does not load concurrently`() {
        val source = FakeSource(total = 100).apply { gate = CompletableDeferred() }
        val vm = BookListViewModel(BookListMenu.TO_READ, source, Navigator(), SessionExpiryHandler(onExpired = {}))
        assertTrue(vm.state.value.isLoading)
        vm.loadMore()
        vm.loadMore()
        assertEquals(listOf(0), source.offsets)
        source.gate!!.complete(Unit)
        assertFalse(vm.state.value.isLoading)
        assertEquals(10, vm.state.value.items.size)
    }

    @Test
    fun `error keeps offset and can be retried`() {
        val source = FakeSource(total = 100).apply { failNext = true }
        val vm = BookListViewModel(BookListMenu.QUITTED, source, Navigator(), SessionExpiryHandler(onExpired = {}))
        assertTrue(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
        vm.loadMore()
        assertFalse(vm.state.value.error)
        assertEquals(listOf(0, 0), source.offsets)
        assertEquals(10, vm.state.value.items.size)
    }
}
