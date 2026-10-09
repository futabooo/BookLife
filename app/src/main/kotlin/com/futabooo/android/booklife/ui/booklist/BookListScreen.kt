package com.futabooo.android.booklife.ui.booklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.data.model.Book
import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.model.Resource
import com.futabooo.android.booklife.ui.common.LoadingItem
import com.futabooo.android.booklife.ui.common.bookImageSharedElement
import com.futabooo.android.booklife.ui.theme.BookLifeColors
import com.futabooo.android.booklife.ui.theme.BookLifeSpacing
import com.futabooo.android.booklife.ui.theme.BookLifeTheme

/** Items from the end at which the next page is requested (old `InfiniteScrollListener.visibleThreshold`). */
private const val VISIBLE_THRESHOLD = 2

/**
 * One bookshelf page (ViewModel per [menu]). [contentPadding] carries the edge-to-edge insets of
 * the hosting scaffold; the old 16dp screen margins are added on top.
 */
@Composable
fun BookListScreen(
    menu: BookListMenu,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val viewModel = hiltViewModel<BookListViewModel, BookListViewModel.Factory>(
        key = menu.name,
        creationCallback = { factory -> factory.create(menu) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    BookListContent(
        state = state,
        onLoadMore = viewModel::loadMore,
        onBookClick = viewModel::onBookClick,
        contentPadding = contentPadding,
        modifier = modifier,
    )
}

@Composable
internal fun BookListContent(
    state: BookListUiState,
    onLoadMore: () -> Unit,
    onBookClick: (Resource) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val itemCount = state.items.size
    val nearEnd by remember(itemCount) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= itemCount - 1 - VISIBLE_THRESHOLD
        }
    }
    LaunchedEffect(nearEnd, itemCount, state.isLoading, state.error) {
        if (nearEnd && !state.error) onLoadMore()
    }

    val direction = LocalLayoutDirection.current
    val margin = BookLifeSpacing.ActivityMargin
    val padding = PaddingValues(
        start = contentPadding.calculateStartPadding(direction) + margin,
        top = contentPadding.calculateTopPadding() + margin,
        end = contentPadding.calculateEndPadding(direction) + margin,
        bottom = contentPadding.calculateBottomPadding() + margin,
    )
    LazyColumn(
        state = listState,
        contentPadding = padding,
        modifier = modifier.fillMaxSize(),
    ) {
        itemsIndexed(state.items) { _, resource ->
            BookCard(resource = resource, onClick = { onBookClick(resource) })
        }
        if (state.isLoading) {
            item { LoadingItem() }
        }
        if (state.error) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.load_error),
                        color = BookLifeColors.secondaryText,
                    )
                    TextButton(onClick = onLoadMore) { Text(stringResource(R.string.retry)) }
                }
            }
        }
    }
}

/** Port of `component_book_card_view.xml`. */
@Composable
private fun BookCard(resource: Resource, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val book = resource.book
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = BookLifeColors.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
            .height(172.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = book?.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = BookLifeSpacing.Large, top = BookLifeSpacing.Large)
                    .bookImageSharedElement(book?.id)
                    .size(width = 100.dp, height = 140.dp),
            )
            Text(
                text = book?.title.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = BookLifeColors.primaryText,
                modifier = Modifier.padding(
                    start = 132.dp,
                    top = BookLifeSpacing.Large,
                    end = BookLifeSpacing.Large,
                ),
            )
        }
    }
}

private fun previewResources() = List(4) {
    Resource(id = it, book = Book(id = it, title = "本のタイトルだけどめっちゃ長い文字列が入った時にどうなるのかを表す $it"))
}

@Preview(showBackground = true)
@Composable
private fun BookListContentPreview() {
    BookLifeTheme {
        BookListContent(
            state = BookListUiState(items = previewResources(), isLoading = true),
            onLoadMore = {},
            onBookClick = {},
            contentPadding = PaddingValues(),
        )
    }
}
