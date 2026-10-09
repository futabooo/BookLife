package com.futabooo.android.booklife.ui.licenses

import com.futabooo.android.booklife.ui.navigation.EntryProviderInstaller
import com.futabooo.android.booklife.ui.navigation.Licenses
import com.futabooo.android.booklife.ui.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object LicensesModule {
    @Provides
    @IntoSet
    fun provideLicensesEntries(navigator: Navigator): EntryProviderInstaller = {
        entry<Licenses> { LicensesScreen(onBack = navigator::goBack) }
    }
}
