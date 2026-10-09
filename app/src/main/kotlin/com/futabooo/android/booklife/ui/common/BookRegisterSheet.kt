package com.futabooo.android.booklife.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.ui.theme.BookLifeSpacing
import com.futabooo.android.booklife.ui.theme.BookLifeTheme

/** The order in which the shelves are offered (reading, to read, read, quitted). */
private val SheetMenus = listOf(
    BookListMenu.READING,
    BookListMenu.TO_READ,
    BookListMenu.READ,
    BookListMenu.QUITTED,
)

/**
 * Content of the "register this book to a shelf" bottom sheet (old `component_bottom_sheet.xml`).
 * Display it through the [com.futabooo.android.booklife.ui.navigation.RegisterBook] key, which
 * is wired in `BookActionEntries`; use it directly only for previews / tests.
 */
@Composable
fun BookRegisterSheet(
    onSelect: (BookListMenu) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(BookLifeSpacing.ActivityMargin),
    ) {
        SheetMenus.forEach { menu ->
            Text(
                text = stringResource(menu.titleResId),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(menu) }
                    .padding(vertical = BookLifeSpacing.Medium),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookRegisterSheetPreview() {
    BookLifeTheme { BookRegisterSheet(onSelect = {}) }
}
