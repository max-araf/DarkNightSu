package com.kazesu.manager.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin

/** Logo timeline (ms). 0..1000 = logo assemble + orbit (same as the system-splash AVD). 1000..1380 = shine sweep. */
const val DN_LOGO_END_MS = 1000f
const val DN_LOGO_TOTAL_MS = 1400f

/** Brand blue = the launcher icon / system splash colour. Splash uses this; in-app logo uses the palette accent. */
val DarkNightBrandBlue = Color(0xFF1E9EF5)

private const val ORBIT_START_MS = 360f
private const val ORBIT_END_MS = 960f      // 4 legs x 150ms
private const val ORBIT_DX = 382.5f        // 1000-unit design space
private const val ORBIT_DY = 402.5f

/** Every colour of the logo, derived from ONE accent (palette.primaryGlow). Mirrors the HTML reference `logoTok(a)`. */
@Immutable
class DarkNightLogoColors(
    val oTop: Color, val oMid: Color, val oBot: Color, val oHi: Color,
    val fMid: Color, val fBot: Color,
    val iTop: Color, val iMid: Color, val iBot: Color, val iStroke: Color,
    val sB: Color, val pB: Color,
)

fun darkNightLogoColors(a: Color): DarkNightLogoColors {
    val hi = a.luminance() > 0.55f
    val mid = if (hi) lerp(a, Color.Black, 0.16f) else a
    return DarkNightLogoColors(
        oTop = lerp(a, Color.White, if (hi) 0.30f else 0.50f),
        oMid = mid,
        oBot = lerp(a, Color.Black, if (hi) 0.50f else 0.58f),
        oHi = lerp(a, Color.White, 0.62f),
        fMid = lerp(a, Color.White, 0.82f),
        fBot = lerp(a, Color.White, 0.46f),
        iTop = lerp(a, Color.White, 0.36f),
        iMid = lerp(mid, Color.Black, 0.04f),
        iBot = lerp(a, Color.Black, 0.52f),
        iStroke = lerp(a, Color.White, 0.22f),
        sB = lerp(a, Color.White, 0.72f),
        pB = lerp(a, Color.White, 0.42f),
    )
}

private fun seg(t: Float, a: Float, b: Float): Float = ((t - a) / (b - a)).coerceIn(0f, 1f)
private fun easeOutCubic(x: Float): Float = 1f - (1f - x).pow(3)
private fun easeOutBack(x: Float): Float {
    val c1 = 1.1f
    val c3 = c1 + 1f
    return 1f + c3 * (x - 1f).pow(3) + c1 * (x - 1f).pow(2)
}
private fun lerpF(a: Float, b: Float, t: Float): Float = a + (b - a) * t

/**
 * THEMED LOGO. Use this EVERYWHERE inside the app (rail badge, drawer header, About hero, DarkTune tile,
 * onboarding, splash). Default = final pose, colours follow the current palette accent live.
 */
@Composable
fun DarkNightLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    accent: Color = LocalDarkNightGlass.current.accent,
    progressMs: Float = DN_LOGO_END_MS,
) {
    val colors = remember(accent) { darkNightLogoColors(accent) }
    Canvas(modifier = modifier.size(size)) {
        drawDarkNightLogo(colors, progressMs)
    }
}

fun DrawScope.drawDarkNightLogo(c: DarkNightLogoColors, progressMs: Float) {
    val s = this.size.minDimension
    val u = s / 1000f
    val t = progressMs.coerceIn(0f, DN_LOGO_TOTAL_MS)
    val pivot = Offset(s / 2f, s / 2f)

    val outerP = seg(t, 0f, 300f)
    val outerOp = easeOutCubic(seg(t, 0f, 180f))
    val outerS = lerpF(0.70f, 1f, easeOutBack(outerP))
    val frameP = seg(t, 80f, 340f)
    val frameS = lerpF(0.88f, 1f, easeOutCubic(frameP))
    val innerP = seg(t, 160f, 420f)
    val innerS = lerpF(0.82f, 1f, easeOutCubic(innerP))

    // ORBIT: pair A (top-right) goes DOWN, LEFT, UP, RIGHT and lands HOME. Pair B = -A (point symmetric).
    val orbitP = seg(t, ORBIT_START_MS, ORBIT_END_MS) * 4f
    val legI = floor(orbitP).toInt().coerceIn(0, 3)
    val legF = FastOutSlowInEasing.transform((orbitP - legI).coerceIn(0f, 1f))
    var ax = 0f
    var ay = 0f
    if (orbitP < 4f) {
        when (legI) {
            0 -> { ax = 0f; ay = ORBIT_DY * legF }
            1 -> { ax = -ORBIT_DX * legF; ay = ORBIT_DY }
            2 -> { ax = -ORBIT_DX; ay = ORBIT_DY * (1f - legF) }
            else -> { ax = -ORBIT_DX * (1f - legF); ay = 0f }
        }
    }
    ax *= u; ay *= u
    val popA = easeOutCubic(seg(t, 220f, 400f))
    val popB = easeOutCubic(seg(t, 250f, 430f))

    val shineP = seg(t, 1000f, 1380f)
    val shineX = lerpF(-500f, 1400f, shineP) * u
    val shineOp = if (shineP <= 0f || shineP >= 1f) 0f else sin(PI.toFloat() * shineP)

    // 1. outer glass squircle
    if (outerOp > 0.01f) {
        withTransform({ scale(outerS, outerS, pivot) }) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(0f to c.oTop, 0.45f to c.oMid, 1f to c.oBot),
                    start = Offset.Zero, end = Offset(s, s),
                ),
                cornerRadius = CornerRadius(85f * u), alpha = outerOp,
            )
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(c.oHi.copy(alpha = 0.80f), Color.Transparent),
                    center = Offset(0.18f * s, 0.15f * s), radius = 0.60f * s,
                ),
                cornerRadius = CornerRadius(85f * u), alpha = outerOp,
            )
        }
    }
    // 2. glass frame
    val frameOp = easeOutCubic(frameP)
    if (frameOp > 0.01f) {
        withTransform({ scale(frameS, frameS, pivot) }) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color.White.copy(alpha = 0.92f),
                        0.60f to c.fMid.copy(alpha = 0.55f),
                        1f to c.fBot.copy(alpha = 0.55f),
                    ),
                    start = Offset(83f * u, 65f * u), end = Offset(917f * u, 936f * u),
                ),
                topLeft = Offset(83f * u, 65f * u), size = Size(834f * u, 871f * u),
                cornerRadius = CornerRadius(72f * u), alpha = frameOp,
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.85f * frameOp),
                topLeft = Offset(83f * u, 65f * u), size = Size(834f * u, 871f * u),
                cornerRadius = CornerRadius(72f * u), style = Stroke(width = 4f * u),
            )
        }
    }
    // 3. inner squircle
    val innerOp = easeOutCubic(innerP)
    if (innerOp > 0.01f) {
        withTransform({ scale(innerS, innerS, pivot) }) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(0f to c.iTop, 0.5f to c.iMid, 1f to c.iBot),
                    start = Offset(189f * u, 176f * u), end = Offset(811f * u, 826f * u),
                ),
                topLeft = Offset(189f * u, 176f * u), size = Size(622f * u, 650f * u),
                cornerRadius = CornerRadius(50f * u), alpha = innerOp,
            )
            drawRoundRect(
                color = c.iStroke.copy(alpha = innerOp),
                topLeft = Offset(189f * u, 176f * u), size = Size(622f * u, 650f * u),
                cornerRadius = CornerRadius(50f * u), style = Stroke(width = 9f * u),
            )
        }
    }
    // 4. the two frosted square pairs (back plate + front square)
    fun DrawScope.pair(tx: Float, ty: Float, op: Float, px: Float, py: Float, pw: Float, ph: Float,
                       sx: Float, sy: Float, sw: Float, sh: Float) {
        if (op <= 0.01f) return
        withTransform({ translate(tx, ty) }) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.55f), c.pB.copy(alpha = 0.45f)),
                    start = Offset(px * u, py * u), end = Offset((px + pw) * u, (py + ph) * u),
                ),
                topLeft = Offset(px * u, py * u), size = Size(pw * u, ph * u),
                cornerRadius = CornerRadius(26f * u), alpha = op,
            )
            drawRoundRect(
                brush = Brush.linearGradient(
                    listOf(Color.White, c.sB),
                    start = Offset(sx * u, sy * u), end = Offset((sx + sw) * u, (sy + sh) * u),
                ),
                topLeft = Offset(sx * u, sy * u), size = Size(sw * u, sh * u),
                cornerRadius = CornerRadius(24f * u), alpha = op,
            )
        }
    }
    pair(ax, ay, popA, 517f, 119f, 349f, 363f, 517f, 196f, 275f, 286f)      // A: top-right
    pair(-ax, -ay, popB, 134f, 524f, 350f, 358f, 209f, 524f, 275f, 281f)    // B: bottom-left
    // 5. shine sweep (clipped to the outer squircle)
    if (shineOp > 0.01f) {
        val clip = Path().apply { addRoundRect(RoundRect(Rect(0f, 0f, s, s), CornerRadius(85f * u))) }
        clipPath(clip) {
            withTransform({ translate(shineX, 0f); rotate(-20f, Offset(0f, s / 2f)) }) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.5f to Color.White.copy(alpha = 0.75f * shineOp),
                            1f to Color.Transparent,
                        ),
                        startX = -120f * u, endX = 120f * u,
                    ),
                    topLeft = Offset(-120f * u, -150f * u), size = Size(240f * u, s + 300f * u),
                )
            }
        }
    }
}
