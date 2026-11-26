package com.kazesu.manager.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kazesu.manager.ui.theme.LocalDarkNightGlass

/**
 * E3 (v8): Palette-tinted Icon Tile replacing neutral gray secondaryContainer across Settings,
 * Module, Superuser, Theme, Toolbox, and About rows. Smoothly animates on palette change.
 */
@Composable
fun DarkNightIconTile(
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    overrideAccent: Color? = null,
) {
    val g = LocalDarkNightGlass.current
    val baseAccent = overrideAccent ?: g.accent
    val bg by animateColorAsState(
        targetValue = baseAccent.copy(alpha = if (g.isDark) 0.20f else 0.14f),
        animationSpec = tween(300),
        label = "dn_icon_tile_bg",
    )
    val border by animateColorAsState(
        targetValue = baseAccent.copy(alpha = if (g.isDark) 0.35f else 0.28f),
        animationSpec = tween(300),
        label = "dn_icon_tile_border",
    )
    val tint by animateColorAsState(
        targetValue = overrideAccent ?: g.accentText,
        animationSpec = tween(300),
        label = "dn_icon_tile_tint",
    )
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun DarkNightIconTile(
    painter: Painter,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    overrideAccent: Color? = null,
) {
    val g = LocalDarkNightGlass.current
    val baseAccent = overrideAccent ?: g.accent
    val bg by animateColorAsState(
        targetValue = baseAccent.copy(alpha = if (g.isDark) 0.20f else 0.14f),
        animationSpec = tween(300),
        label = "dn_icon_tile_p_bg",
    )
    val border by animateColorAsState(
        targetValue = baseAccent.copy(alpha = if (g.isDark) 0.35f else 0.28f),
        animationSpec = tween(300),
        label = "dn_icon_tile_p_border",
    )
    val tint by animateColorAsState(
        targetValue = overrideAccent ?: g.accentText,
        animationSpec = tween(300),
        label = "dn_icon_tile_p_tint",
    )
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}
