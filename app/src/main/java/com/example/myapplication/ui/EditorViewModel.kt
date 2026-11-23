package com.example.myapplication.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HistorySnapshot(
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val temperature: Float,
    val selectedFilter: FilterPreset,
    val layers: List<LayerItem>
)

data class EditorUiState(
    val brightness: Float = 0f,         // -100..100
    val contrast: Float = 100f,         // 0..200
    val saturation: Float = 100f,       // 0..200
    val temperature: Float = 0f,        // -100..100
    val scale: Float = 1.0f,            // 0.5f..2.0f
    val selectedTool: ToolType = ToolType.FILTERS,
    val selectedFilter: FilterPreset = FilterPreset.ORIGINAL,
    val layers: List<LayerItem> = FakeData.initialLayers,
    val selectedLayerId: String = "layer_bg",
    val isComparingOriginal: Boolean = false,
    val isLayersSheetOpen: Boolean = false,

    // Tool specific properties
    val brushSize: Float = 25f,         // 1..100
    val brushOpacity: Float = 80f,      // 0..100
    val brushColor: Color = Color(0xFF00E5FF),
    val textValue: String = "Sunset",
    val textSize: Float = 36f,          // 14..72
    val textColor: Color = Color.White,
    val cropRatio: String = "Оригинал",
    val selectedSticker: String = "☀️",
    val frameStyle: String = "Нет",

    val canUndo: Boolean = false,
    val canRedo: Boolean = false
) {
    val activeColorMatrix: ColorMatrix
        get() = if (isComparingOriginal) {
            ColorMatrix(FakeData.identityMatrix())
        } else {
            FakeData.computeCombinedMatrix(
                brightness = brightness,
                contrast = contrast,
                saturation = saturation,
                temperature = temperature,
                preset = selectedFilter
            )
        }
}

class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<HistorySnapshot>()
    private val redoStack = mutableListOf<HistorySnapshot>()

    private fun currentSnapshot(): HistorySnapshot {
        val s = _uiState.value
        return HistorySnapshot(
            brightness = s.brightness,
            contrast = s.contrast,
            saturation = s.saturation,
            temperature = s.temperature,
            selectedFilter = s.selectedFilter,
            layers = s.layers
        )
    }

    private fun pushUndoSnapshot() {
        undoStack.add(currentSnapshot())
        redoStack.clear()
        updateUndoRedoStatus()
    }

    private fun updateUndoRedoStatus() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun onSliderValueChangeStarted() {
        pushUndoSnapshot()
    }

    fun onBrightnessChange(value: Float) {
        _uiState.update { it.copy(brightness = value) }
    }

    fun onContrastChange(value: Float) {
        _uiState.update { it.copy(contrast = value) }
    }

    fun onSaturationChange(value: Float) {
        _uiState.update { it.copy(saturation = value) }
    }

    fun onTemperatureChange(value: Float) {
        _uiState.update { it.copy(temperature = value) }
    }

    fun onScaleChange(value: Float) {
        _uiState.update { it.copy(scale = value.coerceIn(0.5f, 2.0f)) }
    }

    fun selectTool(tool: ToolType) {
        _uiState.update { it.copy(selectedTool = tool) }
    }

    fun selectFilter(filter: FilterPreset) {
        pushUndoSnapshot()
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun setComparingOriginal(comparing: Boolean) {
        _uiState.update { it.copy(isComparingOriginal = comparing) }
    }

    fun setLayersSheetOpen(open: Boolean) {
        _uiState.update { it.copy(isLayersSheetOpen = open) }
    }

    fun selectLayer(id: String) {
        _uiState.update { it.copy(selectedLayerId = id) }
    }

    fun toggleLayerVisibility(id: String) {
        pushUndoSnapshot()
        _uiState.update { state ->
            val updated = state.layers.map {
                if (it.id == id) it.copy(isVisible = !it.isVisible) else it
            }
            state.copy(layers = updated)
        }
    }

    fun updateLayerOpacity(id: String, opacity: Float) {
        _uiState.update { state ->
            val updated = state.layers.map {
                if (it.id == id) it.copy(opacity = opacity.coerceIn(0f, 1f)) else it
            }
            state.copy(layers = updated)
        }
    }

    fun deleteLayer(id: String) {
        pushUndoSnapshot()
        _uiState.update { state ->
            if (state.layers.size <= 1) return@update state
            val updated = state.layers.filter { it.id != id }
            state.copy(
                layers = updated,
                selectedLayerId = updated.firstOrNull()?.id ?: ""
            )
        }
    }

    fun moveLayerUp(id: String) {
        pushUndoSnapshot()
        _uiState.update { state ->
            val list = state.layers.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index > 0) {
                val item = list.removeAt(index)
                list.add(index - 1, item)
            }
            state.copy(layers = list)
        }
    }

    fun moveLayerDown(id: String) {
        pushUndoSnapshot()
        _uiState.update { state ->
            val list = state.layers.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index in 0 until list.lastIndex) {
                val item = list.removeAt(index)
                list.add(index + 1, item)
            }
            state.copy(layers = list)
        }
    }

    fun onBrushSizeChange(size: Float) {
        _uiState.update { it.copy(brushSize = size) }
    }

    fun onBrushOpacityChange(opacity: Float) {
        _uiState.update { it.copy(brushOpacity = opacity) }
    }

    fun onBrushColorChange(color: Color) {
        _uiState.update { it.copy(brushColor = color) }
    }

    fun onTextChange(text: String) {
        _uiState.update { it.copy(textValue = text) }
    }

    fun onTextSizeChange(size: Float) {
        _uiState.update { it.copy(textSize = size) }
    }

    fun onTextColorChange(color: Color) {
        _uiState.update { it.copy(textColor = color) }
    }

    fun onCropRatioChange(ratio: String) {
        _uiState.update { it.copy(cropRatio = ratio) }
    }

    fun onStickerSelect(sticker: String) {
        _uiState.update { it.copy(selectedSticker = sticker) }
    }

    fun onFrameStyleChange(style: String) {
        _uiState.update { it.copy(frameStyle = style) }
    }

    fun resetAll() {
        pushUndoSnapshot()
        _uiState.update {
            it.copy(
                brightness = 0f,
                contrast = 100f,
                saturation = 100f,
                temperature = 0f,
                scale = 1.0f,
                selectedFilter = FilterPreset.ORIGINAL,
                layers = FakeData.initialLayers
            )
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(currentSnapshot())
        applySnapshot(previous)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(currentSnapshot())
        applySnapshot(next)
    }

    private fun applySnapshot(snapshot: HistorySnapshot) {
        _uiState.update {
            it.copy(
                brightness = snapshot.brightness,
                contrast = snapshot.contrast,
                saturation = snapshot.saturation,
                temperature = snapshot.temperature,
                selectedFilter = snapshot.selectedFilter,
                layers = snapshot.layers,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }
}
