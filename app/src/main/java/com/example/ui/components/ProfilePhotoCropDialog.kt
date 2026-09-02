package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.ImageProcessingUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoCropDialog(
    initialBitmap: Bitmap,
    onDismiss: () -> Unit,
    onChangePhotoSource: () -> Unit,
    onCropSuccess: (Bitmap) -> Unit
) {
    var workingBitmap by remember { mutableStateOf(initialBitmap) }
    var croppedPreview by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Crop insets & offsets (Normalized 0.0 to 1.0)
    var cropZoom by remember { mutableFloatStateOf(1.0f) } // 1.0 = full fit, up to 2.5f zoom
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()

    // Helper to compute cropped square bitmap
    fun computeCroppedSquare(source: Bitmap, zoom: Float, panX: Float, panY: Float): Bitmap {
        val width = source.width
        val height = source.height
        val minDim = min(width, height).toFloat()

        // Size of crop area in source coordinates
        val cropSide = (minDim / zoom).coerceIn(50f, minDim)

        // Center point with pan offset
        val centerX = (width / 2f) - (panX * width)
        val centerY = (height / 2f) - (panY * height)

        val left = (centerX - cropSide / 2f).coerceIn(0f, width - cropSide)
        val top = (centerY - cropSide / 2f).coerceIn(0f, height - cropSide)

        val normalizedRect = RectF(
            left / width,
            top / height,
            (left + cropSide) / width,
            (top + cropSide) / height
        )

        return ImageProcessingUtil.cropBitmap(source, normalizedRect)
    }

    // Update cropped preview when parameters change
    LaunchedEffect(workingBitmap, cropZoom, offsetX, offsetY) {
        withContext(Dispatchers.IO) {
            val preview = computeCroppedSquare(workingBitmap, cropZoom, offsetX, offsetY)
            croppedPreview = preview
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f)),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Sesuaikan Foto Profil",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Geser & Zoom untuk memotong 1:1",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val rotated = withContext(Dispatchers.IO) {
                                    ImageProcessingUtil.rotateBitmap(workingBitmap, 90f)
                                }
                                workingBitmap = rotated
                                offsetX = 0f
                                offsetY = 0f
                                isProcessing = false
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CropRotate,
                            contentDescription = "Putar 90°",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Crop Viewport Box (Circular / Square Guide)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E1E1E))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                cropZoom = (cropZoom * zoom).coerceIn(1.0f, 3.0f)
                                offsetX = (offsetX - pan.x / 1000f).coerceIn(-0.35f, 0.35f)
                                offsetY = (offsetY - pan.y / 1000f).coerceIn(-0.35f, 0.35f)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (croppedPreview != null && !isProcessing) {
                        Image(
                            bitmap = croppedPreview!!.asImageBitmap(),
                            contentDescription = "Hasil Crop",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    // Circular Crop Outline Overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = (size.minDimension / 2f) * 0.92f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // Outer circular border guide
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 2.5.dp.toPx())
                        )

                        // 3x3 Grid Lines inside circle for rule of thirds framing
                        val step = (radius * 2) / 3f
                        val leftEdge = center.x - radius
                        val topEdge = center.y - radius

                        // Vertical grid lines
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(leftEdge + step, center.y - radius * 0.7f),
                            end = Offset(leftEdge + step, center.y + radius * 0.7f),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(leftEdge + step * 2, center.y - radius * 0.7f),
                            end = Offset(leftEdge + step * 2, center.y + radius * 0.7f),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Horizontal grid lines
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(center.x - radius * 0.7f, topEdge + step),
                            end = Offset(center.x + radius * 0.7f, topEdge + step),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(center.x - radius * 0.7f, topEdge + step * 2),
                            end = Offset(center.x + radius * 0.7f, topEdge + step * 2),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Zoom & Reset Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Perbesaran (Zoom): ${String.format("%.1f", cropZoom)}x",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color.White
                            )
                        }

                        TextButton(
                            onClick = {
                                cropZoom = 1.0f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                    }

                    Slider(
                        value = cropZoom,
                        onValueChange = { cropZoom = it },
                        valueRange = 1.0f..3.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons: Ganti Sumber Foto & Terapkan Crop
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onChangePhotoSource,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ganti Foto", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Button(
                        onClick = {
                            val finalCropped = croppedPreview ?: workingBitmap
                            onCropSuccess(finalCropped)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Terapkan & Simpan",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
