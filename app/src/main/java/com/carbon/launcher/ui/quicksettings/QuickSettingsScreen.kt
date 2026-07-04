package com.carbon.launcher.ui.quicksettings

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AirplanemodeActive
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.NetworkCell
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuickSettingsScreen(
    onBack: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenAirplaneMode: () -> Unit,
    onOpenLocation: () -> Unit,
    onOpenNetwork: () -> Unit,
    onOpenDisplay: () -> Unit,
    onOpenSound: () -> Unit,
    onOpenBattery: () -> Unit,
    canWriteSettings: Boolean,
    wifiNetworkName: String,
    batteryPercent: Int,
    isBatteryCharging: Boolean,
    brightnessLevel: Float,
    isDarkModeEnabled: Boolean,
    isDoNotDisturbEnabled: Boolean,
    isBluetoothEnabled: Boolean,
    isAirplaneModeEnabled: Boolean,
    isLocationEnabled: Boolean,
    canChangeDoNotDisturb: Boolean,
    onToggleDarkMode: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onToggleDoNotDisturb: () -> Unit,
    onToggleAirplaneMode: () -> Unit,
    onToggleLocation: () -> Unit,
    onToggleBluetooth: () -> Unit,
    onLockScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        topBar = { QuickSettingsHeader(onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickTile(
                    title = "Dark mode",
                    subtitle = "Carbon theme",
                    icon = Icons.Outlined.DarkMode,
                    active = isDarkModeEnabled,
                    onClick = onToggleDarkMode,
                    modifier = Modifier.weight(1f),
                )
                QuickTile(
                    title = "Do Not Disturb",
                    subtitle = if (canChangeDoNotDisturb) "Focus" else "Grant access",
                    icon = Icons.Outlined.DoNotDisturbOn,
                    active = isDoNotDisturbEnabled,
                    onClick = onToggleDoNotDisturb,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickTile(
                    title = "Lock screen",
                    subtitle = "Secure",
                    icon = Icons.Outlined.Lock,
                    onClick = onLockScreen,
                    modifier = Modifier.weight(1f),
                )
                QuickTile(
                    title = "Wi-Fi",
                    subtitle = wifiNetworkName,
                    icon = Icons.Outlined.Wifi,
                    active = true,
                    onClick = onOpenWifi,
                    modifier = Modifier.weight(1f),
                )
                VibrationQuickTile(
                    onOpenSound = onOpenSound,
                    modifier = Modifier.weight(1f),
                )
            }

            BrightnessControlTile(
                canWriteSettings = canWriteSettings,
                brightnessLevel = brightnessLevel,
                onBrightnessChange = onBrightnessChange,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickTile(
                    title = "Airplane",
                    subtitle = if (isAirplaneModeEnabled) "On" else "Off",
                    icon = Icons.Outlined.AirplanemodeActive,
                    active = isAirplaneModeEnabled,
                    onClick = onToggleAirplaneMode,
                    onLongClick = onOpenAirplaneMode,
                    modifier = Modifier.weight(1f),
                )
                QuickTile(
                    title = "Location",
                    subtitle = if (isLocationEnabled) "On" else "Off",
                    icon = Icons.Outlined.LocationOn,
                    active = isLocationEnabled,
                    onClick = onToggleLocation,
                    onLongClick = onOpenLocation,
                    modifier = Modifier.weight(1f),
                )
                QuickTile(
                    title = "Bluetooth",
                    subtitle = if (isBluetoothEnabled) "On" else "Off",
                    icon = Icons.Outlined.Bluetooth,
                    active = isBluetoothEnabled,
                    onClick = onToggleBluetooth,
                    onLongClick = onOpenBluetooth,
                    modifier = Modifier.weight(1f),
                )
            }

            BatteryTile(
                batteryPercent = batteryPercent,
                isCharging = isBatteryCharging,
                onClick = onOpenBattery,
                modifier = Modifier.fillMaxWidth(),
            )

            VolumeControlTile(onOpenSound = onOpenSound)

            Spacer(Modifier.height(12.dp).windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun QuickSettingsHeader(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
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
                text = "Quick Settings",
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    onLongClick: () -> Unit = onClick,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    activeColor: Color = Color(0xFF5E3DA4),
) {
    val background = if (active) activeColor else MaterialTheme.colorScheme.surfaceVariant
    val foreground = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .height(104.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(foreground.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = foreground,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = foreground,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                color = foreground.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun QuickSliderTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    progress: Float,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ControlIcon(icon = icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = Color.White,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BatteryTile(
    batteryPercent: Int,
    isCharging: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = onClick,
    modifier: Modifier = Modifier,
) {
    val level = batteryPercent.coerceIn(0, 100)
    val batteryColor = when {
        level >= 60 -> Color(0xFF35C759)
        level >= 25 -> Color(0xFFB77900)
        else -> Color(0xFFFF453A)
    }
    val foreground = Color.White
    val transition = rememberInfiniteTransition(label = "battery-charge")
    val chargeSweep by transition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Restart,
        ),
        label = "battery-charge-sweep",
    )

    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(batteryColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        if (isCharging) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val bandWidth = size.width * 0.34f
                val startX = (size.width + bandWidth) * chargeSweep - bandWidth
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.34f),
                            Color.Transparent,
                        ),
                        start = Offset(startX, 0f),
                        end = Offset(startX + bandWidth, size.height),
                    ),
                    size = size,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(foreground.copy(alpha = if (isCharging) 0.24f else 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.BatteryChargingFull,
                    contentDescription = null,
                    tint = foreground,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = "Battery",
                    color = foreground,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (isCharging) "Charging" else "Power",
                    color = foreground.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = "$level%",
                color = foreground,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun VibrationQuickTile(
    onOpenSound: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    var isVibrate by remember(audioManager) {
        mutableStateOf(audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE)
    }

    QuickTile(
        title = "Vibration",
        subtitle = if (isVibrate) "Vibrate" else "Ring",
        icon = Icons.Outlined.Vibration,
        active = isVibrate,
        onClick = {
            try {
                audioManager.ringerMode = if (isVibrate) {
                    AudioManager.RINGER_MODE_NORMAL
                } else {
                    AudioManager.RINGER_MODE_VIBRATE
                }
                isVibrate = audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
            } catch (_: SecurityException) {
                onOpenSound()
            }
        },
        modifier = modifier,
    )
}
@Composable
private fun BrightnessControlTile(
    canWriteSettings: Boolean,
    brightnessLevel: Float,
    onBrightnessChange: (Float) -> Unit,
) {
    ControlTile(
        icon = Icons.Outlined.SettingsBrightness,
        title = "Brightness",
        subtitle = if (canWriteSettings) "Stepped display level" else "Grant write settings access",
        trailing = {
            Text(
                text = "${(brightnessLevel * 100).roundToInt()}%",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
    ) {
        Slider(
            value = brightnessLevel,
            onValueChange = { value ->
                val stepped = (value * 20).roundToInt() / 20f
                onBrightnessChange(stepped.coerceIn(0f, 1f))
            },
            valueRange = 0f..1f,
            steps = 19,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!canWriteSettings) {
            Text(
                text = "Grant access",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onBrightnessChange(brightnessLevel) }
                    .padding(vertical = 6.dp),
            )
        }
    }
}
@Composable
private fun VolumeControlTile(onOpenSound: () -> Unit) {
    val context = LocalContext.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val maxVolume = remember(audioManager) {
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    }
    var volume by remember(audioManager) {
        mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).coerceIn(0, maxVolume))
    }

    ControlTile(
        icon = Icons.AutoMirrored.Outlined.VolumeUp,
        title = "Volume",
        subtitle = "Media volume",
        trailing = {
            Text(
                text = "${(volume * 100 / maxVolume)}%",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        },
    ) {
        Slider(
            value = volume.toFloat(),
            onValueChange = { newValue ->
                val nextVolume = newValue.roundToInt().coerceIn(0, maxVolume)
                volume = nextVolume
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nextVolume, 0)
            },
            valueRange = 0f..maxVolume.toFloat(),
            steps = (maxVolume - 1).coerceAtLeast(0),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Sound settings",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onOpenSound)
                .padding(vertical = 6.dp),
        )
    }
}

@Composable
private fun VibrationControlTile(onOpenSound: () -> Unit) {
    val context = LocalContext.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    var isVibrate by remember(audioManager) {
        mutableStateOf(audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE)
    }

    ControlTile(
        icon = Icons.Outlined.Vibration,
        title = "Vibration",
        subtitle = if (isVibrate) "Vibrate mode" else "Ring mode",
        trailing = {
            Switch(
                checked = isVibrate,
                onCheckedChange = { enabled ->
                    try {
                        audioManager.ringerMode = if (enabled) {
                            AudioManager.RINGER_MODE_VIBRATE
                        } else {
                            AudioManager.RINGER_MODE_NORMAL
                        }
                        isVibrate = audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
                    } catch (_: SecurityException) {
                        onOpenSound()
                    }
                },
            )
        },
    )
}

@Composable
private fun ControlTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ControlIcon(icon = icon)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            trailing()
        }
        content()
    }
}

@Composable
private fun ControlIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
    }
}




