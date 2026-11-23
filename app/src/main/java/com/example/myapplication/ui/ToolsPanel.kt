package com.example.myapplication.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush as GBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LumenBorder
import com.example.myapplication.ui.theme.LumenCyanSecondary
import com.example.myapplication.ui.theme.LumenDarkCard
import com.example.myapplication.ui.theme.LumenDarkSurface
import com.example.myapplication.ui.theme.LumenPurpleLight
import com.example.myapplication.ui.theme.LumenPurplePrimary
import com.example.myapplication.ui.theme.LumenStudioTheme
import com.example.myapplication.ui.theme.LumenTextPrimary
import com.example.myapplication.ui.theme.LumenTextSecondary

@Composable
fun ToolsPanel(
    selectedTool: ToolType,
    selectedFilter: FilterPreset,
    brushSize: Float,
    brushOpacity: Float,
    brushColor: Color,
    textValue: String,
    textSize: Float,
    textColor: Color,
    cropRatio: String,
    selectedSticker: String,
    frameStyle: String,
    onToolSelect: (ToolType) -> Unit,
    onFilterSelect: (FilterPreset) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    onBrushOpacityChange: (Float) -> Unit,
    onBrushColorChange: (Color) -> Unit,
    onTextChange: (String) -> Unit,
    onTextSizeChange: (Float) -> Unit,
    onTextColorChange: (Color) -> Unit,
    onCropRatioChange: (String) -> Unit,
    onStickerSelect: (String) -> Unit,
    onFrameStyleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LumenDarkSurface)
    ) {
        // 1. Tools Bar (Horizontal scroll LazyRow)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ToolType.entries) { tool ->
                val isSelected = tool == selectedTool
                val icon = getToolIcon(tool)

                val springScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "toolIconScale"
                )

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) LumenPurplePrimary.copy(alpha = 0.25f) else LumenDarkCard,
                    label = "toolBgColor"
                )

                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) LumenCyanSecondary else LumenBorder,
                    label = "toolBorderColor"
                )

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) LumenCyanSecondary else LumenTextSecondary,
                    label = "toolContentColor"
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { onToolSelect(tool) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tool.title,
                        tint = contentColor,
                        modifier = Modifier
                            .size(18.dp)
                            .scale(springScale)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tool.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )
                }
            }
        }

        // 2. Sub-panel for the active tool with AnimatedVisibility
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LumenDarkCard)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                when (selectedTool) {
                    ToolType.FILTERS -> {
                        FiltersSubPanel(
                            selectedFilter = selectedFilter,
                            onFilterSelect = onFilterSelect
                        )
                    }
                    ToolType.BRUSH -> {
                        BrushSubPanel(
                            size = brushSize,
                            opacity = brushOpacity,
                            color = brushColor,
                            onSizeChange = onBrushSizeChange,
                            onOpacityChange = onBrushOpacityChange,
                            onColorChange = onBrushColorChange
                        )
                    }
                    ToolType.TEXT -> {
                        TextSubPanel(
                            text = textValue,
                            size = textSize,
                            color = textColor,
                            onTextChange = onTextChange,
                            onSizeChange = onTextSizeChange,
                            onColorChange = onTextColorChange
                        )
                    }
                    ToolType.STICKERS -> {
                        StickersSubPanel(
                            selectedSticker = selectedSticker,
                            onSelect = onStickerSelect
                        )
                    }
                    ToolType.CROP -> {
                        CropSubPanel(
                            currentRatio = cropRatio,
                            onRatioSelect = onCropRatioChange
                        )
                    }
                    ToolType.ERASER -> {
                        EraserSubPanel(
                            size = brushSize,
                            onSizeChange = onBrushSizeChange
                        )
                    }
                    ToolType.FRAMES -> {
                        FramesSubPanel(
                            currentFrame = frameStyle,
                            onFrameSelect = onFrameStyleChange
                        )
                    }
                }
            }
        }
    }
}

private fun getToolIcon(tool: ToolType): ImageVector {
    return when (tool) {
        ToolType.CROP -> Icons.Default.Crop
        ToolType.BRUSH -> Icons.Default.Brush
        ToolType.TEXT -> Icons.Default.TextFields
        ToolType.FILTERS -> Icons.Default.AutoAwesome
        ToolType.STICKERS -> Icons.Default.EmojiEmotions
        ToolType.ERASER -> Icons.Default.AutoFixHigh
        ToolType.FRAMES -> Icons.Default.CropPortrait
    }
}

@Composable
private fun FiltersSubPanel(
    selectedFilter: FilterPreset,
    onFilterSelect: (FilterPreset) -> Unit
) {
    Column {
        Text(
            text = "Предустановки фильтров",
            fontSize = 11.sp,
            color = LumenTextSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(FilterPreset.entries) { preset ->
                val isSelected = preset == selectedFilter
                val filterMatrix = FakeData.getFilterMatrix(preset)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onFilterSelect(preset) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) LumenCyanSecondary else LumenBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Canvas(modifier = Modifier.size(54.dp)) {
                            val activeColorFilter = ColorFilter.colorMatrix(ColorMatrix(filterMatrix))
                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    this.colorFilter = activeColorFilter
                                }
                                canvas.saveLayer(size.toRect(), paint)

                                // Draw mini thumbnail sunset
                                drawRect(
                                    brush = GBrush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF2E0854),
                                            Color(0xFFFF5722),
                                            Color(0xFFFFC107)
                                        )
                                    )
                                )
                                drawCircle(
                                    color = Color(0xFFFFF9C4),
                                    radius = size.width * 0.22f,
                                    center = Offset(size.width * 0.5f, size.height * 0.45f)
                                )
                                drawCircle(
                                    color = Color(0xFF1E0C38),
                                    radius = size.width * 0.6f,
                                    center = Offset(size.width * 0.5f, size.height * 1.3f)
                                )

                                canvas.restore()
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset.displayName,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) LumenCyanSecondary else LumenTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun BrushSubPanel(
    size: Float,
    opacity: Float,
    color: Color,
    onSizeChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onColorChange: (Color) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Размер: ${size.toInt()}px",
                fontSize = 11.sp,
                color = LumenTextSecondary,
                modifier = Modifier.width(90.dp)
            )
            Slider(
                value = size,
                onValueChange = onSizeChange,
                valueRange = 1f..100f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = LumenCyanSecondary,
                    activeTrackColor = LumenPurplePrimary,
                    inactiveTrackColor = LumenBorder
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Прозрачность: ${opacity.toInt()}%",
                fontSize = 11.sp,
                color = LumenTextSecondary,
                modifier = Modifier.width(90.dp)
            )
            Slider(
                value = opacity,
                onValueChange = onOpacityChange,
                valueRange = 0f..100f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = LumenCyanSecondary,
                    activeTrackColor = LumenPurplePrimary,
                    inactiveTrackColor = LumenBorder
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Цвет кисти:",
                fontSize = 11.sp,
                color = LumenTextSecondary
            )
            FakeData.brushColors.forEach { c ->
                val isSelected = c == color
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(c)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) LumenCyanSecondary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorChange(c) }
                )
            }
        }
    }
}

@Composable
private fun TextSubPanel(
    text: String,
    size: Float,
    color: Color,
    onTextChange: (String) -> Unit,
    onSizeChange: (Float) -> Unit,
    onColorChange: (Color) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                singleLine = true,
                placeholder = { Text("Введите надпись...", color = LumenTextSecondary, fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LumenCyanSecondary,
                    unfocusedBorderColor = LumenBorder,
                    focusedTextColor = LumenTextPrimary,
                    unfocusedTextColor = LumenTextPrimary
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Размер шрифта: ${size.toInt()}sp",
                fontSize = 11.sp,
                color = LumenTextSecondary,
                modifier = Modifier.width(130.dp)
            )
            Slider(
                value = size,
                onValueChange = onSizeChange,
                valueRange = 14f..72f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = LumenCyanSecondary,
                    activeTrackColor = LumenPurplePrimary,
                    inactiveTrackColor = LumenBorder
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Цвет текста:",
                fontSize = 11.sp,
                color = LumenTextSecondary
            )
            FakeData.brushColors.forEach { c ->
                val isSelected = c == color
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(c)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) LumenCyanSecondary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorChange(c) }
                )
            }
        }
    }
}

@Composable
private fun StickersSubPanel(
    selectedSticker: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(
            text = "Выберите стикер для наложения",
            fontSize = 11.sp,
            color = LumenTextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FakeData.stickers.forEach { s ->
                val isSelected = s == selectedSticker
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) LumenPurplePrimary.copy(alpha = 0.35f) else LumenDarkSurface)
                        .border(
                            1.dp,
                            if (isSelected) LumenCyanSecondary else LumenBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(s) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = s, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun CropSubPanel(
    currentRatio: String,
    onRatioSelect: (String) -> Unit
) {
    Column {
        Text(
            text = "Пропорции кадрирования",
            fontSize = 11.sp,
            color = LumenTextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FakeData.aspectRatios.forEach { ratio ->
                val isSelected = ratio == currentRatio
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) LumenPurplePrimary.copy(alpha = 0.3f) else LumenDarkSurface)
                        .border(
                            1.dp,
                            if (isSelected) LumenCyanSecondary else LumenBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onRatioSelect(ratio) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = ratio,
                        fontSize = 11.sp,
                        color = if (isSelected) LumenCyanSecondary else LumenTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun EraserSubPanel(
    size: Float,
    onSizeChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Размер ластика: ${size.toInt()}px",
            fontSize = 11.sp,
            color = LumenTextSecondary,
            modifier = Modifier.width(130.dp)
        )
        Slider(
            value = size,
            onValueChange = onSizeChange,
            valueRange = 1f..100f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = LumenCyanSecondary,
                activeTrackColor = LumenPurplePrimary,
                inactiveTrackColor = LumenBorder
            )
        )
    }
}

@Composable
private fun FramesSubPanel(
    currentFrame: String,
    onFrameSelect: (String) -> Unit
) {
    Column {
        Text(
            text = "Стиль рамки",
            fontSize = 11.sp,
            color = LumenTextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FakeData.frameStyles.forEach { frame ->
                val isSelected = frame == currentFrame
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) LumenPurplePrimary.copy(alpha = 0.3f) else LumenDarkSurface)
                        .border(
                            1.dp,
                            if (isSelected) LumenCyanSecondary else LumenBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onFrameSelect(frame) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = frame,
                        fontSize = 11.sp,
                        color = if (isSelected) LumenCyanSecondary else LumenTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Preview(name = "ToolsPanel Preview", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun ToolsPanelPreview() {
    LumenStudioTheme {
        ToolsPanel(
            selectedTool = ToolType.FILTERS,
            selectedFilter = FilterPreset.WARM,
            brushSize = 25f,
            brushOpacity = 80f,
            brushColor = Color(0xFF00E5FF),
            textValue = "Sunset",
            textSize = 36f,
            textColor = Color.White,
            cropRatio = "4:5",
            selectedSticker = "☀️",
            frameStyle = "Тонкая",
            onToolSelect = {},
            onFilterSelect = {},
            onBrushSizeChange = {},
            onBrushOpacityChange = {},
            onBrushColorChange = {},
            onTextChange = {},
            onTextSizeChange = {},
            onTextColorChange = {},
            onCropRatioChange = {},
            onStickerSelect = {},
            onFrameStyleChange = {}
        )
    }
}
