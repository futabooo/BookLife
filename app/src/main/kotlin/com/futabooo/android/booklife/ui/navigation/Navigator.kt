package com.futabooo.android.booklife.ui.navigation

import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/**
 * Contributes navigation entries to the app's `entryProvider`. Each screen package provides its own
 * installer with `@Provides @IntoSet` in an `@InstallIn(ActivityRetainedComponent::class)` module:
 *
 * ```
 * @Provides @IntoSet
 * fun provideLoginEntries(navigator: Navigator): EntryProviderInstaller = {
 *     entry<Login> { LoginScreen(onLoggedIn = { navigator.replaceAll(Main) }) }
 * }
 * ```
 */
typealias EntryProviderInstaller = EntryProviderScope<NavKey>.() -> Unit

/**
 * App-wide navigation facade over the (saveable) Navigation 3 back stack. Injectable anywhere in the
 * `ActivityRetainedComponent` (installer providers, `@HiltViewModel`s, ...). The back stack is
 * attached by `AppNavDisplay`.
 */
@ActivityRetainedScoped
class Navigator @Inject constructor() {

    private var backStack: NavBackStack<NavKey>? = null

    /** The top-most key of the back stack (null before [attach]). */
    internal val top: NavKey? get() = backStack?.lastOrNull()

    internal fun attach(backStack: NavBackStack<NavKey>) {
        this.backStack = backStack
    }

    /** Pushes [key] on the back stack. */
    fun goTo(key: NavKey) {
        backStack?.add(key)
    }

    /** Pops the top entry (also dismisses a dialog / bottom sheet). Ignored on the last entry. */
    fun goBack() {
        val stack = backStack ?: return
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    /** Replaces the whole back stack with [key] (e.g. Splash -> Main, sign-out -> Login). */
    fun replaceAll(key: NavKey) {
        val stack = backStack ?: return
        Snapshot.withMutableSnapshot {
            stack.add(key)
            while (stack.size > 1) stack.removeAt(0)
        }
    }
}
