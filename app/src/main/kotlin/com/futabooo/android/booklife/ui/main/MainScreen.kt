package com.futabooo.android.booklife.ui.main

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.analytics.Analytics
import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.ui.home.HomeScreen
import com.futabooo.android.booklife.ui.navigation.Licenses
import com.futabooo.android.booklife.ui.navigation.Navigator
import com.futabooo.android.booklife.ui.navigation.Search
import com.futabooo.android.booklife.ui.navigation.Settings
import com.futabooo.android.booklife.ui.theme.BookLifeColors
import com.futabooo.android.booklife.ui.theme.BookLifeTheme
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.launch

private const val TAB_HOME = 0
private const val TAB_BOOK = 1

/**
 * Main shell: top bar (hides on scroll), bottom navigation (Home / Book), the Book tab's four
 * bookshelf tabs + pager, and the FAB with its arc menu. State (selected bottom tab, menu open,
 * pager page) is saveable so it survives rotation.
 *
 * @param bookListPageRenderer content of the bookshelf pages, `null` shows a placeholder
 * @param onShowMessage shows a snackbar (e.g. `SnackbarController::show`)
 * @param homeContent the Home tab body
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navigator: Navigator,
    analytics: Analytics,
    onShowMessage: (String) -> Unit,
    bookListPageRenderer: BookListPageRenderer?,
    modifier: Modifier = Modifier,
    homeContent: @Composable (PaddingValues) -> Unit = { padding -> HomeScreen(contentPadding = padding) },
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_HOME) }
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    val pagerState = rememberPagerState { BookListMenu.entries.size }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val arcProgress = remember { Animatable(0f) }

    LaunchedEffect(menuOpen) {
        if (menuOpen) {
            arcProgress.animateTo(1f, tween(ARC_MENU_DURATION_MS, easing = OvershootEasing))
        } else {
            arcProgress.animateTo(0f, tween(ARC_MENU_DURATION_MS, easing = AnticipateEasing))
        }
    }
    // Closes the menu first on back (also drives predictive back on SDK 36).
    BackHandler(enabled = menuOpen) { menuOpen = false }

    val fabBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 13.dp
    val arcVisible = menuOpen || arcProgress.value != 0f

    fun closeMenuThen(action: () -> Unit) {
        menuOpen = false
        action()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = BookLifeColors.primary,
            topBar = {
                Box {
                    androidx.compose.foundation.layout.Column {
                        MainTopBar(
                            title = stringResource(if (selectedTab == TAB_HOME) R.string.home else R.string.book),
                            onSettings = { navigator.goTo(Settings) },
                            onLicenses = { navigator.goTo(Licenses) },
                            scrollBehavior = scrollBehavior,
                        )
                        if (selectedTab == TAB_BOOK) {
                            PrimaryTabRow(
                                selectedTabIndex = pagerState.currentPage,
                                containerColor = BookLifeColors.primary,
                                contentColor = BookLifeColors.accent,
                                modifier = Modifier.testTag("book_tabs"),
                            ) {
                                BookListMenu.entries.forEach { menu ->
                                    Tab(
                                        selected = pagerState.currentPage == menu.position,
                                        onClick = { scope.launch { pagerState.animateScrollToPage(menu.position) } },
                                        text = { Text(stringResource(menu.titleResId), maxLines = 1) },
                                        selectedContentColor = BookLifeColors.accent,
                                        unselectedContentColor = BookLifeColors.secondaryText,
                                        modifier = Modifier.testTag("book_tab_${menu.key}"),
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = BookLifeColors.primary) {
                    val colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BookLifeColors.accent,
                        selectedTextColor = BookLifeColors.accent,
                        unselectedIconColor = BookLifeColors.primaryDark,
                        unselectedTextColor = BookLifeColors.primaryDark,
                        indicatorColor = BookLifeColors.primary,
                    )
                    NavigationBarItem(
                        selected = selectedTab == TAB_HOME,
                        onClick = { selectedTab = TAB_HOME },
                        icon = { Icon(painterResource(R.drawable.ic_home), contentDescription = null) },
                        label = { Text(stringResource(R.string.home)) },
                        colors = colors,
                        modifier = Modifier.testTag("nav_home"),
                    )
                    NavigationBarItem(
                        selected = selectedTab == TAB_BOOK,
                        onClick = { selectedTab = TAB_BOOK },
                        icon = { Icon(painterResource(R.drawable.ic_book), contentDescription = null) },
                        label = { Text(stringResource(R.string.book)) },
                        colors = colors,
                        modifier = Modifier.testTag("nav_book"),
                    )
                }
            },
        ) { innerPadding ->
            if (selectedTab == TAB_HOME) {
                homeContent(innerPadding)
            } else {
                val layoutDirection = LocalLayoutDirection.current
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    // Each page gets the bar insets as contentPadding; the list scrolls beneath them.
                    val menu = BookListMenu.fromPosition(page)
                    val padding = PaddingValues(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        top = innerPadding.calculateTopPadding(),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        bottom = innerPadding.calculateBottomPadding(),
                    )
                    if (bookListPageRenderer != null) {
                        bookListPageRenderer.Render(menu, padding)
                    } else {
                        PlaceholderBookListPage(menu, padding)
                    }
                }
            }
        }

        if (arcVisible) {
            ArcMenuOverlay(
                progress = arcProgress.value,
                fabBottomPadding = fabBottomPadding,
                onDismiss = { menuOpen = false },
                onBarcodeScan = {
                    closeMenuThen {
                        analytics.logEvent("barcode_scan")
                        startBarcodeScan(
                            context = context,
                            onIsbn = { isbn -> navigator.goTo(Search(isbn = isbn)) },
                            onFailure = { onShowMessage(context.getString(R.string.error_barcode_scan)) },
                        )
                    }
                },
                onSearch = {
                    closeMenuThen {
                        analytics.logEvent("search")
                        navigator.goTo(Search())
                    }
                },
                onRecordVoice = {
                    closeMenuThen {
                        analytics.logEvent("record_voice")
                        onShowMessage(context.getString(R.string.coming_soon))
                    }
                },
            )
        }

        FloatingActionButton(
            onClick = { menuOpen = !menuOpen },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = fabBottomPadding)
                .testTag("fab"),
            containerColor = BookLifeColors.primary,
            contentColor = BookLifeColors.accent,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.main_add_menu),
                modifier = Modifier.graphicsLayer { rotationZ = 45f * arcProgress.value },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    title: String,
    onSettings: () -> Unit,
    onLicenses: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
) {
    var overflowOpen by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(title, color = BookLifeColors.accent) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BookLifeColors.primary,
            scrolledContainerColor = BookLifeColors.primary,
            titleContentColor = BookLifeColors.accent,
            actionIconContentColor = BookLifeColors.primaryText,
        ),
        actions = {
            IconButton(onClick = { overflowOpen = true }, modifier = Modifier.testTag("overflow")) {
                Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.main_more_options))
            }
            DropdownMenu(expanded = overflowOpen, onDismissRequest = { overflowOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings)) },
                    onClick = {
                        overflowOpen = false
                        onSettings()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.licenses)) },
                    onClick = {
                        overflowOpen = false
                        onLicenses()
                    },
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

/** Google Code Scanner (EAN-13 / EAN-8). [onIsbn] gets the raw value; [onFailure] on error. */
private fun startBarcodeScan(context: Context, onIsbn: (String) -> Unit, onFailure: () -> Unit) {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8)
        .enableAutoZoom()
        .build()
    GmsBarcodeScanning.getClient(context, options).startScan()
        .addOnSuccessListener { barcode -> barcode.rawValue?.let(onIsbn) }
        .addOnFailureListener { onFailure() }
}

@Preview
@Composable
private fun MainScreenPreview() {
    BookLifeTheme {
        MainScreen(
            navigator = Navigator(),
            analytics = object : Analytics {
                override fun logEvent(name: String) = Unit
            },
            onShowMessage = {},
            bookListPageRenderer = null,
            homeContent = { Text("Home", modifier = Modifier.padding(it)) },
        )
    }
}
