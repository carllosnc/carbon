package com.carbon.launcher.ui.wallpaper

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carbon.launcher.R
import com.carbon.launcher.data.WallpaperPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WallpaperItem(
    val id: Int,
    val name: String,
    val drawableRes: Int,
)

private val wallpapers = listOf(
    WallpaperItem(1, "Aurora Drift", R.drawable.aurora_drift),
    WallpaperItem(2, "Violet Haze", R.drawable.violet_haze),
    WallpaperItem(3, "Prism Bloom", R.drawable.prism_bloom),
    WallpaperItem(4, "Obsidian Wave", R.drawable.obsidian_wave),
    WallpaperItem(5, "Liquid Sunrise", R.drawable.liquid_sunrise),
    WallpaperItem(6, "Ember Mist", R.drawable.ember_mist),
    WallpaperItem(7, "Lunar Current", R.drawable.lunar_current),
    WallpaperItem(8, "Neon Fog", R.drawable.neon_fog),
    WallpaperItem(9, "Cosmic Silk", R.drawable.cosmic_silk),
    WallpaperItem(10, "Electric Orbit", R.drawable.electric_orbit),
    WallpaperItem(11, "Shadow Glass", R.drawable.shadow_glass),
    WallpaperItem(12, "Plasma Field", R.drawable.plasma_field),
    WallpaperItem(13, "Midnight Flux", R.drawable.midnight_flux),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperPickerScreen(
    onBack: () -> Unit,
    onWallpaperChanged: () -> Unit,
) {
    val context = LocalContext.current
    var currentResId by remember { mutableIntStateOf(WallpaperPref.get(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Wallpapers",
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(wallpapers, key = { it.id }) { wallpaper ->
                AsyncWallpaperCard(
                    wallpaper = wallpaper,
                    isSelected = currentResId == wallpaper.drawableRes,
                    onClick = {
                        WallpaperPref.save(context, wallpaper.drawableRes)
                        currentResId = wallpaper.drawableRes
                        onWallpaperChanged()
                        Toast.makeText(context, "Wallpaper applied!", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }
}

@Composable
private fun AsyncWallpaperCard(
    wallpaper: WallpaperItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(wallpaper.drawableRes) {
        bitmap = withContext(Dispatchers.IO) {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
            }
            BitmapFactory.decodeResource(
                context.resources,
                wallpaper.drawableRes,
                options,
            ).asImageBitmap()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
    ) {
        bitmap?.let { bmp ->
            Image(
                bitmap = bmp,
                contentDescription = wallpaper.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.1f)),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                    ),
                ),
        )

        Text(
            text = wallpaper.name,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
