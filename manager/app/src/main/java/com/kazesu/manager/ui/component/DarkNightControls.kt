package com.kazesu.manager.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazesu.manager.ui.LocalUiMode
import com.kazesu.manager.ui.UiMode
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import com.kazesu.manager.ui.theme.contrastOn
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch
import top.yukonga.miuix.kmp.basic.SwitchDefaults as MiuixSwitchDefaults

/**
 * Palette-aware accent helper: ensures readable contrast in Light mode for bright palettes (B3).
 */
@Composable
fun darkNightEffectiveAccent(): Color {
    val glass = LocalDarkNightGlass.current
    val target = if (!glass.isDark && glass.accent.luminance() > 0.55f) glass.accentDark else glass.accent
    val animated by animateColorAsState(
        targetValue = target,
        animationSpec = tween(300),
        label = "dn_effective_accent",
    )
    return animated
}

/**
 * Unified Switch matching Material or Miuix shell while always honoring the active DarkNight palette (B3).
 */
@Composable
fun DarkNightSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    DarkNightToggle(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
    )
}

/**
 * Palette-tinted selectable pill chip for both Material and Miuix (B3).
 */
@Composable
fun DarkNightChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glass = LocalDarkNightGlass.current
    val accent = darkNightEffectiveAccent()
    val bg by animateColorAsState(
        targetValue = if (selected) accent else glass.content.copy(alpha = 0.10f),
        animationSpec = tween(220),
        label = "dn_chip_bg",
    )
    val fg = if (selected) contrastOn(accent) else glass.content
    val shape = RoundedCornerShape(99.dp)

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = bg,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) accent else glass.content.copy(alpha = 0.18f),
        ),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = fg,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

/**
 * Palette-tinted Slider for both Material and Miuix (B3).
 */
@Composable
fun DarkNightSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
) {
    val glass = LocalDarkNightGlass.current
    val accent = darkNightEffectiveAccent()
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = accent,
            activeTrackColor = accent,
            inactiveTrackColor = glass.content.copy(alpha = 0.18f),
        ),
    )
}
