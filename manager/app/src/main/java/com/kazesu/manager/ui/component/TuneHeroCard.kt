package com.kazesu.manager.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazesu.manager.R
import com.kazesu.manager.ui.LocalUiMode
import com.kazesu.manager.ui.UiMode
import com.kazesu.manager.ui.component.material.TonalCard
import com.kazesu.manager.ui.component.miuix.AppCard
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles

/**
 * Home hero card for KazeTune. Tapping opens the tuning page when root is
 * available; otherwise the preview is dimmed and tapping routes to the
 * installer, matching the locked teaser used by the legacy OriginTune card.
 */
@Composable
fun TuneHeroCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
) {
    when (LocalUiMode.current) {
        UiMode.Material -> TuneHeroCardMaterial(onClick = onClick, modifier = modifier, locked = locked)
        UiMode.Miuix -> TuneHeroCardMiuix(onClick = onClick, modifier = modifier, locked = locked)
    }
}

@Composable
private fun TuneHeroCardMaterial(
    onClick: () -> Unit,
    modifier: Modifier,
    locked: Boolean,
) {
    TonalCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (locked) Modifier.alpha(0.55f) else Modifier),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.kernel_tuning),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(if (locked) R.string.locked_sub else R.string.kernel_tuning_tagline),
                        fontSize = 14.sp,
                        maxLines = 2,
                    )
                }
                DarkNightLogo(size = 44.dp)
            }
            if (locked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = stringResource(R.string.locked_title),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(18.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun TuneHeroCardMiuix(
    onClick: () -> Unit,
    modifier: Modifier,
    locked: Boolean,
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (locked) Modifier.alpha(0.55f) else Modifier),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    MiuixText(
                        text = stringResource(R.string.kernel_tuning),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                    )
                    MiuixText(
                        text = stringResource(if (locked) R.string.locked_sub else R.string.kernel_tuning_tagline),
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariantSummary,
                        maxLines = 2,
                    )
                }
                DarkNightLogo(size = 44.dp)
            }
            if (locked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = stringResource(R.string.locked_title),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(18.dp),
                    tint = colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun LockedHint() {
    val mode = LocalUiMode.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (mode == UiMode.Material) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Column {
            when (mode) {
                UiMode.Material -> {
                    Text(
                        text = stringResource(R.string.kernel_tuning_locked_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.kernel_tuning_locked_summary),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                UiMode.Miuix -> {
                    MiuixText(
                        text = stringResource(R.string.kernel_tuning_locked_title),
                        style = textStyles.title4,
                    )
                    MiuixText(
                        text = stringResource(R.string.kernel_tuning_locked_summary),
                        style = textStyles.body2,
                        color = colorScheme.onSurfaceVariantActions,
                    )
                }
            }
        }
    }
}
