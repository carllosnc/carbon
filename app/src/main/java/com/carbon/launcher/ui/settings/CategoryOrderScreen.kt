package com.carbon.launcher.ui.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carbon.launcher.data.AppCategory
import kotlin.math.roundToInt
import android.view.HapticFeedbackConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryOrderScreen(
    categoryOrder: List<AppCategory>,
    categoryCounts: Map<AppCategory, Int>,
    onBack: () -> Unit,
    onOrderChange: (List<AppCategory>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var categories by remember(categoryOrder) { mutableStateOf(normalizeCategoryOrder(categoryOrder)) }
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var slotHeightPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val view = LocalView.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        topBar = { CategoryOrderHeader(onBack = onBack) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            userScrollEnabled = draggedIndex < 0,
        ) {
            item {
                Text(
                    text = "Long-press and drag to reorder categories.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                )
            }
            itemsIndexed(categories, key = { _, category -> category.name }) { index, category ->
                CategoryOrderRow(
                    category = category,
                    appCount = categoryCounts[category] ?: 0,
                    position = index + 1,
                    index = index,
                    isDragged = draggedIndex == index,
                    dragOffset = dragOffset,
                    draggedIndex = draggedIndex,
                    slotHeightPx = slotHeightPx,
                    onDragStart = {
                        draggedIndex = index
                        dragOffset = 0f
                    },
                    onDrag = { delta ->
                        dragOffset += delta
                    },
                    onDragEnd = {
                        if (slotHeightPx > 0f) {
                            val shift = (dragOffset / slotHeightPx).roundToInt()
                            val to = (draggedIndex + shift).coerceIn(0, categories.lastIndex)
                            if (to != draggedIndex) {
                                categories = categories.move(draggedIndex, to)
                                onOrderChange(categories)
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            }
                        }
                        draggedIndex = -1
                        dragOffset = 0f
                    },
                    onSizeChanged = { height ->
                        if (slotHeightPx == 0f) {
                            slotHeightPx = height + with(density) { 10.dp.toPx() }
                        }
                    },
                    modifier = Modifier,
                )
            }
            item {
                Spacer(Modifier.height(20.dp).windowInsetsPadding(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun CategoryOrderRow(
    category: AppCategory,
    appCount: Int,
    position: Int,
    index: Int,
    isDragged: Boolean,
    dragOffset: Float,
    draggedIndex: Int,
    slotHeightPx: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onSizeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val projectedIndex = if (draggedIndex >= 0 && slotHeightPx > 0f) {
        (draggedIndex + (dragOffset / slotHeightPx).roundToInt()).coerceIn(0, Int.MAX_VALUE)
    } else {
        -1
    }

    val shiftPx = when {
        isDragged -> dragOffset
        draggedIndex >= 0 && index in minOf(draggedIndex, projectedIndex)..maxOf(draggedIndex, projectedIndex) -> {
            if (draggedIndex < projectedIndex) -slotHeightPx else slotHeightPx
        }
        else -> 0f
    }

    val animatedShiftPx by animateFloatAsState(
        targetValue = shiftPx,
        animationSpec = if (draggedIndex >= 0) spring(stiffness = 600f) else snap(),
        label = "row-shift",
    )

    val elevation by animateFloatAsState(
        targetValue = if (isDragged) 8f else 0f,
        label = "card-elevation",
    )
    val scale by animateFloatAsState(
        targetValue = if (isDragged) 1.04f else 1f,
        label = "card-scale",
    )
    val rotation by animateFloatAsState(
        targetValue = if (isDragged) 3f else 0f,
        label = "card-rotation",
    )

    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { onSizeChanged(it.height.toFloat()) }
            .graphicsLayer {
                translationY = animatedShiftPx
                shadowElevation = elevation
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .pointerInput(category) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { currentOnDragStart() },
                    onDrag = { change, offset ->
                        change.consume()
                        currentOnDrag(offset.y)
                    },
                    onDragEnd = { currentOnDragEnd() },
                    onDragCancel = { currentOnDragEnd() },
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.DragHandle,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                modifier = Modifier.size(28.dp),
            )
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        RoundedCornerShape(14.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = position.toString().padStart(2, '0'),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = category.label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (appCount == 1) "1 app" else "$appCount apps",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun CategoryOrderHeader(onBack: () -> Unit) {
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
                text = "Categories",
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

private fun normalizeCategoryOrder(categories: List<AppCategory>): List<AppCategory> =
    (categories + AppCategory.entries.sortedBy { it.order }).distinct()

private fun <T> List<T>.move(from: Int, to: Int): List<T> =
    toMutableList().apply {
        add(to, removeAt(from))
    }
