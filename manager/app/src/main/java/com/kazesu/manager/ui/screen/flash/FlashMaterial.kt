package com.kazesu.manager.ui.screen.flash

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import com.kazesu.manager.ui.util.rememberNavBottom
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.kazesu.manager.R
import com.kazesu.manager.ui.component.KeyEventBlocker
import com.kazesu.manager.ui.component.material.ExpressiveScaffold
import com.kazesu.manager.ui.component.material.SnackBarHost
import com.kazesu.manager.ui.component.material.TopBarBackButton
import com.kazesu.manager.ui.component.material.expressiveTopAppBarColors

@Composable
fun FlashScreenMaterial(
    state: FlashUiState,
    actions: FlashScreenActions,
    snackBarHost: SnackbarHostState,
) {
    val scrollState = rememberScrollState()
    if (state.showJailbreakWarning) {
        JailbreakFlashWarningDialog(
            onConfirm = actions.onConfirmJailbreakWarning,
            onDismiss = actions.onDismissJailbreakWarning,
        )
    }

    ExpressiveScaffold(
        snackbarHost = {
            SnackBarHost(
                hostState = snackBarHost,
                modifier = Modifier.let { if (state.flashingStatus == FlashingStatus.SUCCESS) it else it.safeDrawingPadding() })
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            when (state.flashingStatus) {
                                FlashingStatus.FLASHING -> R.string.flashing
                                FlashingStatus.SUCCESS -> R.string.flash_success
                                FlashingStatus.FAILED -> R.string.flash_failed
                            }
                        )
                    )
                },
                colors = expressiveTopAppBarColors(),
                navigationIcon = {
                    TopBarBackButton(onClick = actions.onBack)
                },
                actions = {
                    IconButton(onClick = actions.onSaveLog) {
                        Icon(Icons.Filled.Save, stringResource(R.string.save_log))
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.flashingStatus == FlashingStatus.SUCCESS) {
                val isRebootAction = state.showRebootAction
                SmallExtendedFloatingActionButton(
                    onClick = {
                        if (!state.actionInProgress) {
                            if (isRebootAction) actions.onReboot() else actions.onClose()
                        }
                    },
                    expanded = !state.actionInProgress,
                    icon = {
                        if (state.actionInProgress) {
                            CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
                        } else if (isRebootAction) {
                            Icon(Icons.Filled.Refresh, null)
                        } else {
                            Icon(Icons.Filled.Close, null)
                        }
                    },
                    text = {
                        Text(
                            stringResource(if (isRebootAction) state.rebootLabelRes else R.string.close)
                        )
                    },
                    modifier = Modifier.padding(bottom = rememberNavBottom() + 16.dp)
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val navBars = WindowInsets.navigationBars.asPaddingValues()
        val captionBar = WindowInsets.captionBar.asPaddingValues()
        KeyEventBlocker {
            it.key == Key.VolumeDown || it.key == Key.VolumeUp
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                )
                .verticalScroll(scrollState)
        ) {
            LaunchedEffect(state.text) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            val navBottom = rememberNavBottom()
            Spacer(Modifier.height(innerPadding.calculateTopPadding()))
            if (state.flashingStatus == FlashingStatus.FAILED) {
                FlashFailureDiagnosisCard(
                    logText = state.text,
                    onSaveLog = actions.onSaveLog,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Text(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(16.dp),
                    text = state.text,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    softWrap = false,
                )
            }
            Spacer(Modifier.height(navBottom + 88.dp))
        }
    }
}

@Composable
internal fun FlashFailureDiagnosisCard(
    logText: String,
    onSaveLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glass = LocalDarkNightGlass.current
    val amber = Color(0xFFF59E0B)
    val message = when (val diag = diagnoseFlashFailure(logText)) {
        is FlashFailureDiag.OldHeader -> stringResource(R.string.flash_diag_header_old, diag.version)
        FlashFailureDiag.WrongFile -> stringResource(R.string.flash_diag_wrong_file)
        FlashFailureDiag.Unknown -> stringResource(R.string.flash_diag_unknown)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(amber.copy(alpha = if (glass.isDark) 0.16f else 0.14f))
            .border(1.dp, amber.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.flash_diag_title),
            color = glass.content,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = message,
            color = glass.content.copy(alpha = 0.90f),
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onSaveLog,
                border = BorderStroke(1.dp, amber.copy(alpha = 0.65f)),
            ) {
                Text(
                    text = stringResource(R.string.save_log),
                    color = glass.content,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
