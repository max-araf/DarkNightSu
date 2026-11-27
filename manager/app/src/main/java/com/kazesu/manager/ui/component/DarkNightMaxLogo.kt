package com.kazesu.manager.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kazesu.manager.R

/**
 * MAX logo (contributor card). The background gradient follows the theme accent, the "MAX" letters are
 * ALWAYS white (res/drawable-nodpi/max_logo_text.png is white-on-transparent). 3:2 aspect, never cropped.
 *
 * [accent] = the current palette's main glow colour (e.g. palette.primaryGlow).
 */
@Composable
fun DarkNightMaxLogo(
    accent: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
) {
    val glow by animateColorAsState(accent, tween(500), label = "maxLogoGlow")
    val base = Color(0xFF01030A)
    val dark = lerp(base, glow, 0.10f)
    val mid = lerp(base, glow, 0.62f)
    val end = lerp(glow, Color.White, 0.18f)
    Box(
        modifier = modifier
            .aspectRatio(3f / 2f)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(0f to dark, 0.5f to mid, 1f to end),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                )
            )
    ) {
        Image(
            painter = painterResource(R.drawable.max_logo_text),
            contentDescription = "MAX",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}
