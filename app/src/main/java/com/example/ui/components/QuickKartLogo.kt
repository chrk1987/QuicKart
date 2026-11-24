package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens

@Composable
fun QuickKartLogo(
    modifier: Modifier = Modifier,
    size: Dp = QuickKartMetricsTokens.metrics.badgeButtonSize,
    cornerRadius: Dp = 12.dp,
    contentDescription: String? = "QuickKart logo"
) {
    val tokens = QuickKartThemeTokens.colors
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        shape = shape,
        color = tokens.brandYellow,
        border = BorderStroke(
            width = 1.dp,
            color = if (tokens.isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)
        ),
        shadowElevation = 2.dp,
        modifier = modifier.size(size)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_quickkart_icon),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
        )
    }
}
