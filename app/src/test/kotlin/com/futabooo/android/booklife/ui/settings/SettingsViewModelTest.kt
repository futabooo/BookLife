package com.futabooo.android.booklife.ui.settings

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
class SettingsViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun signOut_logsOutThenNavigates() {
        val events = mutableListOf<String>()
        val vm = SettingsViewModel({ events += "logout" }, { events += "login" })
        vm.signOut()
        assertEquals(listOf("logout", "login"), events)
    }

    @Test
    fun signOut_failureStillNavigatesToLogin() {
        val events = mutableListOf<String>()
        val vm = SettingsViewModel({ throw java.io.IOException("disk") }, { events += "login" })
        vm.signOut()
        assertEquals(listOf("login"), events)
    }
}
