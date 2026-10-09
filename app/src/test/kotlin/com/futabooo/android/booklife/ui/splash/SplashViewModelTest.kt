package com.futabooo.android.booklife.ui.splash

import com.futabooo.android.booklife.data.repository.SessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val events = mutableListOf<String>()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun create(hasCookie: Boolean = false, check: suspend () -> SessionState) =
        SplashViewModel(check, { hasCookie }, { events += "main" }, { events += "login" })

    @Test
    fun loggedIn_goesToMain() {
        create { SessionState.LoggedIn }
        assertEquals(listOf("main"), events)
    }

    @Test
    fun loggedOut_goesToLogin_evenWithCookies() {
        create(hasCookie = true) { SessionState.LoggedOut }
        assertEquals(listOf("login"), events)
    }

    @Test
    fun unknown_withSessionCookie_goesToMain() {
        create(hasCookie = true) { SessionState.Unknown(java.io.IOException("offline")) }
        assertEquals(listOf("main"), events)
    }

    @Test
    fun unknown_withoutCookie_goesToLogin() {
        create(hasCookie = false) { SessionState.Unknown(null) }
        assertEquals(listOf("login"), events)
    }

    @Test
    fun exception_isTreatedAsUnknown() {
        create(hasCookie = true) { throw java.io.IOException("offline") }
        assertEquals(listOf("main"), events)
        events.clear()
        create(hasCookie = false) { throw java.io.IOException("offline") }
        assertEquals(listOf("login"), events)
    }
}
