package com.carbon.launcher.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carbon.launcher.ui.components.ListItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenUsageAccess: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenDefaultLauncherSettings: () -> Unit,
    onOpenLockScreenAdminSettings: () -> Unit,
    onOpenWallpaperPicker: () -> Unit,
    isUsageAccessGranted: Boolean,
    isNotificationAccessGranted: Boolean,
    isDefaultLauncher: Boolean,
    isLockScreenAdminGranted: Boolean,
    appCount: Int,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        topBar = { SettingsHeader(onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionTitle("Permissions & Status")
            PermissionStatusRow(
                title = "Usage access",
                subtitle = "Shows cache and data sizes for installed apps",
                isActive = isUsageAccessGranted,
                leading = { SettingsIcon(Icons.Outlined.Security) },
                onClick = onOpenUsageAccess,
            )
            PermissionStatusRow(
                title = "Notification access",
                subtitle = "Enables notification badges in the app list",
                isActive = isNotificationAccessGranted,
                leading = { SettingsIcon(Icons.Outlined.Notifications) },
                onClick = onOpenNotificationAccess,
            )
            PermissionStatusRow(
                title = "Default launcher",
                subtitle = "Uses Carbon when you press the Home button",
                isActive = isDefaultLauncher,
                leading = { SettingsIcon(Icons.Outlined.Home) },
                onClick = onOpenDefaultLauncherSettings,
            )
            PermissionStatusRow(
                title = "Lock screen admin",
                subtitle = "Allows Carbon to lock the screen",
                isActive = isLockScreenAdminGranted,
                leading = { SettingsIcon(Icons.Outlined.Lock) },
                onClick = onOpenLockScreenAdminSettings,
            )
            ListItemRow(
                title = "Installed apps",
                subtitle = "$appCount apps found",
                leading = { SettingsIcon(Icons.Outlined.Apps) },
                trailing = { StatusBadge(text = "OK", isActive = true) },
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("Appearance")
            ListItemRow(
                title = "Wallpaper",
                subtitle = "Change launcher wallpaper",
                leading = { SettingsIcon(Icons.Outlined.Wallpaper) },
                onClick = onOpenWallpaperPicker,
            )


            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("About")
            ListItemRow(
                title = "Carbon Launcher",
                subtitle = "Version 0.1.0",
                leading = { SettingsIcon(Icons.Outlined.Info) },
            )

            Spacer(Modifier.height(24.dp).windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    subtitle: String,
    isActive: Boolean,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    ListItemRow(
        title = title,
        subtitle = subtitle,
        leading = leading,
        onClick = onClick,
        trailing = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusBadge(
                    text = if (isActive) "Active" else "Missing",
                    isActive = isActive,
                )
            }
        },
    )
}

@Composable
private fun StatusBadge(
    text: String,
    isActive: Boolean,
) {
    val background = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val foreground = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    Text(
        text = text,
        color = foreground,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconButton(onClick = onBack) {
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
