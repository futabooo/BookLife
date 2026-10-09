package com.futabooo.android.booklife.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Full-screen destinations. */
@Serializable data object Splash : NavKey

@Serializable data object Login : NavKey

@Serializable data object Main : NavKey

@Serializable data class BookDetail(val bookId: Int, val imageUrl: String) : NavKey

/** [isbn] (from the barcode scanner) triggers an initial search when non-null. */
@Serializable data class Search(val isbn: String? = null) : NavKey

@Serializable data object Settings : NavKey

@Serializable data object Licenses : NavKey

/** Shown as a modal bottom sheet ([BookRegisterSheet][com.futabooo.android.booklife.ui.common.BookRegisterSheet]). */
@Serializable data class RegisterBook(val bookId: Int) : NavKey

/**
 * Shown as a dialog. Add a new read book ([reviewId] == null) or edit an existing review
 * ([reviewId] = `Review.id`, with [initialReview] / [initialReadAt] / [initialNetabare] prefilled).
 */
@Serializable
data class ReadBookDialog(
    val bookId: Int,
    val csrfToken: String,
    val reviewId: Int? = null,
    val initialReview: String? = null,
    val initialReadAt: String? = null,
    val initialNetabare: Boolean = false,
) : NavKey
