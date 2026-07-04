package com.carbon.launcher

import android.app.AppOpsManager
import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.carbon.launcher.data.AppCategory
import com.carbon.launcher.data.AppModel
import com.carbon.launcher.data.CategoryOrderPref
import com.carbon.launcher.data.DockPref
import com.carbon.launcher.data.LockDeviceAdminReceiver
import com.carbon.launcher.data.NotificationBadgeService
import com.carbon.launcher.data.WallpaperPref
import com.carbon.launcher.ui.LauncherViewModel
import com.carbon.launcher.ui.home.HomeScreen
import com.carbon.launcher.ui.quicksettings.QuickSettingsScreen
import com.carbon.launcher.ui.settings.CategoryOrderScreen
import com.carbon.launcher.ui.settings.SettingsScreen
import com.carbon.launcher.ui.theme.CarbonTheme
import com.carbon.launcher.ui.wallpaper.WallpaperPickerScreen
import kotlin.math.roundToInt

private object LauncherRoute {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val QUICK_SETTINGS = "quick_settings"
    const val WALLPAPER = "wallpaper"
    const val CATEGORY_ORDER = "category_order"
}

class MainActivity : ComponentActivity() {

    private var pendingUninstallPackage: String? = null
    private var uninstallResult by mutableStateOf<Boolean?>(null)
    private var usageAccessGranted by mutableStateOf(false)
    private var notificationAccessGranted by mutableStateOf(false)
    private var defaultLauncher by mutableStateOf(false)
    private var lockScreenAdminGranted by mutableStateOf(false)
    private var writeSettingsGranted by mutableStateOf(false)
    private var notificationPolicyAccessGranted by mutableStateOf(false)
    private var developerModeEnabled by mutableStateOf(false)
    private var autoRotateEnabled by mutableStateOf(false)
    private var brightnessLevel by mutableStateOf(0.7f)
    private var doNotDisturbEnabled by mutableStateOf(false)
    private var bluetoothEnabled by mutableStateOf(false)
    private var airplaneModeEnabled by mutableStateOf(false)
    private var locationEnabled by mutableStateOf(false)
    private var wifiNetworkName by mutableStateOf("Network")
    private var batteryPercent by mutableStateOf(0)
    private var batteryCharging by mutableStateOf(false)
    private var carbonDarkTheme by mutableStateOf(true)
    private var wallpaperResId by mutableStateOf(0)
    private var dockPackages by mutableStateOf<List<String>>(emptyList())
    private var categoryOrder by mutableStateOf<List<AppCategory>>(emptyList())
    private var isDockCustomized by mutableStateOf(false)
    private val handler = Handler(Looper.getMainLooper())

    private val packageRemovedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_PACKAGE_REMOVED) {
                val pkg = intent.data?.schemeSpecificPart
                val replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                if (pkg == pendingUninstallPackage && !replacing) {
                    pendingUninstallPackage = null
                    uninstallResult = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        registerReceiver(
            packageRemovedReceiver,
            IntentFilter(Intent.ACTION_PACKAGE_REMOVED),
            Context.RECEIVER_NOT_EXPORTED,
        )
        refreshPermissionStatus()
        wallpaperResId = WallpaperPref.get(this)
        dockPackages = DockPref.get(this)
        categoryOrder = CategoryOrderPref.get(this)
        isDockCustomized = DockPref.isConfigured(this)
        setContent {
            CarbonTheme(darkTheme = carbonDarkTheme) {
                val navController = rememberNavController()
                val vm: LauncherViewModel = viewModel()
                val state by vm.state.collectAsState()
                val badgeSubtitles by NotificationBadgeService.badgeNotificationsFlow.collectAsState(emptyMap())

                fun launch(app: AppModel) {
                    val intent = packageManager.getLaunchIntentForPackage(app.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    intent?.let(::startActivity)
                }

                fun uninstall(app: AppModel) {
                    pendingUninstallPackage = app.packageName
                    uninstallResult = null
                    val intent = Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${app.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openAppSettings(app: AppModel) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openUsageAccess() {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openDefaultLauncherSettings() {
                    val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openNotificationAccess() {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openWifiSettings() {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openBluetoothSettings() {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openAirplaneModeSettings() {
                    val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openLocationSettings() {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openNetworkSettings() {
                    val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openDisplaySettings() {
                    val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openSoundSettings() {
                    val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openWriteSettingsAccess() {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openNotificationPolicyAccess() {
                    val intent = Intent("android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                fun openBatterySettings() {
                    val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }
                fun toggleAutoRotate() {
                    if (!Settings.System.canWrite(this@MainActivity)) {
                        openWriteSettingsAccess()
                        return
                    }
                    val enabled = !autoRotateEnabled
                    Settings.System.putInt(
                        contentResolver,
                        Settings.System.ACCELEROMETER_ROTATION,
                        if (enabled) 1 else 0,
                    )
                    autoRotateEnabled = enabled
                    writeSettingsGranted = true
                }

                fun setBrightness(level: Float) {
                    if (!Settings.System.canWrite(this@MainActivity)) {
                        openWriteSettingsAccess()
                        return
                    }
                    val normalized = level.coerceIn(0f, 1f)
                    Settings.System.putInt(
                        contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                    )
                    Settings.System.putInt(
                        contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        (normalized * 255).toInt().coerceIn(1, 255),
                    )
                    window.attributes = window.attributes.apply { screenBrightness = normalized.coerceAtLeast(0.01f) }
                    brightnessLevel = normalized
                    writeSettingsGranted = true
                }

                fun toggleDarkMode() {
                    carbonDarkTheme = !carbonDarkTheme
                }

                fun toggleAirplaneMode() {
                    Toast.makeText(this@MainActivity, "Airplane mode is restricted by Android", Toast.LENGTH_SHORT).show()
                    openAirplaneModeSettings()
                }

                fun toggleLocation() {
                    Toast.makeText(this@MainActivity, "Location toggle is restricted by Android", Toast.LENGTH_SHORT).show()
                    openLocationSettings()
                }

                fun toggleBluetooth() {
                    try {
                        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                        val adapter = bluetoothManager.adapter ?: run {
                            openBluetoothSettings()
                            return
                        }
                        if (adapter.isEnabled) {
                            @Suppress("DEPRECATION")
                            adapter.disable()
                        } else {
                            @Suppress("DEPRECATION")
                            adapter.enable()
                        }
                        bluetoothEnabled = !adapter.isEnabled
                    } catch (_: SecurityException) {
                        openBluetoothSettings()
                    } catch (_: Exception) {
                        openBluetoothSettings()
                    }
                }
                fun toggleDoNotDisturb() {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (!notificationManager.isNotificationPolicyAccessGranted) {
                        openNotificationPolicyAccess()
                        return
                    }
                    val nextFilter = if (doNotDisturbEnabled) {
                        NotificationManager.INTERRUPTION_FILTER_ALL
                    } else {
                        NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    }
                    notificationManager.setInterruptionFilter(nextFilter)
                    doNotDisturbEnabled = nextFilter != NotificationManager.INTERRUPTION_FILTER_ALL
                    notificationPolicyAccessGranted = true
                }


                fun openLockScreenAdminSettings() {
                    val adminComponent = ComponentName(this@MainActivity, LockDeviceAdminReceiver::class.java)
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                        putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "Allow Carbon to lock the screen from the launcher.",
                        )
                    }
                    try {
                        startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    }
                }

                fun lockScreen() {
                    val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                    val adminComponent = ComponentName(this@MainActivity, LockDeviceAdminReceiver::class.java)
                    if (devicePolicyManager.isAdminActive(adminComponent)) {
                        try {
                            devicePolicyManager.lockNow()
                        } catch (_: SecurityException) {
                            Toast.makeText(this@MainActivity, "Enable lock screen permission first", Toast.LENGTH_SHORT).show()
                            openLockScreenAdminSettings()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Enable lock screen permission first", Toast.LENGTH_SHORT).show()
                        openLockScreenAdminSettings()
                    }
                }

                fun addToDock(app: AppModel) {
                    val updatedPackages = (dockPackages + app.packageName).distinct().take(5)
                    dockPackages = updatedPackages
                    isDockCustomized = true
                    DockPref.save(this@MainActivity, updatedPackages)
                }

                fun removeFromDock(app: AppModel) {
                    val updatedPackages = dockPackages.filterNot { it == app.packageName }
                    dockPackages = updatedPackages
                    isDockCustomized = true
                    DockPref.save(this@MainActivity, updatedPackages)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = wallpaperResId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f))
                    )
                    NavHost(
                        navController = navController,
                        startDestination = LauncherRoute.HOME,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(320),
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(320),
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(320),
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(320),
                            )
                        },
                    ) {
                        composable(LauncherRoute.HOME) {
                            HomeScreen(
                                apps = state.apps,
                                onAppClick = ::launch,
                                onAppUninstall = ::uninstall,
                                onAppClearCache = ::openAppSettings,
                                onAppClearStorage = ::openAppSettings,
                                onReloadApps = { vm.loadApps() },
                                uninstallResult = uninstallResult,
                                onUninstallResultConsumed = { uninstallResult = null },
                                onOpenSettings = {
                                    navController.navigate(LauncherRoute.SETTINGS) {
                                        launchSingleTop = true
                                    }
                                },
                                onLockScreen = ::lockScreen,
                                onOpenQuickSettings = {
                                    navController.navigate(LauncherRoute.QUICK_SETTINGS) {
                                        launchSingleTop = true
                                    }
                                },
                                categoryOrder = categoryOrder,
                                badgeSubtitles = badgeSubtitles,
                                dockPackages = dockPackages,
                                isDockCustomized = isDockCustomized,
                                onAddToDock = ::addToDock,
                                onRemoveFromDock = ::removeFromDock,
                                isUsageAccessGranted = usageAccessGranted,
                                isNotificationAccessGranted = notificationAccessGranted,
                                isDefaultLauncher = defaultLauncher,
                                isLockScreenAdminGranted = lockScreenAdminGranted,
                                isWriteSettingsGranted = writeSettingsGranted,
                                isNotificationPolicyAccessGranted = notificationPolicyAccessGranted,
                            )
                        }
                        composable(LauncherRoute.SETTINGS) {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                onOpenUsageAccess = ::openUsageAccess,
                                onOpenNotificationAccess = ::openNotificationAccess,
                                onOpenDefaultLauncherSettings = ::openDefaultLauncherSettings,
                                onOpenLockScreenAdminSettings = ::openLockScreenAdminSettings,
                                onOpenWriteSettings = ::openWriteSettingsAccess,
                                onOpenNotificationPolicyAccess = ::openNotificationPolicyAccess,
                                onOpenWallpaperPicker = {
                                    navController.navigate(LauncherRoute.WALLPAPER) {
                                        launchSingleTop = true
                                    }
                                },
                                onOpenCategoryOrder = {
                                    navController.navigate(LauncherRoute.CATEGORY_ORDER) {
                                        launchSingleTop = true
                                    }
                                },
                                isUsageAccessGranted = usageAccessGranted,
                                isNotificationAccessGranted = notificationAccessGranted,
                                isDefaultLauncher = defaultLauncher,
                                isLockScreenAdminGranted = lockScreenAdminGranted,
                                isWriteSettingsGranted = writeSettingsGranted,
                                isNotificationPolicyAccessGranted = notificationPolicyAccessGranted,
                                isDeveloperModeEnabled = developerModeEnabled,
                                appCount = state.apps.size,
                            )
                        }
                        composable(LauncherRoute.CATEGORY_ORDER) {
                            CategoryOrderScreen(
                                categoryOrder = categoryOrder,
                                categoryCounts = state.apps.groupingBy { it.category }.eachCount(),
                                onBack = { navController.popBackStack() },
                                onOrderChange = { order ->
                                    categoryOrder = order
                                    CategoryOrderPref.save(this@MainActivity, order)
                                },
                            )
                        }
                        composable(LauncherRoute.QUICK_SETTINGS) {
                            QuickSettingsScreen(
                                onBack = { navController.popBackStack() },
                                onOpenWifi = ::openWifiSettings,
                                onOpenBluetooth = ::openBluetoothSettings,
                                onOpenAirplaneMode = ::openAirplaneModeSettings,
                                onOpenLocation = ::openLocationSettings,
                                onOpenNetwork = ::openNetworkSettings,
                                onOpenDisplay = ::openDisplaySettings,
                                onOpenSound = ::openSoundSettings,
                                onOpenBattery = ::openBatterySettings,
                                canWriteSettings = writeSettingsGranted,
                                wifiNetworkName = wifiNetworkName,
                                batteryPercent = batteryPercent,
                                isBatteryCharging = batteryCharging,
                                brightnessLevel = brightnessLevel,
                                isDarkModeEnabled = carbonDarkTheme,
                                isDoNotDisturbEnabled = doNotDisturbEnabled,
                                isBluetoothEnabled = bluetoothEnabled,
                                isAirplaneModeEnabled = airplaneModeEnabled,
                                isLocationEnabled = locationEnabled,
                                canChangeDoNotDisturb = notificationPolicyAccessGranted,
                                onToggleDarkMode = ::toggleDarkMode,
                                onBrightnessChange = ::setBrightness,
                                onToggleDoNotDisturb = ::toggleDoNotDisturb,
                                onToggleAirplaneMode = ::toggleAirplaneMode,
                                onToggleLocation = ::toggleLocation,
                                onToggleBluetooth = ::toggleBluetooth,
                                onLockScreen = ::lockScreen,
                            )
                        }
                        composable(LauncherRoute.WALLPAPER) {
                            WallpaperPickerScreen(
                                onBack = { navController.popBackStack() },
                                onWallpaperChanged = {
                                    wallpaperResId = WallpaperPref.get(this@MainActivity)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStatus()
        pendingUninstallPackage?.let { pkg ->
            handler.postDelayed({
                if (pkg == pendingUninstallPackage) {
                    try {
                        packageManager.getPackageInfo(pkg, 0)
                        uninstallResult = false
                    } catch (e: Exception) {
                        uninstallResult = true
                    }
                    pendingUninstallPackage = null
                }
            }, 500)
        }
    }

    private fun refreshPermissionStatus() {
        usageAccessGranted = hasUsageAccess()
        notificationAccessGranted = hasNotificationAccess()
        defaultLauncher = isDefaultLauncher()
        lockScreenAdminGranted = hasLockScreenAdmin()
        writeSettingsGranted = Settings.System.canWrite(this)
        notificationPolicyAccessGranted = hasNotificationPolicyAccess()
        developerModeEnabled = isDeveloperModeEnabled()
        autoRotateEnabled = isAutoRotateEnabled()
        wifiNetworkName = readWifiNetworkName()
        batteryPercent = readBatteryPercent()
        batteryCharging = isBatteryCharging()
        brightnessLevel = readBrightnessLevel()
        doNotDisturbEnabled = isDoNotDisturbEnabled()
        bluetoothEnabled = isBluetoothEnabled()
        airplaneModeEnabled = isAirplaneModeEnabled()
        locationEnabled = isLocationEnabled()
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName,
        ) == AppOpsManager.MODE_ALLOWED
    }

    private fun hasNotificationAccess(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        return enabledListeners.split(':').any { listener ->
            ComponentName.unflattenFromString(listener)?.packageName == packageName
        }
    }


    private fun hasLockScreenAdmin(): Boolean {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, LockDeviceAdminReceiver::class.java)
        return devicePolicyManager.isAdminActive(adminComponent)
    }
    private fun isDeveloperModeEnabled(): Boolean {
        return Settings.Global.getInt(
            contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0,
        ) == 1
    }

    private fun hasNotificationPolicyAccess(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    private fun isAutoRotateEnabled(): Boolean {
        return Settings.System.getInt(
            contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            0,
        ) == 1
    }

    private fun isBluetoothEnabled(): Boolean {
        return try {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter?.isEnabled == true
        } catch (_: SecurityException) {
            false
        }
    }

    private fun isAirplaneModeEnabled(): Boolean {
        return Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
    }

    private fun isLocationEnabled(): Boolean {
        return Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) !=
            Settings.Secure.LOCATION_MODE_OFF
    }
    private fun readBatteryPercent(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        if (capacity in 0..100) return capacity

        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            ((level * 100f) / scale).roundToInt().coerceIn(0, 100)
        } else {
            0
        }
    }
    private fun readWifiNetworkName(): String {
        return try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ssid = wifiManager.connectionInfo?.ssid
                ?.trim()
                ?.trim('"')
                .orEmpty()
            if (ssid.isBlank() || ssid == "<unknown ssid>") "Network" else ssid
        } catch (_: Exception) {
            "Network"
        }
    }
    private fun isBatteryCharging(): Boolean {
        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryStatus?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN,
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL ||
            plugged != 0
    }

    private fun readBrightnessLevel(): Float {
        val brightness = Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            178,
        )
        return (brightness / 255f).coerceIn(0f, 1f)
    }

    private fun isDoNotDisturbEnabled(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted &&
            notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }
    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved?.activityInfo?.packageName == packageName
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(packageRemovedReceiver)
    }
}






