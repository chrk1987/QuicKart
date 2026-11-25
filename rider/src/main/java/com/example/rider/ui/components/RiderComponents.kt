package com.example.rider.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.rider.data.GeoPoint
import com.example.ui.components.BrandBlack
import com.example.ui.theme.QuickKartThemeTokens
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Swipe-to-confirm slider (prevents accidental taps while riding). Yellow track, black thumb.
 * Accessibility: also confirmable with a single semantic click action.
 */
@Composable
fun SwipeToConfirm(text: String, enabled: Boolean, onConfirm: () -> Unit, modifier: Modifier = Modifier, testTag: String = "swipe_confirm") {
    val tokens = QuickKartThemeTokens.colors
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    val thumb = 52.dp
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(if (enabled) tokens.brandYellow else tokens.surfaceElevated, RoundedCornerShape(50))
            .testTag(testTag)
            .semantics { role = Role.Button; onClick(label = text) { if (enabled) onConfirm(); enabled } }
    ) {
        val maxPx = with(LocalDensity.current) { (maxWidth - thumb - 8.dp).toPx() }
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold,
            color = if (enabled) tokens.onBrandYellow else tokens.muted, modifier = Modifier.align(Alignment.Center).padding(start = thumb))
        Box(
            Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .padding(4.dp)
                .size(thumb)
                .background(if (enabled) BrandBlack else tokens.border, CircleShape)
                .pointerInput(enabled, maxPx) {
                    if (!enabled) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offset.value > maxPx * 0.85f) { offset.animateTo(maxPx); onConfirm(); offset.snapTo(0f) } else offset.animateTo(0f)
                            }
                        },
                        onHorizontalDrag = { _, delta -> scope.launch { offset.snapTo((offset.value + delta).coerceIn(0f, maxPx)) } }
                    )
                },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = if (enabled) tokens.brandYellow else tokens.muted) }
    }
}

/** Opens the dialer (no call permission needed). Customer numbers are masked (calls are bridged). */
fun dial(context: Context, number: String) {
    val digits = number.filter(Char::isDigit)
    val target = if (digits.length >= 10) digits else "08045671234" // masked → QuickKart call bridge (demo)
    runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$target"))) }
}

/** Hands off to the phone's navigation app for turn-by-turn directions. */
fun openNavigation(context: Context, to: GeoPoint, label: String) {
    val uri = Uri.parse("geo:${to.lat},${to.lng}?q=${to.lat},${to.lng}(${Uri.encode(label)})")
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
}
