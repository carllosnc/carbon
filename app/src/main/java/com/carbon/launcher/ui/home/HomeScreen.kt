package com.carbon.launcher.ui.home

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.Window
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.carbon.launcher.data.AppCategory
import com.carbon.launcher.data.AppModel
import com.carbon.launcher.ui.components.AppList
import com.carbon.launcher.ui.components.ListItemRow
import com.carbon.launcher.ui.components.toImageBitmap
import kotlinx.coroutines.delay
import java.io.File

private enum class UninstallState { Idle, Confirming, Loading, Success }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun applyReadableSystemBars(window: Window, view: View) {
    window.statusBarColor = Color.Transparent.toArgb()
    window.navigationBarColor = Color.Transparent.toArgb()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }
    WindowCompat.getInsetsController(window, view).apply {
        isAppearanceLightStatusBars = false
        isAppearanceLightNavigationBars = false
    }
}

@Composable
private fun KeepHomeSystemBarsReadable(trigger: Boolean) {
    val view = LocalView.current
    val window = view.context.findActivity()?.window
    SideEffect {
        window?.let { applyReadableSystemBars(it, view) }
    }
    LaunchedEffect(trigger) {
        repeat(if (trigger) 4 else 1) {
            withFrameNanos { }
            window?.let { applyReadableSystemBars(it, view) }
        }
    }
}

@Composable
private fun KeepSheetSystemBarsReadable() {
    val view = LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window ?: view.context.findActivity()?.window
    SideEffect {
        window?.let { applyReadableSystemBars(it, view) }
    }
    LaunchedEffect(Unit) {
        repeat(8) {
            withFrameNanos { }
            window?.let { applyReadableSystemBars(it, view) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    onLockScreen: () -> Unit,
    onOpenQuickSettings: () -> Unit,
    categoryOrder: List<AppCategory> = AppCategory.entries.sortedBy { it.order },
    badgeSubtitles: Map<String, String> = emptyMap(),
    dockPackages: List<String> = emptyList(),
    isDockCustomized: Boolean = false,
    onAddToDock: (AppModel) -> Unit,
    onRemoveFromDock: (AppModel) -> Unit,
    isUsageAccessGranted: Boolean = false,
    isNotificationAccessGranted: Boolean = false,
    isDefaultLauncher: Boolean = false,
    isLockScreenAdminGranted: Boolean = false,
    isWriteSettingsGranted: Boolean = false,
    isNotificationPolicyAccessGranted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val hasMissingPermission = !isUsageAccessGranted || !isNotificationAccessGranted || !isDefaultLauncher || !isLockScreenAdminGranted || !isWriteSettingsGranted || !isNotificationPolicyAccessGranted
    var longPressedApp by remember { mutableStateOf<AppModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetTopOffset = LocalConfiguration.current.screenHeightDp.dp * 0.1f
    var uninstallState by remember { mutableStateOf(UninstallState.Idle) }
    var uninstallTarget by remember { mutableStateOf<AppModel?>(null) }

    KeepHomeSystemBarsReadable(longPressedApp != null || uninstallTarget != null)

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
    val availableCategories = remember(apps) { apps.map { it.category }.toSet() }
    val availableLetters = remember(apps, selectedCategory) {
        apps
            .filter { app -> selectedCategory == null || app.category == selectedCategory }
            .mapNotNull { app -> app.label.firstOrNull()?.uppercaseChar() }
            .filter { it in 'A'..'Z' }
            .toSet()
    }

    LaunchedEffect(availableCategories, selectedCategory) {
        if (selectedCategory != null && selectedCategory !in availableCategories) {
            selectedCategory = null
        }
    }
    LaunchedEffect(availableLetters, selectedLetter) {
        if (selectedLetter != null && selectedLetter !in availableLetters) {
            selectedLetter = null
        }
    }

    val filteredApps = remember(apps, selectedLetter, selectedCategory, categoryOrder) {
        val orderMap = categoryOrder.withIndex().associate { (index, category) -> category to index }
        apps
            .filter { app ->
                val letterMatch = selectedLetter == null || app.label.firstOrNull()?.uppercaseChar() == selectedLetter
                val categoryMatch = selectedCategory == null || app.category == selectedCategory
                letterMatch && categoryMatch
            }
            .sortedWith(compareBy({ orderMap[it.category] ?: it.category.order }, { it.label.lowercase() }))
    }

    val dockApps = remember(apps, dockPackages, isDockCustomized) {
        val appMap = apps.associateBy { it.packageName }
        if (isDockCustomized) {
            dockPackages.mapNotNull { appMap[it] }.take(5)
        } else {
            val slotPackages = listOf(
                listOf("com.google.android.dialer", "com.android.dialer", "com.android.phone"),
                listOf("com.android.vending"),
                listOf("com.google.android.GoogleCamera", "com.android.camera", "org.codeaurora.snapcam"),
                listOf("com.android.chrome", "com.brave.browser", "org.mozilla.firefox"),
                listOf("com.google.android.apps.messaging", "com.android.mms", "com.whatsapp"),
            )
            val matched = slotPackages.mapNotNull { candidates -> candidates.firstNotNullOfOrNull { appMap[it] } }
            val usedPackages = matched.map { it.packageName }.toSet()
            val remaining = apps.filter { it.packageName !in usedPackages }
            (matched + remaining).take(5)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ClockWidget()
            LetterBar(availableLetters = availableLetters, selectedLetter = selectedLetter, onLetterClick = { letter ->
                selectedLetter = if (selectedLetter == letter) null else letter
            })
            CategoryFilter(
                availableCategories = availableCategories,
                categoryOrder = categoryOrder,
                selectedCategory = selectedCategory,
                onCategoryClick = { cat ->
                    selectedCategory = if (selectedCategory == cat) null else cat
                },
            )

            AppList(
                apps = filteredApps,
                onAppClick = onAppClick,
                onAppLongClick = { longPressedApp = it },
                badgeSubtitles = badgeSubtitles,
                labelColor = Color.White,
                subtitleColor = Color.White.copy(alpha = 0.68f),
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )

            DockRow(
                apps = dockApps,
                onAppClick = onAppClick,
                onAppLongClick = { longPressedApp = it },
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(end = 20.dp, bottom = 116.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FloatingActionButton(
                onClick = onOpenQuickSettings,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "Open quick settings",
                    modifier = Modifier.size(24.dp),
                )
            }
            FloatingActionButton(
                onClick = onLockScreen,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Lock screen",
                    modifier = Modifier.size(24.dp),
                )
            }
            Box {
                FloatingActionButton(
                    onClick = onOpenSettings,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Open settings",
                        modifier = Modifier.size(24.dp),
                    )
                }
                if (hasMissingPermission) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .size(16.dp)
                            .background(Color(0xFFFF4444), CircleShape)
                            .border(1.dp, Color.White, CircleShape),
                    )
                }
            }
        }

        longPressedApp?.let { app ->
            ModalBottomSheet(
                onDismissRequest = { longPressedApp = null },
                sheetState = sheetState,
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .offset(y = sheetTopOffset),
                containerColor = MaterialTheme.colorScheme.surface,
                scrimColor = Color.Black.copy(alpha = 0.5f),
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            ) {
                KeepSheetSystemBarsReadable()
                AppInfoSheet(
                    app = app,
                    isInDock = isDockCustomized && app.packageName in dockPackages,
                    canAddToDock = dockPackages.size < 5,
                    onAddToDock = {
                        longPressedApp = null
                        onAddToDock(app)
                    },
                    onRemoveFromDock = {
                        longPressedApp = null
                        onRemoveFromDock(app)
                    },
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
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
    isInDock: Boolean,
    canAddToDock: Boolean,
    onAddToDock: () -> Unit,
    onRemoveFromDock: () -> Unit,
    onUninstall: () -> Unit,
    onClearCache: (AppModel) -> Unit,
    onClearStorage: (AppModel) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        ListItemRow(
            title = app.label,
            minHeight = 72.dp,
            leading = {
                Image(
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
        if (isInDock) {
            ListItemRow(
                title = "Remove from dock",
                subtitle = "Keep this app out of the bottom dock",
                leading = { LeadingIcon(Icons.Outlined.Apps) },
                onClick = onRemoveFromDock,
            )
        } else if (canAddToDock) {
            ListItemRow(
                title = "Add to dock",
                subtitle = "Pin this app to the bottom dock",
                leading = { LeadingIcon(Icons.Outlined.Apps) },
                onClick = onAddToDock,
            )
        } else {
            ListItemRow(
                title = "Dock is full",
                subtitle = "Remove one pinned app before adding another",
                leading = { LeadingIcon(Icons.Outlined.Apps) },
            )
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
        ListItemRow(title = "Version", subtitle = app.versionName, leading = { LeadingIcon(Icons.Outlined.Star) })
        ListItemRow(title = "Size", subtitle = app.sizeMb, leading = { LeadingIcon(Icons.Outlined.Inventory2) })
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
        ListItemRow(title = "Category", subtitle = app.category.label, leading = { LeadingIcon(Icons.Outlined.Category) })
        ListItemRow(title = "System app", subtitle = if (app.isSystem) "Yes" else "No", leading = { LeadingIcon(Icons.Outlined.Apps) })
        ListItemRow(title = "Installed", subtitle = app.installDate, leading = { LeadingIcon(Icons.Outlined.CalendarMonth) })
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
        ListItemRow(title = "Package", subtitle = app.packageName, leading = { LeadingIcon(Icons.Outlined.Sell) })
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
    availableLetters: Set<Char>,
    selectedLetter: Char?,
    onLetterClick: (Char) -> Unit,
) {
    val letters = remember(availableLetters) { ('A'..'Z').filter { it in availableLetters } }
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
            .drawBehind {
                val color = Color.White.copy(alpha = 0.15f)
                val stroke = 1.dp.toPx()
                drawLine(color, strokeWidth = stroke, start = Offset.Zero, end = Offset(size.width, 0f))
                drawLine(color, strokeWidth = stroke, start = Offset(0f, size.height), end = Offset(size.width, size.height))
            }
            .onSizeChanged { containerWidth = it.width }
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
                    .background(if (isSelected) Color.White else Color.Transparent, CircleShape)
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
            (context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager).getMemoryInfo(mi)
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
        Text(text = label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall, fontSize = 12.sp)
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
        Text(text = detail, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall, fontSize = 13.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryFilter(
    availableCategories: Set<AppCategory>,
    categoryOrder: List<AppCategory>,
    selectedCategory: AppCategory?,
    onCategoryClick: (AppCategory) -> Unit,
) {
    val categories = remember(availableCategories, categoryOrder) {
        val orderMap = categoryOrder.withIndex().associate { (index, category) -> category to index }
        AppCategory.entries
            .filter { it in availableCategories }
            .sortedWith(compareBy({ orderMap[it] ?: it.order }, { it.label }))
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    strokeWidth = 1.dp.toPx(),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                )
            },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories, key = { it.name }) { category ->
            val isSelected = category == selectedCategory
            val bringIntoViewRequester = remember { BringIntoViewRequester() }

            LaunchedEffect(isSelected, selectedCategory) {
                if (isSelected) {
                    bringIntoViewRequester.bringIntoView()
                }
            }

            Box(
                modifier = Modifier
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DockRow(
    apps: List<AppModel>,
    onAppClick: (AppModel) -> Unit,
    onAppLongClick: (AppModel) -> Unit,
) {
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
        AnimatedContent(
            targetState = apps,
            transitionSpec = {
                (fadeIn(animationSpec = tween(durationMillis = 180)) +
                    scaleIn(animationSpec = tween(durationMillis = 180), initialScale = 0.96f))
                    .togetherWith(
                        fadeOut(animationSpec = tween(durationMillis = 120)) +
                            scaleOut(animationSpec = tween(durationMillis = 120), targetScale = 0.96f),
                    )
            },
            label = "dock-apps",
        ) { targetApps ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                targetApps.forEach { app ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .combinedClickable(
                                onClick = { onAppClick(app) },
                                onLongClick = { onAppLongClick(app) },
                            )
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
}




