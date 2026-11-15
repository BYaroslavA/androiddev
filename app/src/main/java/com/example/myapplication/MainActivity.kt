package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.airbnb.lottie.LottieAnimationView
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    // 1. Lottie Views
    private lateinit var lottieView: LottieAnimationView
    private lateinit var btnLottiePlayPause: MaterialButton
    private lateinit var btnLottieSpeed: MaterialButton
    private val lottieSpeeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)
    private var lottieSpeedIndex = 1

    // 2. MPAndroidChart Views
    private lateinit var barChart: BarChart
    private lateinit var btnChartAnimate: MaterialButton
    private lateinit var btnChartRandomize: MaterialButton

    // 3. Coil Views
    private lateinit var coilImageView: ImageView
    private lateinit var tvCoilStatus: TextView
    private lateinit var btnCoilTransform: MaterialButton
    private lateinit var btnCoilReload: MaterialButton
    private var isCircleCrop = true
    private var imageSeed = 10
    private val baseImageUrl get() = "https://picsum.photos/seed/android$imageSeed/400/400"

    // 4. Shimmer Views
    private lateinit var shimmerContainer: ShimmerFrameLayout
    private lateinit var layoutLoadedContent: LinearLayout
    private lateinit var btnToggleShimmer: MaterialButton
    private var isShimmering = true

    // 5. Konfetti Views
    private lateinit var konfettiView: KonfettiView
    private lateinit var btnKonfettiBurst: MaterialButton
    private lateinit var btnKonfettiRain: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupLottieSection()
        setupChartSection()
        setupCoilSection()
        setupShimmerSection()
        setupKonfettiSection()
    }

    // ------------------------------------------------------------------------
    // 1. Lottie Android (airbnb/lottie-android)
    // ------------------------------------------------------------------------
    private fun setupLottieSection() {
        lottieView = findViewById(R.id.lottieView)
        btnLottiePlayPause = findViewById(R.id.btnLottiePlayPause)
        btnLottieSpeed = findViewById(R.id.btnLottieSpeed)

        btnLottiePlayPause.setOnClickListener {
            if (lottieView.isAnimating) {
                lottieView.pauseAnimation()
                btnLottiePlayPause.text = getString(R.string.lottie_play)
            } else {
                lottieView.resumeAnimation()
                btnLottiePlayPause.text = getString(R.string.lottie_pause)
            }
        }

        btnLottieSpeed.setOnClickListener {
            lottieSpeedIndex = (lottieSpeedIndex + 1) % lottieSpeeds.size
            val currentSpeed = lottieSpeeds[lottieSpeedIndex]
            lottieView.speed = currentSpeed
            btnLottieSpeed.text = getString(R.string.lottie_speed_format, currentSpeed)
        }
    }

    // ------------------------------------------------------------------------
    // 2. MPAndroidChart (PhilJay/MPAndroidChart)
    // ------------------------------------------------------------------------
    private fun setupChartSection() {
        barChart = findViewById(R.id.barChart)
        btnChartAnimate = findViewById(R.id.btnChartAnimate)
        btnChartRandomize = findViewById(R.id.btnChartRandomize)

        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            legend.isEnabled = false
            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
                valueFormatter = IndexAxisValueFormatter(days)
                textColor = getColor(R.color.text_secondary)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = getColor(R.color.card_stroke)
                textColor = getColor(R.color.text_secondary)
                axisMinimum = 0f
            }
        }

        loadChartData()

        btnChartAnimate.setOnClickListener {
            barChart.animateY(1200)
        }

        btnChartRandomize.setOnClickListener {
            loadChartData()
        }
    }

    private fun loadChartData() {
        val entries = ArrayList<BarEntry>()
        val defaultValues = listOf(55f, 85f, 65f, 110f, 130f, 95f, 150f)
        defaultValues.forEachIndexed { index, base ->
            val randomFactor = (0.7 + Random.nextDouble() * 0.6).toFloat()
            entries.add(BarEntry(index.toFloat(), base * randomFactor))
        }

        val dataSet = BarDataSet(entries, "Завантаження додатку").apply {
            colors = listOf(
                getColor(R.color.primary_light),
                getColor(R.color.secondary),
                getColor(R.color.accent_purple),
                getColor(R.color.accent_orange),
                getColor(R.color.accent_pink),
                getColor(R.color.primary_light),
                getColor(R.color.secondary)
            )
            valueTextColor = getColor(R.color.text_primary)
            valueTextSize = 10f
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.55f
        }

        barChart.data = barData
        barChart.invalidate()
        barChart.animateY(1000)
    }

    // ------------------------------------------------------------------------
    // 3. Coil (coil-kt/coil)
    // ------------------------------------------------------------------------
    private fun setupCoilSection() {
        coilImageView = findViewById(R.id.coilImageView)
        tvCoilStatus = findViewById(R.id.tvCoilStatus)
        btnCoilTransform = findViewById(R.id.btnCoilTransform)
        btnCoilReload = findViewById(R.id.btnCoilReload)

        loadCoilImage()

        btnCoilTransform.setOnClickListener {
            isCircleCrop = !isCircleCrop
            loadCoilImage()
        }

        btnCoilReload.setOnClickListener {
            imageSeed++
            loadCoilImage()
        }
    }

    private fun loadCoilImage() {
        coilImageView.load(baseImageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_placeholder)
            error(R.drawable.ic_error)
            if (isCircleCrop) {
                transformations(CircleCropTransformation())
            } else {
                transformations(RoundedCornersTransformation(32f))
            }
        }

        tvCoilStatus.text = if (isCircleCrop) {
            getString(R.string.coil_status_circle)
        } else {
            getString(R.string.coil_status_rounded)
        }
    }

    // ------------------------------------------------------------------------
    // 4. Facebook Shimmer (facebook/shimmer-android)
    // ------------------------------------------------------------------------
    private fun setupShimmerSection() {
        shimmerContainer = findViewById(R.id.shimmerViewContainer)
        layoutLoadedContent = findViewById(R.id.layoutLoadedContent)
        btnToggleShimmer = findViewById(R.id.btnToggleShimmer)

        shimmerContainer.startShimmer()

        btnToggleShimmer.setOnClickListener {
            isShimmering = !isShimmering
            if (isShimmering) {
                layoutLoadedContent.visibility = View.GONE
                shimmerContainer.visibility = View.VISIBLE
                shimmerContainer.startShimmer()
                btnToggleShimmer.text = getString(R.string.shimmer_show_data)
            } else {
                shimmerContainer.stopShimmer()
                shimmerContainer.visibility = View.GONE
                layoutLoadedContent.visibility = View.VISIBLE
                btnToggleShimmer.text = getString(R.string.shimmer_show_skeleton)
            }
        }
    }

    // ------------------------------------------------------------------------
    // 5. Konfetti (DanielMartinus/Konfetti)
    // ------------------------------------------------------------------------
    private fun setupKonfettiSection() {
        konfettiView = findViewById(R.id.konfettiView)
        btnKonfettiBurst = findViewById(R.id.btnKonfettiBurst)
        btnKonfettiRain = findViewById(R.id.btnKonfettiRain)

        val festiveColors = listOf(
            0xfce18a.toInt() or (0xFF shl 24),
            0xff726d.toInt() or (0xFF shl 24),
            0xf4306d.toInt() or (0xFF shl 24),
            0xb48def.toInt() or (0xFF shl 24),
            0x48bb78.toInt() or (0xFF shl 24)
        )

        btnKonfettiBurst.setOnClickListener {
            val party = Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = festiveColors,
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.5, 0.3)
            )
            konfettiView.start(party)
        }

        btnKonfettiRain.setOnClickListener {
            val rain = Party(
                speed = 10f,
                maxSpeed = 25f,
                damping = 0.9f,
                angle = 90,
                spread = 45,
                colors = festiveColors,
                emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(50),
                position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
            )
            konfettiView.start(rain)
        }
    }
}
