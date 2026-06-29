package com.carbon.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carbon.launcher.data.AppModel

fun Drawable.toImageBitmap() = when (this) {
    is BitmapDrawable -> bitmap.asImageBitmap()
    else -> Bitmap.createBitmap(
        intrinsicWidth.coerceAtLeast(1),
        intrinsicHeight.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888,
    ).also { bmp ->
        Canvas(bmp).also { setBounds(0, 0, it.width, it.height); draw(it) }
    }.asImageBitmap()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    app: AppModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    onLongClick: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Image(
            bitmap = app.icon.toImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier.size(54.dp),
        )
        if (showLabel) {
            Text(
                text = app.label,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListRow(
    app: AppModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    hasBadge: Boolean = false,
    badgeSubtitle: String? = null,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Box(modifier = Modifier.size(56.dp)) {
            Image(
                bitmap = app.icon.toImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp)),
            )
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 1.dp)
                        .size(16.dp)
                        .background(Color(0xFFFF4444), CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                color = labelColor,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (badgeSubtitle != null) {
                Text(
                    text = badgeSubtitle,
                    color = subtitleColor,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun AppList(
    apps: List<AppModel>,
    onAppClick: (AppModel) -> Unit,
    modifier: Modifier = Modifier,
    onAppLongClick: ((AppModel) -> Unit)? = null,
    badgeSubtitles: Map<String, String> = emptyMap(),
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
    ) {
        itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
            val subtitle = badgeSubtitles[app.packageName]
            var isVisible by remember(app.packageName) { mutableStateOf(false) }
            val alpha by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 180, delayMillis = (index * 6).coerceAtMost(48)),
                label = "app-row-alpha",
            )
            val translationY by animateFloatAsState(
                targetValue = if (isVisible) 0f else 18f,
                animationSpec = tween(durationMillis = 220, delayMillis = (index * 6).coerceAtMost(48)),
                label = "app-row-translation-y",
            )

            LaunchedEffect(app.packageName) {
                isVisible = true
            }

            AppListRow(
                app = app,
                onClick = { onAppClick(app) },
                onLongClick = onAppLongClick?.let { { it(app) } },
                hasBadge = subtitle != null,
                badgeSubtitle = subtitle,
                labelColor = labelColor,
                subtitleColor = subtitleColor,
                modifier = Modifier.graphicsLayer {
                    this.alpha = alpha
                    this.translationY = translationY
                },
            )
        }
    }
}