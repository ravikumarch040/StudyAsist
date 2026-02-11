package com.studyasist.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Displays an image with a resizable selection region.
 * User can drag corners to adjust the crop area, then confirm to get the cropped image URI.
 */
@Composable
fun ImageCropSelector(
    imageUri: Uri?,
    onCropped: (Uri) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var left by remember { mutableFloatStateOf(0.1f) }
    var top by remember { mutableFloatStateOf(0.1f) }
    var right by remember { mutableFloatStateOf(0.9f) }
    var bottom by remember { mutableFloatStateOf(0.9f) }

    LaunchedEffect(imageUri) {
        if (imageUri == null) return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(imageUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) {
                null
            }
        }
        left = 0.1f
        top = 0.1f
        right = 0.9f
        bottom = 0.9f
    }

    if (bitmap == null && imageUri != null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Failed to load image", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val bmp = bitmap ?: return
    val imageBitmap = remember(bmp) { bmp.asImageBitmap() }
    val minSize = 0.08f

    fun clampRect() {
        if (right - left < minSize) right = left + minSize
        if (bottom - top < minSize) bottom = top + minSize
        left = left.coerceIn(0f, 1f)
        right = right.coerceIn(0f, 1f)
        top = top.coerceIn(0f, 1f)
        bottom = bottom.coerceIn(0f, 1f)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { containerSize = it }
        ) {
            if (containerSize != IntSize.Zero && bmp.width > 0 && bmp.height > 0) {
                val imgW = bmp.width.toFloat()
                val imgH = bmp.height.toFloat()
                val scale = minOf(
                    containerSize.width / imgW,
                    containerSize.height / imgH
                )
                val scaledWidth = imgW * scale
                val scaledHeight = imgH * scale
                val offsetX = (containerSize.width - scaledWidth) / 2
                val offsetY = (containerSize.height - scaledHeight) / 2

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                val cx = (change.position.x - offsetX) / scaledWidth
                                val cy = (change.position.y - offsetY) / scaledHeight
                                val dx = -dragAmount.x / scaledWidth
                                val dy = -dragAmount.y / scaledHeight
                                val pad = 0.08f
                                when {
                                    kotlin.math.abs(cx - left) < pad && kotlin.math.abs(cy - top) < pad -> {
                                        left = (left + dx).coerceIn(0f, right - minSize)
                                        top = (top + dy).coerceIn(0f, bottom - minSize)
                                    }
                                    kotlin.math.abs(cx - right) < pad && kotlin.math.abs(cy - top) < pad -> {
                                        right = (right + dx).coerceIn(left + minSize, 1f)
                                        top = (top + dy).coerceIn(0f, bottom - minSize)
                                    }
                                    kotlin.math.abs(cx - left) < pad && kotlin.math.abs(cy - bottom) < pad -> {
                                        left = (left + dx).coerceIn(0f, right - minSize)
                                        bottom = (bottom + dy).coerceIn(top + minSize, 1f)
                                    }
                                    kotlin.math.abs(cx - right) < pad && kotlin.math.abs(cy - bottom) < pad -> {
                                        right = (right + dx).coerceIn(left + minSize, 1f)
                                        bottom = (bottom + dy).coerceIn(top + minSize, 1f)
                                    }
                                    else -> {
                                        left = (left + dx).coerceIn(0f, 1f - minSize)
                                        right = (right + dx).coerceIn(minSize, 1f)
                                        top = (top + dy).coerceIn(0f, 1f - minSize)
                                        bottom = (bottom + dy).coerceIn(minSize, 1f)
                                    }
                                }
                                clampRect()
                            }
                        }
                ) {
                    drawImage(
                        image = imageBitmap,
                        dstOffset = androidx.compose.ui.unit.IntOffset(offsetX.toInt(), offsetY.toInt()),
                        dstSize = androidx.compose.ui.unit.IntSize(scaledWidth.toInt().coerceAtLeast(1), scaledHeight.toInt().coerceAtLeast(1))
                    )
                    val px = offsetX + left * scaledWidth
                    val py = offsetY + top * scaledHeight
                    val pw = (right - left) * scaledWidth
                    val ph = (bottom - top) * scaledHeight
                    drawRect(Color.Black.copy(alpha = 0.5f), Offset(0f, 0f), Size(containerSize.width.toFloat(), py))
                    drawRect(Color.Black.copy(alpha = 0.5f), Offset(0f, py), Size(px, ph))
                    drawRect(Color.Black.copy(alpha = 0.5f), Offset(px + pw, py), Size(containerSize.width - px - pw, ph))
                    drawRect(Color.Black.copy(alpha = 0.5f), Offset(0f, py + ph), Size(containerSize.width.toFloat(), containerSize.height - py - ph))
                    drawRect(Color.White, Offset(px, py), Size(pw, ph), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            Text("Drag corners to adjust region", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCancel) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        scope.launch {
                            val uri = withContext(Dispatchers.IO) {
                                val x = (left * bmp.width).toInt().coerceIn(0, bmp.width - 1)
                                val y = (top * bmp.height).toInt().coerceIn(0, bmp.height - 1)
                                val w = ((right - left) * bmp.width).toInt().coerceIn(1, bmp.width - x)
                                val h = ((bottom - top) * bmp.height).toInt().coerceIn(1, bmp.height - y)
                                val cropped = Bitmap.createBitmap(bmp, x, y, w, h)
                                val file = File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg")
                                java.io.FileOutputStream(file).use { out ->
                                    cropped.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                }
                                cropped.recycle()
                                Uri.fromFile(file)
                            }
                            onCropped(uri)
                        }
                    }
                ) {
                    Text("Use selection")
                }
            }
        }
    }
}
