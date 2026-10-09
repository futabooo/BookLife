package com.futabooo.android.booklife.ui.licenses

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.ui.theme.BookLifeTheme
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

@Composable
fun LicensesScreen(onBack: () -> Unit) {
    LicensesContent(onBack = onBack) { modifier ->
        val libraries by produceLibraries()
        LibrariesContainer(libraries = libraries, modifier = modifier)
    }
}

/** App bar + body slot; split out so the chrome can be tested without loading AboutLibraries (needs a Java 21 runtime). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LicensesContent(onBack: () -> Unit, body: @Composable (Modifier) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.licenses)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.ic_arrow_left),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        body(Modifier.fillMaxSize().padding(padding))
    }
}

@Preview(showBackground = true)
@Composable
private fun LicensesPreview() {
    BookLifeTheme { LicensesScreen(onBack = {}) }
}
