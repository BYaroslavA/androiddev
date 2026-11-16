package com.example.myapplication123

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication123.databinding.ActivityMainBinding
import com.example.myapplication123.databinding.LayoutDialogWebImageBinding
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayDeque

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var filterAdapter: FilterAdapter

    // Bitmaps
    private var originalBitmap: Bitmap? = null
    private var baseBitmap: Bitmap? = null
    private var currentDisplayBitmap: Bitmap? = null

    // Adjustment states
    private var currentFilter: FilterType = FilterType.ORIGINAL
    private var brightness: Float = 0f
    private var contrast: Float = 0f
    private var saturation: Float = 0f
    private var vignette: Float = 0f

    // History for Undo & Redo
    private data class HistorySnapshot(
        val bitmap: Bitmap,
        val filter: FilterType,
        val brightness: Float,
        val contrast: Float,
        val saturation: Float,
        val vignette: Float
    )

    private val undoStack = ArrayDeque<HistorySnapshot>()
    private val redoStack = ArrayDeque<HistorySnapshot>()

    // Activity Result Launchers
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let { loadNewBitmap(it) }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { loadBitmapFromUri(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupTopBarActions()
        setupSourceButtons()
        setupFiltersRecyclerView()
        setupTabsAndPanels()
        setupAdjustments()
        setupTransforms()
        setupCropMode()
        setupEffects()

        updateUndoRedoButtons()
        updateEmptyState()

        // Automatically load demo image on very first launch so user sees the editor in all its glory immediately!
        loadDemoImage()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.topBar.setPadding(
                binding.topBar.paddingLeft,
                systemBars.top,
                binding.topBar.paddingRight,
                binding.topBar.paddingBottom
            )
            val topBarParams = binding.topBar.layoutParams
            topBarParams.height = (56 * resources.displayMetrics.density).toInt() + systemBars.top
            binding.topBar.layoutParams = topBarParams

            binding.bottomToolContainer.setPadding(
                binding.bottomToolContainer.paddingLeft,
                binding.bottomToolContainer.paddingTop,
                binding.bottomToolContainer.paddingRight,
                systemBars.bottom + (4 * resources.displayMetrics.density).toInt()
            )

            binding.cropControlsBar.setPadding(
                binding.cropControlsBar.paddingLeft,
                binding.cropControlsBar.paddingTop,
                binding.cropControlsBar.paddingRight,
                systemBars.bottom + (8 * resources.displayMetrics.density).toInt()
            )
            insets
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTopBarActions() {
        // Compare with Original: Hold down to see original, release to see edited
        binding.btnCompare.setOnTouchListener { _, event ->
            if (originalBitmap == null) return@setOnTouchListener false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    binding.ivMainImage.setImageBitmap(originalBitmap)
                    binding.ivMainImage.colorFilter = null
                    binding.tvCompareBadge.visibility = View.VISIBLE
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.ivMainImage.setImageBitmap(currentDisplayBitmap)
                    applyLiveColorFilter()
                    binding.tvCompareBadge.visibility = View.GONE
                    true
                }
                else -> false
            }
        }

        binding.btnUndo.setOnClickListener { undo() }
        binding.btnRedo.setOnClickListener { redo() }
        binding.btnReset.setOnClickListener { resetAll() }
        binding.btnSave.setOnClickListener { saveImageToGallery() }
        binding.btnShare.setOnClickListener { shareImage() }
    }

    private fun setupSourceButtons() {
        // Empty state buttons
        binding.btnEmptyCamera.setOnClickListener { launchCamera() }
        binding.btnEmptyGallery.setOnClickListener { launchGallery() }
        binding.btnEmptyWeb.setOnClickListener { showWebImageDialog() }
        binding.btnEmptyDemo.setOnClickListener { loadDemoImage() }

        // Bottom source bar buttons
        binding.btnSourceCamera.setOnClickListener { launchCamera() }
        binding.btnSourceGallery.setOnClickListener { launchGallery() }
        binding.btnSourceWeb.setOnClickListener { showWebImageDialog() }
        binding.btnSourceDemo.setOnClickListener { loadDemoImage() }
    }

    private fun launchCamera() {
        try {
            takePictureLauncher.launch(null)
        } catch (e: Exception) {
            Toast.makeText(this, "Не вдалося відкрити камеру: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchGallery() {
        try {
            pickImageLauncher.launch("image/*")
        } catch (e: Exception) {
            Toast.makeText(this, "Не вдалося відкрити галерею: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showWebImageDialog() {
        val dialogBinding = LayoutDialogWebImageBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Завантажити", null)
            .setNegativeButton("Скасувати", null)
            .create()

        dialogBinding.btnSampleNature.setOnClickListener {
            dialogBinding.etImageUrl.setText("https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&q=80")
        }
        dialogBinding.btnSampleCity.setOnClickListener {
            dialogBinding.etImageUrl.setText("https://images.unsplash.com/photo-1514565131-fce0801e5785?w=1200&q=80")
        }
        dialogBinding.btnSampleOcean.setOnClickListener {
            dialogBinding.etImageUrl.setText("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200&q=80")
        }

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val url = dialogBinding.etImageUrl.text?.toString()?.trim()
                if (url.isNullOrEmpty()) {
                    dialogBinding.tilUrl.error = "Введіть URL"
                } else {
                    dialog.dismiss()
                    loadBitmapFromUrl(url)
                }
            }
        }

        dialog.show()
    }

    private fun loadBitmapFromUri(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = ImageProcessor.decodeSampledBitmapFromUri(this@MainActivity, uri)
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (bitmap != null) {
                    loadNewBitmap(bitmap)
                } else {
                    Toast.makeText(this@MainActivity, R.string.msg_load_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadBitmapFromUrl(url: String) {
        binding.progressBar.visibility = View.VISIBLE
        Toast.makeText(this, R.string.msg_downloading, Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = ImageProcessor.decodeSampledBitmapFromUrl(url)
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (bitmap != null) {
                    loadNewBitmap(bitmap)
                } else {
                    Toast.makeText(this@MainActivity, R.string.msg_load_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadDemoImage() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.Default) {
            val demo = ImageProcessor.generateDemoBitmap()
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                loadNewBitmap(demo)
            }
        }
    }

    private fun loadNewBitmap(bitmap: Bitmap) {
        originalBitmap = bitmap
        baseBitmap = bitmap
        currentDisplayBitmap = bitmap

        currentFilter = FilterType.ORIGINAL
        brightness = 0f
        contrast = 0f
        saturation = 0f
        vignette = 0f

        undoStack.clear()
        redoStack.clear()

        binding.ivMainImage.setImageBitmap(bitmap)
        applyLiveColorFilter()

        // Update Filter Thumbnail in Adapter
        val thumb = Bitmap.createScaledBitmap(bitmap, 120, 120, true)
        filterAdapter.setThumbnail(thumb)
        filterAdapter.setSelected(FilterType.ORIGINAL)

        // Reset sliders UI
        binding.sliderAdjust.value = 0f
        binding.chipBrightness.isChecked = true
        binding.chipBrightness.text = "Яскравість (0)"
        binding.chipContrast.text = "Контраст (0)"
        binding.chipSaturation.text = "Насиченість (0)"
        binding.sliderVignette.value = 0f
        binding.tvVignetteLabel.text = "Віньєтка: 0%"

        updateUndoRedoButtons()
        updateEmptyState()
    }

    private fun setupFiltersRecyclerView() {
        val filters = FilterType.values().toList()
        filterAdapter = FilterAdapter(filters, currentFilter) { selected ->
            saveStateToUndo()
            currentFilter = selected
            applyLiveColorFilter()
        }
        binding.rvFilters.adapter = filterAdapter
    }

    private fun setupTabsAndPanels() {
        val tabs = listOf(
            binding.tabFilters to binding.rvFilters,
            binding.tabAdjust to binding.panelAdjust,
            binding.tabTransform to binding.panelTransform,
            binding.tabCrop to binding.panelCrop,
            binding.tabEffects to binding.panelEffects
        )

        fun selectTab(activeTab: TextView) {
            for ((tab, panel) in tabs) {
                if (tab == activeTab) {
                    tab.setTextColor(Color.parseColor("#00E5FF"))
                    panel.visibility = View.VISIBLE
                } else {
                    tab.setTextColor(Color.parseColor("#94A3B8"))
                    panel.visibility = View.GONE
                }
            }
        }

        binding.tabFilters.setOnClickListener { selectTab(binding.tabFilters) }
        binding.tabAdjust.setOnClickListener { selectTab(binding.tabAdjust) }
        binding.tabTransform.setOnClickListener { selectTab(binding.tabTransform) }
        binding.tabCrop.setOnClickListener { selectTab(binding.tabCrop) }
        binding.tabEffects.setOnClickListener { selectTab(binding.tabEffects) }
    }

    private fun setupAdjustments() {
        var activeAdjustment = 0 // 0 = Brightness, 1 = Contrast, 2 = Saturation

        binding.chipGroupAdjust.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(R.id.chipBrightness) -> {
                    activeAdjustment = 0
                    binding.sliderAdjust.value = brightness
                }
                checkedIds.contains(R.id.chipContrast) -> {
                    activeAdjustment = 1
                    binding.sliderAdjust.value = contrast
                }
                checkedIds.contains(R.id.chipSaturation) -> {
                    activeAdjustment = 2
                    binding.sliderAdjust.value = saturation
                }
            }
        }

        binding.sliderAdjust.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            when (activeAdjustment) {
                0 -> {
                    brightness = value
                    binding.chipBrightness.text = "Яскравість (${value.toInt()})"
                }
                1 -> {
                    contrast = value
                    binding.chipContrast.text = "Контраст (${value.toInt()})"
                }
                2 -> {
                    saturation = value
                    binding.chipSaturation.text = "Насиченість (${value.toInt()})"
                }
            }
            applyLiveColorFilter()
        }

        binding.sliderAdjust.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                saveStateToUndo()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Adjustment committed
            }
        })
    }

    private fun setupTransforms() {
        binding.btnRotateLeft.setOnClickListener {
            baseBitmap?.let { bmp ->
                saveStateToUndo()
                val rotated = ImageProcessor.rotate(bmp, -90f)
                updateBaseBitmap(rotated)
            }
        }

        binding.btnRotateRight.setOnClickListener {
            baseBitmap?.let { bmp ->
                saveStateToUndo()
                val rotated = ImageProcessor.rotate(bmp, 90f)
                updateBaseBitmap(rotated)
            }
        }

        binding.btnFlipH.setOnClickListener {
            baseBitmap?.let { bmp ->
                saveStateToUndo()
                val flipped = ImageProcessor.flip(bmp, horizontal = true, vertical = false)
                updateBaseBitmap(flipped)
            }
        }

        binding.btnFlipV.setOnClickListener {
            baseBitmap?.let { bmp ->
                saveStateToUndo()
                val flipped = ImageProcessor.flip(bmp, horizontal = false, vertical = true)
                updateBaseBitmap(flipped)
            }
        }
    }

    private fun setupCropMode() {
        binding.btnStartCrop.setOnClickListener {
            enterCropMode()
        }

        binding.chipGroupCropRatios.setOnCheckedStateChangeListener { _, checkedIds ->
            val ratio = when {
                checkedIds.contains(R.id.chipCrop1_1) -> 1.0f
                checkedIds.contains(R.id.chipCrop4_3) -> 4f / 3f
                checkedIds.contains(R.id.chipCrop16_9) -> 16f / 9f
                checkedIds.contains(R.id.chipCrop9_16) -> 9f / 16f
                checkedIds.contains(R.id.chipCrop3_4) -> 3f / 4f
                else -> null // Free
            }
            binding.cropOverlayView.setAspectRatio(ratio)
        }

        binding.btnCancelCrop.setOnClickListener {
            exitCropMode()
        }

        binding.btnApplyCrop.setOnClickListener {
            val currentBmp = baseBitmap ?: return@setOnClickListener
            val cropRect = binding.cropOverlayView.getCropRectInBitmap(currentBmp.width, currentBmp.height)
            if (cropRect != null && cropRect.width() > 10 && cropRect.height() > 10) {
                saveStateToUndo()
                val cropped = ImageProcessor.crop(currentBmp, cropRect)
                updateBaseBitmap(cropped)
            }
            exitCropMode()
        }
    }

    private fun enterCropMode() {
        if (baseBitmap == null) return
        val displayedRect = getDisplayedImageRect()
        if (displayedRect.width() <= 0 || displayedRect.height() <= 0) return

        binding.cropOverlayView.imageBounds = displayedRect
        binding.cropOverlayView.visibility = View.VISIBLE
        binding.cropControlsBar.visibility = View.VISIBLE
        binding.bottomToolContainer.visibility = View.GONE
    }

    private fun exitCropMode() {
        binding.cropOverlayView.visibility = View.GONE
        binding.cropControlsBar.visibility = View.GONE
        binding.bottomToolContainer.visibility = View.VISIBLE
    }

    private fun getDisplayedImageRect(): RectF {
        val iv = binding.ivMainImage
        val drawable = iv.drawable ?: return RectF()
        val dWidth = drawable.intrinsicWidth.toFloat()
        val dHeight = drawable.intrinsicHeight.toFloat()
        if (dWidth <= 0 || dHeight <= 0) return RectF()

        val vWidth = iv.width.toFloat()
        val vHeight = iv.height.toFloat()

        val scale = (vWidth / dWidth).coerceAtMost(vHeight / dHeight)
        val renderedW = dWidth * scale
        val renderedH = dHeight * scale

        val left = (vWidth - renderedW) / 2f
        val top = (vHeight - renderedH) / 2f
        return RectF(left, top, left + renderedW, top + renderedH)
    }

    private fun setupEffects() {
        binding.sliderVignette.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            vignette = value
            binding.tvVignetteLabel.text = "Віньєтка: ${value.toInt()}%"
            applyVignetteAndAdjustments()
        }

        binding.sliderVignette.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                saveStateToUndo()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // committed
            }
        })
    }

    private fun updateBaseBitmap(newBase: Bitmap) {
        baseBitmap = newBase
        applyVignetteAndAdjustments()

        // Update Filter Thumbnail in Adapter
        val thumb = Bitmap.createScaledBitmap(newBase, 120, 120, true)
        filterAdapter.setThumbnail(thumb)
    }

    private fun applyLiveColorFilter() {
        val matrix = ImageProcessor.getCombinedColorMatrix(currentFilter, brightness, contrast, saturation)
        binding.ivMainImage.colorFilter = ColorMatrixColorFilter(matrix)
    }

    private fun applyVignetteAndAdjustments() {
        val base = baseBitmap ?: return
        if (vignette > 0f) {
            val withVignette = ImageProcessor.applyAll(base, FilterType.ORIGINAL, 0f, 0f, 0f, vignette)
            currentDisplayBitmap = withVignette
            binding.ivMainImage.setImageBitmap(withVignette)
        } else {
            currentDisplayBitmap = base
            binding.ivMainImage.setImageBitmap(base)
        }
        applyLiveColorFilter()
    }

    private fun saveStateToUndo() {
        val bmp = baseBitmap ?: return
        if (undoStack.size >= 12) {
            undoStack.removeFirst()
        }
        undoStack.addLast(
            HistorySnapshot(
                bitmap = bmp,
                filter = currentFilter,
                brightness = brightness,
                contrast = contrast,
                saturation = saturation,
                vignette = vignette
            )
        )
        redoStack.clear()
        updateUndoRedoButtons()
    }

    private fun undo() {
        if (undoStack.isEmpty()) return
        val currentBmp = baseBitmap ?: return
        val currentSnapshot = HistorySnapshot(
            bitmap = currentBmp,
            filter = currentFilter,
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            vignette = vignette
        )
        redoStack.addLast(currentSnapshot)

        val previous = undoStack.removeLast()
        restoreSnapshot(previous)
        updateUndoRedoButtons()
    }

    private fun redo() {
        if (redoStack.isEmpty()) return
        val currentBmp = baseBitmap ?: return
        val currentSnapshot = HistorySnapshot(
            bitmap = currentBmp,
            filter = currentFilter,
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            vignette = vignette
        )
        undoStack.addLast(currentSnapshot)

        val next = redoStack.removeLast()
        restoreSnapshot(next)
        updateUndoRedoButtons()
    }

    private fun restoreSnapshot(snapshot: HistorySnapshot) {
        baseBitmap = snapshot.bitmap
        currentFilter = snapshot.filter
        brightness = snapshot.brightness
        contrast = snapshot.contrast
        saturation = snapshot.saturation
        vignette = snapshot.vignette

        filterAdapter.setSelected(currentFilter)
        val thumb = Bitmap.createScaledBitmap(snapshot.bitmap, 120, 120, true)
        filterAdapter.setThumbnail(thumb)

        binding.chipBrightness.text = "Яскравість (${brightness.toInt()})"
        binding.chipContrast.text = "Контраст (${contrast.toInt()})"
        binding.chipSaturation.text = "Насиченість (${saturation.toInt()})"
        binding.sliderVignette.value = vignette
        binding.tvVignetteLabel.text = "Віньєтка: ${vignette.toInt()}%"

        applyVignetteAndAdjustments()
    }

    private fun resetAll() {
        val orig = originalBitmap ?: return
        saveStateToUndo()
        loadNewBitmap(orig)
        Toast.makeText(this, "Всі зміни скинуто до оригіналу", Toast.LENGTH_SHORT).show()
    }

    private fun updateUndoRedoButtons() {
        binding.btnUndo.isEnabled = undoStack.isNotEmpty()
        binding.btnUndo.alpha = if (undoStack.isNotEmpty()) 1.0f else 0.35f

        binding.btnRedo.isEnabled = redoStack.isNotEmpty()
        binding.btnRedo.alpha = if (redoStack.isNotEmpty()) 1.0f else 0.35f
    }

    private fun updateEmptyState() {
        val hasImage = originalBitmap != null
        binding.emptyStateContainer.visibility = if (hasImage) View.GONE else View.VISIBLE
        binding.ivMainImage.visibility = if (hasImage) View.VISIBLE else View.GONE
        binding.topActions.visibility = if (hasImage) View.VISIBLE else View.GONE
        binding.bottomToolContainer.visibility = if (hasImage) View.VISIBLE else View.GONE
    }

    private fun bakeFinalBitmap(): Bitmap? {
        val base = baseBitmap ?: return null
        return ImageProcessor.applyAll(base, currentFilter, brightness, contrast, saturation, vignette)
    }

    private fun saveImageToGallery() {
        val finalBmp = bakeFinalBitmap() ?: return
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val uri = ImageProcessor.saveToGallery(this@MainActivity, finalBmp)
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (uri != null) {
                    Toast.makeText(this@MainActivity, R.string.msg_saved_success, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, R.string.msg_saved_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun shareImage() {
        val finalBmp = bakeFinalBitmap() ?: return
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val uri = ImageProcessor.saveToCache(this@MainActivity, finalBmp)
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (uri != null) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Поділитися фото"))
                } else {
                    Toast.makeText(this@MainActivity, "Не вдалося підготувати фото для відправки", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
