package com.carbon.launcher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carbon.launcher.ui.components.ListItemRow
import com.carbon.launcher.ui.theme.Grayscale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenUsageAccess: () -> Unit,
    onSetWallpaper: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionTitle("Appearance")
            ListItemRow(
                title = "Wallpaper",
                subtitle = "Change home screen wallpaper",
                leading = { SettingsIcon(Icons.Outlined.Wallpaper) },
                trailing = { SettingsArrow() },
                modifier = Modifier,
            )

            var grayscaleOnly by remember { mutableStateOf(true) }
            ListItemRow(
                title = "Grayscale theme",
                subtitle = "Use only gray tones in the UI",
                leading = { SettingsIcon(Icons.Outlined.Palette) },
                trailing = {
                    Switch(checked = grayscaleOnly, onCheckedChange = { grayscaleOnly = it })
                },
            )

            HorizontalDivider(thickness = 1.dp, color = Grayscale.g05, modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("System")
            ListItemRow(
                title = "Usage access",
                subtitle = "Required to show app storage sizes",
                leading = { SettingsIcon(Icons.Outlined.Security) },
                trailing = {
                    OutlinedIconButton(onClick = onOpenUsageAccess) {
                        Text("Open", style = MaterialTheme.typography.labelSmall)
                    }
                },
            )
            ListItemRow(
                title = "Default launcher",
                subtitle = "Set Carbon as your home app",
                leading = { SettingsIcon(Icons.Outlined.Apps) },
                trailing = { SettingsArrow() },
            )

            HorizontalDivider(thickness = 1.dp, color = Grayscale.g05, modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("About")
            ListItemRow(
                title = "Carbon Launcher",
                subtitle = "Version 0.1.0",
                leading = { SettingsIcon(Icons.Outlined.Info) },
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedIconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = "Settings",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun SettingsIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun SettingsArrow() {
    Icon(
        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        modifier = Modifier.size(20.dp),
    )
}
