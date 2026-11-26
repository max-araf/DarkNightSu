package com.kazesu.manager.ui.screen.darknight

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazesu.manager.R
import com.kazesu.manager.ui.navigation3.LocalNavigator
import com.kazesu.manager.ui.theme.DarkNightGlassCard
import com.kazesu.manager.ui.theme.DarkNightScreenBackground
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.DarkNightToastManager
import com.kazesu.manager.ui.theme.LocalDarkNightGlass

@Composable
fun DarkNightSideInfoBarSettingsScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    DarkNightThemeStore.init(context)
    val state by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val glass = LocalDarkNightGlass.current

    DarkNightScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dn_back),
                        contentDescription = "Back",
                        tint = glass.content,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.darknight_infobar_title),
                        color = glass.content,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                    )
                    Text(
                        text = stringResource(R.string.darknight_infobar_summary),
                        color = glass.contentSecondary,
                        fontSize = 12.sp,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DarkNightGlassCard(palette = state.palette) {
                    SettingSwitchRow(
                        title = stringResource(R.string.darknight_infobar_enable),
                        subtitle = stringResource(R.string.darknight_infobar_summary),
                        checked = state.infoBarEnabled,
                        onCheckedChange = { DarkNightThemeStore.setInfoBarEnabled(context, it) },
                    )
                    SettingSwitchRow(
                        title = stringResource(R.string.darknight_infobar_pin),
                        subtitle = stringResource(R.string.darknight_infobar_pin_summary),
                        checked = state.infoBarPinned,
                        onCheckedChange = { DarkNightThemeStore.setInfoBarPinned(context, it) },
                    )
                }

                DarkNightGlassCard(palette = state.palette) {
                    Text("Visible Rings", color = glass.content, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    SettingSwitchRow(
                        title = stringResource(R.string.darknight_infobar_show_cpu),
                        subtitle = "Highest cluster live GHz",
                        checked = state.infoBarShowCpu,
                        onCheckedChange = { DarkNightThemeStore.setInfoBarRing(context, "cpu", it) },
                    )
                    SettingSwitchRow(
                        title = stringResource(R.string.darknight_infobar_show_battery),
                        subtitle = "Battery level & charging indicator",
                        checked = state.infoBarShowBattery,
                        onCheckedChange = { DarkNightThemeStore.setInfoBarRing(context, "battery", it) },
                    )
                    SettingSwitchRow(
                        title = stringResource(R.string.darknight_infobar_show_temp),
                        subtitle = "Live thermal gauge",
                        checked = state.infoBarShowTemp,
                        onCheckedChange = { DarkNightThemeStore.setInfoBarRing(context, "temp", it) },
                    )
                }

                DarkNightGlassCard(palette = state.palette) {
                    Text("Temperature Source & Unit", color = glass.content, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(false to "Battery", true to "CPU Peak (Root)").forEach { (cpuSrc, label) ->
                            val selected = state.infoBarTempSourceCpu == cpuSrc
                            val shape = RoundedCornerShape(99.dp)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(shape)
                                    .clickable { DarkNightThemeStore.setInfoBarTempSourceCpu(context, cpuSrc) },
                                shape = shape,
                                color = if (selected) glass.accent.copy(alpha = 0.28f) else glass.fillA,
                                border = BorderStroke(1.dp, if (selected) glass.accent else glass.borderLite.copy(alpha = 0.22f)),
                            ) {
                                Box(Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(label, color = glass.content, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(false to "Celsius (°C)", true to "Fahrenheit (°F)").forEach { (useF, label) ->
                            val selected = state.infoBarUnitF == useF
                            val shape = RoundedCornerShape(99.dp)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(shape)
                                    .clickable { DarkNightThemeStore.setInfoBarUnitF(context, useF) },
                                shape = shape,
                                color = if (selected) glass.accent.copy(alpha = 0.28f) else glass.fillA,
                                border = BorderStroke(1.dp, if (selected) glass.accent else glass.borderLite.copy(alpha = 0.22f)),
                            ) {
                                Box(Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(label, color = glass.content, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                DarkNightGlassCard(palette = state.palette) {
                    Text(
                        text = "Bar Opacity: ${state.infoBarOpacityPercent}%",
                        color = glass.content,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )
                    Slider(
                        value = state.infoBarOpacityPercent.toFloat(),
                        onValueChange = { DarkNightThemeStore.setInfoBarOpacity(context, it.toInt()) },
                        valueRange = 60f..100f,
                    )

                    Text(
                        text = "Vertical Position: ${state.infoBarVerticalPercent}%",
                        color = glass.content,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )
                    Slider(
                        value = state.infoBarVerticalPercent.toFloat(),
                        onValueChange = { DarkNightThemeStore.setInfoBarVerticalPercent(context, it.toInt()) },
                        valueRange = 10f..75f,
                    )

                    OutlinedButton(
                        onClick = {
                            DarkNightThemeStore.resetInfoBarDefaults(context)
                            DarkNightToastManager.show("Side Info Bar reset to defaults")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, glass.borderLite.copy(alpha = 0.30f)),
                    ) {
                        Text("Reset to Default", color = glass.content)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val glass = LocalDarkNightGlass.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = glass.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, color = glass.contentSecondary, fontSize = 11.sp)
        }
        com.kazesu.manager.ui.component.DarkNightToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}
