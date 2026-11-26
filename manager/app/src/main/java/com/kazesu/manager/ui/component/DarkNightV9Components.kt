package com.kazesu.manager.ui.component

import android.content.Context
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazesu.manager.R
import com.kazesu.manager.ui.theme.DarkNightGlassCard
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import com.kazesu.manager.ui.theme.contrastOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.kazesu.manager.ui.util.BootImageExtractor
import com.kazesu.manager.ui.util.CompatItem
import com.kazesu.manager.ui.util.CompatOverrides
import com.kazesu.manager.ui.util.CompatReport
import com.kazesu.manager.ui.util.CompatSeverity
import com.kazesu.manager.ui.util.DarkNightShare
import com.kazesu.manager.ui.util.RecommendedMethod
import com.kazesu.manager.ui.util.RootCompatChecker
import com.kazesu.manager.ui.util.RootVerdict
import com.kazesu.manager.ui.util.rememberDarkNightToolsUnlocked
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val VerdictGreen = Color(0xFF3DDC84)
private val VerdictLime = Color(0xFFA4D65E)
private val VerdictAmber = Color(0xFFFFB020)
private val VerdictRed = Color(0xFFFF5A4F)
private val SeverityBlue = Color(0xFF4DA3FF)
private val SeverityOrangeRed = Color(0xFFFF7043)

fun verdictColor(verdict: RootVerdict): Color = when (verdict) {
    RootVerdict.SUPPORTED -> VerdictGreen
    RootVerdict.LIKELY -> VerdictLime
    RootVerdict.RISKY -> VerdictAmber
    RootVerdict.UNSUPPORTED -> VerdictRed
}

fun verdictLabelRes(verdict: RootVerdict): Int = when (verdict) {
    RootVerdict.SUPPORTED -> R.string.cmp_verdict_supported
    RootVerdict.LIKELY -> R.string.cmp_verdict_likely
    RootVerdict.RISKY -> R.string.cmp_verdict_risky
    RootVerdict.UNSUPPORTED -> R.string.cmp_verdict_unsupported
}

fun verdictIcon(verdict: RootVerdict): ImageVector = when (verdict) {
    RootVerdict.SUPPORTED -> Icons.Filled.CheckCircle
    RootVerdict.LIKELY -> Icons.Filled.Info
    RootVerdict.RISKY -> Icons.Filled.Warning
    RootVerdict.UNSUPPORTED -> Icons.Filled.Close
}

private fun severityColor(sev: CompatSeverity): Color = when (sev) {
    CompatSeverity.OK -> VerdictGreen
    CompatSeverity.INFO -> SeverityBlue
    CompatSeverity.WARN -> VerdictAmber
    CompatSeverity.RISK -> SeverityOrangeRed
    CompatSeverity.BLOCK -> VerdictRed
}

private fun severityIcon(sev: CompatSeverity): ImageVector = when (sev) {
    CompatSeverity.OK -> Icons.Filled.CheckCircle
    CompatSeverity.INFO -> Icons.Filled.Info
    CompatSeverity.WARN -> Icons.Filled.Warning
    CompatSeverity.RISK -> Icons.Filled.Warning
    CompatSeverity.BLOCK -> Icons.Filled.Close
}

/**
 * Reusable frosted-glass dialog that adapts cleanly to Material/Miuix & Light/Dark themes.
 */
@Composable
fun GlassDialog(
    title: String,
    onDismissRequest: () -> Unit,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
    actions: @Composable () -> Unit,
) {
    val glass = LocalDarkNightGlass.current
    val shape = RoundedCornerShape(26.dp)
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, glass.outline, shape),
            shape = shape,
            color = glass.surface,
            contentColor = glass.content,
            tonalElevation = 0.dp,
            shadowElevation = 18.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint ?: glass.accentText,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Text(
                        text = title,
                        color = glass.content,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actions()
                }
            }
        }
    }
}

/**
 * Dialog shown when a user without an active DarkNightSu kernel taps a locked tool.
 */
@Composable
fun LockedFeatureDialog(
    onCheckDevice: () -> Unit,
    onDismiss: () -> Unit,
) {
    val glass = LocalDarkNightGlass.current
    GlassDialog(
        title = stringResource(R.string.locked_dialog_title),
        onDismissRequest = onDismiss,
        icon = Icons.Filled.Lock,
        iconTint = glass.accentText,
        content = {
            Text(
                text = stringResource(R.string.locked_dialog_body),
                color = glass.contentSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        },
        actions = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.locked_close), color = glass.contentSecondary)
            }
            Button(
                onClick = {
                    onDismiss()
                    onCheckDevice()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = glass.accent,
                    contentColor = contrastOn(glass.accent),
                ),
            ) {
                Text(text = stringResource(R.string.locked_action), fontWeight = FontWeight.SemiBold)
            }
        },
    )
}

/**
 * Route-level guard screen rendered if a locked route (DarkTune / DarkNight Toolbox) is opened directly.
 */
@Composable
fun LockedFeatureScreen(
    onCheckDevice: () -> Unit,
    onBack: () -> Unit,
) {
    val glass = LocalDarkNightGlass.current
    val themeState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(glass.base)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        DarkNightGlassCard(
            palette = themeState.palette,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(glass.accent.copy(alpha = 0.16f))
                        .border(1.dp, glass.accent.copy(alpha = 0.40f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = glass.accentText,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.locked_dialog_title),
                    color = glass.content,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.locked_dialog_body),
                    color = glass.contentSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, glass.outline),
                    ) {
                        Text(text = stringResource(R.string.locked_close), color = glass.content)
                    }
                    Button(
                        onClick = onCheckDevice,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = glass.accent,
                            contentColor = contrastOn(glass.accent),
                        ),
                    ) {
                        Text(text = stringResource(R.string.locked_action), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Compact verdict chip displayed on Home when the user is not rooted with a DarkNightSu kernel.
 */
@Composable
fun HomeCompatChip(
    report: CompatReport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glass = LocalDarkNightGlass.current
    val color = verdictColor(report.verdict)
    val label = stringResource(verdictLabelRes(report.verdict))
    val shape = RoundedCornerShape(99.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = if (glass.isDark) 0.16f else 0.14f))
            .border(1.dp, color.copy(alpha = 0.45f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = verdictIcon(report.verdict),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.cmp_home_chip, label),
            color = glass.content,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Pre-flash safety guard dialog for RISKY / UNSUPPORTED verdicts.
 * UNSUPPORTED requires both checkbox AND a 5-second countdown before Continue activates.
 */
@Composable
fun BrickGuardDialog(
    report: CompatReport,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val glass = LocalDarkNightGlass.current
    val verdictLabel = stringResource(verdictLabelRes(report.verdict))
    val accent = verdictColor(report.verdict)
    var checked by remember(report) { mutableStateOf(false) }
    var countdown by remember(report) {
        mutableIntStateOf(if (report.verdict == RootVerdict.UNSUPPORTED) 5 else 0)
    }

    LaunchedEffect(report) {
        while (countdown > 0) {
            delay(1000L)
            countdown -= 1
        }
    }

    val canContinue = checked && countdown == 0

    GlassDialog(
        title = stringResource(R.string.cmp_warn_title),
        onDismissRequest = onDismiss,
        icon = Icons.Filled.Warning,
        iconTint = accent,
        content = {
            Text(
                text = stringResource(R.string.cmp_warn_body, verdictLabel),
                color = glass.contentSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { checked = !checked }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = accent,
                        uncheckedColor = glass.contentSecondary,
                    ),
                )
                Text(
                    text = stringResource(R.string.cmp_warn_check),
                    color = glass.content,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        },
        actions = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cmp_warn_cancel), color = glass.contentSecondary)
            }
            Button(
                enabled = canContinue,
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = contrastOn(accent),
                ),
            ) {
                val baseLabel = stringResource(R.string.cmp_warn_continue)
                Text(
                    text = if (countdown > 0) "$baseLabel ($countdown)" else baseLabel,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
    )
}

/**
 * Primary Install screen glass card: Root Compatibility Check (Material & Miuix).
 */
private const val COMPAT_PREFS = "darknight_compat"
private const val KEY_OVR_BOOTLOADER = "cmp_ovr_bootloader"
private const val KEY_OVR_VENDOR = "cmp_ovr_vendor"

fun readSavedCompatOverrides(context: Context, isGki: Boolean? = null): CompatOverrides {
    val prefs = context.getSharedPreferences(COMPAT_PREFS, Context.MODE_PRIVATE)
    return CompatOverrides(
        bootloaderUnlocked = prefs.getBoolean(KEY_OVR_BOOTLOADER, false),
        vendorProtectionOff = prefs.getBoolean(KEY_OVR_VENDOR, false),
        gki = isGki,
    )
}

@Composable
fun RootCompatCheckCard(
    rootAvailable: Boolean,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    isGkiDevice: Boolean? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val glass = LocalDarkNightGlass.current
    val themeState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val report by RootCompatChecker.latest.collectAsStateWithLifecycle()
    val toolsUnlocked = rememberDarkNightToolsUnlocked()

    val prefs = remember(context) { context.getSharedPreferences(COMPAT_PREFS, Context.MODE_PRIVATE) }
    var running by remember { mutableStateOf(false) }
    var deepEnabled by remember { mutableStateOf(false) }
    var overridesExpanded by remember { mutableStateOf(false) }
    var ovrBootloader by remember { mutableStateOf(prefs.getBoolean(KEY_OVR_BOOTLOADER, false)) }
    var ovrVendor by remember { mutableStateOf(prefs.getBoolean(KEY_OVR_VENDOR, false)) }
    var manualRunOnRooted by remember { mutableStateOf(false) }

    LaunchedEffect(rootAvailable) {
        if (!rootAvailable) deepEnabled = false
    }

    DarkNightGlassCard(
        palette = themeState.palette,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(glass.accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = glass.accentText,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.cmp_title),
                        color = glass.content,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.cmp_subtitle),
                        color = glass.contentSecondary,
                        fontSize = 12.5.sp,
                    )
                }
            }

            // Already rooted green glass banner (Part 3)
            if (toolsUnlocked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VerdictGreen.copy(alpha = if (glass.isDark) 0.16f else 0.14f))
                        .border(1.dp, VerdictGreen.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = VerdictGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.cmp_already_rooted),
                        color = glass.content,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Deep check toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.cmp_deep_toggle),
                        color = if (rootAvailable) glass.content else glass.contentSecondary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(
                            if (rootAvailable) R.string.cmp_deep_desc else R.string.cmp_deep_no_root
                        ),
                        color = glass.contentSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
                DarkNightSwitch(
                    checked = deepEnabled && rootAvailable,
                    onCheckedChange = { if (rootAvailable) deepEnabled = it },
                    enabled = rootAvailable && !running,
                )
            }

            // Collapsible "I know my device" overrides (Part 1)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(glass.surfaceHi.copy(alpha = 0.45f))
                    .border(1.dp, glass.outline.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { overridesExpanded = !overridesExpanded }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.cmp_override_title),
                        color = glass.content,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        imageVector = if (overridesExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = glass.contentSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                AnimatedVisibility(visible = overridesExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.cmp_override_desc),
                            color = glass.contentSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.cmp_override_bootloader),
                                color = glass.content,
                                fontSize = 12.5.sp,
                                modifier = Modifier.weight(1f),
                            )
                            DarkNightSwitch(
                                checked = ovrBootloader,
                                onCheckedChange = {
                                    ovrBootloader = it
                                    prefs.edit().putBoolean(KEY_OVR_BOOTLOADER, it).apply()
                                },
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.cmp_override_vendor),
                                color = glass.content,
                                fontSize = 12.5.sp,
                                modifier = Modifier.weight(1f),
                            )
                            DarkNightSwitch(
                                checked = ovrVendor,
                                onCheckedChange = {
                                    ovrVendor = it
                                    prefs.edit().putBoolean(KEY_OVR_VENDOR, it).apply()
                                },
                            )
                        }
                    }
                }
            }

            // Run button
            Button(
                onClick = {
                    if (!running) {
                        scope.launch {
                            running = true
                            manualRunOnRooted = true
                            try {
                                RootCompatChecker.run(
                                    deep = deepEnabled && rootAvailable,
                                    overrides = CompatOverrides(
                                        bootloaderUnlocked = ovrBootloader,
                                        vendorProtectionOff = ovrVendor,
                                        gki = isGkiDevice,
                                    ),
                                )
                            } finally {
                                running = false
                            }
                        }
                    }
                },
                enabled = !running,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = glass.accent,
                    contentColor = contrastOn(glass.accent),
                ),
            ) {
                if (running) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = contrastOn(glass.accent),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.cmp_checking), fontWeight = FontWeight.SemiBold)
                } else {
                    Text(text = stringResource(R.string.cmp_run), fontWeight = FontWeight.SemiBold)
                }
            }

            // Results (when already rooted, only expand detailed verdict after user taps Check)
            val currentReport = report
            AnimatedVisibility(visible = currentReport != null && (!toolsUnlocked || manualRunOnRooted)) {
                if (currentReport != null) {
                    val vColor = verdictColor(currentReport.verdict)
                    val sortedItems = remember(currentReport) {
                        currentReport.items.sortedByDescending { it.severity.penalty }
                    }
                    val copyTitle = stringResource(R.string.cmp_copy_report)
                    val sendTitle = stringResource(R.string.cmp_send_report)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Verdict badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(vColor.copy(alpha = if (glass.isDark) 0.16f else 0.13f))
                                .border(1.dp, vColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = verdictIcon(currentReport.verdict),
                                contentDescription = null,
                                tint = vColor,
                                modifier = Modifier.size(26.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(verdictLabelRes(currentReport.verdict)),
                                    color = vColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                                Text(
                                    text = "${currentReport.deviceName} · ${currentReport.kernelRelease}",
                                    color = glass.contentSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        // Recommended install method line (Part 2)
                        val methodLabel = when (currentReport.recommended) {
                            RecommendedMethod.LKM_PATCH_FILE -> stringResource(R.string.cmp_method_lkm, currentReport.imageToPatch)
                            RecommendedMethod.ANYKERNEL3 -> stringResource(R.string.cmp_method_ak3, "boot.img")
                        }
                        Text(
                            text = stringResource(R.string.cmp_recommended, methodLabel),
                            color = glass.accentText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )

                        // Items sorted by severity descending
                        sortedItems.forEach { item ->
                            CompatItemRow(item = item)
                        }

                        // Disclaimer
                        Text(
                            text = stringResource(R.string.cmp_disclaimer),
                            color = glass.contentSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                        )

                        // Copy / Send buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = {
                                    DarkNightShare.copyText(context, "DarkNightSu report", currentReport.toPlainText())
                                    onShowMessage(copyTitle)
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, glass.outline),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = copyTitle,
                                    color = glass.content,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    DarkNightShare.shareTextToDeveloper(
                                        context = context,
                                        text = currentReport.toPlainText(),
                                        chooserTitle = sendTitle,
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, glass.accent.copy(alpha = 0.50f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = sendTitle,
                                    color = glass.accentText,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompatItemRow(item: CompatItem) {
    val glass = LocalDarkNightGlass.current
    val sColor = severityColor(item.severity)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(glass.surfaceHi.copy(alpha = 0.55f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = severityIcon(item.severity),
            contentDescription = null,
            tint = sColor,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.title,
                color = glass.content,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item.detail,
                color = glass.contentSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

/**
 * Install screen glass card: Raw Boot Image Extractor (init_boot / boot / vendor_boot / recovery).
 */
@Composable
fun BootImageExtractorCard(
    rootAvailable: Boolean,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val glass = LocalDarkNightGlass.current
    val themeState by DarkNightThemeStore.state.collectAsStateWithLifecycle()

    var partitions by remember { mutableStateOf<List<BootImageExtractor.PartitionInfo>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }
    val extracting = remember { mutableStateMapOf<String, Boolean>() }
    val results = remember { mutableStateMapOf<String, BootImageExtractor.ExtractResult>() }

    LaunchedEffect(rootAvailable) {
        if (rootAvailable) {
            partitions = BootImageExtractor.listPartitions()
            loaded = true
        } else {
            partitions = emptyList()
            loaded = false
        }
    }

    DarkNightGlassCard(
        palette = themeState.palette,
        modifier = modifier
            .fillMaxWidth()
            .then(if (!rootAvailable) Modifier.alpha(0.60f) else Modifier),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(glass.accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (rootAvailable) Icons.Filled.Share else Icons.Filled.Lock,
                        contentDescription = null,
                        tint = glass.accentText,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ext_title),
                        color = glass.content,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(if (rootAvailable) R.string.ext_desc else R.string.ext_need_root),
                        color = glass.contentSecondary,
                        fontSize = 12.5.sp,
                    )
                }
            }

            if (!rootAvailable) {
                Text(
                    text = stringResource(R.string.ext_nonroot_tip),
                    color = glass.contentSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.ext_extract))
                }
            } else if (loaded && partitions.isEmpty()) {
                Text(
                    text = stringResource(R.string.ext_no_partitions),
                    color = glass.contentSecondary,
                    fontSize = 13.sp,
                )
            } else {
                val shareTitle = stringResource(R.string.ext_share)
                partitions.forEach { part ->
                    val isBusy = extracting[part.name] == true
                    val res = results[part.name]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(glass.surfaceHi.copy(alpha = 0.55f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = part.name,
                                    color = glass.content,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                )
                                if (part.sizeBytes > 0L) {
                                    Text(
                                        text = Formatter.formatShortFileSize(context, part.sizeBytes),
                                        color = glass.contentSecondary,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    if (!isBusy) {
                                        scope.launch {
                                            extracting[part.name] = true
                                            try {
                                                results[part.name] = BootImageExtractor.extract(context, part)
                                            } finally {
                                                extracting[part.name] = false
                                            }
                                        }
                                    }
                                },
                                enabled = !isBusy,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = glass.accent,
                                    contentColor = contrastOn(glass.accent),
                                ),
                            ) {
                                if (isBusy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = contrastOn(glass.accent),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(text = stringResource(R.string.ext_extracting), fontSize = 12.sp)
                                } else {
                                    Text(text = stringResource(R.string.ext_extract), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (res != null) {
                            if (res.success) {
                                res.savedAs?.let { saved ->
                                    Text(
                                        text = stringResource(R.string.ext_done, saved),
                                        color = VerdictGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                res.sha256?.let { sha ->
                                    val shaText = stringResource(R.string.ext_sha, sha)
                                    Text(
                                        text = shaText,
                                        color = glass.contentSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.clickable {
                                            DarkNightShare.copyText(context, "${part.name} SHA-256", sha)
                                            onShowMessage("SHA-256 copied")
                                        },
                                    )
                                }
                                if (res.magic != null && !BootImageExtractor.magicLooksValid(res.magic)) {
                                    Text(
                                        text = stringResource(R.string.ext_magic_bad),
                                        color = VerdictAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                val uri = res.uri
                                if (uri != null) {
                                    OutlinedButton(
                                        onClick = { DarkNightShare.shareFile(context, uri, shareTitle) },
                                        border = BorderStroke(1.dp, glass.accent.copy(alpha = 0.50f)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = null,
                                            tint = glass.accentText,
                                            modifier = Modifier.size(15.dp),
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(text = shareTitle, color = glass.accentText, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.ext_failed, res.error ?: "unknown"),
                                    color = VerdictRed,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
