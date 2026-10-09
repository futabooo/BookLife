package com.futabooo.android.booklife.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.data.model.Author
import com.futabooo.android.booklife.data.model.Book
import com.futabooo.android.booklife.data.model.SearchResultContents
import com.futabooo.android.booklife.data.model.SearchResultResource
import com.futabooo.android.booklife.ui.common.LoadingItem
import com.futabooo.android.booklife.ui.common.SnackbarController
import com.futabooo.android.booklife.ui.navigation.BookDetail
import com.futabooo.android.booklife.ui.navigation.Navigator
import com.futabooo.android.booklife.ui.navigation.RegisterBook
import com.futabooo.android.booklife.ui.navigation.Search
import com.futabooo.android.booklife.ui.theme.BookLifeColors
import com.futabooo.android.booklife.ui.theme.BookLifeSpacing
import com.futabooo.android.booklife.ui.theme.BookLifeTextSizes
import com.futabooo.android.booklife.ui.theme.BookLifeTheme

/** Remaining items before the end of the list at which the next page is requested (old threshold). */
private const val LOAD_MORE_THRESHOLD = 2

@Composable
fun SearchScreen(
    key: Search,
    navigator: Navigator,
    snackbar: SnackbarController,
    viewModel: SearchViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.errors.collect { snackbar.show(context.getString(R.string.search_error)) }
    }
    SearchContent(
        state = state,
        autoFocus = key.isbn == null,
        onBack = navigator::goBack,
        onQueryChange = viewModel::onQueryChange,
        onSubmit = viewModel::submit,
        onLoadMore = viewModel::loadMore,
        onOpenBook = { book -> navigator.goTo(BookDetail(book.id, book.imageUrl)) },
        onRegister = { book -> navigator.goTo(RegisterBook(book.id)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    state: SearchUiState,
    autoFocus: Boolean,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenBook: (Book) -> Unit,
    onRegister: (Book) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    val loadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index
            last != null && info.totalItemsCount > 0 &&
                last >= info.totalItemsCount - 1 - LOAD_MORE_THRESHOLD
        }.collect { nearEnd -> if (nearEnd) loadMore() }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_left),
                            contentDescription = stringResource(R.string.search_back),
                        )
                    }
                },
                title = {
                    TextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("search_field"),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.search_hint)) },
                        leadingIcon = {
                            Icon(painterResource(R.drawable.ic_search), contentDescription = null)
                        },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(
                                        painterResource(R.drawable.ic_clear),
                                        contentDescription = stringResource(R.string.search_clear),
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboard?.hide()
                                focusManager.clearFocus()
                                onSubmit()
                            },
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BookLifeColors.primary,
                            unfocusedContainerColor = BookLifeColors.primary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BookLifeColors.primary),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().testTag("search_results"),
                contentPadding = innerPadding,
            ) {
                items(state.items) { item ->
                    SearchResultItem(
                        item = item,
                        onClick = { onOpenBook(item.contents.book) },
                        onRegister = { onRegister(item.contents.book) },
                    )
                }
                if (state.isLoading) {
                    item { LoadingItem() }
                }
            }
            if (state.searched && state.items.isEmpty() && !state.isLoading) {
                Text(
                    text = stringResource(R.string.search_no_results),
                    modifier = Modifier.align(Alignment.Center).padding(innerPadding),
                    color = BookLifeColors.secondaryText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun SearchResultItem(
    item: SearchResultResource,
    onClick: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val book = item.contents.book
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = BookLifeSpacing.Medium, vertical = BookLifeSpacing.Small),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = BookLifeColors.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = BookLifeSpacing.Large),
        ) {
            Row(Modifier.fillMaxSize()) {
                AsyncImage(
                    model = book.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = BookLifeSpacing.Large)
                        .size(width = 80.dp, height = 120.dp)
                        .align(Alignment.CenterVertically),
                )
                Column(
                    Modifier.weight(1f).padding(end = 32.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    Text(
                        text = book.title,
                        modifier = Modifier.padding(top = BookLifeSpacing.Small),
                        color = BookLifeColors.primaryText,
                        fontSize = BookLifeTextSizes.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = book.author.name.orEmpty(),
                        modifier = Modifier.padding(top = 2.dp),
                        color = BookLifeColors.secondaryText,
                        fontSize = BookLifeTextSizes.Small,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = book.registrationCount.toString(),
                        modifier = Modifier.padding(top = 2.dp),
                        color = BookLifeColors.secondaryText,
                        fontSize = BookLifeTextSizes.Small,
                        maxLines = 1,
                    )
                    // Invisible but space kept when there is no read mark.
                    Text(
                        text = item.statusText.ifEmpty { " " },
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .alpha(if (item.statusText.isEmpty()) 0f else 1f),
                        color = BookLifeColors.secondaryText,
                        fontSize = BookLifeTextSizes.Small,
                        maxLines = 1,
                    )
                }
            }
            IconButton(
                onClick = onRegister,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .testTag("search_add_${book.id}"),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.search_register),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

private fun fakeResult(id: Int, status: String = "") = SearchResultResource(
    contents = SearchResultContents(
        Book(
            id = id,
            title = "Sample book title $id that is long enough to need two lines in the card",
            author = Author(name = "Author $id"),
            registrationCount = 100 * id,
        ),
    ),
    statusText = status,
)

@Preview(showBackground = true)
@Composable
private fun SearchContentPreview() {
    BookLifeTheme {
        SearchContent(
            state = SearchUiState(
                query = "kotlin",
                items = listOf(fakeResult(1, "読んだ"), fakeResult(2), fakeResult(3)),
                searched = true,
                isLoading = true,
            ),
            autoFocus = false,
            onBack = {},
            onQueryChange = {},
            onSubmit = {},
            onLoadMore = {},
            onOpenBook = {},
            onRegister = {},
        )
    }
}
