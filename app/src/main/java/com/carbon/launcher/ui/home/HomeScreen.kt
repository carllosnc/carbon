package com.carbon.launcher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carbon.launcher.data.AppModel
import com.carbon.launcher.ui.components.AppList
import com.carbon.launcher.ui.components.ListItemRow
import com.carbon.launcher.ui.components.toImageBitmap
import com.carbon.launcher.ui.theme.Grayscale

private enum class UninstallState { Idle, Confirming, Loading, Success }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    apps: List<AppModel>,
    onAppClick: (AppModel) -> Unit,
    onAppUninstall: (AppModel) -> Unit,
    onAppClearCache: (AppModel) -> Unit,
    onAppClearStorage: (AppModel) -> Unit,
    onGrantUsageAccess: () -> Unit,
    onReloadApps: () -> Unit,
    uninstallResult: Boolean?,
    onUninstallResultConsumed: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var longPressedApp by remember { mutableStateOf<AppModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var uninstallState by remember { mutableStateOf(UninstallState.Idle) }
    var uninstallTarget by remember { mutableStateOf<AppModel?>(null) }

    LaunchedEffect(uninstallResult) {
        when (uninstallResult) {
            true -> {
                uninstallState = UninstallState.Success
                onReloadApps()
            }
            false -> {
                uninstallState = UninstallState.Idle
            }
            null -> {}
        }
        if (uninstallResult != null) {
            onUninstallResultConsumed()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(appCount = apps.size, onOpenSettings = onOpenSettings)

            AppList(
                apps = apps,
                onAppClick = onAppClick,
                onAppLongClick = { longPressedApp = it },
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )

            Footer(appCount = apps.size)
        }

        longPressedApp?.let { app ->
            ModalBottomSheet(
                onDismissRequest = { longPressedApp = null },
                sheetState = sheetState,
                containerColor = Grayscale.g03,
                scrimColor = Color.Black.copy(alpha = 0.5f),
            ) {
                AppInfoSheet(
                    app = app,
                    onUninstall = {
                        longPressedApp = null
                        uninstallTarget = app
                        uninstallState = UninstallState.Confirming
                    },
                    onClearCache = {
                        longPressedApp = null
                        onAppClearCache(app)
                    },
                    onClearStorage = {
                        longPressedApp = null
                        onAppClearStorage(app)
                    },
                    onGrantUsageAccess = {
                        longPressedApp = null
                        onGrantUsageAccess()
                    },
                )
            }
        }

        uninstallTarget?.let { app ->
            when (uninstallState) {
                UninstallState.Confirming -> {
                    AlertDialog(
                        onDismissRequest = {
                            uninstallState = UninstallState.Idle
                            uninstallTarget = null
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        title = { Text("Uninstall ${app.label}?") },
                        text = { Text("This app will be removed from your device.") },
                        confirmButton = {
                            OutlinedButton(
                                onClick = {
                                    uninstallState = UninstallState.Loading
                                    onAppUninstall(app)
                                },
                            ) {
                                Text("Uninstall", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = {
                                    uninstallState = UninstallState.Idle
                                    uninstallTarget = null
                                },
                            ) {
                                Text("Cancel")
                            }
                        },
                        containerColor = Grayscale.g02,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        textContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }

                UninstallState.Loading -> {
                    AlertDialog(
                        onDismissRequest = {},
                        confirmButton = {},
                        dismissButton = {},
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "Uninstalling ${app.label}...",
                                    textAlign = TextAlign.Center,
                                )
                            }
                        },
                        containerColor = Grayscale.g02,
                    )
                }

                UninstallState.Success -> {
                    AlertDialog(
                        onDismissRequest = {
                            uninstallState = UninstallState.Idle
                            uninstallTarget = null
                        },
                        icon = {
                            Text(
                                text = "✓",
                                color = Grayscale.g14,
                                style = MaterialTheme.typography.headlineLarge,
                            )
                        },
                        title = { Text("Uninstalled") },
                        text = { Text("${app.label} has been removed from your device.") },
                        confirmButton = {
                            OutlinedButton(
                                onClick = {
                                    uninstallState = UninstallState.Idle
                                    uninstallTarget = null
                                },
                            ) {
                                Text("OK")
                            }
                        },
                        containerColor = Grayscale.g02,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        textContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }

                UninstallState.Idle -> {}
            }
        }
    }
}

@Composable
private fun AppInfoSheet(
    app: AppModel,
    onUninstall: () -> Unit,
    onClearCache: (AppModel) -> Unit,
    onClearStorage: (AppModel) -> Unit,
    onGrantUsageAccess: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        ListItemRow(
            title = app.label,
            minHeight = 72.dp,
            leading = {
                androidx.compose.foundation.Image(
                    bitmap = app.icon.toImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)),
                )
            },
            trailing = {
                androidx.compose.material3.OutlinedIconButton(onClick = onUninstall) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Uninstall ${app.label}",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
        )
        HorizontalDivider(thickness = 1.dp, color = Grayscale.g05, modifier = Modifier.padding(vertical = 8.dp))
        ListItemRow(
            title = "Version",
            subtitle = app.versionName,
            leading = { LeadingIcon(Icons.Outlined.Star) },
        )
        ListItemRow(
            title = "Size",
            subtitle = app.sizeMb,
            leading = { LeadingIcon(Icons.Outlined.Inventory2) },
        )
        ListItemRow(
            title = "Cache",
            subtitle = app.cacheMb,
            leading = { LeadingIcon(Icons.Outlined.CleaningServices) },
            trailing = {
                OutlinedButton(onClick = { onClearCache(app) }) {
                    Text("Clear")
                }
            },
        )
        ListItemRow(
            title = "Data",
            subtitle = app.dataMb,
            leading = { LeadingIcon(Icons.Outlined.SdStorage) },
            trailing = {
                OutlinedButton(onClick = { onClearStorage(app) }) {
                    Text("Clear")
                }
            },
        )
        if (app.cacheMb == "—" && app.dataMb == "—") {
            ListItemRow(
                title = "Grant usage access",
                subtitle = "Required to see cache and data sizes",
                leading = { LeadingIcon(Icons.Outlined.Security) },
                trailing = {
                    OutlinedButton(onClick = onGrantUsageAccess) {
                        Text("Grant")
                    }
                },
            )
        }
        ListItemRow(
            title = "Category",
            subtitle = app.category.label,
            leading = { LeadingIcon(Icons.Outlined.Category) },
        )
        ListItemRow(
            title = "System app",
            subtitle = if (app.isSystem) "Yes" else "No",
            leading = { LeadingIcon(Icons.Outlined.Apps) },
        )
        ListItemRow(
            title = "Installed",
            subtitle = app.installDate,
            leading = { LeadingIcon(Icons.Outlined.CalendarMonth) },
        )
        HorizontalDivider(thickness = 1.dp, color = Grayscale.g05, modifier = Modifier.padding(vertical = 8.dp))
        ListItemRow(
            title = "Package",
            subtitle = app.packageName,
            leading = { LeadingIcon(Icons.Outlined.Sell) },
        )
    }
}

@Composable
private fun LeadingIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(40.dp),
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
private fun Header(appCount: Int, onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Carbon",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "$appCount apps",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelMedium,
                )
                OutlinedIconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        )
    }
}

@Composable
private fun Footer(appCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        )
        Text(
            text = "Carbon Launcher",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}
