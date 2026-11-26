package com.kazesu.manager.ui.screen.home
import com.kazesu.manager.ui.theme.DarkNightGlassCard
import com.kazesu.manager.ui.theme.DarkNightLinks
import com.kazesu.manager.ui.theme.DarkNightThemeStore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.kazesu.manager.ui.component.Storefront
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kazesu.manager.KernelVersion
import com.kazesu.manager.Natives
import com.kazesu.manager.R
import com.kazesu.manager.ai.api.AiFeatureBridge
import com.kazesu.manager.ui.component.HomeCardStickerOverlays
import com.kazesu.manager.ui.component.WarningLevel
import com.kazesu.manager.ui.component.dialog.rememberConfirmDialog
import com.kazesu.manager.ui.component.material.ExpressiveScaffold
import com.kazesu.manager.ui.component.material.IconTile
import com.kazesu.manager.ui.component.material.SegmentedColumn
import com.kazesu.manager.ui.component.material.SegmentedListItem
import com.kazesu.manager.ui.component.material.TonalCard
import com.kazesu.manager.ui.component.material.expressiveTopAppBarColors
import com.kazesu.manager.ui.component.rebootlistpopup.RebootListPopup
import com.kazesu.manager.ui.component.statustag.StatusTag
import com.kazesu.manager.ui.component.DarkNightStatusCardMaterial
import com.kazesu.manager.ui.component.DarkNightStatusKind
import com.kazesu.manager.ui.component.TuneHeroCard
import com.kazesu.manager.ui.theme.LocalBackgroundImagePath
import com.kazesu.manager.ui.theme.backgroundAwareColor
import com.kazesu.manager.ui.util.CustomTextKey
import com.kazesu.manager.ui.util.HomeLayout
import com.kazesu.manager.ui.util.customText
import com.kazesu.manager.ui.util.drawPainterCover
import com.kazesu.manager.ui.util.module.LatestVersionInfo
import java.io.File

private val HeroShape = RoundedCornerShape(28.dp)
private val TileShape = RoundedCornerShape(22.dp)

@Composable
fun HomePagerMaterial(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty()

    ExpressiveScaffold(
        containerColor = if (hasCustomBackground) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer,
        topBar = { TopBar(scrollBehavior = scrollBehavior, onOpenThemeStore = actions.onOpenThemeStore) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                state = state,
                actions = actions,
            )
            TuneHeroCard(
                onClick = if (state.isRootAvailable) actions.onOpenTune else actions.onInstallClick,
                locked = !state.isRootAvailable,
            )
            DarkNightToolboxCard(
                onClick = actions.onOpenDarkNightToolbox,
            )

            if (state.showVersionMismatchWarning) {
                WarningCard(
                    customText(CustomTextKey.HOME_WARN_VERSION_SPOOF),
                    onClick = actions.onOpenVersionSpoof,
                )
            }
            if (state.checkUpdateEnabled) {
                UpdateCard(state = state, actions = actions)
            }
            if (!state.hideHomeWarnings) {
                if (state.showManagerPrBuildWarning && state.showFullStatus) {
                    WarningCard(customText(CustomTextKey.HOME_WARN_PR_BUILD), level = WarningLevel.Notice)
                } else if (state.showKernelPrBuildWarning && state.showFullStatus) {
                    WarningCard(customText(CustomTextKey.HOME_WARN_PR_KERNEL), level = WarningLevel.Notice)
                }
                if (state.requiresNewKernel && state.showFullStatus) {
                    WarningCard(
                        customText(
                            if (state.canInstallKernelUpdate) CustomTextKey.HOME_WARN_REQUIRE_KERNEL
                            else CustomTextKey.HOME_WARN_REQUIRE_KERNEL_GKI
                        ),
                        onClick = if (state.canInstallKernelUpdate) actions.onInstallClick else null
                    )
                }
                if (state.requiresNewManager) {
                    WarningCard(
                        customText(CustomTextKey.HOME_WARN_REQUIRE_MANAGER)
                    )
                }
                if (state.showLkmUpdate && state.showFullStatus) {
                    WarningCard(
                        message = customText(CustomTextKey.HOME_WARN_LKM_UPDATE),
                        level = WarningLevel.Notice,
                        onClick = actions.onInstallClick,
                    )
                }
            }
            if (state.showRootWarning) {
                WarningCard(customText(CustomTextKey.HOME_WARN_ROOT_FAILED))
            }
            if (state.isPreviewMode && !state.hideHomeWarnings) {
                WarningCard(customText(CustomTextKey.HOME_WARN_PREVIEW_MODE), level = WarningLevel.Notice)
            }
            if (state.showAiHomeCard && AiFeatureBridge.isAvailable) {
                AiFeatureBridge.HomeCard(onOpenChat = actions.onOpenAi)
            }
            StatsRow(state = state, actions = actions)
            InfoCard(systemInfo = state.systemInfo, showFullStatus = state.showFullStatus)
            SupportLinks(onOpenUrl = actions.onOpenUrl)
            Spacer(
                Modifier.height(
                    bottomInnerPadding + 24.dp + if (!Natives.isFullFeatured())
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp
                )
            )
        }
    }
}

@Composable
private fun UpdateCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    val ciInfo = state.ciLatestVersionInfo
    val ciDialog = rememberConfirmDialog(onConfirm = actions.onCiUpdateClick)
    val ciTitle = customText(CustomTextKey.HOME_UPDATE_NEW_VERSION, ciInfo.versionCode)
    val ciConfirm = stringResource(R.string.module_update)
    val ciContent = ciInfo.changelog
    if (state.checkCiUpdateEnabled && ciInfo.versionCode > state.currentManagerVersionCode) {
        WarningCard(
            message = if (state.ciUpdateBusy) customText(CustomTextKey.HOME_CI_UPDATE_BUSY)
            else customText(CustomTextKey.HOME_CI_UPDATE_AVAILABLE, ciInfo.versionCode),
            level = WarningLevel.Notice,
            onClick = {
                if (!state.ciUpdateBusy) ciDialog.showConfirm(
                    title = ciTitle, content = ciContent, confirm = ciConfirm, markdown = true,
                )
            },
        )
    }
    val newVersion = state.latestVersionInfo
    val title = stringResource(id = R.string.module_changelog)
    val updateText = stringResource(id = R.string.module_update)

    AnimatedVisibility(
        visible = state.hasUpdate,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val updateDialog = rememberConfirmDialog(onConfirm = { actions.onReleaseUpdateClick() })
        WarningCard(
            message = customText(CustomTextKey.HOME_UPDATE_NEW_VERSION, newVersion.versionCode),
            level = WarningLevel.Notice
        ) {
            if (state.ciUpdateBusy) return@WarningCard
            if (newVersion.changelog.isEmpty()) {
                actions.onReleaseUpdateClick()
            } else {
                updateDialog.showConfirm(
                    title = title,
                    content = newVersion.changelog,
                    markdown = true,
                    confirm = updateText
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onOpenThemeStore: () -> Unit = {},
) {
    val hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty()
    val displayTitle = customText(CustomTextKey.HOME_TITLE)
    LargeFlexibleTopAppBar(
        title = {
            Text(
                text = displayTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenThemeStore) {
                Icon(
                    imageVector = Icons.Rounded.Storefront,
                    contentDescription = customText(CustomTextKey.PAGE_THEME_STORE),
                )
            }
        },
        actions = { RebootListPopup() },
        colors = expressiveTopAppBarColors(
            containerColor = if (hasCustomBackground) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer,
            scrolledContainerColor = if (hasCustomBackground) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer,
        ),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun StatusCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    val ksuActive = state.ksuVersion != null
    val onClick = {
        if (!state.isLateLoadMode) {
            actions.onInstallClick()
        }
    }
    val hasCustomCard = state.homeLayout == HomeLayout.IMMERSIVE ||
        state.homeCardImagePath.isNotEmpty() ||
        state.homeCardStickers.isNotEmpty()
    if (!hasCustomCard) {
        val kind = when {
            ksuActive -> DarkNightStatusKind.WORKING
            state.isRootAvailable || state.isPreviewMode -> DarkNightStatusKind.LIMITED
            else -> DarkNightStatusKind.NOT_INSTALLED
        }
        val ver = if (ksuActive) {
            "${state.ksuVersion}-${state.kernelUAPIVersion ?: state.managerUAPIVersion}"
        } else {
            state.kernelVersion.toString()
        }
        val pillTag = when (kind) {
            DarkNightStatusKind.WORKING -> state.workingModeLabel.ifBlank { if (state.lkmMode == true) "LKM · GKI" else "Non-GKI" }
            DarkNightStatusKind.LIMITED -> "SuSFS"
            DarkNightStatusKind.NOT_INSTALLED -> "Install"
        }
        DarkNightStatusCardMaterial(
            kind = kind,
            versionLabel = ver,
            pillTag = pillTag,
            onClick = onClick,
        )
    } else if (ksuActive && state.homeLayout == HomeLayout.IMMERSIVE) {
        ImmersiveStatusCardMaterial(state = state, onClick = onClick)
    } else if (ksuActive) {
        ActiveStatusCard(state = state, onClick = onClick)
    } else {
        InactiveStatusCard(
            state = state,
            actions = actions,
            notInstalled = state.kernelVersion.isSupported(),
            onClick = onClick,
        )
    }
}

@Composable
private fun ActiveStatusCard(
    state: HomeUiState,
    onClick: () -> Unit,
) {
    val containerColor = backgroundAwareColor(MaterialTheme.colorScheme.primary)
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val workingMode = state.workingModeLabel

    val cardImagePainter = state.homeCardImagePath.takeIf { it.isNotEmpty() }?.let { path ->
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(File(path))
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Crop,
        )
    }

    val hideCardDecoration = state.homeCardImagePath.isNotEmpty() && state.hideHomeCardDecoration
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HeroShape)
            .then(
                if (cardImagePainter != null) {
                    Modifier.drawPainterCover(cardImagePainter)
                } else {
                    Modifier
                }
            ),
        color = if (cardImagePainter != null) Color.Transparent else containerColor,
        contentColor = contentColor,
        shape = HeroShape,
        onClick = onClick,
    ) {
        BoxWithConstraints(
            modifier = if (hideCardDecoration) {
                Modifier
            } else {
                Modifier.drawBehind { drawStatusRings(contentColor.copy(alpha = 0.18f)) }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 188.dp)
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (workingMode.isNotEmpty()) {
                        StatusTag(
                            label = workingMode,
                            contentColor = containerColor,
                            backgroundColor = contentColor,
                        )
                    }
                    if (state.showCustomLkmBadge) {
                        StatusTag(
                            label = customText(CustomTextKey.HOME_TAG_LKM),
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        )
                    }
                    if (state.isSafeMode) {
                        StatusTag(
                            label = customText(CustomTextKey.HOME_TAG_SAFEMODE),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            backgroundColor = MaterialTheme.colorScheme.errorContainer,
                        )
                    }
                    if (state.isLateLoadMode) {
                        StatusTag(
                            label = stringResource(id = R.string.jailbreak_mode),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            backgroundColor = MaterialTheme.colorScheme.errorContainer,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!(state.homeCardImagePath.isNotEmpty() && state.hideHomeCardStatusText)) {
                        Text(
                            text = customText(CustomTextKey.HOME_STATUS_WORKING),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = stringResource(R.string.home_working_version, "${state.ksuVersion}-${state.kernelUAPIVersion}"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
            HomeCardStickerOverlays(
                stickers = state.homeCardStickers,
                cardWidth = maxWidth,
            )
        }
    }
}

@Composable
private fun InactiveStatusCard(
    state: HomeUiState,
    actions: HomeActions,
    notInstalled: Boolean,
    onClick: () -> Unit,
) {
    val isLimitedMode = state.isRootAvailable && state.ksuVersion == null
    val outlineColor = if (isLimitedMode) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline
    val accentColor = if (isLimitedMode) Color(0xFFF59E0B) else MaterialTheme.colorScheme.error

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundAwareColor(MaterialTheme.colorScheme.surfaceBright),
        shape = HeroShape,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind { drawDashedBorder(outlineColor) }
                .then(if (isLimitedMode) Modifier else Modifier.heightIn(min = 188.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = if (notInstalled) Icons.Rounded.Warning else Icons.Rounded.Block,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = when {
                        isLimitedMode -> stringResource(R.string.home_limited_mode_summary)
                        notInstalled -> customText(CustomTextKey.HOME_STATUS_INSTALL_HINT)
                        else -> stringResource(R.string.home_unsupported_reason)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentColor,
                )
            }
            Text(
                text = stringResource(
                    when {
                        isLimitedMode -> R.string.home_limited_mode
                        notInstalled -> R.string.home_not_installed
                        else -> R.string.home_unsupported
                    }
                ),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
            )
            if (isLimitedMode) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = { actions.onOpenUrl(DarkNightLinks.KERNEL_INTEGRATION_GUIDE_URL) }) {
                        Text(stringResource(R.string.home_how_to_integrate_kernel), maxLines = 1)
                    }
                    OutlinedButton(onClick = actions.onInstallClick) {
                        Text(customText(CustomTextKey.HOME_BTN_INSTALL), maxLines = 1)
                    }
                }
            } else if (notInstalled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = actions.onInstallClick) {
                        Text(customText(CustomTextKey.HOME_BTN_INSTALL))
                    }
                    if (state.isSELinuxPermissive) {
                        OutlinedButton(onClick = actions.onJailbreakClick) {
                            Text(stringResource(R.string.home_jailbreak))
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStatusRings(color: Color) {
    val center = Offset(size.width - 36.dp.toPx(), 32.dp.toPx())
    val stroke = 2.dp.toPx()
    drawCircle(color = color, radius = 112.dp.toPx(), center = center, style = Stroke(stroke))
    drawCircle(
        color = color,
        radius = 84.dp.toPx(),
        center = center,
        style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 8.dp.toPx()))),
    )
    drawCircle(color = color, radius = 56.dp.toPx(), center = center, style = Stroke(stroke))
    drawCircle(color = color, radius = 18.dp.toPx(), center = center)
}

private fun DrawScope.drawDashedBorder(color: Color) {
    val stroke = 2.dp.toPx()
    val radius = 28.dp.toPx() - stroke / 2
    drawRoundRect(
        color = color,
        topLeft = Offset(stroke / 2, stroke / 2),
        size = size.copy(width = size.width - stroke, height = size.height - stroke),
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))),
    )
}

@Composable
private fun StatsRow(
    state: HomeUiState,
    actions: HomeActions,
) {
    val tiles = buildList {
        state.superuserCount?.let { add(Triple(it, R.string.superuser, actions.onSuperuserClick)) }
        state.moduleCount?.let { add(Triple(it, R.string.module, actions.onModuleClick)) }
        state.kpmCount?.let { add(Triple(it, R.string.kpm_title, null)) }
    }
    if (tiles.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tiles.forEach { (count, label, onClick) ->
            val labelText = when (label) {
                R.string.superuser -> customText(CustomTextKey.HOME_STATS_SUPERUSER)
                R.string.module -> customText(CustomTextKey.HOME_STATS_MODULE)
                else -> stringResource(label)
            }
            StatTile(
                count = count,
                label = labelText,
                onClick = onClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatTile(
    count: Int,
    label: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val content = @Composable {
        Column(
            modifier = Modifier
                .height(88.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = TileShape,
            color = backgroundAwareColor(MaterialTheme.colorScheme.surfaceBright),
            content = content,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = TileShape,
            color = backgroundAwareColor(MaterialTheme.colorScheme.surfaceBright),
            content = content,
        )
    }
}

@Composable
private fun WarningCard(
    message: String,
    level: WarningLevel = WarningLevel.Error,
    onClick: (() -> Unit)? = null
) {
    val glass = com.kazesu.manager.ui.theme.LocalDarkNightGlass.current
    val shape = RoundedCornerShape(18.dp)
    val accent = when (level) {
        WarningLevel.Error -> Color(0xFFFF5A36)
        WarningLevel.Notice -> Color(0xFFFFBE5A)
    }
    val textColor = if (glass.isDark) {
        when (level) {
            WarningLevel.Error -> Color(0xFFFFDDD5)
            WarningLevel.Notice -> Color(0xFFFFF1D4)
        }
    } else {
        when (level) {
            WarningLevel.Error -> Color(0xFF7A1907)
            WarningLevel.Notice -> Color(0xFF5C3B00)
        }
    }
    val fillColors = if (glass.isDark) {
        listOf(accent.copy(alpha = 0.17f), accent.copy(alpha = 0.05f))
    } else {
        listOf(accent.copy(alpha = 0.24f), accent.copy(alpha = 0.10f))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(fillColors))
            .border(1.dp, accent.copy(alpha = if (glass.isDark) 0.36f else 0.48f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
            )
        }
    }
}

@Composable
private fun SupportLinks(
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val learnMoreUrl = stringResource(R.string.home_learn_kernelsu_url)

    SegmentedColumn(modifier = modifier.fillMaxWidth()) {
        if (DarkNightLinks.SHOW_SUPPORT_CARD && DarkNightLinks.SUPPORT_URL.isNotBlank()) {
            item {
                SegmentedListItem(
                    onClick = { onOpenUrl(DarkNightLinks.SUPPORT_URL) },
                headlineContent = { Text(stringResource(R.string.home_support_title)) },
                supportingContent = { Text(stringResource(R.string.home_support_content)) },
                leadingContent = {
                    IconTile(Icons.Filled.VolunteerActivism, stringResource(R.string.home_support_title))
                },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                )
            }
        }
        item {
            SegmentedListItem(
                onClick = { onOpenUrl(learnMoreUrl) },
                headlineContent = { Text(stringResource(R.string.home_learn_kernelsu)) },
                supportingContent = { Text(stringResource(R.string.home_click_to_learn_kernelsu)) },
                leadingContent = {
                    IconTile(Icons.AutoMirrored.Filled.MenuBook, stringResource(R.string.home_learn_kernelsu))
                },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
            )
        }
    }
}

@Composable
private fun SpecSheet(
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = TileShape,
        color = backgroundAwareColor(MaterialTheme.colorScheme.surfaceBright),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 46.dp)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = value,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    systemInfo: SystemInfo,
    modifier: Modifier = Modifier,
    showFullStatus: Boolean = true,
) {
    val selinuxDisplay = when (systemInfo.selinuxStatus) {
        "Enforcing" -> stringResource(R.string.selinux_status_enforcing)
        "Permissive" -> stringResource(R.string.selinux_status_permissive)
        "Disabled" -> stringResource(R.string.selinux_status_disabled)
        else -> stringResource(R.string.selinux_status_unknown)
    }
    val seccompDisplay = when (systemInfo.seccompStatus) {
        -1 -> stringResource(R.string.seccomp_status_not_supported)
        0 -> stringResource(R.string.seccomp_status_disabled)
        1 -> stringResource(R.string.seccomp_status_strict)
        2 -> stringResource(R.string.seccomp_status_filter)
        else -> stringResource(R.string.seccomp_status_unknown)
    }

    val manualHookText = stringResource(R.string.manual_hook)
    val inlineHookText = stringResource(R.string.inline_hook)
    val tracepointHookText = stringResource(R.string.tracepoint_hook)
    val unknownHookText = stringResource(R.string.selinux_status_unknown)
    val susfsInfo = rememberSusfsInfo(manualHookText, inlineHookText)
    val isSusfsSupported = susfsInfo.status == SusfsStatus.Supported
    val hookTypeLabel = rememberHookTypeLabel(manualHookText, inlineHookText, tracepointHookText, unknownHookText)

    val systemRows = buildList {
        add(customText(CustomTextKey.HOME_INFO_MANAGER_VERSION) to systemInfo.managerVersion)
        add(customText(CustomTextKey.HOME_INFO_KERNEL) to systemInfo.kernelVersion)
        if (showFullStatus) {
            if (!systemInfo.kernelFullVersion.isNullOrBlank()) {
                add(customText(CustomTextKey.HOME_INFO_KERNEL_FULL) to systemInfo.kernelFullVersion)
            }
            if (isSusfsSupported) {
                add(stringResource(R.string.home_susfs_version) to susfsInfo.detail)
            } else if (!hookTypeLabel.isNullOrBlank()) {
                add(customText(CustomTextKey.HOME_INFO_HOOK_TYPE) to hookTypeLabel)
            }
            if (!systemInfo.zygiskImplementation.isNullOrBlank()) {
                add(customText(CustomTextKey.HOME_INFO_ZYGISK) to systemInfo.zygiskImplementation)
            }
        }
        add(customText(CustomTextKey.HOME_INFO_DEVICE_MODEL) to systemInfo.deviceModel)
        add(customText(CustomTextKey.HOME_INFO_FINGERPRINT) to systemInfo.fingerprint)
    }
    val securityRows = listOf(
        customText(CustomTextKey.HOME_INFO_SELINUX) to selinuxDisplay,
        customText(CustomTextKey.HOME_INFO_SECCOMP) to seccompDisplay,
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SpecSheet(rows = systemRows)
        SpecSheet(rows = securityRows)
    }
}

@Preview(name = "Activated")
@Composable
private fun StatusCardActivatedPreview() {
    StatusCard(
        state = previewHomeScreenState(ksuVersion = 12345, lkmMode = true),
        actions = HomeActions({}, {})
    )
}

@Preview(name = "Not Activated")
@Composable
private fun StatusCardNotActivatedPreview() {
    StatusCard(state = previewHomeScreenState(ksuVersion = null, lkmMode = null), actions = HomeActions({}, {}))
}

@Preview(name = "Permissive")
@Composable
private fun StatusCardPermissivePreview() {
    StatusCard(
        state = previewHomeScreenState(ksuVersion = null, lkmMode = null, selinuxStatus = "Permissive"),
        actions = HomeActions({}, {})
    )
}

@Preview(name = "Jailbreak")
@Composable
private fun StatusCardJailbreakPreview() {
    StatusCard(
        state = previewHomeScreenState(ksuVersion = 12345, lkmMode = true, isLateLoadMode = true),
        actions = HomeActions({}, {})
    )
}

private val previewSystemInfo = SystemInfo(
    kernelVersion = "6.1.0-android14-0-g1234567",
    managerVersion = "1.0.0 (10000)",
    deviceModel = "Google Pixel 6 Pro",
    kernelFullVersion = "v4.1.2-abc1234@main",
    fingerprint = "google/raven/raven:14/AP1A.240305.019:user/release-keys",
    selinuxStatus = "Enforcing",
    seccompStatus = 2
)

private val previewUriHandler = object : UriHandler {
    override fun openUri(uri: String) {}
}

@Composable
private fun HomeScreenPreviewContent(
    ksuVersion: Int?,
    lkmMode: Boolean?,
    isSafeMode: Boolean = false,
    isLateLoadMode: Boolean = false,
    selinuxStatus: String = "Enforcing",
) {
    CompositionLocalProvider(LocalUriHandler provides previewUriHandler) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val actions = HomeActions({}, {})
            StatusCard(
                state = previewHomeScreenState(
                    ksuVersion = ksuVersion,
                    lkmMode = lkmMode,
                    isSafeMode = isSafeMode,
                    isLateLoadMode = isLateLoadMode,
                    selinuxStatus = selinuxStatus,
                ),
                actions = actions
            )
            InfoCard(previewSystemInfo.copy(selinuxStatus = selinuxStatus), showFullStatus = true)
            SupportLinks(onOpenUrl = {})
        }
    }
}

@Preview(name = "Home Activated", showBackground = true)
@Composable
private fun HomeScreenActivatedPreview() {
    HomeScreenPreviewContent(ksuVersion = 12345, lkmMode = true)
}

@Preview(name = "Home Not Activated", showBackground = true)
@Composable
private fun HomeScreenNotActivatedPreview() {
    HomeScreenPreviewContent(ksuVersion = null, lkmMode = null)
}

@Preview(name = "Home Permissive", showBackground = true)
@Composable
private fun HomeScreenPermissivePreview() {
    HomeScreenPreviewContent(ksuVersion = null, lkmMode = null, selinuxStatus = "Permissive")
}

@Preview(name = "Home Jailbreak", showBackground = true)
@Composable
private fun HomeScreenJailbreakPreview() {
    HomeScreenPreviewContent(ksuVersion = 12345, lkmMode = true, isLateLoadMode = true)
}

private fun previewHomeScreenState(
    ksuVersion: Int?,
    lkmMode: Boolean?,
    isSafeMode: Boolean = false,
    isLateLoadMode: Boolean = false,
    selinuxStatus: String = "Enforcing",
) = HomeUiState(
    kernelVersion = KernelVersion(6, 1, 0),
    ksuVersion = ksuVersion,
    lkmMode = lkmMode,
    isLkmBundled = lkmMode == true,
    isManager = true,
    isManagerPrBuild = false,
    isKernelPrBuild = false,
    requiresNewKernel = false,
    requiresNewManager = false,
    isRootAvailable = ksuVersion != null,
    isSafeMode = isSafeMode,
    isLateLoadMode = isLateLoadMode,
    checkUpdateEnabled = false,
    showFullStatus = true,
    latestVersionInfo = LatestVersionInfo(),
    currentManagerVersionCode = 10000,
    systemInfo = previewSystemInfo.copy(selinuxStatus = selinuxStatus),
    kernelUAPIVersion = 1,
    managerUAPIVersion = 1,
)


@Composable
private fun DarkNightToolboxCard(
    onClick: () -> Unit,
) {
    val palette = DarkNightThemeStore.state.value.palette
    DarkNightGlassCard(
        palette = palette,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DarkNight Toolbox",
                    style = MaterialTheme.typography.titleMedium,
                    color = com.kazesu.manager.ui.theme.LocalDarkNightGlass.current.content,
                )
                Text(
                    text = "Game Profiles • Bypass Charge • Integrity Hub • Live Logs • Rescue",
                    style = MaterialTheme.typography.bodySmall,
                    color = com.kazesu.manager.ui.theme.LocalDarkNightGlass.current.contentSecondary,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_dn_chevron),
                contentDescription = null,
                tint = com.kazesu.manager.ui.theme.LocalDarkNightGlass.current.accentText,
            )
        }
    }
}

@Composable
private fun DarkNightThemeStudioCard(
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DarkNight Gradient Studio & Theme Store",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "10 Aura Gradient Palettes • Online Theme Upload & Download",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        }
    }
}
