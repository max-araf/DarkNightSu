package com.kazesu.manager.ui.component.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazesu.manager.ui.component.WarningLevel
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun WarningCard(
    message: String,
    modifier: Modifier = Modifier,
    level: WarningLevel = WarningLevel.Error,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val glass = LocalDarkNightGlass.current
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
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(fillColors))
            .border(1.dp, accent.copy(alpha = if (glass.isDark) 0.36f else 0.48f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = textColor,
                modifier = Modifier.weight(1f),
            )
            action?.invoke()
        }
    }
}
