package com.kazesu.manager.ui.component.bottombar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kazesu.manager.R
import java.io.File

/**
 * Draws a custom bottom bar icon image when [iconPath] points at a real file, otherwise maps
 * built-in destinations to the DarkNightSu custom ic_dn_* vector set.
 */
@Composable
fun BottomBarIconContent(
    icon: ImageVector,
    iconPath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
) {
    val file = iconPath?.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.isFile }
    if (file != null) {
        Image(
            painter = rememberAsyncImagePainter(model = file),
            contentDescription = contentDescription,
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit,
        )
    } else {
        val dnDrawableRes = when (icon) {
            Icons.Rounded.Cottage -> R.drawable.ic_dn_home
            Icons.Rounded.Security -> R.drawable.ic_dn_superuser
            Icons.Rounded.Extension -> R.drawable.ic_dn_module
            Icons.Rounded.Settings -> R.drawable.ic_dn_settings
            else -> null
        }
        val painter = if (dnDrawableRes != null) {
            painterResource(dnDrawableRes)
        } else {
            rememberVectorPainter(icon)
        }
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier.size(size),
            colorFilter = tint.takeIf { it != Color.Unspecified }?.let { color ->
                ColorFilter.tint(color)
            },
        )
    }
}
