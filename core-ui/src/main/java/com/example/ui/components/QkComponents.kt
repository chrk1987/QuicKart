package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import com.example.ui.theme.zoomScaledControl
import java.text.NumberFormat
import java.util.Locale

/*
 * Shared QuickKart UI building blocks for the Merchant and Rider apps (same tokens, shapes and
 * selection style as the customer app).
 */

/** Brand black used for hero cards (same as the customer app). */
val BrandBlack = Color(0xFF141414)

/** "₹1,23,456" with Indian digit grouping. */
fun rupees(amount: Int): String =
    "₹" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("en-IN")).format(amount)

fun timeAgo(atMillis: Long, nowMillis: Long): String {
    val mins = ((nowMillis - atMillis) / 60_000).coerceAtLeast(0)
    return when {
        mins < 1 -> "Just now"
        mins < 60 -> "$mins min ago"
        mins < 24 * 60 -> "${mins / 60} h ago"
        else -> "${mins / (24 * 60)} d ago"
    }
}

fun minuteOfDayLabel(minuteOfDay: Int): String {
    val h = minuteOfDay / 60
    val m = minuteOfDay % 60
    val suffix = if (h < 12) "AM" else "PM"
    val h12 = when (val x = h % 12) { 0 -> 12; else -> x }
    return String.format(Locale.US, "%d:%02d %s", h12, m, suffix)
}

/** Sub-screen header: back arrow + title (+ optional actions), like the customer app. */
@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)?,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val tokens = QuickKartThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("header_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = tokens.ink)
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = tokens.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = tokens.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        actions()
    }
}

/** White rounded card with a hairline border (the customer app's card style). */
@Composable
fun QkCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = QuickKartThemeTokens.colors.surface,
    borderColor: Color = QuickKartThemeTokens.colors.border,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Column(modifier = Modifier.padding(contentPadding), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null) {
    val tokens = QuickKartThemeTokens.colors
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = tokens.ink, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

enum class BadgeTone { BRAND, SUCCESS, WARNING, DANGER, NEUTRAL, DARK }

@Composable
fun StatusBadge(text: String, tone: BadgeTone, modifier: Modifier = Modifier) {
    val tokens = QuickKartThemeTokens.colors
    val (bg, fg) = when (tone) {
        BadgeTone.BRAND -> tokens.brandYellow to tokens.onBrandYellow
        BadgeTone.SUCCESS -> tokens.deliveryGreenSoft to tokens.deliveryGreenDeep
        BadgeTone.WARNING -> tokens.brandYellowSoft to tokens.ink
        BadgeTone.DANGER -> tokens.alertRed.copy(alpha = 0.12f) to tokens.alertRed
        BadgeTone.NEUTRAL -> tokens.surfaceElevated to tokens.muted
        BadgeTone.DARK -> BrandBlack to tokens.brandYellow
    }
    Surface(color = bg, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}

/** Primary action: brand yellow with black text. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    testTag: String? = null
) {
    val tokens = QuickKartThemeTokens.colors
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = tokens.brandYellow,
            contentColor = tokens.onBrandYellow,
            disabledContainerColor = tokens.surfaceElevated,
            disabledContentColor = tokens.muted
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val tokens = QuickKartThemeTokens.colors
    val color = if (destructive) tokens.alertRed else tokens.ink
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (destructive) tokens.alertRed.copy(alpha = 0.5f) else tokens.border),
        modifier = modifier.height(52.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

/** Selectable chip: selected = yellow fill + black text (brand selection style). */
@Composable
fun ChoiceChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, count: Int? = null) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) tokens.brandYellow else tokens.surface,
        border = BorderStroke(1.dp, if (selected) tokens.brandYellow else tokens.border),
        modifier = modifier.clip(RoundedCornerShape(50)).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) tokens.onBrandYellow else tokens.ink,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
            if (count != null && count > 0) {
                Surface(color = if (selected) BrandBlack else tokens.surfaceElevated, shape = RoundedCornerShape(50)) {
                    Text(
                        count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) tokens.brandYellow else tokens.ink,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

/** Segmented control (Light / Dark / System, Day / Week / Month …). */
@Composable
fun <T> SegmentedChoice(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.surfaceElevated,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { option ->
                val isSel = option == selected
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) tokens.brandYellow else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(option) }
                ) {
                    Text(
                        label(option),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSel) tokens.onBrandYellow else tokens.muted,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 9.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Brand switch: yellow track with black thumb when on, clear outline when off. */
@Composable
fun BrandSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val tokens = QuickKartThemeTokens.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedTrackColor = tokens.brandYellow,
            checkedThumbColor = BrandBlack,
            checkedBorderColor = tokens.brandYellow,
            uncheckedTrackColor = tokens.surfaceElevated,
            uncheckedThumbColor = tokens.muted,
            uncheckedBorderColor = tokens.border
        ),
        modifier = modifier.zoomScaledControl()
    )
}

/** Row with leading icon circle, title/subtitle and a trailing slot (switch, chevron …). */
@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    iconTint: Color = QuickKartThemeTokens.colors.ink,
    iconBackground: Color = QuickKartThemeTokens.colors.brandYellowSoft,
    trailing: @Composable (() -> Unit)? = {
        if (onClick != null) Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = QuickKartThemeTokens.colors.muted)
    }
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(metrics.iconMedium))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        }
        trailing?.invoke()
    }
}

@Composable
fun QkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    supporting: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    prefix: String? = null,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val tokens = QuickKartThemeTokens.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = error != null,
        supportingText = (error ?: supporting)?.let { msg -> { Text(msg, color = if (error != null) tokens.alertRed else tokens.muted) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        prefix = prefix?.let { { Text(it, fontWeight = FontWeight.Bold) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tokens.ink, unfocusedBorderColor = tokens.border, focusedLabelColor = tokens.ink),
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    )
}

/** Large centred empty state. */
@Composable
fun EmptyState(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier, action: @Composable (() -> Unit)? = null) {
    val tokens = QuickKartThemeTokens.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(64.dp).background(tokens.brandYellowSoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tokens.ink, modifier = Modifier.size(30.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, color = tokens.ink)
        Text(body, style = MaterialTheme.typography.bodySmall, color = tokens.muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        action?.invoke()
    }
}

/** Label / value line used in bill and ledger summaries. */
@Composable
fun AmountRow(label: String, value: String, bold: Boolean = false, valueColor: Color = QuickKartThemeTokens.colors.ink) {
    val tokens = QuickKartThemeTokens.colors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (bold) tokens.ink else tokens.muted
        )
        Text(
            value,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

/** Four OTP boxes backed by one hidden text field (paste-friendly). */
@Composable
fun OtpBoxes(value: String, onValueChange: (String) -> Unit, enabled: Boolean = true) {
    val tokens = QuickKartThemeTokens.colors
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(4)) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.focusRequester(focus).testTag("otp_input"),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) { i ->
                    val ch = value.getOrNull(i)
                    val active = i == value.length
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(if (ch != null) tokens.brandYellow else tokens.surfaceElevated, RoundedCornerShape(14.dp))
                            .border(if (active) 2.dp else 1.dp, if (active) tokens.ink else tokens.border, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ch?.toString() ?: "", style = MaterialTheme.typography.headlineSmall, color = tokens.onBrandYellow, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// Charts (drawn in code, brand colours)
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun TrendChart(values: List<Int>, labels: List<String>, modifier: Modifier = Modifier, bars: Boolean = false) {
    val tokens = QuickKartThemeTokens.colors
    val lineColor = tokens.ink
    val fillColor = tokens.brandYellow
    val gridColor = tokens.border
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(150.dp)) {
            if (values.isEmpty()) return@Canvas
            val max = (values.maxOrNull() ?: 1).coerceAtLeast(1) * 1.15f
            repeat(4) { i -> val y = size.height * i / 3f; drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f) }
            if (bars) {
                val slot = size.width / values.size
                values.forEachIndexed { i, v ->
                    val h = size.height * (v / max)
                    drawRoundRect(fillColor, Offset(slot * i + slot * 0.2f, size.height - h), Size(slot * 0.6f, h), CornerRadius(8f, 8f))
                }
            } else {
                val step = if (values.size > 1) size.width / (values.size - 1) else size.width
                val pts = values.mapIndexed { i, v -> Offset(step * i, size.height - size.height * (v / max)) }
                val area = Path().apply {
                    moveTo(pts.first().x, size.height); pts.forEach { lineTo(it.x, it.y) }; lineTo(pts.last().x, size.height); close()
                }
                drawPath(area, fillColor.copy(alpha = 0.35f))
                val line = Path().apply { moveTo(pts.first().x, pts.first().y); pts.drop(1).forEach { lineTo(it.x, it.y) } }
                drawPath(line, lineColor, style = Stroke(width = 5f, cap = StrokeCap.Round))
                pts.forEach { drawCircle(fillColor, 8f, it); drawCircle(lineColor, 8f, it, style = Stroke(3f)) }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = tokens.muted) }
        }
    }
}
