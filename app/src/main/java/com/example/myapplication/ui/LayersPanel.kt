package com.example.myapplication.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LumenBorder
import com.example.myapplication.ui.theme.LumenCyanSecondary
import com.example.myapplication.ui.theme.LumenDarkCard
import com.example.myapplication.ui.theme.LumenDarkSurface
import com.example.myapplication.ui.theme.LumenDarkSurfaceHighlight
import com.example.myapplication.ui.theme.LumenPurplePrimary
import com.example.myapplication.ui.theme.LumenRed
import com.example.myapplication.ui.theme.LumenStudioTheme
import com.example.myapplication.ui.theme.LumenTextMuted
import com.example.myapplication.ui.theme.LumenTextPrimary
import com.example.myapplication.ui.theme.LumenTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayersBottomSheet(
    isOpen: Boolean,
    layers: List<LayerItem>,
    selectedLayerId: String,
    onDismiss: () -> Unit,
    onSelectLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onUpdateOpacity: (String, Float) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LumenDarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        LayersContent(
            layers = layers,
            selectedLayerId = selectedLayerId,
            onDismiss = onDismiss,
            onSelectLayer = onSelectLayer,
            onToggleVisibility = onToggleVisibility,
            onUpdateOpacity = onUpdateOpacity,
            onDeleteLayer = onDeleteLayer,
            onMoveUp = onMoveUp,
            onMoveDown = onMoveDown,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
fun LayersContent(
    layers: List<LayerItem>,
    selectedLayerId: String,
    onDismiss: () -> Unit,
    onSelectLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onUpdateOpacity: (String, Float) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Панель слоёв",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumenTextPrimary
                )
                Text(
                    text = "Всего слоёв: ${layers.size} (порядок отрисовки снизу вверх)",
                    fontSize = 12.sp,
                    color = LumenTextSecondary
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = LumenTextSecondary
                )
            }
        }

        // Layers List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(layers, key = { _, item -> item.id }) { index, layer ->
                val isSelected = layer.id == selectedLayerId
                LayerRowItem(
                    layer = layer,
                    isSelected = isSelected,
                    canMoveUp = index > 0,
                    canMoveDown = index < layers.lastIndex,
                    canDelete = layers.size > 1,
                    onSelect = { onSelectLayer(layer.id) },
                    onToggleVisibility = { onToggleVisibility(layer.id) },
                    onUpdateOpacity = { onUpdateOpacity(layer.id, it) },
                    onMoveUp = { onMoveUp(layer.id) },
                    onMoveDown = { onMoveDown(layer.id) },
                    onDelete = { onDeleteLayer(layer.id) }
                )
            }
        }
    }
}

@Composable
private fun LayerRowItem(
    layer: LayerItem,
    isSelected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onToggleVisibility: () -> Unit,
    onUpdateOpacity: (Float) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) LumenDarkSurfaceHighlight else LumenDarkCard)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) LumenCyanSecondary else LumenBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Layer thumbnail
            LayerThumbnail(layerType = layer.type)

            Spacer(modifier = Modifier.width(10.dp))

            // Name and type
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = layer.name,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) LumenCyanSecondary else LumenTextPrimary
                )
                Text(
                    text = "Прозрачность: ${(layer.opacity * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = LumenTextSecondary
                )
            }

            // Visibility Eye
            IconButton(
                onClick = onToggleVisibility,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Видимость",
                    tint = if (layer.isVisible) LumenCyanSecondary else LumenTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Reorder Up
            IconButton(
                onClick = onMoveUp,
                enabled = canMoveUp,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Вверх",
                    tint = if (canMoveUp) LumenTextPrimary else LumenTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Reorder Down
            IconButton(
                onClick = onMoveDown,
                enabled = canMoveDown,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Вниз",
                    tint = if (canMoveDown) LumenTextPrimary else LumenTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete
            IconButton(
                onClick = onDelete,
                enabled = canDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Удалить",
                    tint = if (canDelete) LumenRed else LumenTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Opacity slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, start = 44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = layer.opacity,
                onValueChange = onUpdateOpacity,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = LumenCyanSecondary,
                    activeTrackColor = LumenPurplePrimary,
                    inactiveTrackColor = LumenBorder
                )
            )
        }
    }
}

@Composable
private fun LayerThumbnail(layerType: LayerType) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(LumenDarkSurface)
            .border(1.dp, LumenBorder, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (layerType) {
            LayerType.BACKGROUND -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF2E0854), Color(0xFFFF5722), Color(0xFFFFC107))
                        )
                    )
                    drawCircle(
                        color = Color(0xFFFFF9C4),
                        radius = size.width * 0.2f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                    )
                }
            }
            LayerType.PORTRAIT -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    drawCircle(
                        color = Color(0xFF1A0E2E),
                        radius = size.width * 0.45f,
                        center = Offset(size.width * 0.5f, size.height * 1.1f)
                    )
                    drawCircle(
                        color = Color(0xFF2E0854),
                        radius = size.width * 0.25f,
                        center = Offset(size.width * 0.5f, size.height * 0.4f)
                    )
                }
            }
            LayerType.TEXT -> {
                Text(
                    text = "T",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = LumenCyanSecondary
                )
            }
            LayerType.STICKER -> {
                Text(
                    text = "☀️",
                    fontSize = 14.sp
                )
            }
            LayerType.FILTER -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0x8000E5FF), Color(0x807C4DFF), Color.Transparent)
                        ),
                        radius = size.width * 0.6f
                    )
                }
            }
        }
    }
}

@Preview(name = "LayersContent Preview", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun LayersContentPreview() {
    LumenStudioTheme {
        LayersContent(
            layers = FakeData.initialLayers,
            selectedLayerId = "layer_bg",
            onDismiss = {},
            onSelectLayer = {},
            onToggleVisibility = {},
            onUpdateOpacity = { _, _ -> },
            onDeleteLayer = {},
            onMoveUp = {},
            onMoveDown = {}
        )
    }
}
