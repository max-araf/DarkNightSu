package com.kazesu.manager.ui.screen.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazesu.manager.Natives
import com.kazesu.manager.R
import com.kazesu.manager.ui.component.DarkNightAuroraBackground
import com.kazesu.manager.ui.component.DarkNightLogo
import com.kazesu.manager.ui.component.DarkNightStepDots
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import com.kazesu.manager.ui.theme.contrastOn
import com.kazesu.manager.ui.util.rootAvailable

private data class OnboardingBloom(val x: Float, val y: Float, val scale: Float)

private val PageBlooms = listOf(
    OnboardingBloom(0.46f, 0.34f, 0.95f),
    OnboardingBloom(0.52f, 0.28f, 1.05f),
    OnboardingBloom(0.56f, 0.40f, 0.95f),
    OnboardingBloom(0.40f, 0.33f, 1.00f),
    OnboardingBloom(0.50f, 0.46f, 1.15f),
)

/**
 * PART 4 (v8): Stryker-style 5-page full-screen First-Launch Onboarding with dynamic Aurora Bloom,
 * step dots, 3-checkbox responsible-use gate, live Kernel/SuSFS check, and notification permission request.
 */
@Composable
fun DarkNightFirstRunOnboarding(
    visible: Boolean,
    onFinished: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(320)),
        exit = fadeOut(tween(320)),
    ) {
        val context = LocalContext.current
        val haptic = LocalHapticFeedback.current
        DarkNightThemeStore.init(context)
        val dnState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
        val glass = LocalDarkNightGlass.current
        val accent = glass.accent

        var page by remember { mutableIntStateOf(0) }
        var chkWarranty by remember { mutableStateOf(false) }
        var chkBackup by remember { mutableStateOf(false) }
        var chkResponsibility by remember { mutableStateOf(false) }

        val ksuVer = remember { runCatching { Natives.version }.getOrDefault(0) }
        val hasRoot = remember { runCatching { rootAvailable() }.getOrDefault(false) }
        val hasKernel = ksuVer > 0
        var notifGranted by remember {
            mutableStateOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
            )
        }

        val notifLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            notifGranted = granted
            if (page == 3) page = 4
        }

        BackHandler(enabled = visible) {
            if (page > 0) page--
        }

        val bloom = PageBlooms[page.coerceIn(0, PageBlooms.lastIndex)]
        val allChecked = chkWarranty && chkBackup && chkResponsibility
        val ctaEnabled = page != 1 || allChecked

        val ctaScale = remember { Animatable(1f) }
        LaunchedEffect(allChecked, page) {
            if (page == 1 && allChecked) {
                ctaScale.animateTo(1.04f, tween(110))
                ctaScale.animateTo(1f, tween(110))
            }
        }
        val ctaAlpha by animateFloatAsState(
            targetValue = if (ctaEnabled) 1f else 0.35f,
            animationSpec = tween(200),
            label = "onb_cta_alpha",
        )

        val ctaLabel = when (page) {
            0 -> stringResource(R.string.onb_p1_cta)
            1 -> stringResource(R.string.onb_p2_cta)
            2 -> stringResource(R.string.onb_p3_cta)
            3 -> stringResource(R.string.onb_p4_cta)
            else -> stringResource(R.string.onb_p5_cta)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07090F))
                .pointerInput(Unit) {},
        ) {
            DarkNightAuroraBackground(
                accent = accent,
                bloomX = bloom.x,
                bloomY = bloom.y,
                bloomScale = bloom.scale,
                animate = dnState.startupAnimEnabled,
            )

            // Top-left subtle brand header
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DarkNightLogo(size = 22.dp, accent = accent)
                Text(
                    text = "DarkNightSu",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // Bottom-anchored content + footer controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
            ) {
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        (fadeIn(tween(260, easing = FastOutSlowInEasing)) +
                            slideInVertically(tween(260)) { it / 12 }) togetherWith
                            (fadeOut(tween(120)) + slideOutVertically(tween(120)) { -16 })
                    },
                    label = "onb_page_content",
                ) { p ->
                    when (p) {
                        0 -> OnboardingPageBody(
                            icon = Icons.Outlined.Star,
                            tag = stringResource(R.string.onb_p1_tag),
                            title = stringResource(R.string.onb_p1_title),
                            subtitle = stringResource(R.string.onb_p1_sub),
                        )

                        1 -> OnboardingPageBody(
                            icon = Icons.Outlined.Edit,
                            tag = stringResource(R.string.onb_p2_tag),
                            title = stringResource(R.string.onb_p2_title),
                            subtitle = stringResource(R.string.onb_p2_sub),
                        ) {
                            Spacer(Modifier.height(14.dp))
                            OnboardingCheckRow(
                                label = stringResource(R.string.onb_p2_c1),
                                checked = chkWarranty,
                                accent = accent,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    chkWarranty = !chkWarranty
                                },
                            )
                            OnboardingCheckRow(
                                label = stringResource(R.string.onb_p2_c2),
                                checked = chkBackup,
                                accent = accent,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    chkBackup = !chkBackup
                                },
                            )
                            OnboardingCheckRow(
                                label = stringResource(R.string.onb_p2_c3),
                                checked = chkResponsibility,
                                accent = accent,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    chkResponsibility = !chkResponsibility
                                },
                            )
                        }

                        2 -> OnboardingPageBody(
                            icon = Icons.Outlined.Info,
                            tag = stringResource(R.string.onb_p3_tag),
                            title = stringResource(R.string.onb_p3_title),
                            subtitle = stringResource(R.string.onb_p3_sub),
                        ) {
                            Spacer(Modifier.height(14.dp))
                            OnboardingStatusRow(
                                label = stringResource(R.string.onb_p3_r1),
                                value = if (hasKernel) "v$ksuVer" else stringResource(R.string.onb_not_detected),
                                ok = hasKernel,
                                accent = accent,
                            )
                            OnboardingStatusRow(
                                label = stringResource(R.string.onb_p3_r2),
                                value = if (hasKernel) stringResource(R.string.onb_ready) else stringResource(R.string.onb_not_detected),
                                ok = hasKernel,
                                accent = accent,
                            )
                            OnboardingStatusRow(
                                label = stringResource(R.string.onb_p3_r3),
                                value = if (hasRoot) stringResource(R.string.onb_granted) else stringResource(R.string.onb_not_detected),
                                ok = hasRoot,
                                accent = accent,
                            )
                        }

                        3 -> OnboardingPageBody(
                            icon = Icons.Outlined.Lock,
                            tag = stringResource(R.string.onb_p4_tag),
                            title = stringResource(R.string.onb_p4_title),
                            subtitle = stringResource(R.string.onb_p4_sub),
                        ) {
                            Spacer(Modifier.height(14.dp))
                            OnboardingStatusRow(
                                label = stringResource(R.string.onb_p4_r1),
                                value = if (hasRoot) stringResource(R.string.onb_granted) else stringResource(R.string.onb_pending),
                                ok = hasRoot,
                                accent = accent,
                            )
                            OnboardingStatusRow(
                                label = stringResource(R.string.onb_p4_r2),
                                value = stringResource(R.string.onb_ready),
                                ok = true,
                                accent = accent,
                            )
                            OnboardingStatusRow(
                                label = stringResource(R.string.onb_p4_r3),
                                value = if (notifGranted) stringResource(R.string.onb_granted) else stringResource(R.string.onb_allow),
                                ok = notifGranted,
                                accent = accent,
                            )
                        }

                        else -> OnboardingPageBody(
                            icon = Icons.Outlined.CheckCircle,
                            tag = stringResource(R.string.onb_p5_tag),
                            title = stringResource(R.string.onb_p5_title),
                            subtitle = stringResource(R.string.onb_p5_sub),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(44.dp))

                // Footer row: Left = Step Dots, Right = Pill CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DarkNightStepDots(count = 5, index = page, accent = accent)

                    val pillShape = RoundedCornerShape(999.dp)
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = ctaScale.value
                                scaleY = ctaScale.value
                            }
                            .alpha(ctaAlpha)
                            .shadow(
                                elevation = if (ctaEnabled) 14.dp else 0.dp,
                                shape = pillShape,
                                ambientColor = accent.copy(alpha = 0.67f),
                                spotColor = accent.copy(alpha = 0.67f),
                            )
                            .clip(pillShape)
                            .background(accent)
                            .clickable(enabled = ctaEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                when (page) {
                                    3 -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifGranted) {
                                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            page = 4
                                        }
                                    }
                                    4 -> onFinished()
                                    else -> page++
                                }
                            }
                            .padding(horizontal = 22.dp, vertical = 11.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = ctaLabel,
                            color = contrastOn(accent),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageBody(
    icon: ImageVector,
    tag: String,
    title: String,
    subtitle: String,
    extra: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = tag,
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 32.sp,
            letterSpacing = (-0.4).sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.58f),
            fontSize = 12.5.sp,
            lineHeight = 18.sp,
        )
        extra?.invoke()
    }
}

@Composable
private fun OnboardingCheckRow(
    label: String,
    checked: Boolean,
    accent: Color,
    onToggle: () -> Unit,
) {
    val fill = lerp(accent, Color.White, 0.15f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (checked) fill else Color.Transparent)
                .border(
                    width = 1.2.dp,
                    color = if (checked) fill else Color.White.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(5.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF07090F),
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Text(
            text = label,
            color = Color.White.copy(alpha = if (checked) 0.94f else 0.72f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OnboardingStatusRow(
    label: String,
    value: String,
    ok: Boolean,
    accent: Color,
) {
    val fill = lerp(accent, Color.White, 0.15f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (ok) fill else Color.Transparent)
                .border(
                    width = 1.2.dp,
                    color = if (ok) fill else Color.White.copy(alpha = 0.40f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (ok) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF07090F),
                    modifier = Modifier.size(10.dp),
                )
            }
        }
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
