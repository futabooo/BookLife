package com.futabooo.android.booklife.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Raw palette, mirroring `res/values/colors.xml` of the old app.
 *
 * Material3 mapping (see [BookLifeTheme]): `colorScheme.primary` is the accent colour (so buttons,
 * checkboxes, progress indicators and focused text fields are accent coloured), while
 * [primary] (white) is the old "primary"/toolbar colour and equals `colorScheme.surface`.
 */
object BookLifeColors {
    val primary = Color(0xFFFFFFFF)
    val primaryDark = Color(0xFF9E9E9E)
    val primaryLight = Color(0xFFF5F5F5)
    val accent = Color(0xFF5BC0BE)
    val primaryText = Color(0xFF212121)
    val secondaryText = Color(0xFF757575)
    val attentionText = Color(0xFFFFFFFF)
    val icons = Color(0xFF212121)
    val divider = Color(0xFFBDBDBD)
    val attention = Color(0xFFF7567C)
    val scrim = Color(0x42000000)
}

/** `app_text_size_*` from `res/values/dimens.xml`. */
object BookLifeTextSizes {
    val XSmall = 11.sp
    val Small = 12.sp
    val Medium = 14.sp
    val Large = 18.sp
    val XLarge = 20.sp
}

/** `space_*` and margins from `res/values/dimens.xml`. */
object BookLifeSpacing {
    val XSmall = 2.dp
    val Small = 4.dp
    val Medium = 8.dp
    val Large = 16.dp
    val XLarge = 32.dp
    val ActivityMargin = 16.dp
}

private val BookLifeColorScheme = lightColorScheme(
    primary = BookLifeColors.accent,
    onPrimary = Color.White,
    primaryContainer = BookLifeColors.accent,
    onPrimaryContainer = Color.White,
    secondary = BookLifeColors.accent,
    onSecondary = Color.White,
    secondaryContainer = BookLifeColors.primaryLight,
    onSecondaryContainer = BookLifeColors.primaryText,
    tertiary = BookLifeColors.accent,
    onTertiary = Color.White,
    background = BookLifeColors.primary,
    onBackground = BookLifeColors.primaryText,
    surface = BookLifeColors.primary,
    onSurface = BookLifeColors.primaryText,
    surfaceVariant = BookLifeColors.primaryLight,
    onSurfaceVariant = BookLifeColors.secondaryText,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = BookLifeColors.primary,
    surfaceContainerLow = BookLifeColors.primary,
    surfaceContainer = BookLifeColors.primary,
    surfaceContainerHigh = BookLifeColors.primary,
    surfaceContainerHighest = BookLifeColors.primaryLight,
    outline = BookLifeColors.divider,
    outlineVariant = BookLifeColors.divider,
    error = BookLifeColors.attention,
    onError = BookLifeColors.attentionText,
    scrim = Color(0xFF000000),
)

private fun typography(): Typography {
    val base = Typography()
    fun TextStyle.size(size: androidx.compose.ui.unit.TextUnit) = copy(fontSize = size, lineHeight = size * 1.4f)
    return base.copy(
        titleLarge = base.titleLarge.size(BookLifeTextSizes.XLarge),
        titleMedium = base.titleMedium.size(BookLifeTextSizes.Large),
        titleSmall = base.titleSmall.size(BookLifeTextSizes.Medium),
        bodyLarge = base.bodyLarge.size(BookLifeTextSizes.Large),
        bodyMedium = base.bodyMedium.size(BookLifeTextSizes.Medium),
        bodySmall = base.bodySmall.size(BookLifeTextSizes.Small),
        labelLarge = base.labelLarge.size(BookLifeTextSizes.Medium),
        labelMedium = base.labelMedium.size(BookLifeTextSizes.Small),
        labelSmall = base.labelSmall.size(BookLifeTextSizes.XSmall),
    )
}

/** Light-only Material3 theme (the old app had no dark theme). */
@Composable
fun BookLifeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BookLifeColorScheme,
        typography = typography(),
        content = content,
    )
}
