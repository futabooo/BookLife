package com.futabooo.android.booklife.ui.common

import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.scene.DialogSceneStrategy
import com.futabooo.android.booklife.ui.navigation.BottomSheetSceneStrategy
import com.futabooo.android.booklife.ui.navigation.EntryProviderInstaller
import com.futabooo.android.booklife.ui.navigation.Navigator
import com.futabooo.android.booklife.ui.navigation.ReadBookDialog
import com.futabooo.android.booklife.ui.navigation.RegisterBook
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

/** Entries for the shared overlays: the register bottom sheet and the read-book dialog. */
@Module
@InstallIn(ActivityRetainedComponent::class)
object BookActionEntries {

    @Provides
    @IntoSet
    fun provideBookActionEntries(
        navigator: Navigator,
        coordinator: BookActionCoordinator,
    ): EntryProviderInstaller = {
        entry<RegisterBook>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
            BookRegisterSheet(
                onSelect = { menu ->
                    navigator.goBack()
                    coordinator.onMenuSelected(menu, key.bookId)
                },
            )
        }
        entry<ReadBookDialog>(
            metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
        ) { key ->
            val editing = key.reviewId != null
            AddReadBookDialog(
                isEditing = editing,
                initialReview = key.initialReview.orEmpty(),
                initialReadAt = key.initialReadAt,
                initialNetabare = key.initialNetabare,
                onDismiss = { navigator.goBack() },
                onSubmit = { review, readAt, netabare ->
                    if (key.reviewId != null) {
                        coordinator.updateReadBook(
                            key.csrfToken, key.reviewId, key.bookId, review, readAt, netabare,
                        )
                    } else {
                        coordinator.submitReadBook(key.csrfToken, key.bookId, review, readAt, netabare)
                    }
                },
            )
        }
    }
}
