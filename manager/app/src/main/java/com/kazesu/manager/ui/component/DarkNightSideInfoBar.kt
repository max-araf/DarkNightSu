package com.kazesu.manager.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.kazesu.manager.R
import com.kazesu.manager.ui.theme.DarkNightBlurQuality
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported

/**
 * Right-edge floating Side Info Bar (PART 3):
 * - Collapsed: 7x64dp right-edge handle; tap or swipe-left expands (4s idle auto-collapse unless Pinned)
 * - Expanded: 58dp flush right pill with 3 circular 38dp rings (CPU speed, Battery %, Phone temp) + Gear button
 * - Zero polling when collapsed or disabled!
 */
@Composable
fun DarkNightSideInfoBarOverlay(
    backdrop: LayerBackdrop?,
    onOpenFullSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DarkNightThemeStore.init(context)
    val state by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val glass = LocalDarkNightGlass.current

    if (!state.infoBarEnabled) return
    if (!state.infoBarShowCpu && !state.infoBarShowBattery && !state.infoBarShowTemp) return

    var expanded by remember(state.infoBarPinned) { mutableStateOf(state.infoBarPinned) }
    var lastInteractionMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showQuickSheet by remember { mutableStateOf(false) }
    var stats by remember { mutableStateOf(SideInfoBarStats()) }

    val isVisibleBar = state.infoBarPinned || expanded

    // Auto-collapse after 4s idle when not pinned
    LaunchedEffect(expanded, state.infoBarPinned, lastInteractionMs, showQuickSheet) {
        if (expanded && !state.infoBarPinned && !showQuickSheet) {
            delay(4000L)
            expanded = false
        }
    }

    // Lifecycle-aware polling ONLY while expanded/pinned AND STARTED
    LaunchedEffect(isVisibleBar, state.infoBarTempSourceCpu) {
        if (!isVisibleBar) return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            DarkNightSystemStats.observeStats(context, state.infoBarTempSourceCpu).collect {
                stats = it
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        val yFraction = (state.infoBarVerticalPercent / 100f).coerceIn(0.10f, 0.72f)
        val topOffset = (maxHeight * yFraction).coerceAtLeast(16.dp)

        // Collapsed 7x64dp edge handle
        if (!isVisibleBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = topOffset)
                    .systemGestureExclusion()
                    .width(22.dp)
                    .height(76.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount < -10f) {
                                lastInteractionMs = System.currentTimeMillis()
                                expanded = true
                            }
                        }
                    }
                    .clickable {
                        lastInteractionMs = System.currentTimeMillis()
                        expanded = true
                    },
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier = Modifier
                        .width(7.dp)
                        .height(64.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                            ambientColor = glass.lite,
                            spotColor = glass.lite,
                        )
                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.65f), glass.lite.copy(alpha = 0.65f))
                            )
                        )
                )
            }
        }

        // Expanded 58dp flush right-edge pill
        AnimatedVisibility(
            visible = isVisibleBar,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = topOffset),
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = if (state.startupAnimEnabled) spring(dampingRatio = 0.8f, stiffness = 420f) else tween(0),
            ) + fadeIn(tween(140)),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(180),
            ) + fadeOut(tween(140)),
        ) {
            val pillShape = RoundedCornerShape(topStart = 29.dp, bottomStart = 29.dp)
            val baseAlpha = (if (glass.isDark) 0.88f else 0.82f) * (state.infoBarOpacityPercent / 100f)
            val hasRealBlur = state.glassOn &&
                backdrop != null &&
                isRenderEffectSupported()

            val blurMod = if (hasRealBlur && backdrop != null) {
                Modifier.textureBlur(
                    backdrop = backdrop,
                    shape = pillShape,
                    blurRadius = 22f,
                    colors = BlurColors(
                        blendColors = listOf(BlendColorEntry(Color.Black.copy(alpha = 0.25f)))
                    )
                )
            } else Modifier

            Column(
                modifier = Modifier
                    .systemGestureExclusion()
                    .width(58.dp)
                    .then(blurMod)
                    .clip(pillShape)
                    .background(Color.Black.copy(alpha = baseAlpha.coerceIn(0.55f, 0.96f)))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(glass.lite.copy(alpha = 0.38f), glass.lite.copy(alpha = 0.06f))
                        ),
                        shape = pillShape,
                    )
                    .pointerInput(state.infoBarPinned) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (!state.infoBarPinned && dragAmount > 14f) {
                                expanded = false
                            }
                        }
                    }
                    .clickable {
                        lastInteractionMs = System.currentTimeMillis()
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.infoBarShowCpu) {
                    InfoRingGauge(
                        progress = stats.cpuRatio,
                        ringColor = Color(0xFFFF5A36),
                        iconRes = R.drawable.ic_dn_cpu,
                        label = stats.cpuLabel,
                        a11yDesc = "CPU speed ${stats.cpuLabel}",
                    )
                }
                if (state.infoBarShowBattery) {
                    val batRatio = if (stats.batteryLevel >= 0) stats.batteryLevel / 100f else 0f
                    val batColor = if (stats.batteryLevel in 0..19) Color(0xFFFF5A36) else Color(0xFF2EE59D)
                    val batIcon = if (stats.isCharging) R.drawable.ic_dn_bolt else R.drawable.ic_dn_battery
                    val batText = if (stats.batteryLevel >= 0) "${stats.batteryLevel}%" else "--"
                    InfoRingGauge(
                        progress = batRatio,
                        ringColor = batColor,
                        iconRes = batIcon,
                        label = batText,
                        a11yDesc = "Battery $batText",
                    )
                }
                if (state.infoBarShowTemp) {
                    val c = stats.tempC(state.infoBarTempSourceCpu)
                    val tempColor = when {
                        c >= 43f -> Color(0xFFFF5A36)
                        c in 1f..30f -> Color(0xFF2EE59D)
                        else -> Color(0xFFFFD60A)
                    }
                    val tempText = stats.tempLabel(state.infoBarTempSourceCpu, state.infoBarUnitF)
                    InfoRingGauge(
                        progress = stats.tempRatio(state.infoBarTempSourceCpu),
                        ringColor = tempColor,
                        iconRes = R.drawable.ic_dn_temp,
                        label = tempText,
                        a11yDesc = "Phone temperature $tempText",
                    )
                }

                // Bottom 36dp White circle gear button -> quick settings sheet
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            lastInteractionMs = System.currentTimeMillis()
                            showQuickSheet = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dn_settings),
                        contentDescription = stringResource(R.string.darknight_infobar_title),
                        tint = Color(0xFF0B1020),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }

    if (showQuickSheet) {
        AlertDialog(
            onDismissRequest = { showQuickSheet = false },
            title = { Text(stringResource(R.string.darknight_infobar_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickToggleRow(stringResource(R.string.darknight_infobar_enable), state.infoBarEnabled) {
                        DarkNightThemeStore.setInfoBarEnabled(context, it)
                        if (!it) showQuickSheet = false
                    }
                    QuickToggleRow(stringResource(R.string.darknight_infobar_pin), state.infoBarPinned) {
                        DarkNightThemeStore.setInfoBarPinned(context, it)
                    }
                    QuickToggleRow(stringResource(R.string.darknight_infobar_show_cpu), state.infoBarShowCpu) {
                        DarkNightThemeStore.setInfoBarRing(context, "cpu", it)
                    }
                    QuickToggleRow(stringResource(R.string.darknight_infobar_show_battery), state.infoBarShowBattery) {
                        DarkNightThemeStore.setInfoBarRing(context, "battery", it)
                    }
                    QuickToggleRow(stringResource(R.string.darknight_infobar_show_temp), state.infoBarShowTemp) {
                        DarkNightThemeStore.setInfoBarRing(context, "temp", it)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showQuickSheet = false
                        onOpenFullSettings()
                    }
                ) {
                    Text(stringResource(R.string.darknight_infobar_open_full))
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickSheet = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        )
    }
}

@Composable
private fun InfoRingGauge(
    progress: Float,
    ringColor: Color,
    iconRes: Int,
    label: String,
    a11yDesc: String,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "ring_gauge_$label",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.semantics { contentDescription = a11yDesc },
    ) {
        Box(
            modifier = Modifier.size(38.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = 3.5.dp.toPx()
                val inset = strokePx / 2f
                val arcSize = Size(size.width - strokePx, size.height - strokePx)
                val topLeft = Offset(inset, inset)
                drawArc(
                    color = Color.White.copy(alpha = 0.16f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
                if (animatedProgress > 0.01f) {
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    )
                }
            }
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp),
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun QuickToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 14.sp)
        DarkNightToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}
