package com.futabooo.android.booklife.ui.common

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.network.CsrfTokenProvider
import com.futabooo.android.booklife.data.network.PersistentCookieJar
import com.futabooo.android.booklife.data.network.SessionExpiryInterceptor
import com.futabooo.android.booklife.data.prefs.UserPreferences
import com.futabooo.android.booklife.data.repository.BookActionRepository
import com.futabooo.android.booklife.data.repository.SessionRepository
import com.futabooo.android.booklife.ui.navigation.Navigator
import com.futabooo.android.booklife.ui.navigation.SessionExpiryHandler
import dagger.hilt.android.ActivityRetainedLifecycle
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BookActionCoordinatorTest {

    @get:Rule val tmp = TemporaryFolder()

    private lateinit var server: MockWebServer
    private lateinit var scope: CoroutineScope
    private lateinit var coordinator: BookActionCoordinator
    private var expired = 0

    private val lifecycle = object : ActivityRetainedLifecycle {
        override fun addOnClearedListener(listener: dagger.hilt.android.lifecycle.RetainedLifecycle.OnClearedListener) = Unit
        override fun removeOnClearedListener(listener: dagger.hilt.android.lifecycle.RetainedLifecycle.OnClearedListener) = Unit
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        server = MockWebServer().also { it.start() }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val jar = PersistentCookieJar(context.getSharedPreferences("test_cookies_coord", Context.MODE_PRIVATE))
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val prefs = UserPreferences(
            PreferenceDataStoreFactory.create(scope = scope) { File(tmp.root, "prefs.preferences_pb") },
        )
        runBlocking { prefs.setUserId(7) }
        val json = Json { ignoreUnknownKeys = true }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().addInterceptor(SessionExpiryInterceptor()).build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(BookmeterApi::class.java)
        val csrf = CsrfTokenProvider(api).also { it.update("tok") }
        coordinator = BookActionCoordinator(
            context,
            BookActionRepository(api, csrf),
            SessionRepository(api, jar, prefs, json, csrf),
            Navigator(),
            SnackbarController(lifecycle),
            SessionExpiryHandler(onExpired = { expired++ }),
            lifecycle,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        server.shutdown()
        scope.cancel()
    }

    private fun awaitIdle() = runBlocking { withTimeout(5_000) { coordinator.inFlight.first { !it } } }

    @Test
    fun doubleSubmit_sendsOnlyOneRequest() {
        server.enqueue(MockResponse().setBody("{}").setBodyDelay(300, TimeUnit.MILLISECONDS))
        server.enqueue(MockResponse().setBody("{}"))

        coordinator.submitReadBook(1, "review", "2024/1/2", false)
        assertTrue(coordinator.inFlight.value)
        coordinator.submitReadBook(1, "review", "2024/1/2", false)
        coordinator.updateReadBook(5, 1, "review", null, false)
        awaitIdle()

        assertEquals(1, server.requestCount)
        assertFalse(coordinator.inFlight.value)
        assertFalse(coordinator.dialogError.value)
    }

    @Test
    fun failure_exposesDialogError_andResetsInFlight() {
        server.enqueue(MockResponse().setResponseCode(500))

        coordinator.submitReadBook(1, "review", "2024/1/2", false)
        awaitIdle()

        assertTrue(coordinator.dialogError.value)
        // The button works again: a retry clears the error and succeeds.
        server.enqueue(MockResponse().setBody("{}"))
        coordinator.submitReadBook(1, "review", "2024/1/2", false)
        awaitIdle()
        assertFalse(coordinator.dialogError.value)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun sheetShelfFailure_doesNotSetDialogError() {
        server.enqueue(MockResponse().setResponseCode(500))
        coordinator.onMenuSelected(BookListMenu.TO_READ, 1)
        awaitIdle()
        assertFalse(coordinator.dialogError.value)
    }

    @Test
    fun update_withoutReadAt_omitsItFromBody_andEmitsUpdated() = runBlocking {
        server.enqueue(MockResponse().setBody("{}"))
        val event = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeout(5_000) { coordinator.events.first() }
        }

        coordinator.updateReadBook(5, 1, "new text", null, true)
        val updated = event.await() as BookActionEvent.Updated

        assertEquals(5, updated.reviewId)
        assertEquals("new text", updated.review)
        assertTrue(!server.takeRequest().body.readUtf8().contains("read_at"))
    }

    @Test
    fun sessionExpiry_isHandled_withoutDialogError() {
        server.enqueue(MockResponse().setResponseCode(401))
        coordinator.submitReadBook(1, "review", "2024/1/2", false)
        awaitIdle()
        assertEquals(1, expired)
        assertFalse(coordinator.dialogError.value)
    }
}
