package com.futabooo.android.booklife.ui.main

import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.ui.theme.BookLifeColors
import kotlin.math.cos
import kotlin.math.sin

/** Opening easing (old `OvershootInterpolator`). */
val OvershootEasing = Easing { OvershootInterpolator().getInterpolation(it) }

/** Closing easing (old `AnticipateInterpolator`). */
val AnticipateEasing = Easing { AnticipateInterpolator().getInterpolation(it) }

const val ARC_MENU_DURATION_MS = 400
val ArcRadius = 140.dp
val ArcItemSize = 56.dp
private const val ARC_ROTATION_DEGREES = 720f

private data class ArcItem(
    val tag: String,
    val angleDegrees: Float,
    @DrawableRes val icon: Int,
    val description: Int,
)

private val ArcItems = listOf(
    ArcItem("arc_barcode_scan", 60f, R.drawable.ic_barcode_scan, R.string.main_menu_barcode_scan),
    ArcItem("arc_search", 90f, R.drawable.ic_search, R.string.main_menu_search),
    ArcItem("arc_record_voice", 120f, R.drawable.ic_record_voice, R.string.main_menu_record_voice),
)

/**
 * Full-screen scrim plus three round buttons on an arc (radius [ArcRadius], 60/90/120 degrees, 90 =
 * straight up) centred on the FAB. [fabBottomPadding] must equal the FAB's bottom padding so the arc
 * centre coincides with the FAB centre. [progress] 0..1 (may overshoot) drives translation from the
 * FAB position, 0..720 degree rotation and fade-in.
 */
@Composable
fun ArcMenuOverlay(
    progress: Float,
    fabBottomPadding: Dp,
    onDismiss: () -> Unit,
    onBarcodeScan: () -> Unit,
    onSearch: () -> Unit,
    onRecordVoice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val radiusPx = with(LocalDensity.current) { ArcRadius.toPx() }
    val actions = listOf(onBarcodeScan, onSearch, onRecordVoice)
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = progress.coerceIn(0f, 1f) }
            .background(BookLifeColors.scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .testTag("arc_scrim"),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = fabBottomPadding)
                .size(ArcItemSize),
            contentAlignment = Alignment.Center,
        ) {
            ArcItems.forEachIndexed { index, item ->
                val radians = Math.toRadians(item.angleDegrees.toDouble())
                val dx = (radiusPx * cos(radians)).toFloat()
                val dy = -(radiusPx * sin(radians)).toFloat()
                Box(
                    modifier = Modifier
                        .size(ArcItemSize)
                        .graphicsLayer {
                            translationX = dx * progress
                            translationY = dy * progress
                            rotationZ = ARC_ROTATION_DEGREES * progress
                        }
                        .clip(CircleShape)
                        .background(BookLifeColors.accent)
                        .clickable(role = Role.Button, onClick = actions[index])
                        .testTag(item.tag),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = androidx.compose.ui.res.stringResource(item.description),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
