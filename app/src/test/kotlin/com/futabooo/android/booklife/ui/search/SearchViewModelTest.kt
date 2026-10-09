package com.futabooo.android.booklife.ui.search

import com.futabooo.android.booklife.data.model.Book
import com.futabooo.android.booklife.data.model.SearchResultContents
import com.futabooo.android.booklife.data.model.SearchResultResource
import com.futabooo.android.booklife.ui.navigation.Search
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

private fun result(id: Int) =
    SearchResultResource(contents = SearchResultContents(Book(id = id, title = "t$id")))

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private class FakeSource(private val total: Int) : SearchSource {
        val calls = mutableListOf<Triple<String, Int, Int>>()
        override suspend fun search(keyword: String, offset: Int, limit: Int): List<SearchResultResource> {
            calls += Triple(keyword, offset, limit)
            return (offset until minOf(offset + limit, total)).map { result(it) }
        }
    }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun newQueryResetsResultsAndOffset() {
        val source = FakeSource(total = 100)
        val vm = SearchViewModel(Search(), source)

        vm.onQueryChange("a")
        vm.submit()
        vm.loadMore()
        assertEquals(40, vm.state.value.items.size)

        vm.onQueryChange("b")
        vm.submit()
        assertEquals(20, vm.state.value.items.size)
        assertEquals(Triple("b", 0, 20), source.calls.last())
    }

    @Test
    fun paginationStopsOnShortPage() {
        val source = FakeSource(total = 25)
        val vm = SearchViewModel(Search(), source)

        vm.onQueryChange("a")
        vm.submit()
        assertFalse(vm.state.value.endReached)
        vm.loadMore()
        assertEquals(25, vm.state.value.items.size)
        assertTrue(vm.state.value.endReached)

        vm.loadMore()
        assertEquals(2, source.calls.size)
    }

    @Test
    fun isbnKeyTriggersInitialSearch() {
        val source = FakeSource(total = 1)
        val vm = SearchViewModel(Search(isbn = "9784000000000"), source)

        assertEquals(listOf(Triple("9784000000000", 0, 20)), source.calls)
        assertEquals("9784000000000", vm.state.value.query)
        assertEquals(1, vm.state.value.items.size)
        assertTrue(vm.state.value.searched)
    }

    @Test
    fun noInitialSearchWithoutIsbn() {
        val source = FakeSource(total = 1)
        SearchViewModel(Search(), source)
        assertTrue(source.calls.isEmpty())
    }
}
