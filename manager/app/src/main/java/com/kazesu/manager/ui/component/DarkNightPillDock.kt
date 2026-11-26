package com.kazesu.manager.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazesu.manager.R
import com.kazesu.manager.ui.theme.DarkNightBlurQuality
import com.kazesu.manager.ui.theme.DarkNightDockStyle
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported

private data class DockTab(
    val index: Int,
    val iconRes: Int,
    val labelRes: Int,
)

private val DockTabs = listOf(
    DockTab(0, R.drawable.ic_dn_home, R.string.home),
    DockTab(1, R.drawable.ic_dn_superuser, R.string.superuser),
    DockTab(2, R.drawable.ic_dn_module, R.string.module),
    DockTab(3, R.drawable.ic_dn_settings, R.string.settings),
)

/**
 * Pierre Su style floating Pill Dock (PART 2):
 * - 3 visual styles: Glass (Dark default), Dark Solid, Light Glass (auto in Light mode)
 * - 52dp circle bubble spring-slides smoothly between the 4 tabs
 * - Outer 1dp halo ring (6dp outside, 44dp radius) on Glass & Light styles
 */
@Composable
fun DarkNightPillDock(
    selectedPage: Int,
    moduleBadgeCount: Int,
    dockStyle: DarkNightDockStyle,
    blurQuality: DarkNightBlurQuality,
    animationsEnabled: Boolean,
    backdrop: LayerBackdrop?,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
    rootTabsVisible: Boolean = true,
) {
    val context = LocalContext.current
    DarkNightThemeStore.init(context)
    val dnState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val glass = LocalDarkNightGlass.current
    val haptic = LocalHapticFeedback.current
    val isLight = !glass.isDark
    val glassOn = dnState.glassOn
    val effectiveSolid = !glassOn || dockStyle == DarkNightDockStyle.DARK_SOLID
    val showHalo = !effectiveSolid

    val itemSize = 52.dp
    val itemGap = 12.dp
    val innerPad = 10.dp
    val dockShape = RoundedCornerShape(38.dp)
    val haloShape = RoundedCornerShape(44.dp)

    val tabs = remember(rootTabsVisible) {
        if (rootTabsVisible) DockTabs else DockTabs.filter { it.index == 0 || it.index == 3 }
    }
    val selectedPos = tabs.indexOfFirst { it.index == selectedPage }.coerceAtLeast(0)
    val bubbleOffsetX by animateDpAsState(
        targetValue = (itemSize + itemGap) * selectedPos,
        animationSpec = if (animationsEnabled) {
            spring(dampingRatio = 0.75f, stiffness = 400f)
        } else {
            tween(0)
        },
        label = "dock_bubble_x",
    )

    val hasRealBlur = !effectiveSolid &&
        glassOn &&
        backdrop != null &&
        isRenderEffectSupported()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Optional outer halo ring (6dp outside, 1dp lite @ 0.26)
        Box(
            modifier = Modifier
                .then(
                    if (showHalo) {
                        Modifier
                            .border(
                                width = 1.dp,
                                color = if (isLight) {
                                    glass.accent.copy(alpha = 0.22f)
                                } else {
                                    glass.lite.copy(alpha = 0.26f)
                                },
                                shape = haloShape,
                            )
                            .padding(6.dp)
                    } else {
                        Modifier
                    }
                )
        ) {
            val blurMod = if (hasRealBlur && backdrop != null) {
                Modifier.textureBlur(
                    backdrop = backdrop,
                    shape = dockShape,
                    blurRadius = 26f,
                    colors = BlurColors(
                        blendColors = listOf(
                            BlendColorEntry(
                                color = if (isLight) Color.White.copy(alpha = 0.30f) else Color(0xFF040710).copy(alpha = 0.22f)
                            )
                        )
                    )
                )
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .shadow(
                        elevation = if (isLight) 20.dp else 18.dp,
                        shape = dockShape,
                        ambientColor = if (isLight) glass.accent.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.45f),
                        spotColor = if (isLight) glass.accent.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.45f),
                    )
                    .then(blurMod)
                    .clip(dockShape)
                    .drawBehind {
                        when {
                            !glassOn -> {
                                drawRect(glass.surfaceHi)
                            }
                            effectiveSolid && isLight -> {
                                drawRect(Color.White)
                            }
                            effectiveSolid && !isLight -> {
                                drawRect(if (dnState.amoledBlack) Color.Black else Color(0xFF0E1424))
                            }
                            isLight -> {
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colorStops = arrayOf(
                                            0.00f to Color.White.copy(alpha = 0.62f),
                                            0.60f to Color.White.copy(alpha = 0.28f),
                                            1.00f to glass.accent.copy(alpha = 0.28f),
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width * 0.35f, size.height),
                                    )
                                )
                            }
                            effectiveSolid -> {
                                drawRect(Color(0xFF08090E).copy(alpha = 0.94f))
                            }
                            else -> {
                                val alphaBoost = if (hasRealBlur) 0f else 0.22f
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colorStops = arrayOf(
                                            0.00f to Color(0xFF040710).copy(alpha = (0.58f + alphaBoost).coerceAtMost(0.92f)),
                                            0.70f to glass.accent.copy(alpha = (0.34f + alphaBoost * 0.4f).coerceAtMost(0.70f)),
                                            1.00f to glass.accent.copy(alpha = (0.55f + alphaBoost * 0.4f).coerceAtMost(0.85f)),
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width * 0.35f, size.height),
                                    )
                                )
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(glass.lite.copy(alpha = 0.26f), Color.Transparent),
                                        center = Offset(size.width * 0.85f, size.height * 0.9f),
                                        radius = size.width * 0.65f,
                                    )
                                )
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colorStops = arrayOf(
                                            0.00f to Color.White.copy(alpha = 0.15f),
                                            0.50f to Color.Transparent,
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width * 0.7f, size.height * 0.6f),
                                    )
                                )
                            }
                        }
                    }
                    .border(
                        width = 1.dp,
                        brush = when {
                            isLight -> Brush.linearGradient(
                                listOf(glass.accent.copy(alpha = 0.28f), glass.accent.copy(alpha = 0.12f))
                            )
                            effectiveSolid -> Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.04f))
                            )
                            else -> Brush.linearGradient(
                                listOf(glass.borderLite.copy(alpha = 0.42f), glass.borderLite.copy(alpha = 0.12f))
                            )
                        },
                        shape = dockShape,
                    )
                    .padding(innerPad),
            ) {
                // Single spring-sliding selected bubble underneath icons
                Box(
                    modifier = Modifier
                        .offset(x = bubbleOffsetX)
                        .size(itemSize)
                        .shadow(
                            elevation = if (effectiveSolid || isLight) 8.dp else 6.dp,
                            shape = CircleShape,
                            ambientColor = if (isLight) glass.accentDark.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.35f),
                        )
                        .clip(CircleShape)
                        .drawBehind {
                            when {
                                isLight || effectiveSolid -> drawRect(Color.White)
                                else -> drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.36f), Color.White.copy(alpha = 0.10f))
                                    )
                                )
                            }
                        }
                        .then(
                            when {
                                isLight -> Modifier.border(1.dp, glass.accent.copy(alpha = 0.35f), CircleShape)
                                effectiveSolid -> Modifier
                                else -> Modifier.border(1.dp, glass.lite.copy(alpha = 0.60f), CircleShape)
                            }
                        )
                )

                // 4 Dock Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(itemGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEach { tab ->
                        val selected = selectedPage == tab.index
                        val targetTint = when {
                            isLight -> if (selected) glass.accent else Color(0xFF0B1020).copy(alpha = 0.62f)
                            effectiveSolid -> if (selected) Color(0xFF0B1020) else Color.White.copy(alpha = 0.55f)
                            else -> if (selected) Color.White else Color.White.copy(alpha = 0.72f)
                        }
                        val iconTint by animateColorAsState(
                            targetValue = targetTint,
                            animationSpec = if (animationsEnabled) tween(150) else tween(0),
                            label = "dock_tint_${tab.index}",
                        )
                        val interaction = remember { MutableInteractionSource() }

                        Box(
                            modifier = Modifier
                                .size(itemSize)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = interaction,
                                    indication = null,
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectPage(tab.index)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(tab.iconRes),
                                    contentDescription = stringResource(tab.labelRes),
                                    tint = iconTint,
                                    modifier = Modifier.size(24.dp),
                                )
                                if (tab.index == 2 && moduleBadgeCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 6.dp, y = (-6).dp)
                                            .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                                            .clip(CircleShape)
                                            .background(glass.accent)
                                            .border(1.5.dp, glass.base, CircleShape)
                                            .padding(horizontal = 3.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = if (moduleBadgeCount > 99) "99+" else moduleBadgeCount.toString(),
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
