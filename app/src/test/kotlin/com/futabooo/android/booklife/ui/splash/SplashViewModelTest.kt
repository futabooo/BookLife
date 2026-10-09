package com.futabooo.android.booklife.ui.splash

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

    private fun create(check: suspend () -> Boolean) =
        SplashViewModel(check, { events += "main" }, { events += "login" })

    @Test
    fun loggedIn_goesToMain() {
        create { true }
        assertEquals(listOf("main"), events)
    }

    @Test
    fun loggedOut_goesToLogin() {
        create { false }
        assertEquals(listOf("login"), events)
    }

    @Test
    fun exception_goesToLogin() {
        create { throw java.io.IOException("offline") }
        assertEquals(listOf("login"), events)
    }
}
