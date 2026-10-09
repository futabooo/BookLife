package com.futabooo.android.booklife.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.futabooo.android.booklife.ui.common.LocalSharedTransitionScope
import com.futabooo.android.booklife.ui.common.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Holds the activity-retained navigation objects for [AppNavDisplay]. */
@HiltViewModel
class AppNavViewModel @Inject constructor(
    val navigator: Navigator,
    val snackbar: SnackbarController,
    val installers: Set<@JvmSuppressWildcards EntryProviderInstaller>,
) : ViewModel()

/**
 * The app's single [NavDisplay]. Screens contribute entries with `@IntoSet` [EntryProviderInstaller]s
 * (see [EntryProviderInstaller]); keys without an installer show a placeholder with the key name.
 * Dialog / bottom-sheet keys use `DialogSceneStrategy.dialog(...)` / `BottomSheetSceneStrategy.bottomSheet()`
 * metadata. A global snackbar ([SnackbarController]) is hosted above everything.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavDisplay(viewModel: AppNavViewModel = hiltViewModel()) {
    val backStack = rememberNavBackStack(Splash)
    // Attach synchronously so child effects can already navigate on their first run.
    remember(backStack) { viewModel.navigator.attach(backStack); backStack }

    val sceneStrategies = remember {
        listOf(BottomSheetSceneStrategy<NavKey>(), DialogSceneStrategy<NavKey>())
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = { viewModel.navigator.goBack() },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                        sceneStrategies = sceneStrategies,
                        entryProvider = entryProvider(fallback = { key -> placeholderEntry(key) }) {
                            viewModel.installers.forEach { installer -> installer() }
                        },
                    )
                }
            }
            SnackbarHost(
                hostState = viewModel.snackbar.hostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            )
        }
    }
}

/** Entry shown for keys that no installer registered (yet): a centered Text with the key name. */
private fun placeholderEntry(key: NavKey): NavEntry<NavKey> {
    val metadata = when (key) {
        is RegisterBook -> BottomSheetSceneStrategy.bottomSheet()
        is ReadBookDialog -> DialogSceneStrategy.dialog(DialogProperties())
        else -> emptyMap()
    }
    return NavEntry(key, metadata = metadata) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = key.toString())
        }
    }
}
