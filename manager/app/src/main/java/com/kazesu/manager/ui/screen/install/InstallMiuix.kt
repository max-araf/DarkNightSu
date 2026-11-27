package com.kazesu.manager.ui.screen.install

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazesu.manager.R
import com.kazesu.manager.getKernelVersion
import com.kazesu.manager.ui.component.BootImageExtractorCard
import com.kazesu.manager.ui.component.DarkNightInstallMethodCard
import com.kazesu.manager.ui.component.InstallCollapsibleGlassCard
import com.kazesu.manager.ui.component.InstallPinnedNextBar
import com.kazesu.manager.ui.component.InstallQuickStartCard
import com.kazesu.manager.ui.component.InstallSectionHeader
import com.kazesu.manager.ui.component.RootCompatCheckCard
import com.kazesu.manager.ui.component.dialog.rememberConfirmDialog
import com.kazesu.manager.ui.component.miuix.AppCard
import com.kazesu.manager.ui.component.miuix.AppScaffold
import com.kazesu.manager.ui.kernelFlash.KpmPatchOption
import com.kazesu.manager.ui.theme.LocalBackgroundImagePath
import com.kazesu.manager.ui.theme.LocalEnableBlur
import com.kazesu.manager.ui.util.BlurredBar
import com.kazesu.manager.ui.util.CustomTextKey
import com.kazesu.manager.ui.util.LkmSelection
import com.kazesu.manager.ui.util.RecommendedMethod
import com.kazesu.manager.ui.util.RootCompatChecker
import com.kazesu.manager.ui.util.customText
import com.kazesu.manager.ui.util.isAbDevice
import com.kazesu.manager.ui.util.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.ConvertFile
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.MoveFile
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * @author weishu
 * @date 2024/3/12.
 */
@Composable
internal fun InstallScreenMiuix(
    uiState: InstallUiState,
    actions: InstallScreenActions,
    snackbarHost: SnackbarHostState,
) {
    val enableBlur = LocalEnableBlur.current
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop(enableBlur)
    val blurActive = backdrop != null
    val hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty()
    val barColor = if (blurActive || hasCustomBackground) Color.Transparent else colorScheme.surface
    val compatReport by RootCompatChecker.latest.collectAsStateWithLifecycle()
    val isAbDevice by produceState(initialValue = false) { value = isAbDevice() }
    val isGkiDevice by produceState(initialValue = false) { value = getKernelVersion().isGKI() }

    var otherMethodsExpanded by rememberSaveable { mutableStateOf(false) }
    var toolsExpanded by rememberSaveable { mutableStateOf(false) }

    val confirmDialog = rememberConfirmDialog(
        onConfirm = { actions.onSelectMethod(InstallMethod.DirectInstallToInactiveSlot) }
    )
    val dialogTitle = stringResource(id = android.R.string.dialog_alert_title)
    val dialogContent = stringResource(id = R.string.install_inactive_slot_warning)

    val triggerMethodClick: (InstallMethod) -> Unit = { option ->
        when (option) {
            is InstallMethod.SelectFile -> actions.onSelectBootImage(option)
            is InstallMethod.HorizonKernel -> actions.onSelectBootImage(option)
            is InstallMethod.DownloadFile -> actions.onDownloadFile()
            is InstallMethod.DirectInstall -> actions.onSelectMethod(option)
            is InstallMethod.DirectInstallToInactiveSlot -> confirmDialog.showConfirm(dialogTitle, dialogContent)
        }
    }

    val recommendedOption = remember(uiState.installMethodOptions, compatReport, uiState.isGkiDevice) {
        val wantAk3 = compatReport?.recommended == RecommendedMethod.ANYKERNEL3 || !uiState.isGkiDevice
        if (wantAk3) {
            uiState.installMethodOptions.firstOrNull { it is InstallMethod.HorizonKernel }
        } else {
            uiState.installMethodOptions.firstOrNull { it is InstallMethod.SelectFile }
        } ?: uiState.installMethodOptions.firstOrNull()
    }
    val otherOptions = remember(uiState.installMethodOptions, recommendedOption) {
        uiState.installMethodOptions.filter { it !== recommendedOption }
    }
    val quickRecommendedLabel = compatReport?.let { report ->
        when (report.recommended) {
            RecommendedMethod.LKM_PATCH_FILE -> stringResource(R.string.cmp_method_lkm, report.imageToPatch)
            RecommendedMethod.ANYKERNEL3 -> stringResource(R.string.cmp_method_ak3, "boot.img")
        }
    }

    AppScaffold(
        topBar = {
            TopBar(
                onBack = actions.onBack,
                scrollBehavior = scrollBehavior,
                backdrop = backdrop,
                barColor = barColor,
            )
        },
        popupHost = { },
        snackbarHost = {
            SnackbarHost(
                state = snackbarHost,
                modifier = Modifier.padding(bottom = 86.dp),
            )
        },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(top = 12.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = innerPadding,
                overscrollEffect = null,
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // 1. Check your phone
                        InstallSectionHeader(text = stringResource(R.string.install_step_check))
                        RootCompatCheckCard(
                            rootAvailable = uiState.rootAvailable,
                            isGkiDevice = uiState.isGkiDevice,
                            onShowMessage = actions.onShowMessage,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Quick start card
                        InstallQuickStartCard(
                            report = compatReport,
                            recommendedLabel = quickRecommendedLabel,
                            onStart = { recommendedOption?.let { triggerMethodClick(it) } },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // 2. Install methods
                        InstallSectionHeader(text = stringResource(R.string.install_step_install))
                        Column(
                            modifier = Modifier.selectableGroup(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (recommendedOption != null) {
                                DarkNightInstallMethodCard(
                                    title = stringResource(id = recommendedOption.label),
                                    summary = recommendedOption.summary,
                                    selected = uiState.installMethod?.let { recommendedOption::class == it::class } ?: false,
                                    recommended = true,
                                    icon = miuixMethodIcon(recommendedOption),
                                    onClick = { triggerMethodClick(recommendedOption) },
                                )
                            }
                            if (otherOptions.isNotEmpty()) {
                                val showDirectly = compatReport == null
                                if (showDirectly) {
                                    otherOptions.forEach { option ->
                                        DarkNightInstallMethodCard(
                                            title = stringResource(id = option.label),
                                            summary = option.summary,
                                            selected = uiState.installMethod?.let { option::class == it::class } ?: false,
                                            recommended = false,
                                            icon = miuixMethodIcon(option),
                                            onClick = { triggerMethodClick(option) },
                                        )
                                    }
                                } else {
                                    InstallCollapsibleGlassCard(
                                        title = stringResource(R.string.install_other_methods),
                                        icon = Icons.Filled.Search,
                                        expanded = otherMethodsExpanded,
                                        onToggle = { otherMethodsExpanded = !otherMethodsExpanded },
                                    ) {
                                        otherOptions.forEach { option ->
                                            DarkNightInstallMethodCard(
                                                title = stringResource(id = option.label),
                                                summary = option.summary,
                                                selected = uiState.installMethod?.let { option::class == it::class } ?: false,
                                                recommended = false,
                                                icon = miuixMethodIcon(option),
                                                onClick = { triggerMethodClick(option) },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Backup and tools (collapsible, default collapsed)
                        InstallCollapsibleGlassCard(
                            title = stringResource(R.string.install_tools_title),
                            icon = Icons.Filled.Share,
                            expanded = toolsExpanded,
                            onToggle = { toolsExpanded = !toolsExpanded },
                        ) {
                            BootImageExtractorCard(
                                rootAvailable = uiState.rootAvailable,
                                onShowMessage = actions.onShowMessage,
                                embedded = true,
                            )
                        }

                        // 5. Advanced group (collapsible, default collapsed)
                        InstallCollapsibleGlassCard(
                            title = stringResource(id = R.string.advanced_options),
                            icon = Icons.Filled.Settings,
                            expanded = uiState.advancedOptionsShown,
                            onToggle = actions.onAdvancedOptionsClicked,
                        ) {
                            if (isGkiDevice && uiState.canSelectPartition) {
                                val isDownload = uiState.installMethod is InstallMethod.DownloadFile
                                val partitionItems = if (isDownload) uiState.remoteDisplayPartitions else uiState.displayPartitions
                                val partitionIndex = if (isDownload) uiState.remotePartitionSelectionIndex else uiState.partitionSelectionIndex
                                AppCard(modifier = Modifier.fillMaxWidth()) {
                                    OverlayDropdownPreference(
                                        items = partitionItems,
                                        selectedIndex = partitionIndex,
                                        title = if (isDownload) {
                                            stringResource(R.string.install_select_partition)
                                        } else {
                                            "${stringResource(R.string.install_select_partition)} (${uiState.slotSuffix})"
                                        },
                                        onSelectedIndexChange = actions.onSelectPartition,
                                        startAction = {
                                            Icon(
                                                MiuixIcons.ConvertFile,
                                                tint = colorScheme.onSurface,
                                                modifier = Modifier.padding(end = 12.dp),
                                                contentDescription = null,
                                            )
                                        },
                                    )
                                }
                            }

                            if (uiState.canForceBackup) {
                                AppCard(modifier = Modifier.fillMaxWidth()) {
                                    CheckboxPreference(
                                        title = stringResource(id = R.string.install_force_backup),
                                        checked = uiState.forceBackup,
                                        summary = stringResource(id = R.string.install_force_backup_summary),
                                        onCheckedChange = actions.onSelectForceBackup,
                                    )
                                }
                            }

                            if (isGkiDevice && uiState.installMethod !is InstallMethod.HorizonKernel) {
                                AppCard(modifier = Modifier.fillMaxWidth()) {
                                    BasicComponent(
                                        title = stringResource(id = R.string.install_upload_lkm_file),
                                        summary = (uiState.lkmSelection as? LkmSelection.LkmUri)?.let {
                                            stringResource(
                                                id = R.string.selected_lkm,
                                                it.uri.lastPathSegment ?: "(file)",
                                            )
                                        },
                                        onClick = actions.onUploadLkm,
                                        startAction = {
                                            Icon(
                                                MiuixIcons.MoveFile,
                                                tint = colorScheme.onSurface,
                                                modifier = Modifier.padding(end = 12.dp),
                                                contentDescription = null,
                                            )
                                        },
                                        endActions = {
                                            if (uiState.lkmSelection is LkmSelection.LkmUri) {
                                                IconButton(onClick = actions.onClearLkm) {
                                                    Icon(
                                                        MiuixIcons.Close,
                                                        modifier = Modifier.size(16.dp),
                                                        contentDescription = stringResource(android.R.string.cancel),
                                                        tint = colorScheme.onSurfaceVariantActions,
                                                    )
                                                }
                                            } else {
                                                val layoutDirection = LocalLayoutDirection.current
                                                Icon(
                                                    modifier = Modifier
                                                        .size(width = 10.dp, height = 16.dp)
                                                        .graphicsLayer {
                                                            scaleX = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f
                                                        }
                                                        .align(Alignment.CenterVertically),
                                                    imageVector = MiuixIcons.Basic.ArrowRight,
                                                    contentDescription = null,
                                                    tint = colorScheme.onSurfaceVariantActions,
                                                )
                                            }
                                        },
                                    )
                                }
                            }

                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                OverlayDropdownPreference(
                                    enabled = uiState.canPatchKpm,
                                    title = stringResource(R.string.install_kpm_operation),
                                    summary = if (uiState.canPatchKpm) {
                                        stringResource(R.string.install_kpm_summary)
                                    } else {
                                        stringResource(R.string.install_kpm_needs_arm64, Build.SUPPORTED_ABIS.joinToString())
                                    },
                                    items = listOf(
                                        stringResource(R.string.kpm_follow_kernel_file),
                                        stringResource(R.string.enable_kpm_patch),
                                        stringResource(R.string.enable_kpm_undo_patch),
                                    ),
                                    selectedIndex = KpmPatchOption.entries.indexOf(uiState.kpmPatchOption),
                                    onSelectedIndexChange = { actions.onKpmOptionChange(KpmPatchOption.entries[it]) },
                                )
                                AnimatedVisibility(visible = uiState.canSelectKpmBoot) {
                                    Column {
                                        BasicComponent(
                                            title = stringResource(
                                                if (uiState.rootAvailable) R.string.install_kpm_manual
                                                else R.string.install_kpm_manual_required_title
                                            ),
                                            summary = uiState.kpmBootUri?.lastPathSegment
                                                ?: stringResource(
                                                    when {
                                                        !uiState.rootAvailable -> R.string.install_kpm_manual_required
                                                        uiState.installMethod == null -> R.string.install_kpm_auto_current_boot
                                                        else -> R.string.install_kpm_auto
                                                    }
                                                ),
                                            onClick = actions.onToggleKpmBoot,
                                            endActions = {
                                                Icon(
                                                    if (uiState.kpmBootExpanded) MiuixIcons.ExpandLess else MiuixIcons.ExpandMore,
                                                    contentDescription = stringResource(R.string.expand),
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            },
                                        )
                                        AnimatedVisibility(visible = uiState.kpmBootExpanded) {
                                            Column {
                                                BasicComponent(
                                                    title = stringResource(R.string.select_file),
                                                    summary = uiState.kpmBootUri?.lastPathSegment,
                                                    onClick = actions.onPatchKpmFile,
                                                )
                                                if (uiState.kpmBootUri != null) {
                                                    BasicComponent(
                                                        title = stringResource(R.string.install_kpm_clear),
                                                        onClick = actions.onClearKpmBoot,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                CheckboxPreference(
                                    title = stringResource(id = R.string.allow_shell),
                                    checked = uiState.allowShell,
                                    summary = stringResource(id = R.string.allow_shell_summary),
                                    onCheckedChange = actions.onSelectAllowShell,
                                )
                                CheckboxPreference(
                                    title = stringResource(id = R.string.enable_adb),
                                    checked = uiState.enableAdb,
                                    summary = stringResource(id = R.string.enable_adb_summary),
                                    onCheckedChange = actions.onSelectEnableAdb,
                                )
                                CheckboxPreference(
                                    title = stringResource(id = R.string.install_block_modules),
                                    checked = uiState.blockModulesEnabled,
                                    summary = stringResource(id = R.string.install_block_modules_warning),
                                    onCheckedChange = actions.onSelectBlockModules,
                                )
                                if (uiState.blockModulesEnabled) {
                                    TextField(
                                        value = uiState.blockModules,
                                        onValueChange = actions.onBlockModulesChange,
                                        label = stringResource(id = R.string.install_block_modules_list),
                                        useLabelAsPlaceholder = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        singleLine = true,
                                    )
                                    Text(
                                        text = stringResource(
                                            if (uiState.blockModulesValid) {
                                                R.string.install_block_modules_summary
                                            } else {
                                                R.string.install_block_modules_invalid
                                            }
                                        ),
                                        fontSize = 12.sp,
                                        color = if (uiState.blockModulesValid) {
                                            colorScheme.onSurfaceVariantSummary
                                        } else {
                                            colorScheme.error
                                        },
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    )
                                }
                                CheckboxPreference(
                                    title = stringResource(id = R.string.kernel_spoof),
                                    checked = uiState.spoofEnabled,
                                    summary = stringResource(id = R.string.kernel_spoof_summary),
                                    onCheckedChange = actions.onSpoofEnabledChange,
                                )
                                AnimatedVisibility(
                                    visible = uiState.spoofEnabled,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut(),
                                ) {
                                    Column {
                                        TextField(
                                            value = uiState.spoofRelease,
                                            onValueChange = actions.onSpoofReleaseChange,
                                            label = stringResource(R.string.kernel_spoof_release),
                                            useLabelAsPlaceholder = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            singleLine = true,
                                        )
                                        TextField(
                                            value = uiState.spoofVersion,
                                            onValueChange = actions.onSpoofVersionChange,
                                            label = stringResource(R.string.kernel_spoof_version),
                                            useLabelAsPlaceholder = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            singleLine = true,
                                        )
                                    }
                                }
                            }

                            (uiState.installMethod as? InstallMethod.HorizonKernel)?.let { method ->
                                if (isAbDevice && method.slot != null) {
                                    AppCard(modifier = Modifier.fillMaxWidth()) {
                                        ArrowPreference(
                                            title = stringResource(
                                                id = R.string.selected_slot,
                                                if (method.slot == "a") stringResource(id = R.string.slot_a)
                                                else stringResource(id = R.string.slot_b),
                                            ),
                                            onClick = { actions.onReopenSlotDialog(method) },
                                            startAction = {
                                                Icon(
                                                    Icons.Filled.SdStorage,
                                                    tint = colorScheme.primary,
                                                    modifier = Modifier.padding(end = 16.dp),
                                                    contentDescription = null,
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(104.dp))
                    }
                }
            }

            // 6. Pinned Glass Next Bar
            InstallPinnedNextBar(
                enabled = uiState.canInstall,
                onClick = actions.onNext,
                onDisabledClick = { actions.onShowMessage("Select a method first") },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

private fun miuixMethodIcon(method: InstallMethod): ImageVector = when (method) {
    is InstallMethod.SelectFile -> Icons.Filled.Edit
    is InstallMethod.DownloadFile -> Icons.Filled.Share
    is InstallMethod.DirectInstall -> Icons.Filled.CheckCircle
    is InstallMethod.DirectInstallToInactiveSlot -> Icons.Filled.CheckCircle
    is InstallMethod.HorizonKernel -> Icons.Filled.Info
}

@Composable
private fun TopBar(
    onBack: () -> Unit = {},
    scrollBehavior: ScrollBehavior,
    backdrop: LayerBackdrop?,
    barColor: Color,
) {
    BlurredBar(backdrop) {
        TopAppBar(
            color = barColor,
            title = customText(CustomTextKey.PAGE_INSTALL),
            navigationIcon = {
                IconButton(
                    modifier = Modifier.padding(start = 16.dp),
                    onClick = onBack,
                ) {
                    val layoutDirection = LocalLayoutDirection.current
                    Icon(
                        modifier = Modifier.graphicsLayer {
                            if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                        },
                        imageVector = MiuixIcons.Back,
                        contentDescription = null,
                        tint = colorScheme.onBackground,
                    )
                }
            },
            scrollBehavior = scrollBehavior,
        )
    }
}
