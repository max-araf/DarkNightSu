package com.kazesu.manager.ui.screen.install

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.kazesu.manager.ui.component.material.ExpressiveScaffold
import com.kazesu.manager.ui.component.material.IconTile
import com.kazesu.manager.ui.component.material.SegmentedCheckboxItem
import com.kazesu.manager.ui.component.material.SegmentedColumn
import com.kazesu.manager.ui.component.material.SegmentedDropdownItem
import com.kazesu.manager.ui.component.material.SegmentedListItem
import com.kazesu.manager.ui.component.material.SegmentedTextField
import com.kazesu.manager.ui.component.material.SnackBarHost
import com.kazesu.manager.ui.component.material.TopBarBackButton
import com.kazesu.manager.ui.component.material.expressiveTopAppBarColors
import com.kazesu.manager.ui.kernelFlash.KpmPatchOption
import com.kazesu.manager.ui.util.CustomTextKey
import com.kazesu.manager.ui.util.LkmSelection
import com.kazesu.manager.ui.util.RecommendedMethod
import com.kazesu.manager.ui.util.RootCompatChecker
import com.kazesu.manager.ui.util.customText
import com.kazesu.manager.ui.util.isAbDevice

/**
 * @author weishu
 * @date 2024/3/12.
 */
@Composable
internal fun InstallScreenMaterial(
    uiState: InstallUiState,
    actions: InstallScreenActions,
    snackBarHost: SnackbarHostState,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    val compatReport by RootCompatChecker.latest.collectAsStateWithLifecycle()

    val isAbDevice by produceState(initialValue = false) { value = isAbDevice() }
    val isGkiDevice by produceState(initialValue = false) { value = getKernelVersion().isGKI() }

    var otherMethodsExpanded by rememberSaveable { mutableStateOf(false) }
    var toolsExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scrollBehavior.state.heightOffset = scrollBehavior.state.heightOffsetLimit
    }

    val confirmDialog = rememberConfirmDialog(
        onConfirm = { actions.onSelectMethod(InstallMethod.DirectInstallToInactiveSlot) },
        onDismiss = null,
    )
    val dialogTitle = stringResource(android.R.string.dialog_alert_title)
    val dialogContent = stringResource(R.string.install_inactive_slot_warning)

    val triggerMethodClick: (InstallMethod) -> Unit = { option ->
        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
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

    ExpressiveScaffold(
        topBar = {
            TopBar(
                onBack = actions.onBack,
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = {
            SnackBarHost(
                hostState = snackBarHost,
                modifier = Modifier
                    .safeDrawingPadding()
                    .padding(bottom = 80.dp),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 1. Check your phone
                InstallSectionHeader(text = stringResource(R.string.install_step_check))
                RootCompatCheckCard(
                    rootAvailable = uiState.rootAvailable,
                    isGkiDevice = uiState.isGkiDevice,
                    onShowMessage = actions.onShowMessage,
                )

                // Quick start card
                InstallQuickStartCard(
                    report = compatReport,
                    recommendedLabel = quickRecommendedLabel,
                    onStart = {
                        recommendedOption?.let { triggerMethodClick(it) }
                    },
                )

                // 2. Install methods
                InstallSectionHeader(text = stringResource(R.string.install_step_install))
                key(uiState.installMethodOptions.size) {
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (recommendedOption != null) {
                            DarkNightInstallMethodCard(
                                title = stringResource(recommendedOption.label),
                                summary = recommendedOption.summary,
                                selected = uiState.installMethod?.let { recommendedOption::class == it::class } ?: false,
                                recommended = true,
                                icon = methodIcon(recommendedOption),
                                onClick = { triggerMethodClick(recommendedOption) },
                            )
                        }
                        if (otherOptions.isNotEmpty()) {
                            val showDirectly = compatReport == null
                            if (showDirectly) {
                                otherOptions.forEach { option ->
                                    DarkNightInstallMethodCard(
                                        title = stringResource(option.label),
                                        summary = option.summary,
                                        selected = uiState.installMethod?.let { option::class == it::class } ?: false,
                                        recommended = false,
                                        icon = methodIcon(option),
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
                                            title = stringResource(option.label),
                                            summary = option.summary,
                                            selected = uiState.installMethod?.let { option::class == it::class } ?: false,
                                            recommended = false,
                                            icon = methodIcon(option),
                                            onClick = { triggerMethodClick(option) },
                                        )
                                    }
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
                    title = stringResource(R.string.advanced_options),
                    icon = Icons.Filled.Settings,
                    expanded = uiState.advancedOptionsShown,
                    onToggle = actions.onAdvancedOptionsClicked,
                ) {
                    val isDownload = uiState.installMethod is InstallMethod.DownloadFile
                    val partitionItems = if (isDownload) uiState.remoteDisplayPartitions else uiState.displayPartitions
                    val partitionIndex = if (isDownload) uiState.remotePartitionSelectionIndex else uiState.partitionSelectionIndex

                    SegmentedColumn(
                        content = buildList {
                            if (isGkiDevice && partitionItems.isNotEmpty() && uiState.installMethod !is InstallMethod.HorizonKernel) add {
                                SegmentedDropdownItem(
                                    enabled = uiState.canSelectPartition,
                                    items = partitionItems,
                                    selectedIndex = partitionIndex,
                                    title = if (isDownload) {
                                        stringResource(R.string.install_select_partition)
                                    } else {
                                        "${stringResource(R.string.install_select_partition)} (${uiState.slotSuffix})"
                                    },
                                    onItemSelected = actions.onSelectPartition,
                                    icon = Icons.Filled.Edit,
                                )
                            }
                            if (uiState.canForceBackup && uiState.installMethod !is InstallMethod.HorizonKernel) add {
                                SegmentedCheckboxItem(
                                    title = stringResource(R.string.install_force_backup),
                                    summary = stringResource(R.string.install_force_backup_summary),
                                    onCheckedChange = actions.onSelectForceBackup,
                                    checked = uiState.forceBackup,
                                )
                            }
                            if (isGkiDevice && uiState.installMethod !is InstallMethod.HorizonKernel) add {
                                SegmentedListItem(
                                    leadingContent = {
                                        IconTile(Icons.AutoMirrored.Filled.DriveFileMove, null)
                                    },
                                    headlineContent = { Text(stringResource(R.string.install_upload_lkm_file)) },
                                    supportingContent = {
                                        (uiState.lkmSelection as? LkmSelection.LkmUri)?.let {
                                            Text(
                                                stringResource(
                                                    R.string.selected_lkm,
                                                    it.uri.lastPathSegment ?: "(file)",
                                                )
                                            )
                                        }
                                    },
                                    trailingContent = {
                                        if (uiState.lkmSelection is LkmSelection.LkmUri) {
                                            IconButton(onClick = actions.onClearLkm) {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    contentDescription = stringResource(android.R.string.cancel),
                                                )
                                            }
                                        } else {
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                                        }
                                    },
                                    onClick = actions.onUploadLkm,
                                )
                            }
                        }
                    )

                    SegmentedColumn {
                        item {
                            SegmentedDropdownItem(
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
                                onItemSelected = { actions.onKpmOptionChange(KpmPatchOption.entries[it]) },
                            )
                        }
                        item(visible = uiState.canSelectKpmBoot) {
                            SegmentedListItem(
                                headlineContent = {
                                    Text(
                                        stringResource(
                                            if (uiState.rootAvailable) R.string.install_kpm_manual
                                            else R.string.install_kpm_manual_required_title
                                        )
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        uiState.kpmBootUri?.lastPathSegment ?: stringResource(
                                            when {
                                                !uiState.rootAvailable -> R.string.install_kpm_manual_required
                                                uiState.installMethod == null -> R.string.install_kpm_auto_current_boot
                                                else -> R.string.install_kpm_auto
                                            }
                                        )
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        Icons.Filled.ExpandMore,
                                        stringResource(R.string.expand),
                                        Modifier.graphicsLayer { rotationZ = if (uiState.kpmBootExpanded) 180f else 0f },
                                    )
                                },
                                onClick = actions.onToggleKpmBoot,
                            )
                        }
                        item(visible = uiState.canSelectKpmBoot && uiState.kpmBootExpanded) {
                            SegmentedListItem(
                                headlineContent = { Text(stringResource(R.string.select_file)) },
                                supportingContent = { uiState.kpmBootUri?.let { Text(it.lastPathSegment.orEmpty()) } },
                                onClick = actions.onPatchKpmFile,
                                trailingContent = {
                                    if (uiState.kpmBootUri != null) {
                                        IconButton(onClick = actions.onClearKpmBoot) {
                                            Icon(Icons.Filled.Close, stringResource(R.string.install_kpm_clear))
                                        }
                                    }
                                },
                            )
                        }
                    }

                    SegmentedColumn {
                        item {
                            SegmentedCheckboxItem(
                                title = stringResource(id = R.string.allow_shell),
                                summary = stringResource(id = R.string.allow_shell_summary),
                                checked = uiState.allowShell,
                                onCheckedChange = actions.onSelectAllowShell,
                            )
                        }
                        item {
                            SegmentedCheckboxItem(
                                title = stringResource(id = R.string.enable_adb),
                                summary = stringResource(id = R.string.enable_adb_summary),
                                checked = uiState.enableAdb,
                                onCheckedChange = actions.onSelectEnableAdb,
                            )
                        }
                        item {
                            SegmentedCheckboxItem(
                                title = stringResource(id = R.string.install_block_modules),
                                summary = stringResource(id = R.string.install_block_modules_warning),
                                checked = uiState.blockModulesEnabled,
                                onCheckedChange = actions.onSelectBlockModules,
                            )
                        }
                        item(visible = uiState.blockModulesEnabled) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                value = uiState.blockModules,
                                onValueChange = actions.onBlockModulesChange,
                                label = { Text(stringResource(id = R.string.install_block_modules_list)) },
                                supportingText = {
                                    Text(
                                        stringResource(
                                            if (uiState.blockModulesValid) {
                                                R.string.install_block_modules_summary
                                            } else {
                                                R.string.install_block_modules_invalid
                                            }
                                        )
                                    )
                                },
                                isError = !uiState.blockModulesValid,
                                singleLine = true,
                            )
                        }
                        item {
                            SegmentedCheckboxItem(
                                title = stringResource(id = R.string.kernel_spoof),
                                summary = stringResource(id = R.string.kernel_spoof_summary),
                                checked = uiState.spoofEnabled,
                                onCheckedChange = actions.onSpoofEnabledChange,
                            )
                        }
                        item(visible = uiState.spoofEnabled) {
                            SegmentedTextField(
                                value = uiState.spoofRelease,
                                onValueChange = actions.onSpoofReleaseChange,
                                label = stringResource(R.string.kernel_spoof_release),
                                singleLine = true,
                            )
                        }
                        item(visible = uiState.spoofEnabled) {
                            SegmentedTextField(
                                value = uiState.spoofVersion,
                                onValueChange = actions.onSpoofVersionChange,
                                label = stringResource(R.string.kernel_spoof_version),
                                singleLine = true,
                            )
                        }
                    }

                    (uiState.installMethod as? InstallMethod.HorizonKernel)?.let { method ->
                        if (isAbDevice && method.slot != null) {
                            SegmentedColumn {
                                item {
                                    SegmentedListItem(
                                        onClick = { actions.onReopenSlotDialog(method) },
                                        leadingContent = { IconTile(Icons.Filled.SdStorage, null) },
                                        headlineContent = {
                                            Text(
                                                stringResource(
                                                    id = R.string.selected_slot,
                                                    if (method.slot == "a") stringResource(id = R.string.slot_a)
                                                    else stringResource(id = R.string.slot_b),
                                                )
                                            )
                                        },
                                        trailingContent = {
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(104.dp))
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

private fun methodIcon(method: InstallMethod): ImageVector = when (method) {
    is InstallMethod.SelectFile -> Icons.Filled.Edit
    is InstallMethod.DownloadFile -> Icons.Filled.Share
    is InstallMethod.DirectInstall -> Icons.Filled.CheckCircle
    is InstallMethod.DirectInstallToInactiveSlot -> Icons.Filled.CheckCircle
    is InstallMethod.HorizonKernel -> Icons.Filled.Info
}

@Composable
private fun TopBar(
    onBack: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    LargeFlexibleTopAppBar(
        title = { Text(customText(CustomTextKey.PAGE_INSTALL)) },
        navigationIcon = {
            TopBarBackButton(onClick = onBack)
        },
        colors = expressiveTopAppBarColors(),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior,
    )
}
