package com.carbon.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carbon.launcher.data.AppModel
import com.carbon.launcher.ui.LauncherViewModel
import com.carbon.launcher.ui.home.HomeScreen
import com.carbon.launcher.ui.settings.SettingsScreen
import com.carbon.launcher.ui.theme.CarbonTheme

class MainActivity : ComponentActivity() {

    private var pendingUninstallPackage: String? = null
    private var uninstallResult by mutableStateOf<Boolean?>(null)
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
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        registerReceiver(
            packageRemovedReceiver,
            IntentFilter(Intent.ACTION_PACKAGE_REMOVED),
            Context.RECEIVER_NOT_EXPORTED,
        )
        setContent {
            CarbonTheme {
                val vm: LauncherViewModel = viewModel()
                val state by vm.state.collectAsState()

                fun launch(app: AppModel) {
                    val intent = packageManager.getLaunchIntentForPackage(app.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    startActivity(intent)
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

                fun setWallpaper() {
                    val intent = Intent(Intent.ACTION_SET_WALLPAPER).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }

                var showSettings by remember { mutableStateOf(false) }

                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                ) {
                    if (showSettings) {
                        SettingsScreen(
                            onBack = { showSettings = false },
                            onOpenUsageAccess = ::openUsageAccess,
                            onSetWallpaper = ::setWallpaper,
                        )
                    } else {
                        HomeScreen(
                            apps = state.apps,
                            onAppClick = ::launch,
                            onAppUninstall = ::uninstall,
                            onAppClearCache = ::openAppSettings,
                            onAppClearStorage = ::openAppSettings,
                            onGrantUsageAccess = ::openUsageAccess,
                            onReloadApps = { vm.loadApps() },
                            uninstallResult = uninstallResult,
                            onUninstallResultConsumed = { uninstallResult = null },
                            onOpenSettings = { showSettings = true },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(packageRemovedReceiver)
    }
}
