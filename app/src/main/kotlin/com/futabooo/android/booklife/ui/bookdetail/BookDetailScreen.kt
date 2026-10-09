package com.futabooo.android.booklife.ui.bookdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.data.model.Netabare
import com.futabooo.android.booklife.data.model.Review
import com.futabooo.android.booklife.data.model.User
import com.futabooo.android.booklife.data.repository.BookDetail
import com.futabooo.android.booklife.ui.navigation.BookDetail as BookDetailKey
import com.futabooo.android.booklife.ui.navigation.Navigator
import com.futabooo.android.booklife.ui.navigation.ReadBookDialog
import com.futabooo.android.booklife.ui.navigation.RegisterBook
import com.futabooo.android.booklife.ui.theme.BookLifeColors
import com.futabooo.android.booklife.ui.theme.BookLifeSpacing
import com.futabooo.android.booklife.ui.theme.BookLifeTextSizes
import com.futabooo.android.booklife.ui.theme.BookLifeTheme
import timber.log.Timber

@Composable
fun BookDetailScreen(
    key: BookDetailKey,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<BookDetailViewModel, BookDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(key) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    BookDetailContent(
        state = state,
        imageUrl = key.imageUrl,
        onBack = navigator::goBack,
        onAmazon = { url ->
            try {
                uriHandler.openUri(url)
            } catch (e: Exception) {
                Timber.e(e, e.message)
            }
        },
        onAdd = { navigator.goTo(RegisterBook(key.bookId)) },
        onEditReview = { review ->
            val csrf = state.detail?.csrfToken ?: return@BookDetailContent
            navigator.goTo(
                ReadBookDialog(
                    bookId = key.bookId,
                    csrfToken = csrf,
                    reviewId = review.id,
                    initialReview = review.content,
                    initialReadAt = review.createdAt,
                    initialNetabare = review.netabare.netabare,
                ),
            )
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookDetailContent(
    state: BookDetailUiState,
    imageUrl: String,
    onBack: () -> Unit,
    onAmazon: (String) -> Unit,
    onAdd: () -> Unit,
    onEditReview: (Review) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.detail?.title.orEmpty(),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_left),
                            contentDescription = null,
                            tint = BookLifeColors.icons,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BookLifeColors.primary),
                modifier = Modifier.shadow(4.dp),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize(),
        ) {
            item { Header(state.detail, imageUrl, onAmazon, onAdd) }
            item { Divider(bottom = BookLifeSpacing.Large) }
            state.myReview?.let { review ->
                item { MyReview(review, onEdit = { onEditReview(review) }) }
            }
            itemsIndexed(state.reviews) { index, review ->
                ReviewItem(
                    review = review,
                    modifier = Modifier
                        .padding(horizontal = BookLifeSpacing.ActivityMargin)
                        .padding(top = if (index == 0) 0.dp else 30.dp),
                )
            }
            item { Box(Modifier.height(BookLifeSpacing.ActivityMargin)) }
        }
    }
}

@Composable
private fun Header(
    detail: BookDetail?,
    imageUrl: String,
    onAmazon: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val margin = BookLifeSpacing.ActivityMargin
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(margin),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = detail?.thumbnail?.takeIf { it.isNotEmpty() } ?: imageUrl,
                contentDescription = null,
                modifier = Modifier.size(width = 120.dp, height = 160.dp),
            )
            Column(modifier = Modifier.padding(start = BookLifeSpacing.Large)) {
                Text(
                    text = detail?.title.orEmpty(),
                    color = BookLifeColors.primaryText,
                    fontSize = BookLifeTextSizes.Large,
                )
                Text(
                    text = detail?.author.orEmpty(),
                    color = BookLifeColors.primaryText,
                    fontSize = BookLifeTextSizes.Medium,
                    modifier = Modifier.padding(top = BookLifeSpacing.Medium),
                )
            }
        }
        Button(
            onClick = { detail?.amazonUrl?.takeIf { it.isNotEmpty() }?.let(onAmazon) },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BookLifeColors.accent,
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 50.dp)
                .height(36.dp),
        ) {
            Text(text = stringResource(R.string.amazon), fontSize = BookLifeTextSizes.Small)
        }
        IconButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = BookLifeSpacing.Medium)
                .size(30.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                tint = BookLifeColors.icons,
            )
        }
    }
}

@Composable
private fun Divider(bottom: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = BookLifeSpacing.ActivityMargin)
            .padding(bottom = bottom)
            .height(1.dp)
            .background(BookLifeColors.divider),
    )
}

@Composable
private fun MyReview(review: Review, onEdit: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.book_impressions_review),
            color = BookLifeColors.primaryText,
            fontSize = BookLifeTextSizes.Medium,
            modifier = Modifier.padding(start = BookLifeSpacing.ActivityMargin),
        )
        Text(
            text = review.content.orEmpty(),
            color = BookLifeColors.secondaryText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(BookLifeSpacing.Large),
        )
        IconButton(
            onClick = onEdit,
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = BookLifeSpacing.ActivityMargin, bottom = BookLifeSpacing.Medium)
                .size(width = 42.dp, height = 24.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_horiz),
                contentDescription = null,
                tint = BookLifeColors.icons,
            )
        }
        Divider(bottom = BookLifeSpacing.Large)
    }
}

@Composable
internal fun ReviewItem(review: Review, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        AsyncImage(
            model = review.user.image,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
        )
        Column(modifier = Modifier.padding(start = BookLifeSpacing.Medium)) {
            Text(
                text = review.user.name.orEmpty(),
                color = BookLifeColors.primaryText,
                fontSize = BookLifeTextSizes.Medium,
                modifier = Modifier.padding(bottom = BookLifeSpacing.Small),
            )
            if (review.netabare.netabare) {
                Text(
                    text = stringResource(R.string.book_spoilers),
                    color = BookLifeColors.attentionText,
                    fontSize = BookLifeTextSizes.XSmall,
                    modifier = Modifier
                        .padding(bottom = BookLifeSpacing.Small)
                        .background(BookLifeColors.attention)
                        .padding(BookLifeSpacing.XSmall),
                )
            }
            Text(
                text = review.content.orEmpty(),
                color = BookLifeColors.primaryText,
                fontSize = BookLifeTextSizes.Small,
            )
        }
    }
}

private fun previewState() = BookDetailUiState(
    detail = BookDetail(
        title = "本のタイトルをここに入れよう長い場合は改行いい感じに",
        author = "本の著者",
        thumbnail = "",
        amazonUrl = "https://example.com",
        csrfToken = "token",
    ),
    myReview = Review(id = 1, content = "自分の感想です。", netabare = Netabare(netabare = false)),
    reviews = listOf(
        Review(id = 2, content = "ネタバレありのレビュー", netabare = Netabare(netabare = true), user = User(name = "ユーザーA")),
        Review(id = 3, content = "普通のレビュー", user = User(name = "ユーザーB")),
    ),
)

@Preview(showBackground = true)
@Composable
private fun BookDetailContentPreview() {
    BookLifeTheme {
        BookDetailContent(
            state = previewState(),
            imageUrl = "",
            onBack = {},
            onAmazon = {},
            onAdd = {},
            onEditReview = {},
        )
    }
}
