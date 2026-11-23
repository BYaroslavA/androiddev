package com.example.myapplication.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.LumenBorder
import com.example.myapplication.ui.theme.LumenCyanSecondary
import com.example.myapplication.ui.theme.LumenDarkBackground
import com.example.myapplication.ui.theme.LumenDarkCard
import com.example.myapplication.ui.theme.LumenPurplePrimary
import com.example.myapplication.ui.theme.LumenStudioTheme
import com.example.myapplication.ui.theme.LumenTextMuted
import com.example.myapplication.ui.theme.LumenTextSecondary
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LumenApp(
    viewModel: EditorViewModel = viewModel()
) {
    LumenStudioTheme {
        EditorScreen(viewModel = viewModel)
    }
}

@Composable
fun EditorScreen(
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    EditorContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onUndo = { viewModel.undo() },
        onRedo = { viewModel.redo() },
        onExport = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Экспортировано в Галерею")
            }
        },
        onShare = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Ссылка на проект скопирована")
            }
        },
        onToggleLayers = { viewModel.setLayersSheetOpen(true) },
        onHoldStart = { viewModel.setComparingOriginal(true) },
        onHoldEnd = { viewModel.setComparingOriginal(false) },
        onScaleChange = { viewModel.onScaleChange(it) },
        onToolSelect = { viewModel.selectTool(it) },
        onFilterSelect = { viewModel.selectFilter(it) },
        onBrushSizeChange = { viewModel.onBrushSizeChange(it) },
        onBrushOpacityChange = { viewModel.onBrushOpacityChange(it) },
        onBrushColorChange = { viewModel.onBrushColorChange(it) },
        onTextChange = { viewModel.onTextChange(it) },
        onTextSizeChange = { viewModel.onTextSizeChange(it) },
        onTextColorChange = { viewModel.onTextColorChange(it) },
        onCropRatioChange = { viewModel.onCropRatioChange(it) },
        onStickerSelect = { viewModel.onStickerSelect(it) },
        onFrameStyleChange = { viewModel.onFrameStyleChange(it) },
        onBrightnessChange = { viewModel.onBrightnessChange(it) },
        onContrastChange = { viewModel.onContrastChange(it) },
        onSaturationChange = { viewModel.onSaturationChange(it) },
        onTemperatureChange = { viewModel.onTemperatureChange(it) },
        onResetAdjustments = { viewModel.resetAll() },
        onSliderStart = { viewModel.onSliderValueChangeStarted() },
        onDismissLayers = { viewModel.setLayersSheetOpen(false) },
        onSelectLayer = { viewModel.selectLayer(it) },
        onToggleLayerVisibility = { viewModel.toggleLayerVisibility(it) },
        onUpdateLayerOpacity = { id, op -> viewModel.updateLayerOpacity(id, op) },
        onDeleteLayer = { viewModel.deleteLayer(it) },
        onMoveLayerUp = { viewModel.moveLayerUp(it) },
        onMoveLayerDown = { viewModel.moveLayerDown(it) },
        modifier = modifier
    )
}

@Composable
fun EditorContent(
    uiState: EditorUiState,
    snackbarHostState: SnackbarHostState,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onToggleLayers: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onScaleChange: (Float) -> Unit,
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
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onResetAdjustments: () -> Unit,
    onSliderStart: () -> Unit,
    onDismissLayers: () -> Unit,
    onSelectLayer: (String) -> Unit,
    onToggleLayerVisibility: (String) -> Unit,
    onUpdateLayerOpacity: (String, Float) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onMoveLayerUp: (String) -> Unit,
    onMoveLayerDown: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LumenDarkBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopBar(
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
                onExport = onExport,
                onShare = onShare,
                onToggleLayers = onToggleLayers
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Center photo preview section
            PreviewSection(
                uiState = uiState,
                onHoldStart = onHoldStart,
                onHoldEnd = onHoldEnd,
                onScaleChange = onScaleChange,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )

            // 2. Tools panel (LazyRow + animated subpanels)
            ToolsPanel(
                selectedTool = uiState.selectedTool,
                selectedFilter = uiState.selectedFilter,
                brushSize = uiState.brushSize,
                brushOpacity = uiState.brushOpacity,
                brushColor = uiState.brushColor,
                textValue = uiState.textValue,
                textSize = uiState.textSize,
                textColor = uiState.textColor,
                cropRatio = uiState.cropRatio,
                selectedSticker = uiState.selectedSticker,
                frameStyle = uiState.frameStyle,
                onToolSelect = onToolSelect,
                onFilterSelect = onFilterSelect,
                onBrushSizeChange = onBrushSizeChange,
                onBrushOpacityChange = onBrushOpacityChange,
                onBrushColorChange = onBrushColorChange,
                onTextChange = onTextChange,
                onTextSizeChange = onTextSizeChange,
                onTextColorChange = onTextColorChange,
                onCropRatioChange = onCropRatioChange,
                onStickerSelect = onStickerSelect,
                onFrameStyleChange = onFrameStyleChange
            )

            // 3. Color correction adjustment panel
            AdjustPanel(
                brightness = uiState.brightness,
                contrast = uiState.contrast,
                saturation = uiState.saturation,
                temperature = uiState.temperature,
                onBrightnessChange = onBrightnessChange,
                onContrastChange = onContrastChange,
                onSaturationChange = onSaturationChange,
                onTemperatureChange = onTemperatureChange,
                onResetAdjustments = onResetAdjustments,
                onSliderStart = onSliderStart,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // 4. Layers BottomSheet
        LayersBottomSheet(
            isOpen = uiState.isLayersSheetOpen,
            layers = uiState.layers,
            selectedLayerId = uiState.selectedLayerId,
            onDismiss = onDismissLayers,
            onSelectLayer = onSelectLayer,
            onToggleVisibility = onToggleLayerVisibility,
            onUpdateOpacity = onUpdateLayerOpacity,
            onDeleteLayer = onDeleteLayer,
            onMoveUp = onMoveLayerUp,
            onMoveDown = onMoveLayerDown
        )
    }
}

@Composable
private fun PreviewSection(
    uiState: EditorUiState,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = uiState.scale,
        label = "photoScale"
    )

    val aspect = when (uiState.cropRatio) {
        "1:1" -> 1.0f
        "4:5" -> 0.8f
        "16:9" -> 1.777f
        "9:16" -> 0.5625f
        else -> 0.8f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top info chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LumenDarkCard)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${(uiState.scale * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumenCyanSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LumenDarkCard)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "1080x1350",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = LumenTextSecondary
                    )
                }
            }

            Text(
                text = "Зажмите фото для сравнения",
                fontSize = 11.sp,
                color = LumenTextMuted
            )
        }

        // Photo card with shadow and 16dp rounded corners
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(aspect)
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, LumenBorder, RoundedCornerShape(16.dp))
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onHoldStart()
                            tryAwaitRelease()
                            onHoldEnd()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Actual Canvas Artwork
            ArtworkCanvas(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
            )

            // Frame overlay
            FrameOverlay(frameStyle = uiState.frameStyle)

            // Hold indicator overlay
            if (uiState.isComparingOriginal) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .border(1.dp, LumenCyanSecondary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "ОРИГИНАЛ (БЕЗ ОБРАБОТКИ)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = LumenCyanSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Scale slider under photo (50% - 200%)
        Row(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onScaleChange(uiState.scale - 0.1f) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = "Zoom Out",
                    tint = LumenTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Slider(
                value = uiState.scale,
                onValueChange = onScaleChange,
                valueRange = 0.5f..2.0f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = LumenCyanSecondary,
                    activeTrackColor = LumenPurplePrimary,
                    inactiveTrackColor = LumenBorder
                )
            )

            IconButton(
                onClick = { onScaleChange(uiState.scale + 0.1f) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom In",
                    tint = LumenTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(LumenDarkCard)
                    .border(0.5.dp, LumenBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${(uiState.scale * 100).toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumenCyanSecondary
                )
            }
        }
    }
}

@Composable
private fun ArtworkCanvas(
    uiState: EditorUiState,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val activeColorFilter = remember(uiState.activeColorMatrix) {
        ColorFilter.colorMatrix(uiState.activeColorMatrix)
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                this.colorFilter = activeColorFilter
            }
            canvas.saveLayer(size.toRect(), paint)

            // Draw layers in bottom-to-top order
            uiState.layers.reversed().forEach { layer ->
                if (layer.isVisible) {
                    when (layer.type) {
                        LayerType.BACKGROUND -> drawBackgroundLayer(w, h, layer.opacity)
                        LayerType.PORTRAIT -> drawPortraitLayer(w, h, layer.opacity)
                        LayerType.STICKER -> drawStickerLayer(w, h, layer.opacity)
                        LayerType.TEXT -> drawTextLayer(w, h, layer.opacity, uiState.textValue, uiState.textColor, uiState.textSize, textMeasurer)
                        LayerType.FILTER -> drawFilterVignetteLayer(w, h, layer.opacity)
                    }
                }
            }

            canvas.restore()
        }
    }
}

private fun DrawScope.drawBackgroundLayer(w: Float, h: Float, opacity: Float) {
    // Sunset gradient sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E0533),
                Color(0xFF4A148C),
                Color(0xFFD81B60),
                Color(0xFFFF5722),
                Color(0xFFFFB300),
                Color(0xFFFFE082)
            ),
            startY = 0f,
            endY = h * 0.65f
        ),
        size = Size(w, h * 0.65f),
        alpha = opacity
    )

    // Radiant Sun
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFFFF9C4),
                Color(0xFFFFD54F),
                Color(0x00FFB300)
            ),
            center = Offset(w * 0.5f, h * 0.48f),
            radius = w * 0.32f
        ),
        radius = w * 0.32f,
        center = Offset(w * 0.5f, h * 0.48f),
        alpha = opacity
    )

    // Distant mountain ridge (far back)
    val distantPath = Path().apply {
        moveTo(0f, h * 0.52f)
        lineTo(w * 0.18f, h * 0.42f)
        lineTo(w * 0.38f, h * 0.49f)
        lineTo(w * 0.52f, h * 0.38f)
        lineTo(w * 0.72f, h * 0.47f)
        lineTo(w * 0.88f, h * 0.41f)
        lineTo(w, h * 0.50f)
        lineTo(w, h * 0.65f)
        lineTo(0f, h * 0.65f)
        close()
    }
    drawPath(
        path = distantPath,
        color = Color(0xFF4527A0),
        alpha = opacity * 0.85f
    )

    // Midground mountain ridge
    val midMountainPath = Path().apply {
        moveTo(0f, h * 0.58f)
        lineTo(w * 0.28f, h * 0.46f)
        lineTo(w * 0.58f, h * 0.54f)
        lineTo(w * 0.82f, h * 0.45f)
        lineTo(w, h * 0.55f)
        lineTo(w, h * 0.65f)
        lineTo(0f, h * 0.65f)
        close()
    }
    drawPath(
        path = midMountainPath,
        color = Color(0xFF26123D),
        alpha = opacity
    )

    // Lake / Water reflection
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF8F00),
                Color(0xFFC2185B),
                Color(0xFF311B92),
                Color(0xFF0D051D)
            ),
            startY = h * 0.65f,
            endY = h
        ),
        topLeft = Offset(0f, h * 0.65f),
        size = Size(w, h * 0.35f),
        alpha = opacity
    )

    // Sun reflection streaks on water
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xAAFFF9C4), Color(0x66FFB300), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.72f),
            radius = w * 0.28f
        ),
        topLeft = Offset(w * 0.25f, h * 0.65f),
        size = Size(w * 0.5f, h * 0.2f),
        alpha = opacity * 0.8f
    )
}

private fun DrawScope.drawPortraitLayer(w: Float, h: Float, opacity: Float) {
    // Foreground mountain ledge & cliffs
    val foregroundCliff = Path().apply {
        moveTo(0f, h * 0.68f)
        lineTo(w * 0.45f, h * 0.72f)
        lineTo(w * 0.35f, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = foregroundCliff,
        color = Color(0xFF10071C),
        alpha = opacity
    )

    // Right foreground ridge
    val rightCliff = Path().apply {
        moveTo(w, h * 0.70f)
        lineTo(w * 0.55f, h * 0.78f)
        lineTo(w * 0.65f, h)
        lineTo(w, h)
        close()
    }
    drawPath(
        path = rightCliff,
        color = Color(0xFF0B0414),
        alpha = opacity
    )

    // Silhouette of pine trees on left ridge
    drawPineTree(Offset(w * 0.08f, h * 0.68f), w * 0.09f, h * 0.12f, opacity)
    drawPineTree(Offset(w * 0.18f, h * 0.70f), w * 0.07f, h * 0.09f, opacity)
    drawPineTree(Offset(w * 0.28f, h * 0.72f), w * 0.06f, h * 0.08f, opacity)

    // Lone traveler / photographer silhouette on rock
    val personX = w * 0.38f
    val personY = h * 0.70f

    // Head
    drawCircle(
        color = Color(0xFF090312),
        radius = w * 0.022f,
        center = Offset(personX, (personY - h * 0.045f)),
        alpha = opacity
    )
    // Body / coat
    val bodyPath = Path().apply {
        moveTo(personX - w * 0.025f, personY)
        lineTo(personX - w * 0.018f, personY - h * 0.038f)
        lineTo(personX + w * 0.018f, personY - h * 0.038f)
        lineTo(personX + w * 0.025f, personY)
        close()
    }
    drawPath(path = bodyPath, color = Color(0xFF090312), alpha = opacity)

    // Camera on tripod
    drawLine(
        color = Color(0xFF090312),
        start = Offset(personX + w * 0.035f, personY - h * 0.032f),
        end = Offset(personX + w * 0.022f, personY),
        strokeWidth = 3f,
        alpha = opacity
    )
    drawLine(
        color = Color(0xFF090312),
        start = Offset(personX + w * 0.035f, personY - h * 0.032f),
        end = Offset(personX + w * 0.048f, personY),
        strokeWidth = 3f,
        alpha = opacity
    )
    drawRect(
        color = Color(0xFF090312),
        topLeft = Offset(personX + w * 0.028f, personY - h * 0.038f),
        size = Size(w * 0.022f, h * 0.012f),
        alpha = opacity
    )
}

private fun DrawScope.drawPineTree(base: Offset, treeWidth: Float, treeHeight: Float, opacity: Float) {
    val treePath = Path().apply {
        moveTo(base.x, base.y - treeHeight)
        lineTo(base.x + treeWidth * 0.35f, base.y - treeHeight * 0.65f)
        lineTo(base.x + treeWidth * 0.2f, base.y - treeHeight * 0.65f)
        lineTo(base.x + treeWidth * 0.45f, base.y - treeHeight * 0.3f)
        lineTo(base.x + treeWidth * 0.3f, base.y - treeHeight * 0.3f)
        lineTo(base.x + treeWidth * 0.5f, base.y)
        lineTo(base.x - treeWidth * 0.5f, base.y)
        lineTo(base.x - treeWidth * 0.3f, base.y - treeHeight * 0.3f)
        lineTo(base.x - treeWidth * 0.45f, base.y - treeHeight * 0.3f)
        lineTo(base.x - treeWidth * 0.2f, base.y - treeHeight * 0.65f)
        lineTo(base.x - treeWidth * 0.35f, base.y - treeHeight * 0.65f)
        close()
    }
    drawPath(path = treePath, color = Color(0xFF090312), alpha = opacity)
}

private fun DrawScope.drawStickerLayer(w: Float, h: Float, opacity: Float) {
    // Retro stamp circle badge at top-right
    val cx = w * 0.82f
    val cy = h * 0.16f
    val rad = w * 0.11f

    drawCircle(
        color = Color(0x3300E5FF),
        radius = rad + 6f,
        center = Offset(cx, cy),
        alpha = opacity
    )
    drawCircle(
        color = Color(0x55000000),
        radius = rad,
        center = Offset(cx, cy),
        alpha = opacity
    )
    drawCircle(
        color = Color(0xFF00E5FF),
        radius = rad,
        center = Offset(cx, cy),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f),
        alpha = opacity
    )

    // Starburst rays
    for (i in 0 until 8) {
        val angle = (i * 45f) * (Math.PI / 180f)
        val x1 = cx + (rad * 0.55f * cos(angle)).toFloat()
        val y1 = cy + (rad * 0.55f * sin(angle)).toFloat()
        val x2 = cx + (rad * 0.85f * cos(angle)).toFloat()
        val y2 = cy + (rad * 0.85f * sin(angle)).toFloat()
        drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 2.5f,
            alpha = opacity
        )
    }

    drawCircle(
        color = Color(0xFFFFD54F),
        radius = rad * 0.35f,
        center = Offset(cx, cy),
        alpha = opacity
    )
}

private fun DrawScope.drawTextLayer(
    w: Float,
    h: Float,
    opacity: Float,
    text: String,
    color: Color,
    sizeSp: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    if (text.isBlank()) return

    val textStyle = TextStyle(
        color = color.copy(alpha = opacity),
        fontSize = sizeSp.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif,
        letterSpacing = 2.sp
    )

    val shadowStyle = TextStyle(
        color = Color.Black.copy(alpha = opacity * 0.75f),
        fontSize = sizeSp.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif,
        letterSpacing = 2.sp
    )

    val layoutResult = textMeasurer.measure(text = text, style = textStyle)
    val textX = (w - layoutResult.size.width) / 2f
    val textY = h * 0.84f

    // Drop shadow
    drawText(
        textMeasurer = textMeasurer,
        text = text,
        style = shadowStyle,
        topLeft = Offset(textX + 3f, textY + 3f)
    )

    // Main text
    drawText(
        textMeasurer = textMeasurer,
        text = text,
        style = textStyle,
        topLeft = Offset(textX, textY)
    )
}

private fun DrawScope.drawFilterVignetteLayer(w: Float, h: Float, opacity: Float) {
    // Vignette / subtle film tone
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x33000000),
                Color(0xCC000000)
            ),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = w * 0.85f
        ),
        size = Size(w, h),
        alpha = opacity
    )
}

@Composable
private fun FrameOverlay(frameStyle: String) {
    when (frameStyle) {
        "Тонкая" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            )
        }
        "Полароид" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(10.dp, Color(0xFFF5F5F0), RoundedCornerShape(16.dp))
            )
        }
        "Неон" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(4.dp, LumenCyanSecondary, RoundedCornerShape(16.dp))
            )
        }
        "Золото" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(5.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
            )
        }
    }
}

@Preview(name = "EditorScreen Preview", showBackground = true, backgroundColor = 0xFF121212, heightDp = 800)
@Composable
fun EditorScreenPreview() {
    LumenStudioTheme {
        EditorContent(
            uiState = EditorUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onUndo = {},
            onRedo = {},
            onExport = {},
            onShare = {},
            onToggleLayers = {},
            onHoldStart = {},
            onHoldEnd = {},
            onScaleChange = {},
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
            onFrameStyleChange = {},
            onBrightnessChange = {},
            onContrastChange = {},
            onSaturationChange = {},
            onTemperatureChange = {},
            onResetAdjustments = {},
            onSliderStart = {},
            onDismissLayers = {},
            onSelectLayer = {},
            onToggleLayerVisibility = {},
            onUpdateLayerOpacity = { _, _ -> },
            onDeleteLayer = {},
            onMoveLayerUp = {},
            onMoveLayerDown = {}
        )
    }
}
