package com.kazesu.manager.ui.component.miuix

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.LocalBackgroundImagePath
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import com.kazesu.manager.ui.theme.ProvideDarkNightContent
import com.kazesu.manager.ui.theme.backgroundAwareCardColors
import com.kazesu.manager.ui.theme.darkNightGlassCardSurface
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * Miuix [AppCard] with full DarkNight Frosted Glass, Flat Palette mode (PART 2), and LocalContentColor propagation (C1).
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CardDefaults.CornerRadius,
    insideMargin: PaddingValues = CardDefaults.InsideMargin,
    colors: CardColors = backgroundAwareCardColors(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val context = LocalContext.current
    DarkNightThemeStore.init(context)
    val dnState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val glass = LocalDarkNightGlass.current
    val isDarkNightAura = LocalBackgroundImagePath.current == "darknight://aura"
    val isFlatPalette = !dnState.glassOn && LocalBackgroundImagePath.current.isEmpty()
    val shape = RoundedCornerShape(dnState.cardCornerRadiusDp.dp)

    val resolvedMod = when {
        isDarkNightAura && dnState.glassOn -> modifier.darkNightGlassCardSurface(shape = shape)
        isDarkNightAura || isFlatPalette -> modifier.border(1.dp, glass.outline, shape)
        else -> modifier
    }
    val resolvedColors = when {
        isDarkNightAura && dnState.glassOn -> CardDefaults.defaultColors(
            color = Color.Transparent,
            contentColor = glass.content,
        )
        isDarkNightAura || isFlatPalette -> CardDefaults.defaultColors(
            color = glass.surface,
            contentColor = glass.content,
        )
        else -> colors
    }
    Card(
        modifier = resolvedMod,
        cornerRadius = if (isDarkNightAura || isFlatPalette) dnState.cardCornerRadiusDp.dp else cornerRadius,
        insideMargin = insideMargin,
        colors = resolvedColors,
    ) {
        if (isDarkNightAura || isFlatPalette) {
            ProvideDarkNightContent { content() }
        } else {
            content()
        }
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CardDefaults.CornerRadius,
    insideMargin: PaddingValues = CardDefaults.InsideMargin,
    colors: CardColors = backgroundAwareCardColors(),
    pressFeedbackType: PressFeedbackType = PressFeedbackType.None,
    showIndication: Boolean = false,
    holdDownState: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val context = LocalContext.current
    DarkNightThemeStore.init(context)
    val dnState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val glass = LocalDarkNightGlass.current
    val isDarkNightAura = LocalBackgroundImagePath.current == "darknight://aura"
    val isFlatPalette = !dnState.glassOn && LocalBackgroundImagePath.current.isEmpty()
    val shape = RoundedCornerShape(dnState.cardCornerRadiusDp.dp)

    val resolvedMod = when {
        isDarkNightAura && dnState.glassOn -> modifier.darkNightGlassCardSurface(shape = shape)
        isDarkNightAura || isFlatPalette -> modifier.border(1.dp, glass.outline, shape)
        else -> modifier
    }
    val resolvedColors = when {
        isDarkNightAura && dnState.glassOn -> CardDefaults.defaultColors(
            color = Color.Transparent,
            contentColor = glass.content,
        )
        isDarkNightAura || isFlatPalette -> CardDefaults.defaultColors(
            color = glass.surface,
            contentColor = glass.content,
        )
        else -> colors
    }
    Card(
        modifier = resolvedMod,
        cornerRadius = if (isDarkNightAura || isFlatPalette) dnState.cardCornerRadiusDp.dp else cornerRadius,
        insideMargin = insideMargin,
        colors = resolvedColors,
        pressFeedbackType = pressFeedbackType,
        showIndication = showIndication,
        holdDownState = holdDownState,
        onClick = onClick,
        onLongPress = onLongPress,
    ) {
        if (isDarkNightAura || isFlatPalette) {
            ProvideDarkNightContent { content() }
        } else {
            content()
        }
    }
}
