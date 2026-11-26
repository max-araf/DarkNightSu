package com.kazesu.manager.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kazesu.manager.R
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import java.io.File
import kotlin.math.PI
import kotlin.math.sin

enum class DarkNightStatusKind {
    WORKING,
    LIMITED,
    NOT_INSTALLED,
}

private const val MOUNTAIN_FAR_PATH =
    "M0 122L28 114L52 102L70 92L84 102L98 98L120 110L140 82L156 64L168 54L180 66L196 78L208 74L226 92L240 88L255 80L270 94L290 102L336 110V172H0Z"
private const val SNOWCAP_1 = "M168 54L156 64L162 62L166 68L170 62L176 68L180 66Z"
private const val SNOWCAP_2 = "M70 92L62 99L68 97L72 101L78 96Z"
private const val SNOWCAP_3 = "M255 80L249 87L254 85L258 89L263 85Z"
private const val MOUNTAIN_MID_PATH =
    "M0 140L30 130L60 136L100 122L140 134L180 120L220 132L260 124L300 134L336 128V172H0Z"
private const val MOUNTAIN_NEAR_PATH =
    "M0 152Q80 142 170 150T336 148V172H0Z"

private fun buildScaledPath(pathData: String, targetW: Float, targetH: Float, srcW: Float, srcH: Float): Path {
    val raw = PathParser().parsePathString(pathData).toPath()
    val matrix = Matrix().apply { scale(targetW / srcW, targetH / srcH) }
    raw.transform(matrix)
    return raw
}

/**
 * Parametric notched squircle shape with uniform 12dp concentric gap around the bottom-right pill (PART 3.1).
 */
private class DarkNightNotchShape(private val pillW: Dp) : Shape {
    fun buildPath(size: Size, density: Density): Path = with(density) {
        val w = size.width
        val h = size.height
        val ph = 40.dp.toPx()
        val g = 12.dp.toPx()
        val inset = 4.dp.toPx()
        val r = 30.dp.toPx().coerceAtMost(h * 0.25f)
        val rb = 26.dp.toPx().coerceAtMost(h * 0.22f)
        val rl = 24.dp.toPx().coerceAtMost(h * 0.20f)
        val rc = 32.dp.toPx().coerceAtMost(h * 0.28f)
        val nx = (w - inset - pillW.toPx() - g).coerceAtLeast(r + rl + 8f)
        val ny = (h - inset - ph - g).coerceAtLeast(r + rb + 8f)
        val safeRc = rc.coerceAtMost((h - ny - rl).coerceAtLeast(4f))

        Path().apply {
            moveTo(0f, r)
            arcTo(Rect(0f, 0f, 2f * r, 2f * r), 180f, 90f, false)
            lineTo(w - r, 0f)
            arcTo(Rect(w - 2f * r, 0f, w, 2f * r), 270f, 90f, false)
            lineTo(w, ny - rb)
            arcTo(Rect(w - 2f * rb, ny - 2f * rb, w, ny), 0f, 90f, false)
            lineTo(nx + safeRc, ny)
            arcTo(Rect(nx, ny, nx + 2f * safeRc, ny + 2f * safeRc), 270f, -90f, false)
            lineTo(nx, h - rl)
            arcTo(Rect(nx - 2f * rl, h - 2f * rl, nx, h), 0f, 90f, false)
            lineTo(r, h)
            arcTo(Rect(0f, h - 2f * r, 2f * r, h), 90f, 90f, false)
            close()
        }
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(buildPath(size, density))
}

@Composable
private fun rememberStatusAccent(kind: DarkNightStatusKind): Color {
    val glass = LocalDarkNightGlass.current
    val target = when (kind) {
        DarkNightStatusKind.WORKING -> glass.accent
        DarkNightStatusKind.LIMITED -> Color(0xFFFFB84D)
        DarkNightStatusKind.NOT_INSTALLED -> Color(0xFFFF5A36)
    }
    val animated by animateColorAsState(
        targetValue = target,
        animationSpec = tween(300),
        label = "dn_status_accent",
    )
    return animated
}

private fun DrawScope.drawNeonFlowLines(
    a: Color,
    l: Color,
    d: Color,
    dark: Boolean,
    flat: Boolean,
    phase: Float,
) {
    val w = size.width
    val h = size.height
    val dp = density
    val k = if (flat) 0.55f else 1.0f
    val effA = if (!dark && a.luminance() > 0.55f) lerp(a, Color.Black, 0.32f) else a
    val lineBrush = Brush.horizontalGradient(listOf(d, effA, l))

    fun wave(i: Int, yBase: Float, s: Float): Path {
        val sh = sin((phase * 2.0 * PI + i * 0.35).toFloat()) * 12f * dp
        val y = yBase
        return Path().apply {
            moveTo(-10f * dp, y)
            cubicTo(
                w * 0.20f, y - 46f * dp * s + sh,
                w * 0.40f, y + 40f * dp * s - sh,
                w * 0.58f, y - 6f * dp * s,
            )
            cubicTo(
                2f * w * 0.58f - w * 0.40f,
                2f * (y - 6f * dp * s) - (y + 40f * dp * s - sh),
                w * 0.86f, y - 54f * dp * s,
                w + 10f * dp, y - 16f * dp * s,
            )
        }
    }

    // 9 flowing neon waves (kept in bottom half >= 0.50*h so top title stays crystal clear)
    for (i in 0 until 9) {
        drawPath(
            path = wave(i, h * (0.50f + i * 0.05f), 0.62f + i * 0.03f),
            brush = lineBrush,
            alpha = ((if (dark) 0.34f else 0.50f) + i * 0.045f) * k,
            style = Stroke(width = (1.1f + (i % 3) * 0.45f) * dp),
        )
    }

    if (!flat) {
        // 2 glow ribbons using low-end-safe 2-pass alpha stroke (NO maskFilter)
        for ((yb, s, o) in listOf(Triple(0.56f, 0.80f, 1.0f), Triple(0.78f, 0.70f, 0.8f))) {
            val p = wave(9, h * yb, s)
            drawPath(p, l, alpha = 0.16f * o, style = Stroke(14f * dp, cap = StrokeCap.Round))
            drawPath(p, if (dark) Color.White else effA, alpha = 0.90f * o, style = Stroke(2f * dp, cap = StrokeCap.Round))
        }
        // 6 spark dots with halo
        val sparks = listOf(
            0.30f to 0.62f, 0.50f to 0.72f, 0.72f to 0.55f,
            0.88f to 0.40f, 0.12f to 0.80f, 0.62f to 0.86f,
        )
        sparks.forEach { (sx, sy) ->
            val c = Offset(w * sx, h * sy)
            drawCircle(l.copy(alpha = 0.25f), radius = 7f * dp, center = c)
            drawCircle(if (dark) Color.White else effA, radius = 2.2f * dp, center = c)
        }
    }
}

/**
 * PART 3.1 — MATERIAL "Neon Glass" Home Status Card:
 * - Responsive wide (232dp) & compact (<340dp glass sidebar, 214dp) layouts
 * - Own isolated rememberLayerBackdrop() (never merges with sidebar blur)
 * - Concentric 12dp notched squircle + NeonFlow animated waves + flat palette variant
 */
@Composable
fun DarkNightStatusCardMaterial(
    kind: DarkNightStatusKind,
    versionLabel: String,
    pillTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    DarkNightThemeStore.init(context)
    val dnState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val glass = LocalDarkNightGlass.current
    val a = rememberStatusAccent(kind)
    val l = lerp(a, Color.White, 0.38f)
    val d = lerp(a, Color.Black, 0.45f)
    val ad = lerp(a, Color.Black, if (a.luminance() > 0.55f) 0.55f else 0.28f)
    val flat = !dnState.glassOn

    val line1 = when (kind) {
        DarkNightStatusKind.WORKING -> stringResource(R.string.dn_status_mat_working_l1)
        DarkNightStatusKind.LIMITED -> stringResource(R.string.dn_status_mat_limited_l1)
        DarkNightStatusKind.NOT_INSTALLED -> stringResource(R.string.dn_status_mat_notinstalled_l1)
    }
    val line2 = when (kind) {
        DarkNightStatusKind.WORKING -> stringResource(R.string.dn_status_mat_working_l2)
        DarkNightStatusKind.LIMITED -> stringResource(R.string.dn_status_mat_limited_l2)
        DarkNightStatusKind.NOT_INSTALLED -> stringResource(R.string.dn_status_mat_notinstalled_l2)
    }
    val bottomTitle = when (kind) {
        DarkNightStatusKind.WORKING -> stringResource(R.string.dn_status_version_label)
        DarkNightStatusKind.LIMITED -> "Non-GKI"
        DarkNightStatusKind.NOT_INSTALLED -> stringResource(R.string.dn_status_tap_card)
    }
    val bottomSub = when (kind) {
        DarkNightStatusKind.WORKING -> versionLabel
        DarkNightStatusKind.LIMITED -> "no DN kernel"
        DarkNightStatusKind.NOT_INSTALLED -> stringResource(R.string.dn_status_to_install)
    }

    val phase = if (dnState.startupAnimEnabled && !flat) {
        val inf = rememberInfiniteTransition(label = "neon_flow")
        val p by inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
            label = "neon_phase",
        )
        p
    } else {
        0.15f
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(120),
        label = "dn_mat_card_scale",
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val compact = maxWidth < 340.dp
        val cardH = if (compact) 214.dp else 232.dp
        val pillW = if (compact) 124.dp else 152.dp
        val titleSp = if (compact) 27.sp else 34.sp
        val lineH = if (compact) 30.sp else 36.sp

        val cardBackdrop = rememberLayerBackdrop()
        val useRealBlur = !flat && isRenderEffectSupported()
        val notchShape = remember(pillW) { DarkNightNotchShape(pillW) }
        val stageShape = RoundedCornerShape(30.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .height(cardH)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
        ) {
            // 1. Isolated Neon Stage (card's OWN backdrop — never samples sidebar blur)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(stageShape)
                    .layerBackdrop(cardBackdrop),
            ) {
                val w = size.width
                val h = size.height
                val stageBase = if (glass.isDark) Color(0xFF07090F) else Color(0xFFEEF1F9)
                drawRect(stageBase)
                val orbAlpha = when {
                    flat && glass.isDark -> 0.35f
                    flat && !glass.isDark -> 0.25f
                    glass.isDark -> 0.75f
                    else -> 0.50f
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(a.copy(alpha = orbAlpha), Color.Transparent),
                        center = Offset(w * 0.16f, h * 0.28f),
                        radius = w * 0.42f,
                    ),
                    radius = w * 0.42f,
                    center = Offset(w * 0.16f, h * 0.28f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(l.copy(alpha = orbAlpha * 0.85f), Color.Transparent),
                        center = Offset(w * 0.88f, h * 0.86f),
                        radius = w * 0.40f,
                    ),
                    radius = w * 0.40f,
                    center = Offset(w * 0.88f, h * 0.86f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(d.copy(alpha = orbAlpha * 0.75f), Color.Transparent),
                        center = Offset(w * 0.62f, h * 0.06f),
                        radius = w * 0.30f,
                    ),
                    radius = w * 0.30f,
                    center = Offset(w * 0.62f, h * 0.06f),
                )
            }

            // 2. Glass / Flat notched squircle
            val cardFill = when {
                flat && glass.isDark -> Brush.linearGradient(
                    listOf(lerp(a, Color(0xFF0D111A), 0.86f), lerp(a, Color(0xFF0D111A), 0.90f))
                )
                flat && !glass.isDark -> Brush.linearGradient(
                    listOf(lerp(a, Color.White, 0.90f), lerp(a, Color.White, 0.93f))
                )
                glass.isDark -> Brush.linearGradient(
                    colorStops = arrayOf(
                        0.00f to Color.White.copy(alpha = 0.17f),
                        0.70f to Color.White.copy(alpha = 0.05f),
                        1.00f to Color.White.copy(alpha = 0.09f),
                    )
                )
                else -> Brush.linearGradient(
                    colorStops = arrayOf(
                        0.00f to Color.White.copy(alpha = 0.74f),
                        0.70f to Color.White.copy(alpha = 0.38f),
                        1.00f to Color.White.copy(alpha = 0.52f),
                    )
                )
            }

            val blurMod = if (useRealBlur) {
                Modifier.textureBlur(
                    backdrop = cardBackdrop,
                    shape = notchShape,
                    blurRadius = 18f,
                    colors = BlurColors(
                        blendColors = listOf(
                            BlendColorEntry(
                                color = if (glass.isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.24f)
                            )
                        )
                    ),
                )
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (!glass.isDark && !flat) {
                            Modifier.shadow(14.dp, notchShape, ambientColor = ad.copy(alpha = 0.25f), spotColor = ad.copy(alpha = 0.25f))
                        } else {
                            Modifier
                        }
                    )
                    .then(blurMod)
                    .clip(notchShape)
                    .background(cardFill),
            ) {
                // 3. NeonFlow waves with bottom-left DstIn fade mask so version text stays sharp
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
                ) {
                    drawNeonFlowLines(a, l, d, glass.isDark, flat, phase)
                    // Fade out neon lines behind bottom-left text
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Transparent,
                                0.35f to Color.Transparent,
                                1.00f to Color.Black,
                            ),
                            center = Offset(size.width * 0.04f, size.height),
                            radius = size.width * 0.62f,
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                }

                // 4. Rim outline + press feedback
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val p = notchShape.buildPath(size, this)
                    val rimBrush = if (flat) {
                        Brush.linearGradient(listOf(a.copy(alpha = 0.40f), a.copy(alpha = 0.25f)))
                    } else {
                        Brush.linearGradient(
                            colorStops = arrayOf(
                                0.00f to Color.White.copy(alpha = 0.95f),
                                0.45f to Color.White.copy(alpha = 0.16f),
                                1.00f to Color.White.copy(alpha = 0.55f),
                            )
                        )
                    }
                    drawPath(p, brush = rimBrush, style = Stroke(width = if (flat) 1.dp.toPx() else 1.2.dp.toPx()))
                    if (isPressed) drawRect(glass.lite.copy(alpha = 0.16f))
                }

                // 4b. Subtle bottom scrim behind bottom-start text for legibility over NeonFlow waves
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    glass.base.copy(alpha = 0.55f),
                                )
                            )
                        )
                )

                // 5. Card Text Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp, vertical = 20.dp),
                ) {
                    // Top-end brand chip (ring-only on compact sidebar layout)
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .border(1.5.dp, glass.content.copy(alpha = 0.85f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(a),
                            )
                        }
                        if (!compact) {
                            Text(
                                text = "DarkNightSu",
                                color = glass.content,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    // Main 2-line title
                    Column(modifier = Modifier.align(Alignment.TopStart).padding(top = 4.dp)) {
                        Text(
                            text = line1,
                            color = glass.content,
                            fontSize = titleSp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.8).sp,
                            lineHeight = lineH,
                            maxLines = 1,
                        )
                        Text(
                            text = line2,
                            color = if (glass.isDark) l else ad,
                            fontSize = titleSp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.8).sp,
                            lineHeight = lineH,
                            maxLines = 1,
                        )
                    }

                    // Bottom-start small version info
                    Column(modifier = Modifier.align(Alignment.BottomStart)) {
                        Text(
                            text = bottomTitle,
                            color = glass.content.copy(alpha = 0.90f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                        Text(
                            text = bottomSub,
                            color = glass.content.copy(alpha = 0.80f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // 6. Separate Frosted Glass Pill tucked 4dp inset inside the concentric 12dp notch
            val pillShape = RoundedCornerShape(20.dp)
            val pillBlur = if (useRealBlur) {
                Modifier.textureBlur(
                    backdrop = cardBackdrop,
                    shape = pillShape,
                    blurRadius = 18f,
                    colors = BlurColors(
                        blendColors = listOf(
                            BlendColorEntry(
                                color = if (glass.isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.30f)
                            )
                        )
                    ),
                )
            } else {
                Modifier
            }
            val pillBg = when {
                flat -> Brush.linearGradient(listOf(glass.surfaceHi, glass.surfaceHi))
                glass.isDark -> Brush.linearGradient(listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.07f)))
                else -> Brush.linearGradient(listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.50f)))
            }
            val pillBorderColor = when {
                flat -> a.copy(alpha = 0.32f)
                glass.isDark -> Color.White.copy(alpha = 0.30f)
                else -> Color.White
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 4.dp)
                    .width(pillW)
                    .height(40.dp)
                    .then(
                        if (!glass.isDark && !flat) {
                            Modifier.shadow(10.dp, pillShape, ambientColor = ad.copy(alpha = 0.28f), spotColor = ad.copy(alpha = 0.28f))
                        } else {
                            Modifier
                        }
                    )
                    .then(pillBlur)
                    .clip(pillShape)
                    .background(pillBg)
                    .border(1.dp, pillBorderColor, pillShape),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.padding(horizontal = 10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(a),
                    )
                    Text(
                        text = pillTag,
                        color = glass.content,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * PART 3.2 — MIUIX Image Card + DARKNIGHT wordmark + Glowing Crescent Moon (Wide & Compact <340dp).
 */
@Composable
fun DarkNightStatusCardMiuix(
    kind: DarkNightStatusKind,
    versionLabel: String,
    chipLabel: String,
    homeCardImagePath: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val glass = LocalDarkNightGlass.current
    val a = rememberStatusAccent(kind)
    val l = lerp(a, Color.White, 0.38f)

    val upperLabel = when (kind) {
        DarkNightStatusKind.WORKING -> "KERNEL · $versionLabel"
        DarkNightStatusKind.LIMITED -> stringResource(R.string.dn_status_miuix_limited_label)
        DarkNightStatusKind.NOT_INSTALLED -> stringResource(R.string.dn_status_miuix_notinstalled_label)
    }

    val primaryText = if (glass.isDark) Color.White else Color(0xFF0B1020)
    val mutedText = primaryText.copy(alpha = if (glass.isDark) 0.40f else 0.38f)

    val sentence = when (kind) {
        DarkNightStatusKind.WORKING -> buildAnnotatedString {
            withStyle(SpanStyle(color = mutedText, fontWeight = FontWeight.SemiBold)) { append("Your kernel is ") }
            withStyle(SpanStyle(color = primaryText, fontWeight = FontWeight.Bold)) { append("working") }
            withStyle(SpanStyle(color = mutedText, fontWeight = FontWeight.SemiBold)) { append(": root, modules and SuSFS are ") }
            withStyle(SpanStyle(color = primaryText, fontWeight = FontWeight.Bold)) { append("ready") }
            withStyle(SpanStyle(color = a, fontWeight = FontWeight.ExtraBold)) { append(".") }
        }
        DarkNightStatusKind.LIMITED -> buildAnnotatedString {
            withStyle(SpanStyle(color = mutedText, fontWeight = FontWeight.SemiBold)) { append("Root works, but the ") }
            withStyle(SpanStyle(color = primaryText, fontWeight = FontWeight.Bold)) { append("DarkNight kernel") }
            withStyle(SpanStyle(color = mutedText, fontWeight = FontWeight.SemiBold)) { append(" is missing: tap to ") }
            withStyle(SpanStyle(color = primaryText, fontWeight = FontWeight.Bold)) { append("install") }
            withStyle(SpanStyle(color = a, fontWeight = FontWeight.ExtraBold)) { append(".") }
        }
        DarkNightStatusKind.NOT_INSTALLED -> buildAnnotatedString {
            withStyle(SpanStyle(color = primaryText, fontWeight = FontWeight.Bold)) { append("No root") }
            withStyle(SpanStyle(color = mutedText, fontWeight = FontWeight.SemiBold)) { append(" detected: tap here to ") }
            withStyle(SpanStyle(color = primaryText, fontWeight = FontWeight.Bold)) { append("install") }
            withStyle(SpanStyle(color = mutedText, fontWeight = FontWeight.SemiBold)) { append(" the kernel") }
            withStyle(SpanStyle(color = a, fontWeight = FontWeight.ExtraBold)) { append(".") }
        }
    }

    val cardBg = if (glass.isDark) Color(0xFF12151C) else Color.White
    val outerShadowColor = if (glass.isDark) a.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.16f)

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val compact = maxWidth < 340.dp
        val cardRadius = if (compact) 34.dp else 42.dp
        val cardHeight = if (compact) 252.dp else 296.dp
        val panelHeight = if (compact) 128.dp else 172.dp
        val panelTopR = if (compact) 26.dp else 32.dp
        val panelBotR = if (compact) 44.dp else 58.dp
        val moonSize = if (compact) 22.dp else 30.dp
        val wordmarkSp = if (compact) 13.5.sp else 20.sp
        val hairlineW = if (compact) 16.dp else 34.dp
        val wordmarkBot = if (compact) 12.dp else 20.dp
        val labelSp = if (compact) 12.5.sp else 14.sp
        val sentSp = if (compact) 16.sp else 18.5.sp

        val cardShape = RoundedCornerShape(cardRadius)
        val panelShape = RoundedCornerShape(
            topStart = panelTopR,
            topEnd = panelTopR,
            bottomStart = panelBotR,
            bottomEnd = panelBotR,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .height(cardHeight)
                .shadow(
                    elevation = if (glass.isDark) 22.dp else 18.dp,
                    shape = cardShape,
                    ambientColor = outerShadowColor,
                    spotColor = outerShadowColor,
                )
                .clip(cardShape)
                .background(cardBg)
                .then(
                    if (glass.isDark) {
                        Modifier.border(1.dp, Color.White.copy(alpha = 0.08f), cardShape)
                    } else {
                        Modifier.border(5.dp, Color.White, cardShape)
                    }
                )
                .clickable(onClick = onClick)
                .padding(8.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Image Panel
                val hasUserImage = homeCardImagePath.isNotEmpty() && File(homeCardImagePath).exists()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(panelHeight)
                        .clip(panelShape)
                        .drawWithContent {
                            drawContent()
                            drawLine(
                                color = Color.White.copy(alpha = 0.28f),
                                start = Offset(0f, 1.5.dp.toPx()),
                                end = Offset(size.width, 1.5.dp.toPx()),
                                strokeWidth = 3.dp.toPx(),
                            )
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                                    startY = size.height - 26.dp.toPx(),
                                    endY = size.height,
                                )
                            )
                        }
                        .border(1.dp, Color.White.copy(alpha = 0.12f), panelShape),
                ) {
                    if (hasUserImage) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(File(homeCardImagePath)).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        // Subtle bottom scrim ONLY on user photo so DARKNIGHT wordmark stays legible
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f)))
                                )
                        )
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val sx = w / 336f
                            val sy = h / 172f

                            val skyTop = lerp(a, Color.Black, 0.62f)
                            val skyMid = lerp(lerp(a, Color.Black, 0.35f), a, 0.35f)
                            val skyBot = lerp(a, Color.Black, 0.10f)
                            val farColor = lerp(lerp(a, Color.Black, 0.55f), a, 0.28f)
                            val midColor = lerp(a, Color.Black, 0.72f)
                            val nearColor = lerp(a, Color.Black, 0.86f)
                            val mistColor = lerp(a, Color.White, 0.15f)

                            drawRect(
                                brush = Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.00f to skyTop,
                                        0.62f to skyMid,
                                        1.00f to skyBot,
                                    )
                                )
                            )
                            listOf(
                                Triple(44f, 28f, 1.0f), Triple(96f, 46f, 0.8f),
                                Triple(262f, 30f, 1.1f), Triple(302f, 58f, 0.8f),
                                Triple(214f, 20f, 0.8f), Triple(140f, 22f, 0.7f),
                            ).forEach { (cx, cy, r) ->
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.85f),
                                    radius = r * sx.coerceAtLeast(1f),
                                    center = Offset(cx * sx, cy * sy),
                                )
                            }
                            drawPath(buildScaledPath(MOUNTAIN_FAR_PATH, w, h, 336f, 172f), color = farColor)
                            drawPath(buildScaledPath(SNOWCAP_1, w, h, 336f, 172f), color = Color.White.copy(alpha = 0.78f))
                            drawPath(buildScaledPath(SNOWCAP_2, w, h, 336f, 172f), color = Color.White.copy(alpha = 0.70f))
                            drawPath(buildScaledPath(SNOWCAP_3, w, h, 336f, 172f), color = Color.White.copy(alpha = 0.72f))
                            drawPath(buildScaledPath(MOUNTAIN_MID_PATH, w, h, 336f, 172f), color = midColor)
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.00f to mistColor.copy(alpha = 0f),
                                        0.60f to mistColor.copy(alpha = 0.38f),
                                        1.00f to mistColor.copy(alpha = 0f),
                                    ),
                                    startY = 120f * sy,
                                    endY = 154f * sy,
                                ),
                                topLeft = Offset(0f, 120f * sy),
                                size = Size(w, 34f * sy),
                            )
                            drawPath(buildScaledPath(MOUNTAIN_NEAR_PATH, w, h, 336f, 172f), color = nearColor)
                        }
                    }

                    // Top-start status pill
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(1.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(l),
                        )
                        Text(
                            text = chipLabel,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                        )
                    }

                    // Top-end Glowing Crescent Moon (PathOperation.Difference + 2-pass halo)
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 14.dp, end = 16.dp)
                            .size(moonSize),
                    ) {
                        val s = size.width / 40f
                        val cMain = Offset(18f * s, 21f * s)
                        val rMain = 14f * s
                        // 2-pass alpha halo (no maskFilter)
                        drawCircle(a.copy(alpha = 0.18f), radius = rMain + 14f * s, center = cMain)
                        drawCircle(l.copy(alpha = 0.30f), radius = rMain + 6f * s, center = cMain)
                        val p1 = Path().apply {
                            addOval(Rect(cMain.x - rMain, cMain.y - rMain, cMain.x + rMain, cMain.y + rMain))
                        }
                        val cCut = Offset(26f * s, 15f * s)
                        val p2 = Path().apply {
                            addOval(Rect(cCut.x - rMain, cCut.y - rMain, cCut.x + rMain, cCut.y + rMain))
                        }
                        val crescent = Path.combine(PathOperation.Difference, p1, p2)
                        drawPath(crescent, color = Color.White)
                    }

                    // Bottom-center DARKNIGHT Wordmark with side hairlines
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = wordmarkBot),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(hairlineW)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.55f)))
                                )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DARK",
                                style = TextStyle(
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontSize = wordmarkSp,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = 0.36.em,
                                    shadow = Shadow(color = Color.Black.copy(alpha = 0.55f), blurRadius = 10f),
                                ),
                                maxLines = 1,
                            )
                            Text(
                                text = "NIGHT",
                                style = TextStyle(
                                    brush = Brush.horizontalGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color.White,
                                            0.7f to l,
                                            1.0f to a,
                                        )
                                    ),
                                    fontSize = wordmarkSp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.36.em,
                                    shadow = Shadow(color = a.copy(alpha = 0.70f), blurRadius = 8f),
                                ),
                                maxLines = 1,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(hairlineW)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.55f), Color.Transparent))
                                )
                        )
                    }
                }

                // Bottom Text Block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = if (compact) 12.dp else 16.dp),
                ) {
                    Text(
                        text = upperLabel,
                        color = primaryText.copy(alpha = if (glass.isDark) 0.42f else 0.30f),
                        fontSize = labelSp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = sentence,
                        fontSize = sentSp,
                        lineHeight = if (compact) 20.sp else 23.sp,
                        letterSpacing = (-0.3).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
