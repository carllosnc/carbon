package com.carbon.launcher.ui.quicksettings

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.NetworkCell
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsScreen(
    onBack: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenNetwork: () -> Unit,
    onOpenDisplay: () -> Unit,
    onOpenSound: () -> Unit,
    onOpenBattery: () -> Unit,
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
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickTile(
                    title = "Wi-Fi",
                    subtitle = "Network",
                    icon = Icons.Outlined.Wifi,
                    active = true,
                    onClick = onOpenWifi,
                    modifier = Modifier.weight(1f),
                )
                QuickTile(
                    title = "Bluetooth",
                    subtitle = "Devices",
                    icon = Icons.Outlined.Bluetooth,
                    active = true,
                    onClick = onOpenBluetooth,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickTile(
                    title = "Cellular",
                    subtitle = "Connections",
                    icon = Icons.Outlined.NetworkCell,
                    onClick = onOpenNetwork,
                    modifier = Modifier.weight(1f),
                )
                QuickTile(
                    title = "Battery",
                    subtitle = "Power",
                    icon = Icons.Outlined.BatteryChargingFull,
                    onClick = onOpenBattery,
                    modifier = Modifier.weight(1f),
                )
            }

            QuickSliderTile(
                title = "Display",
                subtitle = "Brightness and screen",
                icon = Icons.Outlined.SettingsBrightness,
                progress = 0.72f,
                onClick = onOpenDisplay,
            )

            VolumeControlTile(onOpenSound = onOpenSound)
            VibrationControlTile(onOpenSound = onOpenSound)

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

@Composable
private fun QuickTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    val background = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val foreground = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(foreground.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = foreground,
                modifier = Modifier.size(23.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = foreground,
                style = MaterialTheme.typography.titleMedium,
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
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                    style = MaterialTheme.typography.titleMedium,
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
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ControlIcon(icon = icon)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
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
