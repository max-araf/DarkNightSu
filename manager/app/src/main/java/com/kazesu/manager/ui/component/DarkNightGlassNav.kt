package com.kazesu.manager.ui.component

import android.os.Build
import android.system.Os
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.kazesu.manager.R
import com.kazesu.manager.ui.theme.DarkNightPalette
import com.kazesu.manager.ui.theme.DarkNightPalettes
import com.kazesu.manager.ui.theme.DarkNightToastManager
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import com.kazesu.manager.ui.theme.contrastOn
import com.kazesu.manager.ui.util.getSELinuxStatusRaw
import com.kazesu.manager.ui.util.reboot
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported

/**
 * Reusable DarkNight Liquid Glass surface modifier (FIX-C1):
 * - Real GPU textureBlur (26f) on API 31+, translucent fallback on API < 31
 * - Palette-tinted 1dp gradient border (lite 40% -> 7% -> 16%, neutral capped at 30%; NEVER pure white)
 * - 165-deg dark-to-accent gradient + bottom-right radial highlight + diagonal sheen + top 1dp highlight
 */
@Composable
fun Modifier.darkNightGlass(
    shape: Shape,
    palette: DarkNightPalette,
    backdrop: LayerBackdrop? = null,
): Modifier {
    val glass = LocalDarkNightGlass.current
    val accent = palette.primaryGlow
    val lite = palette.secondaryGlow
    val isNeutral = palette.id == DarkNightPalettes.RoyalCharcoal.id
    val borderTint = if (!glass.isDark) glass.accentDark else if (isNeutral) lerp(lite, accent, 0.4f) else lite
    val maxBorderAlpha = if (!glass.isDark) 0.28f else if (isNeutral) 0.28f else 0.40f

    val navCtx = LocalContext.current
    DarkNightThemeStore.init(navCtx)
    val navDnState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    if (!navDnState.glassOn) {
        return this
            .clip(shape)
            .background(glass.surfaceHi)
            .border(1.dp, glass.outline, shape)
    }
    val hasRealBlur = backdrop != null && isRenderEffectSupported()
    val alphaBoost = if (hasRealBlur) 0f else 0.15f
    val isBrightAccent = accent.luminance() > 0.45f
    val topDarkAlpha = (if (isBrightAccent) 0.90f else 0.80f + alphaBoost).coerceAtMost(0.96f)
    val midDarkAlpha = (if (isBrightAccent) 0.62f else 0.44f + alphaBoost).coerceAtMost(0.88f)
    val accentMidAlpha = (if (isBrightAccent) 0.28f else 0.36f + alphaBoost * 0.5f).coerceAtMost(0.75f)
    val accentEndAlpha = (if (isBrightAccent) 0.48f else 0.64f + alphaBoost * 0.5f).coerceAtMost(0.88f)

    val baseBlur = if (hasRealBlur && backdrop != null) {
        this.textureBlur(
            backdrop = backdrop,
            shape = shape,
            blurRadius = 26f,
            colors = BlurColors(
                blendColors = listOf(
                    BlendColorEntry(color = Color(0xFF040710).copy(alpha = 0.20f)),
                ),
            ),
        )
    } else {
        this
    }

    return baseBlur
        .clip(shape)
        .drawBehind {
            if (glass.isDark) {
                val darkTop = lerp(accent, Color.Black, 0.72f)
                val darkMid = lerp(accent, Color.Black, 0.64f)
                drawRect(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.00f to darkTop.copy(alpha = topDarkAlpha),
                            0.45f to darkMid.copy(alpha = midDarkAlpha),
                            0.78f to accent.copy(alpha = accentMidAlpha),
                            1.00f to accent.copy(alpha = accentEndAlpha),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width * 0.35f, size.height),
                    )
                )
            } else {
                // D6: Pure Light pastel palette glass (zero black/dark stops)
                drawRect(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.00f to Color.White.copy(alpha = 0.86f),
                            0.55f to lerp(accent, Color.White, 0.86f).copy(alpha = 0.90f),
                            1.00f to lerp(accent, Color.White, 0.76f).copy(alpha = 0.92f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width * 0.35f, size.height),
                    )
                )
            }
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(lite.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.88f),
                    radius = size.width * 1.1f,
                )
            )
            drawRect(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.00f to Color.White.copy(alpha = 0.14f),
                        0.46f to Color.Transparent,
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.8f, size.height * 0.45f),
                )
            )
        }
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.00f to borderTint.copy(alpha = maxBorderAlpha),
                    0.55f to borderTint.copy(alpha = 0.07f),
                    1.00f to borderTint.copy(alpha = 0.16f),
                ),
                start = Offset(0f, 0f),
                end = Offset(400f, 900f),
            ),
            shape = shape,
        )
        .drawWithContent {
            drawContent()
            drawLine(
                color = Color.White.copy(alpha = 0.18f),
                start = Offset(size.width * 0.12f, 1.dp.toPx()),
                end = Offset(size.width * 0.88f, 1.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
            )
        }
}

private data class SidebarMenuItem(
    val id: String,
    val labelRes: Int,
    val iconRes: Int,
    val badgeCount: Int = 0,
    val onClick: () -> Unit,
)

@Composable
fun DarkNightGlassSidebarOverlay(
    palette: DarkNightPalette,
    selectedPage: Int,
    moduleBadgeCount: Int,
    backdrop: LayerBackdrop?,
    animationsEnabled: Boolean = true,
    onSelectPage: (Int) -> Unit,
    onOpenDarkTune: () -> Unit,
    onOpenToolbox: () -> Unit,
    onOpenThemeStudio: () -> Unit,
    onOpenSendLogs: () -> Unit,
    onOpenAbout: () -> Unit,
    onExpandedChange: (Boolean) -> Unit = {},
    rootTabsVisible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(expanded) { onExpandedChange(expanded) }
    var showRebootDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = expanded) {
        expanded = false
    }

    val kernelRelease = remember {
        runCatching { Os.uname().release.substringBefore("-") }.getOrDefault("Kernel")
    }
    val selinuxLabel = remember {
        runCatching { getSELinuxStatusRaw() }.getOrDefault("Enforcing")
    }
    val deviceModel = remember { Build.MODEL.ifBlank { "Android Device" } }

    val menuItems = buildList {
        add(SidebarMenuItem("home", R.string.home, R.drawable.ic_dn_home) { onSelectPage(0) })
        if (rootTabsVisible) {
            add(SidebarMenuItem("superuser", R.string.superuser, R.drawable.ic_dn_superuser) { onSelectPage(1) })
            add(SidebarMenuItem("module", R.string.module, R.drawable.ic_dn_module, badgeCount = moduleBadgeCount) { onSelectPage(2) })
            add(SidebarMenuItem("darktune", R.string.kernel_tuning, R.drawable.ic_dn_darktune) { onOpenDarkTune() })
        }
        add(SidebarMenuItem("toolbox", R.string.darknight_toolbox_title, R.drawable.ic_dn_toolbox) { onOpenToolbox() })
    }

    val systemItems = listOf(
        SidebarMenuItem("theme", R.string.darknight_nav_theme_studio, R.drawable.ic_dn_theme) { onOpenThemeStudio() },
        SidebarMenuItem("logs", R.string.send_log, R.drawable.ic_dn_logs) { onOpenSendLogs() },
        SidebarMenuItem("settings", R.string.settings, R.drawable.ic_dn_settings) { onSelectPage(3) },
        SidebarMenuItem("about", R.string.about, R.drawable.ic_dn_about) { onOpenAbout() },
        SidebarMenuItem("reboot", R.string.darknight_nav_reboot_options, R.drawable.ic_dn_reboot) { showRebootDialog = true },
    )

    val selectedId = when (selectedPage) {
        0 -> "home"
        1 -> "superuser"
        2 -> "module"
        3 -> "settings"
        else -> "home"
    }

    val isNeutral = palette.id == DarkNightPalettes.RoyalCharcoal.id
    val borderTint = if (isNeutral) lerp(palette.secondaryGlow, palette.primaryGlow, 0.4f) else palette.secondaryGlow

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val expandedWidth = (maxWidth * 0.80f).coerceAtMost(300.dp)

        // Single progress (0..1) driving zero-reflow morph (FIX-C2)
        val progress by animateFloatAsState(
            targetValue = if (expanded) 1f else 0f,
            animationSpec = if (animationsEnabled) {
                spring(dampingRatio = 0.80f, stiffness = 380f)
            } else {
                tween(0)
            },
            label = "sidebar_morph_progress",
        )

        val sidebarWidth = lerp(56.dp, expandedWidth, progress)
        val cornerRadius = lerp(28.dp, 32.dp, progress)
        val startMargin = lerp(10.dp, 12.dp, progress)
        val scrimAlpha = 0.34f * progress

        // Scrim behind expanded drawer
        if (scrimAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable { expanded = false }
            )
        }

        val shape = RoundedCornerShape(cornerRadius)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = startMargin, top = 8.dp, bottom = 8.dp)
                .width(sidebarWidth)
                .fillMaxHeight()
                .darkNightGlass(shape = shape, palette = palette, backdrop = backdrop)
                .clipToBounds()
                .pointerInput(expanded) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (!expanded && dragAmount > 18f) expanded = true
                        if (expanded && dragAmount < -18f) expanded = false
                    }
                },
            contentAlignment = Alignment.TopStart,
        ) {
            // LAYER 1: Collapsed Rail (fixed 56dp, fades out cleanly on expand)
            val railAlpha = (1f - progress * 2.1f).coerceIn(0f, 1f)
            if (railAlpha > 0.01f) {
                Column(
                    modifier = Modifier
                        .requiredWidth(56.dp)
                        .fillMaxHeight()
                        .graphicsLayer { alpha = railAlpha }
                        .padding(top = 14.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Fixed 36x36dp themed logo (PART 2.2)
                    DarkNightLogo(
                        size = 36.dp,
                        modifier = Modifier.clickable(enabled = !expanded) { expanded = true },
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    GlassDivider(width = 28.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        menuItems.forEach { item ->
                            RailIconButton(
                                item = item,
                                selected = item.id == selectedId,
                                palette = palette,
                                enabled = !expanded,
                                onClick = { item.onClick() },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    GlassDivider(width = 28.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        RailIconButton(
                            item = systemItems[0],
                            selected = false,
                            palette = palette,
                            enabled = !expanded,
                            onClick = { systemItems[0].onClick() },
                        )
                        RailIconButton(
                            item = systemItems[2],
                            selected = selectedId == "settings",
                            palette = palette,
                            enabled = !expanded,
                            onClick = { systemItems[2].onClick() },
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (LocalDarkNightGlass.current.isDark) lerp(palette.primaryGlow, Color.Black, 0.70f).copy(alpha = 0.80f) else palette.primaryGlow.copy(alpha = 0.14f))
                            .border(1.dp, palette.secondaryGlow.copy(alpha = 0.55f), CircleShape)
                            .clickable(enabled = !expanded) { onOpenAbout() },
                        contentAlignment = Alignment.Center,
                    ) {
                        DarkNightLogo(size = 24.dp)
                    }
                }
            }

            // LAYER 2: Expanded Drawer (T4 fix: anchor Start + unbounded so header never centers on collapse; fade out in 60ms)
            val drawerAlpha by animateFloatAsState(
                targetValue = if (expanded) 1f else 0f,
                animationSpec = tween(durationMillis = if (expanded) 170 else 60),
                label = "drawer_content_alpha",
            )
            val glass = LocalDarkNightGlass.current
            if (drawerAlpha > 0.01f) {
                Column(
                    modifier = Modifier
                        .wrapContentWidth(align = Alignment.Start, unbounded = true)
                        .width(expandedWidth)
                        .fillMaxHeight()
                        .graphicsLayer { alpha = drawerAlpha }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                ) {
                    // Fixed Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = expanded) { expanded = false }
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DarkNightLogo(size = 44.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.app_name),
                                color = glass.content,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                            )
                            Text(
                                text = stringResource(R.string.darknight_subtitle),
                                color = glass.contentSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    GlassDivider(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))

                    // Scrollable MENU + SYSTEM list (FIX-C3: weight(1f) so footer never clips "Reboot options")
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.darknight_nav_section_menu),
                            color = LocalDarkNightGlass.current.content.copy(alpha = 0.50f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.6.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )

                        menuItems.forEach { item ->
                            DrawerMenuRow(
                                item = item,
                                selected = item.id == selectedId,
                                palette = palette,
                                enabled = expanded,
                                onClick = {
                                    expanded = false
                                    item.onClick()
                                },
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        GlassDivider(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.darknight_nav_section_system),
                            color = LocalDarkNightGlass.current.content.copy(alpha = 0.50f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.6.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )

                        systemItems.forEach { item ->
                            DrawerMenuRow(
                                item = item,
                                selected = item.id == selectedId,
                                palette = palette,
                                enabled = expanded,
                                onClick = {
                                    expanded = false
                                    item.onClick()
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fixed Footer Device + Kernel card (palette-tinted border, NEVER white!)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(enabled = expanded) {
                                expanded = false
                                onOpenAbout()
                            },
                        shape = RoundedCornerShape(18.dp),
                        color = if (LocalDarkNightGlass.current.isDark) lerp(palette.primaryGlow, Color.Black, 0.72f).copy(alpha = 0.75f) else palette.primaryGlow.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, borderTint.copy(alpha = 0.28f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(palette.primaryGlow.copy(alpha = 0.25f))
                                    .border(1.dp, palette.secondaryGlow.copy(alpha = 0.55f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                DarkNightLogo(size = 26.dp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = deviceModel,
                                    color = LocalDarkNightGlass.current.content,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "Kernel $kernelRelease • SELinux $selinuxLabel",
                                    color = LocalDarkNightGlass.current.contentSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(
                                painter = painterResource(R.drawable.ic_dn_chevron),
                                contentDescription = null,
                                tint = LocalDarkNightGlass.current.contentSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRebootDialog) {
        AlertDialog(
            onDismissRequest = { showRebootDialog = false },
            title = { Text(stringResource(R.string.darknight_nav_reboot_options)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        stringResource(R.string.reboot) to "",
                        stringResource(R.string.reboot_recovery) to "recovery",
                        stringResource(R.string.reboot_bootloader) to "bootloader",
                        stringResource(R.string.reboot_download) to "download",
                    ).forEach { (label, reason) ->
                        TextButton(
                            onClick = {
                                showRebootDialog = false
                                DarkNightToastManager.show(label)
                                reboot(reason)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRebootDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun RailIconButton(
    item: SidebarMenuItem,
    selected: Boolean,
    palette: DarkNightPalette,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val pillShape = RoundedCornerShape(15.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        contentAlignment = Alignment.Center,
    ) {
        // FIX-C1: Palette lite indicator bar (3.5dp x 24dp) with lite @ 90% glow
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.5.dp)
                    .height(24.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp),
                        ambientColor = palette.secondaryGlow.copy(alpha = 0.90f),
                        spotColor = palette.secondaryGlow.copy(alpha = 0.90f),
                    )
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(palette.secondaryGlow)
            )
        }

        // FIX-C1: Selected pill has NO border, only fill gradient + top 1dp inner highlight
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(pillShape)
                .then(
                    if (selected) {
                        Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(palette.primaryGlow, lerp(palette.primaryGlow, Color.White, 0.30f))
                                )
                            )
                            .drawWithContent {
                                drawContent()
                                drawLine(
                                    color = Color.White.copy(alpha = 0.20f),
                                    start = Offset(size.width * 0.16f, 1.dp.toPx()),
                                    end = Offset(size.width * 0.84f, 1.dp.toPx()),
                                    strokeWidth = 1.dp.toPx(),
                                )
                            }
                    } else {
                        Modifier
                    }
                )
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(item.iconRes),
                    contentDescription = stringResource(item.labelRes),
                    tint = if (selected) contrastOn(palette.primaryGlow) else LocalDarkNightGlass.current.content.copy(alpha = 0.72f),
                    modifier = Modifier.size(22.dp),
                )
                if (item.badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                            .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                            .clip(CircleShape)
                            .background(palette.primaryGlow)
                            .border(1.5.dp, Color(0xFF07090F), CircleShape)
                            .padding(horizontal = 3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
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

@Composable
private fun DrawerMenuRow(
    item: SidebarMenuItem,
    selected: Boolean,
    palette: DarkNightPalette,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val pillShape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-14).dp)
                    .width(3.5.dp)
                    .height(24.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp),
                        ambientColor = palette.secondaryGlow.copy(alpha = 0.90f),
                        spotColor = palette.secondaryGlow.copy(alpha = 0.90f),
                    )
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(palette.secondaryGlow)
            )
        }

        // FIX-C1: Selected pill has NO border, only fill gradient + top 1dp inner highlight
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(pillShape)
                .then(
                    if (selected) {
                        Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(palette.primaryGlow, lerp(palette.primaryGlow, Color.White, 0.30f))
                                )
                            )
                            .drawWithContent {
                                drawContent()
                                drawLine(
                                    color = Color.White.copy(alpha = 0.20f),
                                    start = Offset(size.width * 0.06f, 1.dp.toPx()),
                                    end = Offset(size.width * 0.94f, 1.dp.toPx()),
                                    strokeWidth = 1.dp.toPx(),
                                )
                            }
                    } else {
                        Modifier
                    }
                )
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(item.iconRes),
                contentDescription = null,
                tint = if (selected) contrastOn(palette.primaryGlow) else LocalDarkNightGlass.current.content.copy(alpha = 0.72f),
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(item.labelRes),
                color = if (selected) contrastOn(palette.primaryGlow) else LocalDarkNightGlass.current.content.copy(alpha = 0.90f),
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier.weight(1f),
            )
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_dn_chevron),
                    contentDescription = null,
                    tint = LocalDarkNightGlass.current.content.copy(alpha = 0.78f),
                    modifier = Modifier.size(18.dp),
                )
            } else if (item.badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                        .clip(CircleShape)
                        .background(palette.primaryGlow)
                        .border(1.5.dp, Color(0xFF07090F), CircleShape)
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassDivider(
    modifier: Modifier = Modifier,
    width: Dp? = null,
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, LocalDarkNightGlass.current.content.copy(alpha = 0.16f), Color.Transparent)
                )
            )
    )
}
