package com.kazesu.manager.ui.screen.darknight

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kazesu.manager.R
import androidx.compose.ui.unit.sp
import com.kazesu.manager.ui.navigation3.LocalNavigator
import com.kazesu.manager.ui.navigation3.Route
import com.kazesu.manager.ui.theme.DarkNightAuraBackdrop
import com.kazesu.manager.ui.theme.DarkNightGlassCard
import com.kazesu.manager.ui.theme.DarkNightGradientButton
import com.kazesu.manager.ui.theme.DarkNightPalette
import com.kazesu.manager.ui.theme.DarkNightPalettes
import com.kazesu.manager.ui.component.DarkNightSwitch
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.LocalDarkNightGlass

@Composable
fun DarkNightThemeStudioScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    DarkNightThemeStore.init(context)
    val themeState by DarkNightThemeStore.state.collectAsState()
    val active = themeState.palette

    Box(modifier = Modifier.fillMaxSize()) {
        DarkNightAuraBackdrop(
            palette = active,
            animateOnStart = themeState.startupAnimEnabled,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
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
                        tint = LocalDarkNightGlass.current.content,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DarkNight Gradient Studio",
                        color = LocalDarkNightGlass.current.content,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                    )
                    Text(
                        text = "Active: ${active.name} (${active.hexTop})",
                        color = LocalDarkNightGlass.current.contentSecondary,
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
                // Online Community Theme Store Card (Upload & Download themes like KazeSU)
                DarkNightGlassCard(palette = active) {
                    Text(
                        text = "Online Theme Store",
                        color = LocalDarkNightGlass.current.content,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = "Download community themes or upload your own custom DarkNightSu theme online.",
                        color = LocalDarkNightGlass.current.contentSecondary,
                        fontSize = 13.sp,
                    )
                    DarkNightGradientButton(
                        text = "Browse Online Theme Store",
                        palette = active,
                        onClick = { navigator.push(Route.ThemeStore()) },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = { navigator.push(Route.ThemeStoreCurrentUpload) },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        ) {
                            Text("Upload My Theme", color = LocalDarkNightGlass.current.content)
                        }
                        OutlinedButton(
                            onClick = { navigator.push(Route.ColorPalette()) },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        ) {
                            Text("Advanced Editor", color = LocalDarkNightGlass.current.content)
                        }
                    }
                }

                // Aura & Startup Animation Settings
                DarkNightGlassCard(palette = active) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Global Aura Backdrop", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text("Luminous top gradient glow across the manager", color = LocalDarkNightGlass.current.contentSecondary, fontSize = 12.sp)
                        }
                        DarkNightSwitch(
                            checked = themeState.auraEnabled,
                            onCheckedChange = { DarkNightThemeStore.setAuraEnabled(context, it) },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Startup Visual Animation", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text("Smooth aura bloom on app start, then settles static", color = LocalDarkNightGlass.current.contentSecondary, fontSize = 12.sp)
                        }
                        DarkNightSwitch(
                            checked = themeState.startupAnimEnabled,
                            onCheckedChange = { DarkNightThemeStore.setStartupAnimEnabled(context, it) },
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            DarkNightThemeStore.setOnboardingCompleted(context, false)
                            Toast.makeText(context, "Startup intro enabled", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    ) {
                        Text("Replay Startup Intro Screen", color = LocalDarkNightGlass.current.content)
                    }
                }

                Text(
                    text = "Gradient Color Palettes (10)",
                    color = LocalDarkNightGlass.current.content,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )

                // 2-column grid of all 10 palettes
                DarkNightPalettes.all.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        pair.forEach { pal ->
                            PaletteSwatchCard(
                                palette = pal,
                                selected = pal.id == active.id,
                                onClick = {
                                    DarkNightThemeStore.selectPalette(context, pal)
                                    Toast.makeText(context, "Applied ${pal.name}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PaletteSwatchCard(
    palette: DarkNightPalette,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = Color(0xCC11151E),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) palette.primaryGlow else Color.White.copy(alpha = 0.12f),
        ),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Dual gradient preview bars matching reference cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(listOf(palette.deepBase, palette.primaryGlow))
                        )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(listOf(palette.primaryGlow, palette.deepBase))
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = palette.name,
                        color = LocalDarkNightGlass.current.content,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                    )
                    Text(
                        text = "${palette.hexTop} • ${palette.hexBottom}",
                        color = LocalDarkNightGlass.current.contentSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                    )
                }
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = palette.primaryGlow,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(palette.primaryGlow)
                    )
                }
            }
        }
    }
}
