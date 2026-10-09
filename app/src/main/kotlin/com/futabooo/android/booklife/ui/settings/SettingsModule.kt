package com.futabooo.android.booklife.ui.settings

import com.futabooo.android.booklife.ui.navigation.EntryProviderInstaller
import com.futabooo.android.booklife.ui.navigation.Licenses
import com.futabooo.android.booklife.ui.navigation.Navigator
import com.futabooo.android.booklife.ui.navigation.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object SettingsModule {
    @Provides
    @IntoSet
    fun provideSettingsEntries(navigator: Navigator): EntryProviderInstaller = {
        entry<Settings> {
            SettingsScreen(
                onBack = navigator::goBack,
                onOpenLicenses = { navigator.goTo(Licenses) },
            )
        }
    }
}
