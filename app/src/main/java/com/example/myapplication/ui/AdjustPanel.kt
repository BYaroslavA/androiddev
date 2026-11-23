package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.myapplication.ui.theme.LumenTextMuted
import com.example.myapplication.ui.theme.LumenTextPrimary
import com.example.myapplication.ui.theme.LumenTextSecondary

@Composable
fun AdjustPanel(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    temperature: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onResetAdjustments: () -> Unit,
    onSliderStart: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LumenDarkSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Коррекция",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumenTextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ColorMatrix",
                    fontSize = 11.sp,
                    color = LumenPurpleLight,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LumenDarkCard)
                    .clickable { onResetAdjustments() }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Сбросить",
                    tint = LumenCyanSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Сбросить",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LumenCyanSecondary
                )
            }
        }

        // Sliders
        AdjustSliderItem(
            icon = Icons.Default.Brightness6,
            name = "Яркость",
            value = brightness,
            valueRange = -100f..100f,
            displayValue = "${if (brightness > 0) "+" else ""}${brightness.toInt()}%",
            onValueChange = {
                onBrightnessChange(it)
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        AdjustSliderItem(
            icon = Icons.Default.Contrast,
            name = "Контраст",
            value = contrast,
            valueRange = 0f..200f,
            displayValue = "${contrast.toInt()}%",
            onValueChange = {
                onContrastChange(it)
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        AdjustSliderItem(
            icon = Icons.Default.Palette,
            name = "Насыщенность",
            value = saturation,
            valueRange = 0f..200f,
            displayValue = "${saturation.toInt()}%",
            onValueChange = {
                onSaturationChange(it)
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        AdjustSliderItem(
            icon = Icons.Default.Thermostat,
            name = "Температура",
            value = temperature,
            valueRange = -100f..100f,
            displayValue = "${if (temperature > 0) "+" else ""}${temperature.toInt()}%",
            onValueChange = {
                onTemperatureChange(it)
            }
        )
    }
}

@Composable
private fun AdjustSliderItem(
    icon: ImageVector,
    name: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = LumenTextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = LumenTextPrimary,
            modifier = Modifier.width(96.dp)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = LumenCyanSecondary,
                activeTrackColor = LumenPurplePrimary,
                inactiveTrackColor = LumenBorder
            )
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .width(50.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(LumenDarkCard)
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayValue,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = LumenCyanSecondary
            )
        }
    }
}

@Preview(name = "AdjustPanel Preview", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun AdjustPanelPreview() {
    LumenStudioTheme {
        AdjustPanel(
            brightness = 15f,
            contrast = 120f,
            saturation = 110f,
            temperature = 25f,
            onBrightnessChange = {},
            onContrastChange = {},
            onSaturationChange = {},
            onTemperatureChange = {},
            onResetAdjustments = {}
        )
    }
}
