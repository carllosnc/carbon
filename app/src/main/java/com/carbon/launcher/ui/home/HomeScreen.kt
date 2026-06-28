package com.carbon.launcher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.carbon.launcher.data.AppCategory
import com.carbon.launcher.data.AppModel
import com.carbon.launcher.ui.components.AppList
import com.carbon.launcher.ui.components.ListItemRow
import com.carbon.launcher.ui.components.toImageBitmap

private enum class UninstallState { Idle, Confirming, Loading, Success }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    apps: List<AppModel>,
    onAppClick: (AppModel) -> Unit,
    onAppUninstall: (AppModel) -> Unit,
    onAppClearCache: (AppModel) -> Unit,
    onAppClearStorage: (AppModel) -> Unit,
    onReloadApps: () -> Unit,
    uninstallResult: Boolean?,
    onUninstallResultConsumed: () -> Unit,
    onOpenSettings: () -> Unit,
    badgeSubtitles: Map<String, String> = emptyMap(),
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

    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var selectedCategory by remember { mutableStateOf<AppCategory?>(null) }
    val filteredApps = remember(apps, selectedLetter, selectedCategory) {
        apps.filter { app ->
            val letterMatch = selectedLetter == null || app.label.firstOrNull()?.uppercaseChar() == selectedLetter
            val categoryMatch = selectedCategory == null || app.category == selectedCategory
            letterMatch && categoryMatch
        }
    }

    val dockApps = remember(apps) {
        val slotPackages = listOf(
            listOf("com.google.android.dialer", "com.android.dialer", "com.android.phone"),
            listOf("com.android.vending"),
            listOf("com.google.android.GoogleCamera", "com.android.camera", "org.codeaurora.snapcam"),
            listOf("com.android.chrome", "com.brave.browser", "org.mozilla.firefox"),
            listOf("com.google.android.apps.messaging", "com.android.mms", "com.whatsapp"),
        )
        val appMap = apps.associateBy { it.packageName }
        val matched = slotPackages.mapNotNull { candidates -> candidates.firstNotNullOfOrNull { appMap[it] } }
        val usedPackages = matched.map { it.packageName }.toSet()
        val remaining = apps.filter { it.packageName !in usedPackages }
        (matched + remaining).take(5)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ClockWidget()
            LetterBar(selectedLetter = selectedLetter, onLetterClick = { letter ->
                selectedLetter = if (selectedLetter == letter) null else letter
            })
            CategoryFilter(selectedCategory = selectedCategory, onCategoryClick = { cat ->
                selectedCategory = if (selectedCategory == cat) null else cat
            })

            AppList(
                apps = filteredApps,
                onAppClick = onAppClick,
                onAppLongClick = { longPressedApp = it },
                badgeSubtitles = badgeSubtitles,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )

            DockRow(apps = dockApps, onAppClick = onAppClick)
        }

        FloatingActionButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(end = 20.dp, bottom = 116.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Open settings",
                modifier = Modifier.size(24.dp),
            )
        }

        longPressedApp?.let { app ->
            ModalBottomSheet(
                onDismissRequest = { longPressedApp = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
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
                        containerColor = MaterialTheme.colorScheme.surface,
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
                        containerColor = MaterialTheme.colorScheme.surface,
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
                                color = MaterialTheme.colorScheme.primary,
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
                        containerColor = MaterialTheme.colorScheme.surface,
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
                IconButton(onClick = onUninstall) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Uninstall ${app.label}",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
        )
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
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
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
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
private fun LetterBar(
    selectedLetter: Char?,
    onLetterClick: (Char) -> Unit,
) {
    val letters = remember {
        ('A'..'Z').toList()
    }
    val scrollState = rememberScrollState()
    var containerWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    LaunchedEffect(selectedLetter) {
        if (selectedLetter != null && containerWidth > 0) {
            val index = letters.indexOf(selectedLetter)
            if (index >= 0) {
                val itemWidthPx = with(density) { 36.dp.toPx() }
                val spacingPx = with(density) { 4.dp.toPx() }
                val paddingPx = with(density) { 16.dp.toPx() }
                val center = paddingPx + index * (itemWidthPx + spacingPx) + itemWidthPx / 2f
                val target = (center - containerWidth / 2f).toInt().coerceAtLeast(0)
                scrollState.animateScrollTo(target)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { containerWidth = it.width }
            .background(Color.Black.copy(alpha = 0.3f))
            .drawBehind {
                val color = Color.White.copy(alpha = 0.15f)
                val stroke = 1.dp.toPx()
                drawLine(color, strokeWidth = stroke, start = Offset.Zero, end = Offset(size.width, 0f))
                drawLine(color, strokeWidth = stroke, start = Offset(0f, size.height), end = Offset(size.width, size.height))
            }
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        letters.forEach { letter ->
            val isSelected = letter == selectedLetter
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White
                        else Color.Transparent,
                        CircleShape,
                    )
                    .clickable { onLetterClick(letter) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter.toString(),
                    color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ClockWidget() {
    val context = LocalContext.current
    var memUsed by remember { mutableStateOf(0L) }
    var memTotal by remember { mutableStateOf(0L) }
    var storageUsed by remember { mutableStateOf(0L) }
    var storageTotal by remember { mutableStateOf(0L) }
    var batteryTemp by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            val mi = android.app.ActivityManager.MemoryInfo()
            (context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager)
                .getMemoryInfo(mi)
            memTotal = mi.totalMem
            memUsed = mi.totalMem - mi.availMem

            val stat = android.os.StatFs(File(android.os.Environment.getDataDirectory().absolutePath).absolutePath)
            storageTotal = stat.totalBytes
            storageUsed = stat.totalBytes - stat.availableBytes

            val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            val raw = intent?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            batteryTemp = raw / 10f

            delay(5000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MiniWidget(
                label = "RAM",
                pct = if (memTotal > 0) (memUsed * 100 / memTotal).toInt() else 0,
                detail = String.format("%.1f/%.1f GB", memUsed / (1024f * 1024f * 1024f), memTotal / (1024f * 1024f * 1024f)),
            )
            MiniWidget(
                label = "STORAGE",
                pct = if (storageTotal > 0) (storageUsed * 100 / storageTotal).toInt() else 0,
                detail = String.format("%.0f/%.0f GB", storageUsed / (1024f * 1024f * 1024f), storageTotal / (1024f * 1024f * 1024f)),
            )
            MiniWidget(
                label = "TEMP",
                pct = ((batteryTemp / 60f) * 100).toInt().coerceIn(0, 100),
                detail = String.format("%.1f°C", batteryTemp),
            )
        }
    }
}

@Composable
private fun MiniWidget(label: String, pct: Int, detail: String) {
    val barColor = when {
        pct < 50 -> Color(0xFF4CAF50)
        pct < 75 -> Color(0xFFFFC107)
        else -> Color(0xFFFF4444)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.25f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = pct / 100f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = detail,
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun CategoryFilter(
    selectedCategory: AppCategory?,
    onCategoryClick: (AppCategory) -> Unit,
) {
    val categories = remember { AppCategory.entries.sortedBy { it.order } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3f))
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    strokeWidth = 1.dp.toPx(),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                )
            }
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(20.dp),
                    )
                    .clickable { onCategoryClick(category) }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text(
                    text = category.label,
                    color = if (isSelected) Color.Black else Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DockRow(apps: List<AppModel>, onAppClick: (AppModel) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(Color.Black.copy(alpha = 0.3f))
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    strokeWidth = 1.dp.toPx(),
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                )
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            apps.forEach { app ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onAppClick(app) }
                        .padding(8.dp),
                ) {
                    Image(
                        bitmap = app.icon.toImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            }
        }
    }
}


