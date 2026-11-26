package com.kazesu.manager.ui.component.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazesu.manager.R
import com.kazesu.manager.ui.component.DN_LOGO_END_MS
import com.kazesu.manager.ui.component.DN_LOGO_TOTAL_MS
import com.kazesu.manager.ui.component.DarkNightLogo
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

private const val EXIT_DURATION_MS = 320
private const val AURORA_START_MS = 1000f
private const val SPLASH_END_MS = 2400f
private const val SPLASH_MAX_WAIT_MS = 2000L

private fun seg(t: Float, a: Float, b: Float): Float = ((t - a) / (b - a)).coerceIn(0f, 1f)
private fun easeOutCubic(x: Float): Float = 1f - (1f - x).pow(3)
private fun lerpF(a: Float, b: Float, t: Float): Float = a + (b - a) * t

/**
 * "AURORA ORBIT" startup (v8.1: fully palette-coloured).
 *  - The system splash (all API levels) is only the dark background + an EMPTY icon (ic_splash_empty), so the user never
 *    sees a fixed blue logo.
 *  - This composable plays the whole 0..2400ms timeline: logo assembles + two frosted squares orbit 4 directions and
 *    land HOME (0..1000ms), aurora bloom + ring pulse + drifting orbs + contour (1000..2400ms), wordmark letter by
 *    letter, tagline. Everything is derived from dnState.palette.primaryGlow.
 */
@Composable
fun SplashScreen(
    isReady: Boolean,
    minDurationMs: Long = 1000L,
    onDismissed: () -> Unit = {},
) {
    val context = LocalContext.current
    DarkNightThemeStore.init(context)
    val dnState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val currentOnDismissed by rememberUpdatedState(onDismissed)
    // v7.1 FIX kept: always read the LIVE isReady (a captured value stays stale forever).
    val currentIsReady by rememberUpdatedState(isReady)

    var exitTriggered by remember { mutableStateOf(false) }
    var splashDismissed by remember { mutableStateOf(false) }

    val animEnabled = dnState.startupAnimEnabled
    // v8.1: the system splash now shows an EMPTY icon on every API level, so this composable plays the whole
    // 0..2400ms timeline itself (assemble -> orbit -> aurora -> wordmark) in the live PALETTE colour.
    val startMs = if (animEnabled) 0f else SPLASH_END_MS   // animation off: final pose immediately
    val timeline = remember { Animatable(startMs) }
    // v8.1 FIX: splash follows the selected palette (Aura, Royal Violet, Crimson, Forest ...), no more fixed blue.
    val accent = dnState.palette.primaryGlow
    val lite = remember(accent) { lerp(accent, Color.White, 0.38f) }

    LaunchedEffect(Unit) {
        if (animEnabled) {
            val remaining = (SPLASH_END_MS - startMs).roundToInt().coerceAtLeast(1)
            timeline.animateTo(SPLASH_END_MS, tween(durationMillis = remaining, easing = LinearEasing))
            delay(120.milliseconds)
        } else {
            delay(300.milliseconds)
        }
        withTimeoutOrNull(SPLASH_MAX_WAIT_MS) { snapshotFlow { currentIsReady }.first { it } }
        exitTriggered = true
        delay((EXIT_DURATION_MS.toLong() + 20L).milliseconds)
        splashDismissed = true
        currentOnDismissed()
    }

    if (splashDismissed) return

    AnimatedVisibility(
        visible = !exitTriggered,
        enter = fadeIn(tween(120)),
        exit = fadeOut(tween(EXIT_DURATION_MS, easing = FastOutSlowInEasing)) +
            scaleOut(targetScale = 1.04f, animationSpec = tween(EXIT_DURATION_MS, easing = FastOutSlowInEasing)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF06080F))
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center,
        ) {
            // ---- aurora: 3 drifting orbs (full screen)
            Canvas(Modifier.fillMaxSize()) {
                val t = timeline.value
                val w = size.width
                val h = size.height
                val orbs = arrayOf(
                    floatArrayOf(0.22f, 0.30f, 120f, 0f),
                    floatArrayOf(0.80f, 0.42f, 90f, 120f),
                    floatArrayOf(0.35f, 0.74f, 110f, 260f),
                )
                for (o in orbs) {
                    val d = o[3]
                    val op = easeOutCubic(seg(t, AURORA_START_MS + d, AURORA_START_MS + 700f + d)) * 0.55f
                    if (op <= 0.01f) continue
                    val drift = sin((t + d * 5f) / 900f) * 8.dp.toPx()
                    val r = o[2].dp.toPx()
                    val c = Offset(o[0] * w + drift, o[1] * h - drift)
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(accent.copy(alpha = 0.67f * op), Color.Transparent), center = c, radius = r,
                        ),
                        radius = r, center = c,
                    )
                }
            }
            // ---- topographic contour (res/drawable-nodpi/dn_contour.webp, white lines on transparent)
            Image(
                painter = painterResource(R.drawable.dn_contour),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(lite, BlendMode.SrcIn),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val t = timeline.value
                        alpha = 0.16f * easeOutCubic(seg(t, 1100f, 1900f))
                        val sc = 1f + seg(t, AURORA_START_MS, SPLASH_END_MS) * 0.04f
                        scaleX = sc; scaleY = sc
                    },
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
            ) {
                // ---- logo + bloom + ring (drawBehind: may draw outside the 132dp box)
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .drawBehind {
                            val t = timeline.value
                            val bloom = easeOutCubic(seg(t, AURORA_START_MS, 1700f))
                            val fade = if (t > 1900f) lerpF(1f, 0.8f, seg(t, 1900f, SPLASH_END_MS)) else 1f
                            if (bloom > 0.01f) {
                                val r = 210.dp.toPx() * lerpF(0.6f, 1f, bloom)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(accent.copy(alpha = 0.40f * bloom * fade), Color.Transparent),
                                        center = center, radius = r,
                                    ),
                                    radius = r, center = center,
                                )
                            }
                            val pulse = seg(t, 950f, 1650f)
                            if (pulse > 0f && pulse < 1f) {
                                drawCircle(
                                    color = lite.copy(alpha = 0.32f * (1f - pulse)),
                                    radius = 75.dp.toPx() * lerpF(0.92f, 1.7f, easeOutCubic(pulse)),
                                    center = center, style = Stroke(1.5.dp.toPx()),
                                )
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    DarkNightLogo(size = 132.dp, accent = accent, progressMs = timeline.value.coerceIn(0f, DN_LOGO_TOTAL_MS))
                }

                Spacer(Modifier.height(22.dp))

                // ---- wordmark: letter-by-letter (alpha + rise + converge from wide tracking)
                val word = stringResource(R.string.app_name)
                Row(horizontalArrangement = Arrangement.Center) {
                    word.forEachIndexed { i, ch ->
                        Text(
                            text = ch.toString(),
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.graphicsLayer {
                                val t = timeline.value
                                val p = if (!animEnabled) 1f else easeOutCubic(seg(t, 1150f + i * 38f, 1150f + i * 38f + 380f))
                                val track = if (!animEnabled) 1f else easeOutCubic(seg(t, 1150f, 1750f))
                                alpha = p
                                translationY = (1f - p) * 12.dp.toPx()
                                translationX = (i - (word.length - 1) / 2f) * 6.dp.toPx() * (1f - track)
                            },
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.darknight_subtitle),
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.graphicsLayer {
                        val p = if (!animEnabled) 1f else easeOutCubic(seg(timeline.value, 1650f, 2050f))
                        alpha = 0.6f * p
                        translationY = (1f - p) * 8.dp.toPx()
                    },
                )
            }
        }
    }
}
