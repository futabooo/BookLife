package com.futabooo.android.booklife.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.data.repository.HomeStats
import com.futabooo.android.booklife.ui.common.LoadingItem
import com.futabooo.android.booklife.ui.theme.BookLifeColors
import com.futabooo.android.booklife.ui.theme.BookLifeSpacing
import com.futabooo.android.booklife.ui.theme.BookLifeTextSizes
import com.futabooo.android.booklife.ui.theme.BookLifeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Home tab: this month's reading stats (old `HomeFragment` / `fragment_home.xml`). */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(state = state, onRetry = viewModel::retry, modifier = modifier, contentPadding = contentPadding)
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    month: String = currentMonthLabel(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(
                vertical = BookLifeSpacing.ActivityMargin,
                horizontal = BookLifeSpacing.Medium,
            ),
    ) {
        Text(
            text = month,
            fontSize = BookLifeTextSizes.XLarge,
            color = BookLifeColors.primaryText,
            modifier = Modifier.padding(start = BookLifeSpacing.Medium),
        )
        when (state) {
            HomeUiState.Loading -> LoadingItem(Modifier.padding(top = BookLifeSpacing.Large))
            HomeUiState.Error -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = BookLifeSpacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(BookLifeSpacing.Medium),
            ) {
                Text(
                    text = stringResource(R.string.home_load_error),
                    color = BookLifeColors.secondaryText,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onRetry, modifier = Modifier.testTag("home_retry")) {
                    Text(stringResource(R.string.retry))
                }
            }
            is HomeUiState.Success -> Stats(state.stats)
        }
    }
}

@Composable
private fun Stats(stats: HomeStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatCard(
            title = stringResource(R.string.book_record_page_number),
            value = stats.pages,
            unit = stringResource(R.string.book_record_page),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            title = stringResource(R.string.book_record_monthly_average),
            value = stats.pagesPerDay,
            unit = stringResource(R.string.book_record_page_par_day),
            modifier = Modifier.weight(1f),
        )
    }
    StatCard(
        title = stringResource(R.string.book_record_volume_number),
        value = stats.volumes,
        unit = stringResource(R.string.book_record_volume),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StatCard(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.padding(BookLifeSpacing.Medium),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = BookLifeColors.primary),
    ) {
        Column(Modifier.padding(BookLifeSpacing.Medium)) {
            Text(text = title, fontSize = BookLifeTextSizes.Medium, color = BookLifeColors.primaryText)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = BookLifeTextSizes.XLarge, color = BookLifeColors.primaryText)
                Text(text = unit, fontSize = BookLifeTextSizes.Small, color = BookLifeColors.primaryText)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/** Current month as `yyyy/MM`. */
fun currentMonthLabel(now: Date = Date()): String =
    SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(now)

@Preview(showBackground = true)
@Composable
private fun HomeContentSuccessPreview() {
    BookLifeTheme {
        HomeContent(
            state = HomeUiState.Success(HomeStats(pages = "1000", volumes = "200", pagesPerDay = "100")),
            onRetry = {},
            month = "2026/10",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentLoadingPreview() {
    BookLifeTheme { HomeContent(state = HomeUiState.Loading, onRetry = {}, month = "2026/10") }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentErrorPreview() {
    BookLifeTheme { HomeContent(state = HomeUiState.Error, onRetry = {}, month = "2026/10") }
}
