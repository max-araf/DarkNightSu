package com.kazesu.manager.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kazesu.manager.R
import kotlin.math.cos
import kotlin.math.sin

/**
 * Stryker-style onboarding background: deep tinted base + 3 slowly drifting glow orbs + topographic contour lines
 * + one big "bloom" whose position/size spring-animates between onboarding pages. ALL colours come from [accent].
 *
 * @param bloomX,bloomY centre of the bloom as a FRACTION of the screen (see PAGES table in the prompt)
 * @param bloomScale    bloom diameter as a multiple of screen WIDTH (0.95..1.15)
 * @param animate       false => static (reduce-motion / low-end): no drift, no infinite transition
 */
@Composable
fun DarkNightAuroraBackground(
    accent: Color,
    bloomX: Float,
    bloomY: Float,
    bloomScale: Float,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val spec = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 60f) // ~700ms glide
    val bx by animateFloatAsState(bloomX, spec, label = "bloomX")
    val by by animateFloatAsState(bloomY, spec, label = "bloomY")
    val bs by animateFloatAsState(bloomScale, spec, label = "bloomS")
    val drift = if (animate) {
        val inf = rememberInfiniteTransition(label = "aurora")
        inf.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart), label = "drift").value
    } else 0f
    val lite = lerp(accent, Color.White, 0.45f)

    Box(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF050912), lerp(accent, Color.Black, 0.90f))))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // bloom
            val r = bs * w / 2f
            val c = Offset(bx * w, by * h)
            drawCircle(
                brush = Brush.radialGradient(
                    0f to accent.copy(alpha = 0.82f), 0.45f to accent.copy(alpha = 0.33f), 1f to Color.Transparent,
                    center = c, radius = r,
                ),
                radius = r, center = c,
            )
            // 3 drifting orbs
            val ang = (drift * 2f * Math.PI).toFloat()
            val orbs = arrayOf(floatArrayOf(0.20f, 0.22f, 95f, 0f), floatArrayOf(0.82f, 0.34f, 70f, 1f), floatArrayOf(0.30f, 0.70f, 80f, 2f))
            for (o in orbs) {
                val rr = o[2].dp.toPx()
                val oc = Offset(o[0] * w + sin(ang + o[3]) * 9.dp.toPx(), o[1] * h + cos(ang + o[3] * 1.3f) * 9.dp.toPx())
                drawCircle(
                    brush = Brush.radialGradient(listOf(accent.copy(alpha = 0.55f * 0.6f), Color.Transparent), center = oc, radius = rr),
                    radius = rr, center = oc,
                )
            }
        }
        // topographic contour (res/drawable-nodpi/dn_contour.webp)
        Image(
            painter = painterResource(R.drawable.dn_contour),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(lite, BlendMode.SrcIn),
            modifier = Modifier.fillMaxSize().graphicsLayer {
                alpha = 0.15f
                scaleX = 1.05f; scaleY = 1.05f
                translationX = sin(drift * 6.2832f) * 8.dp.toPx()
                translationY = cos(drift * 6.2832f) * 8.dp.toPx()
            },
        )
    }
}

/** Segmented step indicator: active = accent pill (18x4dp), others = 4dp dots white@32%. Width animates. */
@Composable
fun DarkNightStepDots(count: Int, index: Int, accent: Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            val w by animateDpAsState(if (i == index) 18.dp else 4.dp, tween(260), label = "dot$i")
            Box(
                Modifier.padding(end = 5.dp).width(w).height(4.dp)
                    .background(if (i == index) accent else Color.White.copy(alpha = 0.32f), RoundedCornerShape(2.dp))
            )
        }
    }
}
