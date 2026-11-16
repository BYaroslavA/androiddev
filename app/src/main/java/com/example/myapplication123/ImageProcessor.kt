package com.example.myapplication123

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max
import kotlin.math.min

enum class FilterType(val displayName: String) {
    ORIGINAL("Оригінал"),
    BW("Ч/Б"),
    SEPIA("Сепія"),
    INVERT("Негатив"),
    VINTAGE("Вінтаж"),
    WARM("Теплий"),
    COOL("Холодний"),
    CYBERPUNK("Кіберпанк"),
    DRAMA("Драма"),
    EMERALD("Смарагд"),
    RUBY("Рубін"),
    GOLDEN("Золотий")
}

object ImageProcessor {

    fun getFilterColorMatrix(filter: FilterType): ColorMatrix {
        val cm = ColorMatrix()
        when (filter) {
            FilterType.ORIGINAL -> {
                // Identity
            }
            FilterType.BW -> {
                cm.setSaturation(0f)
            }
            FilterType.SEPIA -> {
                cm.setSaturation(0f)
                val sepiaMatrix = ColorMatrix(
                    floatArrayOf(
                        1.0f, 0.0f, 0.0f, 0.0f, 40f,
                        0.0f, 0.95f, 0.0f, 0.0f, 20f,
                        0.0f, 0.0f, 0.82f, 0.0f, -20f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                cm.postConcat(sepiaMatrix)
            }
            FilterType.INVERT -> {
                cm.set(
                    floatArrayOf(
                        -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                        0.0f, -1.0f, 0.0f, 0.0f, 255f,
                        0.0f, 0.0f, -1.0f, 0.0f, 255f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
            }
            FilterType.VINTAGE -> {
                val vintageMatrix = ColorMatrix(
                    floatArrayOf(
                        0.9f, 0.0f, 0.0f, 0.0f, 30f,
                        0.0f, 0.8f, 0.0f, 0.0f, 15f,
                        0.0f, 0.0f, 0.6f, 0.0f, 10f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                cm.postConcat(vintageMatrix)
            }
            FilterType.WARM -> {
                val warmMatrix = ColorMatrix(
                    floatArrayOf(
                        1.2f, 0.0f, 0.0f, 0.0f, 25f,
                        0.0f, 1.0f, 0.0f, 0.0f, 10f,
                        0.0f, 0.0f, 0.8f, 0.0f, -15f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                cm.postConcat(warmMatrix)
            }
            FilterType.COOL -> {
                val coolMatrix = ColorMatrix(
                    floatArrayOf(
                        0.8f, 0.0f, 0.0f, 0.0f, -10f,
                        0.0f, 1.0f, 0.0f, 0.0f, 10f,
                        0.0f, 0.0f, 1.3f, 0.0f, 30f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                cm.postConcat(coolMatrix)
            }
            FilterType.CYBERPUNK -> {
                val cyberpunk = ColorMatrix(
                    floatArrayOf(
                        1.4f, 0.0f, 0.5f, 0.0f, 20f,
                        0.0f, 0.8f, 0.4f, 0.0f, -10f,
                        0.3f, 0.0f, 1.6f, 0.0f, 40f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                cm.postConcat(cyberpunk)
            }
            FilterType.DRAMA -> {
                cm.setSaturation(0.4f)
                val drama = ColorMatrix(
                    floatArrayOf(
                        1.35f, 0.0f, 0.0f, 0.0f, -30f,
                        0.0f, 1.35f, 0.0f, 0.0f, -30f,
                        0.0f, 0.0f, 1.35f, 0.0f, -30f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                cm.postConcat(drama)
            }
            FilterType.EMERALD -> {
                val emerald = ColorMatrix(
                    floatArrayOf(
                        0.6f, 0.0f, 0.0f, 0.0f, 0f,
                        0.0f, 1.3f, 0.0f, 0.0f, 25f,
                        0.0f, 0.0f, 0.9f, 0.0f, 10f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                cm.postConcat(emerald)
            }
            FilterType.RUBY -> {
                val ruby = ColorMatrix(
                    floatArrayOf(
                        1.4f, 0.0f, 0.0f, 0.0f, 30f,
                        0.0f, 0.7f, 0.0f, 0.0f, -10f,
                        0.0f, 0.0f, 0.7f, 0.0f, -10f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                cm.postConcat(ruby)
            }
            FilterType.GOLDEN -> {
                val golden = ColorMatrix(
                    floatArrayOf(
                        1.3f, 0.0f, 0.0f, 0.0f, 35f,
                        0.0f, 1.15f, 0.0f, 0.0f, 20f,
                        0.0f, 0.0f, 0.6f, 0.0f, -25f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                cm.postConcat(golden)
            }
        }
        return cm
    }

    /**
     * Combines Brightness [-100..100], Contrast [-100..100], Saturation [-100..100], and FilterType
     */
    fun getCombinedColorMatrix(
        filter: FilterType,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): ColorMatrix {
        val result = ColorMatrix()

        // 1. Filter Matrix
        val filterMatrix = getFilterColorMatrix(filter)
        result.postConcat(filterMatrix)

        // 2. Saturation: -100..100 maps to 0.0..2.0
        val satFactor = (saturation + 100f) / 100f
        val satMatrix = ColorMatrix().apply { setSaturation(satFactor.coerceIn(0f, 3f)) }
        result.postConcat(satMatrix)

        // 3. Contrast: -100..100 maps to 0.2..2.0
        val c = (contrast + 100f) / 100f
        val t = 128f * (1f - c)
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                c, 0f, 0f, 0f, t,
                0f, c, 0f, 0f, t,
                0f, 0f, c, 0f, t,
                0f, 0f, 0f, 1f, 0f
            )
        )
        result.postConcat(contrastMatrix)

        // 4. Brightness: -100..100 maps to translation -100..100
        val brightnessMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, brightness,
                0f, 1f, 0f, 0f, brightness,
                0f, 0f, 1f, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
        result.postConcat(brightnessMatrix)

        return result
    }

    /**
     * Bakes all color matrix adjustments and vignette into a new Bitmap.
     */
    fun applyAll(
        src: Bitmap,
        filter: FilterType,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        vignette: Float
    ): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val matrix = getCombinedColorMatrix(filter, brightness, contrast, saturation)
        paint.colorFilter = ColorMatrixColorFilter(matrix)

        canvas.drawBitmap(src, 0f, 0f, paint)

        // Apply Vignette overlay if intensity > 0
        if (vignette > 0f) {
            val radius = max(src.width, src.height) / 1.2f
            val maxAlpha = ((vignette / 100f) * 220).toInt().coerceIn(0, 255)
            val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    src.width / 2f,
                    src.height / 2f,
                    radius,
                    intArrayOf(Color.TRANSPARENT, Color.argb(maxAlpha / 2, 0, 0, 0), Color.argb(maxAlpha, 0, 0, 0)),
                    floatArrayOf(0.4f, 0.75f, 1.0f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, src.width.toFloat(), src.height.toFloat(), vignettePaint)
        }

        return output
    }

    /**
     * Rotates bitmap by degrees (e.g. 90, -90, 180).
     */
    fun rotate(src: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    /**
     * Flips bitmap horizontally or vertically.
     */
    fun flip(src: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val sx = if (horizontal) -1f else 1f
        val sy = if (vertical) -1f else 1f
        val matrix = Matrix().apply { postScale(sx, sy) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    /**
     * Crops bitmap to specified rectangular bounds.
     */
    fun crop(src: Bitmap, cropRect: Rect): Bitmap {
        val safeLeft = cropRect.left.coerceIn(0, src.width - 1)
        val safeTop = cropRect.top.coerceIn(0, src.height - 1)
        val safeRight = cropRect.right.coerceIn(safeLeft + 1, src.width)
        val safeBottom = cropRect.bottom.coerceIn(safeTop + 1, src.height)
        val w = safeRight - safeLeft
        val h = safeBottom - safeTop
        return Bitmap.createBitmap(src, safeLeft, safeTop, w, h)
    }

    /**
     * Decodes a sample-downscaled bitmap from Uri to prevent OutOfMemory issues.
     */
    fun decodeSampledBitmapFromUri(context: Context, uri: Uri, maxDim: Int = 2048): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            var inSampleSize = 1
            if (options.outHeight > maxDim || options.outWidth > maxDim) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxDim || halfWidth / inSampleSize >= maxDim) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Downloads and decodes a sample-downscaled bitmap from URL.
     */
    fun decodeSampledBitmapFromUrl(urlString: String, maxDim: Int = 2048): Bitmap? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.doInput = true
            connection.connect()

            val bytes = connection.inputStream.use { it.readBytes() }

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            var inSampleSize = 1
            if (options.outHeight > maxDim || options.outWidth > maxDim) {
                val halfH = options.outHeight / 2
                val halfW = options.outWidth / 2
                while (halfH / inSampleSize >= maxDim || halfW / inSampleSize >= maxDim) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a beautiful artistic demo landscape photo programmatically.
     * Guaranteed to work offline, on emulators without camera or internet.
     */
    fun generateDemoBitmap(width: Int = 1200, height: Int = 900): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Sunset Sky Gradient
        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height * 0.65f,
                intArrayOf(
                    Color.parseColor("#1A1423"),
                    Color.parseColor("#372549"),
                    Color.parseColor("#774C60"),
                    Color.parseColor("#B75D69"),
                    Color.parseColor("#EAC435")
                ),
                floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height * 0.65f, skyPaint)

        // 2. Glowing Sun
        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.5f, height * 0.45f,
                width * 0.2f,
                intArrayOf(Color.parseColor("#FFF3B0"), Color.parseColor("#F4A261"), Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width * 0.5f, height * 0.45f, width * 0.18f, sunPaint)

        // 3. Distant Mountains (Purple-Red)
        val distantMountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5A3048")
            style = Paint.Style.FILL
        }
        val distantPath = Path().apply {
            moveTo(0f, height * 0.65f)
            lineTo(0f, height * 0.48f)
            lineTo(width * 0.2f, height * 0.38f)
            lineTo(width * 0.45f, height * 0.5f)
            lineTo(width * 0.7f, height * 0.35f)
            lineTo(width * 0.9f, height * 0.46f)
            lineTo(width.toFloat(), height * 0.42f)
            lineTo(width.toFloat(), height * 0.65f)
            close()
        }
        canvas.drawPath(distantPath, distantPathPaint(distantMountainPaint))

        // 4. Near Mountains (Dark Violet)
        val nearMountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2B193D")
            style = Paint.Style.FILL
        }
        val nearPath = Path().apply {
            moveTo(0f, height * 0.65f)
            lineTo(0f, height * 0.55f)
            lineTo(width * 0.3f, height * 0.42f)
            lineTo(width * 0.55f, height * 0.56f)
            lineTo(width * 0.8f, height * 0.45f)
            lineTo(width.toFloat(), height * 0.58f)
            lineTo(width.toFloat(), height * 0.65f)
            close()
        }
        canvas.drawPath(nearPath, nearMountainPaint)

        // 5. Lake Water with Sunset Glow
        val waterPaint = Paint().apply {
            shader = LinearGradient(
                0f, height * 0.65f, 0f, height.toFloat(),
                intArrayOf(
                    Color.parseColor("#EAC435"),
                    Color.parseColor("#774C60"),
                    Color.parseColor("#1A1423")
                ),
                floatArrayOf(0f, 0.4f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, height * 0.65f, width.toFloat(), height.toFloat(), waterPaint)

        // 6. Water Ripple Shimmers
        val shimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#66FFF3B0")
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        for (i in 0..12) {
            val y = height * 0.66f + i * (height * 0.32f / 13)
            val w = width * (0.4f - i * 0.02f)
            val startX = width * 0.5f - w / 2f
            canvas.drawLine(startX, y, startX + w, y, shimmerPaint)
        }

        // 7. Silhouette Shore & Pine Trees in Foreground
        val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F0A17")
            style = Paint.Style.FILL
        }
        val shorePath = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(0f, height * 0.78f)
            quadTo(width * 0.25f, height * 0.82f, width * 0.5f, height * 0.88f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(shorePath, foregroundPaint)

        // Draw Silhouette Pine Trees
        drawPineTree(canvas, foregroundPaint, width * 0.08f, height * 0.78f, width * 0.08f, height * 0.22f)
        drawPineTree(canvas, foregroundPaint, width * 0.16f, height * 0.80f, width * 0.07f, height * 0.18f)
        drawPineTree(canvas, foregroundPaint, width * 0.24f, height * 0.83f, width * 0.06f, height * 0.15f)

        return bitmap
    }

    private fun distantPathPaint(paint: Paint) = paint

    private fun drawPineTree(canvas: Canvas, paint: Paint, x: Float, baseY: Float, width: Float, height: Float) {
        val path = Path().apply {
            moveTo(x, baseY - height)
            lineTo(x - width * 0.4f, baseY - height * 0.6f)
            lineTo(x - width * 0.25f, baseY - height * 0.6f)
            lineTo(x - width * 0.5f, baseY - height * 0.25f)
            lineTo(x - width * 0.3f, baseY - height * 0.25f)
            lineTo(x - width * 0.6f, baseY)
            lineTo(x + width * 0.6f, baseY)
            lineTo(x + width * 0.3f, baseY - height * 0.25f)
            lineTo(x + width * 0.5f, baseY - height * 0.25f)
            lineTo(x + width * 0.25f, baseY - height * 0.6f)
            lineTo(x + width * 0.4f, baseY - height * 0.6f)
            close()
        }
        canvas.drawPath(path, paint)
    }

    /**
     * Saves bitmap to public Pictures/Photoshop2.0 album using MediaStore.
     */
    fun saveToGallery(context: Context, bitmap: Bitmap, displayName: String = "Photoshop2_0_${System.currentTimeMillis()}"): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Photoshop2.0")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null

        try {
            resolver.openOutputStream(uri)?.use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    resolver.delete(uri, null, null)
                    return null
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            return uri
        } catch (e: Exception) {
            e.printStackTrace()
            resolver.delete(uri, null, null)
            return null
        }
    }

    /**
     * Saves bitmap to private cache for sharing via Intent.ACTION_SEND.
     */
    fun saveToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(cachePath, "share_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
