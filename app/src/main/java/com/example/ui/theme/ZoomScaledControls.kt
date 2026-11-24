package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

/**
 * Material RadioButton / Checkbox / Switch have fixed sizes and ignore the app's
 * Text & Display Size setting, so at Compact they look oversized next to smaller text
 * (and undersized at Extra large). This scales the drawn control with the same zoom
 * as text and icons. Layout size (the touch target) is unchanged.
 */
@Composable
fun Modifier.zoomScaledControl(): Modifier =
    this.scale(QuickKartMetricsTokens.metrics.zoomScale)
