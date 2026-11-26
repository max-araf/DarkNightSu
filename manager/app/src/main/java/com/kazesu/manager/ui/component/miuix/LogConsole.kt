package com.kazesu.manager.ui.component.miuix

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme


@Composable
fun LogConsole(
    text: String,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            text = text,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            softWrap = false,
            color = colorScheme.onSurfaceContainer,
        )
    }
}
