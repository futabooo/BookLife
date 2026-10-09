package com.futabooo.android.booklife.ui.bookdetail

import com.futabooo.android.booklife.data.model.BookDetailResource
import com.futabooo.android.booklife.data.model.Review
import com.futabooo.android.booklife.data.repository.BookDetail
import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.network.SessionExpiredException
import com.futabooo.android.booklife.ui.common.BookActionEvent
import com.futabooo.android.booklife.ui.navigation.SessionExpiryHandler
import com.futabooo.android.booklife.ui.navigation.BookDetail as BookDetailKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailViewModelTest {

    private class FakeSource : BookDetailSource {
        var myReview: Review? = null
        var fail = false
        var failure: Exception? = null
        var myReviewFails = false
        var myReviewCalls = 0

        override suspend fun fetchDetail(bookId: Int): BookDetail {
            failure?.let { throw it }
            if (fail) error("boom")
            return BookDetail("title $bookId", "author", "thumb", "https://amazon")
        }

        override suspend fun fetchMyReview(bookId: Int): BookDetailResource? {
            myReviewCalls++
            if (myReviewFails) error("my review boom")
            return myReview?.let { BookDetailResource(review = it) }
        }

        override suspend fun fetchReviews(bookId: Int): List<Review> =
            listOf(Review(id = 1, content = "a"), Review(id = 2, content = "b"))
    }

    private class FakeEvents : BookActionEvents {
        val flow = MutableSharedFlow<BookActionEvent>(extraBufferCapacity = 8)
        override val events = flow
    }

    private class FakeErrors : BookDetailErrorReporter {
        var count = 0
        override fun showLoadError() {
            count++
        }
    }

    private val NOOP = SessionExpiryHandler(onExpired = {})

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads detail, my review and reviews`() {
        val source = FakeSource().apply { myReview = Review(id = 9, content = "mine") }
        val vm = BookDetailViewModel(BookDetailKey(1, "img"), source, FakeEvents(), FakeErrors(), NOOP)
        val state = vm.state.value
        assertEquals("title 1", state.detail?.title)
        assertEquals("mine", state.myReview?.content)
        assertEquals(2, state.reviews.size)
    }

    @Test
    fun `no review hides my review`() {
        val vm = BookDetailViewModel(BookDetailKey(1, "img"), FakeSource(), FakeEvents(), FakeErrors(), NOOP)
        assertNull(vm.state.value.myReview)
    }

    @Test
    fun `refreshes my review on events for this book only`() = runTest {
        val source = FakeSource()
        val events = FakeEvents()
        val vm = BookDetailViewModel(BookDetailKey(1, "img"), source, events, FakeErrors(), NOOP)
        assertNull(vm.state.value.myReview)
        source.myReview = Review(id = 3, content = "new")

        events.flow.emit(BookActionEvent.Registered(2, BookListMenu.READ))
        assertNull(vm.state.value.myReview)

        events.flow.emit(BookActionEvent.Registered(1, BookListMenu.READ))
        assertEquals("new", vm.state.value.myReview?.content)

        // An edit is patched locally: no new lookup.
        val callsBefore = source.myReviewCalls
        events.flow.emit(BookActionEvent.Updated(1, reviewId = 3, review = "edited", readAt = null, netabare = true))
        assertEquals("edited", vm.state.value.myReview?.content)
        assertEquals(true, vm.state.value.myReview?.netabare?.netabare)
        assertEquals(callsBefore, source.myReviewCalls)

        // Registering on another shelf creates no review: no lookup either.
        events.flow.emit(BookActionEvent.Registered(1, BookListMenu.READING))
        assertEquals(callsBefore, source.myReviewCalls)
    }

    @Test
    fun `reports load errors`() {
        val errors = FakeErrors()
        val source = FakeSource().apply { fail = true }
        val vm = BookDetailViewModel(BookDetailKey(1, "img"), source, FakeEvents(), errors, NOOP)
        assertEquals(1, errors.count)
        assertNull(vm.state.value.detail)
    }

    @Test
    fun `my review failure keeps reviews and shows no error`() {
        val errors = FakeErrors()
        val source = FakeSource().apply { myReviewFails = true }
        val vm = BookDetailViewModel(BookDetailKey(1, "img"), source, FakeEvents(), errors, NOOP)
        assertEquals(0, errors.count)
        assertEquals("title 1", vm.state.value.detail?.title)
        assertEquals(2, vm.state.value.reviews.size)
        assertNull(vm.state.value.myReview)
    }

    @Test
    fun `load failure sets error and retry recovers`() {
        val source = FakeSource().apply { fail = true }
        val vm = BookDetailViewModel(BookDetailKey(1, "img"), source, FakeEvents(), FakeErrors(), NOOP)
        assertNull(vm.state.value.detail)
        assertEquals(true, vm.state.value.error)

        source.fail = false
        vm.retry()
        assertEquals(false, vm.state.value.error)
        assertEquals("title 1", vm.state.value.detail?.title)
        assertEquals(2, vm.state.value.reviews.size)
    }

    @Test
    fun `session expiry is handled and not reported`() {
        var expired = 0
        val errors = FakeErrors()
        val source = FakeSource().apply { failure = SessionExpiredException() }
        val vm = BookDetailViewModel(
            BookDetailKey(1, "img"), source, FakeEvents(), errors, SessionExpiryHandler(onExpired = { expired++ }),
        )
        assertEquals(1, expired)
        assertEquals(0, errors.count)
        assertEquals(false, vm.state.value.error)
    }
}
