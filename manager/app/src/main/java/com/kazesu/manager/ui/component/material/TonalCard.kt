package com.kazesu.manager.ui.component.material

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.LocalBackgroundImagePath
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import com.kazesu.manager.ui.theme.ProvideDarkNightContent
import com.kazesu.manager.ui.theme.backgroundAwareColor
import com.kazesu.manager.ui.theme.darkNightGlassCardSurface

@Composable
fun TonalCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceBright,
    contentColor: Color = contentColorFor(containerColor),
    shape: Shape = MaterialTheme.shapes.large,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressFeedbackEnabled: Boolean = true,
    pressScale: Float = 0.97f,
    pressHapticEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    DarkNightThemeStore.init(context)
    val dnState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val glass = LocalDarkNightGlass.current
    val isDarkNightAura = LocalBackgroundImagePath.current == "darknight://aura"
    val isFlatPalette = !dnState.glassOn && LocalBackgroundImagePath.current.isEmpty()

    val colors = when {
        isDarkNightAura && dnState.glassOn -> CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = glass.content,
        )
        isDarkNightAura || isFlatPalette -> CardDefaults.cardColors(
            containerColor = glass.surface,
            contentColor = glass.content,
        )
        else -> CardDefaults.cardColors(
            containerColor = backgroundAwareColor(containerColor),
            contentColor = contentColor,
        )
    }
    val glassModifier = when {
        isDarkNightAura && dnState.glassOn -> Modifier.darkNightGlassCardSurface(shape = shape)
        isDarkNightAura || isFlatPalette -> Modifier.border(1.dp, glass.outline, shape)
        else -> Modifier
    }
    val wrappedContent: @Composable () -> Unit = {
        if (isDarkNightAura || isFlatPalette) {
            ProvideDarkNightContent(content)
        } else {
            content()
        }
    }

    when {
        onLongClick != null || onClick != null -> {
            val pressed by interactionSource.collectIsPressedAsState()
            val targetScale = when {
                !pressFeedbackEnabled -> 1f
                pressed -> pressScale
                else -> 1f
            }
            val scale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "TonalCardPressScale",
            )
            val haptic = LocalHapticFeedback.current
            LaunchedEffect(pressed) {
                if (pressed && pressFeedbackEnabled && pressHapticEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
            Card(
                modifier = modifier
                    .scale(scale)
                    .then(glassModifier)
                    .clip(shape)
                    .combinedClickable(
                        enabled = enabled,
                        onClick = onClick ?: {},
                        onLongClick = onLongClick,
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                    ),
                colors = colors,
                shape = shape,
            ) { wrappedContent() }
        }

        else -> Card(
            modifier = modifier.then(glassModifier),
            colors = colors,
            shape = shape,
        ) { wrappedContent() }
    }
}
