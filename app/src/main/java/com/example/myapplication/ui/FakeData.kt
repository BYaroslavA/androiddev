package com.example.myapplication.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix

enum class LayerType {
    BACKGROUND,
    PORTRAIT,
    TEXT,
    STICKER,
    FILTER
}

data class LayerItem(
    val id: String,
    val name: String,
    val type: LayerType,
    val isVisible: Boolean = true,
    val opacity: Float = 1.0f
)

enum class ToolType(val title: String) {
    CROP("Обрезка"),
    BRUSH("Кисть"),
    TEXT("Текст"),
    FILTERS("Фильтры"),
    STICKERS("Стикеры"),
    ERASER("Ластик"),
    FRAMES("Рамки")
}

enum class FilterPreset(val displayName: String) {
    ORIGINAL("Original"),
    VIVID("Vivid"),
    MONO("Mono"),
    WARM("Warm"),
    COLD("Cold"),
    VINTAGE("Vintage")
}

object FakeData {
    val initialLayers = listOf(
        LayerItem(
            id = "layer_filter",
            name = "Фильтр",
            type = LayerType.FILTER,
            isVisible = true,
            opacity = 0.85f
        ),
        LayerItem(
            id = "layer_sticker",
            name = "Стикер",
            type = LayerType.STICKER,
            isVisible = true,
            opacity = 0.95f
        ),
        LayerItem(
            id = "layer_text",
            name = "Текст Sunset",
            type = LayerType.TEXT,
            isVisible = true,
            opacity = 1.0f
        ),
        LayerItem(
            id = "layer_portrait",
            name = "Портрет",
            type = LayerType.PORTRAIT,
            isVisible = true,
            opacity = 1.0f
        ),
        LayerItem(
            id = "layer_bg",
            name = "Фон",
            type = LayerType.BACKGROUND,
            isVisible = true,
            opacity = 1.0f
        )
    )

    val brushColors = listOf(
        Color(0xFF00E5FF),
        Color(0xFF7C4DFF),
        Color(0xFFFF5252),
        Color(0xFFFFEA00),
        Color(0xFFFFFFFF)
    )

    val stickers = listOf("☀️", "⛰️", "🦅", "✨", "🌙", "🌸")
    val aspectRatios = listOf("Оригинал", "1:1", "4:5", "16:9", "9:16")
    val frameStyles = listOf("Нет", "Тонкая", "Полароид", "Неон", "Золото")

    fun getFilterMatrix(preset: FilterPreset): FloatArray {
        return when (preset) {
            FilterPreset.ORIGINAL -> identityMatrix()
            FilterPreset.VIVID -> floatArrayOf(
                1.30f, 0.00f, 0.00f, 0.00f, 5.0f,
                0.00f, 1.30f, 0.00f, 0.00f, 5.0f,
                0.00f, 0.00f, 1.25f, 0.00f, 5.0f,
                0.00f, 0.00f, 0.00f, 1.00f, 0.0f
            )
            FilterPreset.MONO -> {
                val lr = 0.2126f
                val lg = 0.7152f
                val lb = 0.0722f
                floatArrayOf(
                    lr * 1.15f, lg * 1.15f, lb * 1.15f, 0f, -10f,
                    lr * 1.15f, lg * 1.15f, lb * 1.15f, 0f, -10f,
                    lr * 1.15f, lg * 1.15f, lb * 1.15f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )
            }
            FilterPreset.WARM -> floatArrayOf(
                1.15f, 0.00f, 0.00f, 0.00f, 25.0f,
                0.00f, 1.05f, 0.00f, 0.00f, 10.0f,
                0.00f, 0.00f, 0.85f, 0.00f, -20.0f,
                0.00f, 0.00f, 0.00f, 1.00f, 0.0f
            )
            FilterPreset.COLD -> floatArrayOf(
                0.85f, 0.00f, 0.00f, 0.00f, -15.0f,
                0.00f, 1.00f, 0.00f, 0.00f, 0.0f,
                0.00f, 0.00f, 1.25f, 0.00f, 30.0f,
                0.00f, 0.00f, 0.00f, 1.00f, 0.0f
            )
            FilterPreset.VINTAGE -> floatArrayOf(
                0.393f + 0.2f, 0.769f, 0.189f, 0f, 15f,
                0.349f, 0.686f + 0.2f, 0.168f, 0f, 10f,
                0.272f, 0.534f, 0.131f + 0.2f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            )
        }
    }

    fun identityMatrix(): FloatArray {
        return floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    }

    fun multiplyMatrices(a: FloatArray, b: FloatArray): FloatArray {
        val result = FloatArray(20)
        for (row in 0 until 4) {
            for (col in 0 until 4) {
                var sum = 0f
                for (i in 0 until 4) {
                    sum += a[row * 5 + i] * b[i * 5 + col]
                }
                result[row * 5 + col] = sum
            }
            var offsetSum = a[row * 5 + 4]
            for (i in 0 until 4) {
                offsetSum += a[row * 5 + i] * b[i * 5 + 4]
            }
            result[row * 5 + 4] = offsetSum
        }
        return result
    }

    fun computeCombinedMatrix(
        brightness: Float, // -100..100
        contrast: Float,   // 0..200
        saturation: Float, // 0..200
        temperature: Float,// -100..100
        preset: FilterPreset
    ): ColorMatrix {
        val presetMat = getFilterMatrix(preset)

        // 1. Brightness
        val bOffset = (brightness / 100f) * 128f
        val brightMat = floatArrayOf(
            1f, 0f, 0f, 0f, bOffset,
            0f, 1f, 0f, 0f, bOffset,
            0f, 0f, 1f, 0f, bOffset,
            0f, 0f, 0f, 1f, 0f
        )

        // 2. Contrast
        val c = contrast / 100f
        val cOffset = (1f - c) * 128f
        val contrastMat = floatArrayOf(
            c, 0f, 0f, 0f, cOffset,
            0f, c, 0f, 0f, cOffset,
            0f, 0f, c, 0f, cOffset,
            0f, 0f, 0f, 1f, 0f
        )

        // 3. Saturation
        val s = saturation / 100f
        val lr = 0.2126f
        val lg = 0.7152f
        val lb = 0.0722f
        val invS = 1f - s
        val satMat = floatArrayOf(
            lr * invS + s, lg * invS, lb * invS, 0f, 0f,
            lr * invS, lg * invS + s, lb * invS, 0f, 0f,
            lr * invS, lg * invS, lb * invS + s, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )

        // 4. Temperature
        val t = temperature / 100f
        val tempMat = floatArrayOf(
            1f + (t * 0.15f), 0f, 0f, 0f, t * 20f,
            0f, 1f + (t * 0.05f), 0f, 0f, 0f,
            0f, 0f, 1f - (t * 0.2f), 0f, -t * 25f,
            0f, 0f, 0f, 1f, 0f
        )

        val m1 = multiplyMatrices(brightMat, presetMat)
        val m2 = multiplyMatrices(contrastMat, m1)
        val m3 = multiplyMatrices(satMat, m2)
        val mFinal = multiplyMatrices(tempMat, m3)

        return ColorMatrix(mFinal)
    }
}
