package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LumenCyanLight
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
fun TopBar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onToggleLayers: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(LumenDarkSurface)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Logo & File Name
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lumen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = LumenCyanSecondary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LumenDarkCard)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Sunset_v2.jpg",
                    fontSize = 12.sp,
                    color = LumenTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Right: Undo, Redo, Layers, Share, Export
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) LumenCyanSecondary else LumenTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) LumenCyanSecondary else LumenTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onToggleLayers,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Layers",
                    tint = LumenPurpleLight,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onShare,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = LumenTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            Button(
                onClick = onExport,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LumenPurplePrimary,
                    contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Экспорт",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(name = "TopBar Preview", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun TopBarPreview() {
    LumenStudioTheme {
        TopBar(
            canUndo = true,
            canRedo = false,
            onUndo = {},
            onRedo = {},
            onExport = {},
            onShare = {},
            onToggleLayers = {}
        )
    }
}
