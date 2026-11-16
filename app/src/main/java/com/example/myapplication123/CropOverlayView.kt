package com.example.myapplication123

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot

class CropOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dimPaint = Paint().apply {
        color = Color.parseColor("#B3000000") // 70% dark dimming
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 3f * resources.displayMetrics.density
        style = Paint.Style.STROKE
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66FFFFFF")
        strokeWidth = 1f * resources.displayMetrics.density
        style = Paint.Style.STROKE
    }

    private val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF") // Neon Cyan
        strokeWidth = 5f * resources.displayMetrics.density
        style = Paint.Style.STROKE
    }

    var imageBounds: RectF = RectF()
        set(value) {
            field = value
            initCropRect()
            invalidate()
        }

    val cropRect: RectF = RectF()

    private var targetAspectRatio: Float? = null // null means free aspect ratio

    private var touchMode = TOUCH_NONE
    private var lastX = 0f
    private var lastY = 0f

    private val handleRadius = 24f * resources.displayMetrics.density
    private val cornerLength = 20f * resources.displayMetrics.density

    companion object {
        private const val TOUCH_NONE = 0
        private const val TOUCH_INSIDE = 1
        private const val TOUCH_TOP_LEFT = 2
        private const val TOUCH_TOP_RIGHT = 3
        private const val TOUCH_BOTTOM_LEFT = 4
        private const val TOUCH_BOTTOM_RIGHT = 5
    }

    fun setAspectRatio(ratio: Float?) {
        targetAspectRatio = ratio
        applyAspectRatio()
        invalidate()
    }

    private fun initCropRect() {
        if (imageBounds.width() <= 0 || imageBounds.height() <= 0) return
        val margin = 20f * resources.displayMetrics.density
        val left = imageBounds.left + margin
        val top = imageBounds.top + margin
        val right = imageBounds.right - margin
        val bottom = imageBounds.bottom - margin
        cropRect.set(left, top, right, bottom)
        applyAspectRatio()
    }

    private fun applyAspectRatio() {
        val ratio = targetAspectRatio ?: return
        if (cropRect.width() <= 0 || cropRect.height() <= 0) return

        val currentW = cropRect.width()
        val cx = cropRect.centerX()
        val cy = cropRect.centerY()

        var newW = currentW
        var newH = newW / ratio

        if (newH > imageBounds.height() * 0.9f) {
            newH = imageBounds.height() * 0.9f
            newW = newH * ratio
        }
        if (newW > imageBounds.width() * 0.9f) {
            newW = imageBounds.width() * 0.9f
            newH = newW / ratio
        }

        cropRect.set(
            cx - newW / 2f,
            cy - newH / 2f,
            cx + newW / 2f,
            cy + newH / 2f
        )
        clampToImageBounds()
    }

    private fun clampToImageBounds() {
        if (cropRect.left < imageBounds.left) cropRect.offset(imageBounds.left - cropRect.left, 0f)
        if (cropRect.top < imageBounds.top) cropRect.offset(0f, imageBounds.top - cropRect.top)
        if (cropRect.right > imageBounds.right) cropRect.offset(imageBounds.right - cropRect.right, 0f)
        if (cropRect.bottom > imageBounds.bottom) cropRect.offset(0f, imageBounds.bottom - cropRect.bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cropRect.width() <= 0 || cropRect.height() <= 0) return

        // 1. Draw dimmed areas around crop rect
        // Top
        canvas.drawRect(0f, 0f, width.toFloat(), cropRect.top, dimPaint)
        // Bottom
        canvas.drawRect(0f, cropRect.bottom, width.toFloat(), height.toFloat(), dimPaint)
        // Left
        canvas.drawRect(0f, cropRect.top, cropRect.left, cropRect.bottom, dimPaint)
        // Right
        canvas.drawRect(cropRect.right, cropRect.top, width.toFloat(), cropRect.bottom, dimPaint)

        // 2. Draw border
        canvas.drawRect(cropRect, borderPaint)

        // 3. Draw rule-of-thirds grid lines
        val oneThirdW = cropRect.width() / 3f
        val oneThirdH = cropRect.height() / 3f
        canvas.drawLine(cropRect.left + oneThirdW, cropRect.top, cropRect.left + oneThirdW, cropRect.bottom, gridPaint)
        canvas.drawLine(cropRect.left + 2 * oneThirdW, cropRect.top, cropRect.left + 2 * oneThirdW, cropRect.bottom, gridPaint)
        canvas.drawLine(cropRect.left, cropRect.top + oneThirdH, cropRect.right, cropRect.top + oneThirdH, gridPaint)
        canvas.drawLine(cropRect.left, cropRect.top + 2 * oneThirdH, cropRect.right, cropRect.top + 2 * oneThirdH, gridPaint)

        // 4. Draw corner indicators
        val cl = cornerLength.coerceAtMost(cropRect.width() / 3f).coerceAtMost(cropRect.height() / 3f)
        // Top-Left
        canvas.drawLine(cropRect.left, cropRect.top, cropRect.left + cl, cropRect.top, cornerPaint)
        canvas.drawLine(cropRect.left, cropRect.top, cropRect.left, cropRect.top + cl, cornerPaint)
        // Top-Right
        canvas.drawLine(cropRect.right - cl, cropRect.top, cropRect.right, cropRect.top, cornerPaint)
        canvas.drawLine(cropRect.right, cropRect.top, cropRect.right, cropRect.top + cl, cornerPaint)
        // Bottom-Left
        canvas.drawLine(cropRect.left, cropRect.bottom, cropRect.left + cl, cropRect.bottom, cornerPaint)
        canvas.drawLine(cropRect.left, cropRect.bottom - cl, cropRect.left, cropRect.bottom, cornerPaint)
        // Bottom-Right
        canvas.drawLine(cropRect.right - cl, cropRect.bottom, cropRect.right, cropRect.bottom, cornerPaint)
        canvas.drawLine(cropRect.right, cropRect.bottom - cl, cropRect.right, cropRect.bottom, cornerPaint)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || visibility != VISIBLE) return false

        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                touchMode = detectTouchArea(x, y)
                return touchMode != TOUCH_NONE
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY
                handleMove(dx, dy)
                lastX = x
                lastY = y
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                touchMode = TOUCH_NONE
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                touchMode = TOUCH_NONE
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun detectTouchArea(x: Float, y: Float): Int {
        val hr = handleRadius
        if (hypot(x - cropRect.left, y - cropRect.top) <= hr) return TOUCH_TOP_LEFT
        if (hypot(x - cropRect.right, y - cropRect.top) <= hr) return TOUCH_TOP_RIGHT
        if (hypot(x - cropRect.left, y - cropRect.bottom) <= hr) return TOUCH_BOTTOM_LEFT
        if (hypot(x - cropRect.right, y - cropRect.bottom) <= hr) return TOUCH_BOTTOM_RIGHT
        if (cropRect.contains(x, y)) return TOUCH_INSIDE
        return TOUCH_NONE
    }

    private fun handleMove(dx: Float, dy: Float) {
        val minSize = 80f * resources.displayMetrics.density
        when (touchMode) {
            TOUCH_INSIDE -> {
                val newL = cropRect.left + dx
                val newT = cropRect.top + dy
                val newR = cropRect.right + dx
                val newB = cropRect.bottom + dy

                val adjX = when {
                    newL < imageBounds.left -> imageBounds.left - cropRect.left
                    newR > imageBounds.right -> imageBounds.right - cropRect.right
                    else -> dx
                }
                val adjY = when {
                    newT < imageBounds.top -> imageBounds.top - cropRect.top
                    newB > imageBounds.bottom -> imageBounds.bottom - cropRect.bottom
                    else -> dy
                }
                cropRect.offset(adjX, adjY)
            }
            TOUCH_TOP_LEFT -> {
                var newL = (cropRect.left + dx).coerceIn(imageBounds.left, cropRect.right - minSize)
                var newT = (cropRect.top + dy).coerceIn(imageBounds.top, cropRect.bottom - minSize)
                targetAspectRatio?.let { ratio ->
                    val w = cropRect.right - newL
                    val h = w / ratio
                    newT = cropRect.bottom - h
                    if (newT < imageBounds.top) {
                        newT = imageBounds.top
                        newL = cropRect.right - (cropRect.bottom - newT) * ratio
                    }
                }
                cropRect.left = newL
                cropRect.top = newT
            }
            TOUCH_TOP_RIGHT -> {
                var newR = (cropRect.right + dx).coerceIn(cropRect.left + minSize, imageBounds.right)
                var newT = (cropRect.top + dy).coerceIn(imageBounds.top, cropRect.bottom - minSize)
                targetAspectRatio?.let { ratio ->
                    val w = newR - cropRect.left
                    val h = w / ratio
                    newT = cropRect.bottom - h
                    if (newT < imageBounds.top) {
                        newT = imageBounds.top
                        newR = cropRect.left + (cropRect.bottom - newT) * ratio
                    }
                }
                cropRect.right = newR
                cropRect.top = newT
            }
            TOUCH_BOTTOM_LEFT -> {
                var newL = (cropRect.left + dx).coerceIn(imageBounds.left, cropRect.right - minSize)
                var newB = (cropRect.bottom + dy).coerceIn(cropRect.top + minSize, imageBounds.bottom)
                targetAspectRatio?.let { ratio ->
                    val w = cropRect.right - newL
                    val h = w / ratio
                    newB = cropRect.top + h
                    if (newB > imageBounds.bottom) {
                        newB = imageBounds.bottom
                        newL = cropRect.right - (newB - cropRect.top) * ratio
                    }
                }
                cropRect.left = newL
                cropRect.bottom = newB
            }
            TOUCH_BOTTOM_RIGHT -> {
                var newR = (cropRect.right + dx).coerceIn(cropRect.left + minSize, imageBounds.right)
                var newB = (cropRect.bottom + dy).coerceIn(cropRect.top + minSize, imageBounds.bottom)
                targetAspectRatio?.let { ratio ->
                    val w = newR - cropRect.left
                    val h = w / ratio
                    newB = cropRect.top + h
                    if (newB > imageBounds.bottom) {
                        newB = imageBounds.bottom
                        newR = cropRect.left + (newB - cropRect.top) * ratio
                    }
                }
                cropRect.right = newR
                cropRect.bottom = newB
            }
        }
        clampToImageBounds()
    }

    fun getCropRectInBitmap(bitmapWidth: Int, bitmapHeight: Int): Rect? {
        if (imageBounds.width() <= 0 || imageBounds.height() <= 0) return null
        val scaleX = bitmapWidth.toFloat() / imageBounds.width()
        val scaleY = bitmapHeight.toFloat() / imageBounds.height()

        val left = ((cropRect.left - imageBounds.left) * scaleX).toInt().coerceIn(0, bitmapWidth - 1)
        val top = ((cropRect.top - imageBounds.top) * scaleY).toInt().coerceIn(0, bitmapHeight - 1)
        val right = ((cropRect.right - imageBounds.left) * scaleX).toInt().coerceIn(left + 1, bitmapWidth)
        val bottom = ((cropRect.bottom - imageBounds.top) * scaleY).toInt().coerceIn(top + 1, bitmapHeight)

        return Rect(left, top, right, bottom)
    }
}
