package com.futabooo.android.booklife.ui.common

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/** Provided by `AppNavDisplay` (wraps the `NavDisplay` in a `SharedTransitionLayout`). Null in previews/tests. */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * Marks a book thumbnail as a shared element (key `"book_image_<id>"`) so it animates between the
 * BookList / Search thumbnail and the BookDetail header. No-op when there is no transition scope.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.bookImageSharedElement(bookId: Int?): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    if (bookId == null) return this
    val animatedScope = LocalNavAnimatedContentScope.current
    return with(sharedScope) {
        this@bookImageSharedElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = "book_image_$bookId"),
            animatedVisibilityScope = animatedScope,
        )
    }
}
