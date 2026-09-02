package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.model.Penduduk
import com.example.data.model.PendudukDocument
import com.example.util.ImageProcessingUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class CropPreset(val label: String, val ratio: Float?) {
    FULL("Asli / Penuh", null),
    KTP("KTP (85:54)", 85f / 54f),
    KK("Kartu Keluarga (A4)", 1f / 1.414f),
    DOKUMEN("Dokumen (4:3)", 4f / 3f),
    SQUARE("Persegi (1:1)", 1f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScannerDialog(
    resident: Penduduk,
    initialJenisDokumen: String = "KTP",
    onDismiss: () -> Unit,
    onSaveDocument: (jenisDokumen: String, imageBytes: ByteArray) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedJenis by remember { mutableStateOf(initialJenisDokumen) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    var selectedFilter by remember { mutableStateOf(ImageProcessingUtil.DocumentFilter.DOCUMENT_ENHANCE) }
    var selectedPreset by remember { mutableStateOf(CropPreset.FULL) }

    // Normalized Crop Insets (0f to 0.4f)
    var insetHorizontal by remember { mutableFloatStateOf(0f) }
    var insetVertical by remember { mutableFloatStateOf(0f) }

    // Temporary URI for Camera capture
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            coroutineScope.launch {
                isProcessing = true
                val bmp = withContext(Dispatchers.IO) {
                    ImageProcessingUtil.loadBitmapFromUri(context, tempCameraUri!!)
                }
                originalBitmap = bmp
                // Auto-set preset based on document type
                if (selectedJenis.contains("KTP", ignoreCase = true)) {
                    selectedPreset = CropPreset.KTP
                } else if (selectedJenis.contains("Keluarga", ignoreCase = true)) {
                    selectedPreset = CropPreset.KK
                }
                isProcessing = false
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isProcessing = true
                val bmp = withContext(Dispatchers.IO) {
                    ImageProcessingUtil.loadBitmapFromUri(context, uri)
                }
                originalBitmap = bmp
                if (selectedJenis.contains("KTP", ignoreCase = true)) {
                    selectedPreset = CropPreset.KTP
                } else if (selectedJenis.contains("Keluarga", ignoreCase = true)) {
                    selectedPreset = CropPreset.KK
                }
                isProcessing = false
            }
        }
    }

    fun triggerCamera() {
        try {
            val photoFile = File(context.cacheDir, "camera_doc_scan_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuka kamera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Recompute processed bitmap whenever original, filter, or crop parameters change
    LaunchedEffect(originalBitmap, selectedFilter, selectedPreset, insetHorizontal, insetVertical) {
        val src = originalBitmap ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            var working = src

            // Apply crop
            val cropRect = when (selectedPreset) {
                CropPreset.FULL -> RectF(insetHorizontal, insetVertical, 1f - insetHorizontal, 1f - insetVertical)
                CropPreset.KTP -> {
                    // Fit 85:54 inside source
                    val srcRatio = src.width.toFloat() / src.height.toFloat()
                    val targetRatio = 85f / 54f
                    if (srcRatio > targetRatio) {
                        val normW = targetRatio / srcRatio
                        val offset = (1f - normW) / 2f
                        RectF(offset, 0f, 1f - offset, 1f)
                    } else {
                        val normH = srcRatio / targetRatio
                        val offset = (1f - normH) / 2f
                        RectF(0f, offset, 1f, 1f - offset)
                    }
                }
                CropPreset.KK -> {
                    val srcRatio = src.width.toFloat() / src.height.toFloat()
                    val targetRatio = 1.414f // Landscape A4 or 1/1.414
                    val actualTarget = if (srcRatio > 1f) 1.414f else (1f / 1.414f)
                    if (srcRatio > actualTarget) {
                        val normW = actualTarget / srcRatio
                        val offset = (1f - normW) / 2f
                        RectF(offset, 0f, 1f - offset, 1f)
                    } else {
                        val normH = srcRatio / actualTarget
                        val offset = (1f - normH) / 2f
                        RectF(0f, offset, 1f, 1f - offset)
                    }
                }
                CropPreset.DOKUMEN -> {
                    val srcRatio = src.width.toFloat() / src.height.toFloat()
                    val targetRatio = 4f / 3f
                    if (srcRatio > targetRatio) {
                        val normW = targetRatio / srcRatio
                        val offset = (1f - normW) / 2f
                        RectF(offset, 0f, 1f - offset, 1f)
                    } else {
                        val normH = srcRatio / targetRatio
                        val offset = (1f - normH) / 2f
                        RectF(0f, offset, 1f, 1f - offset)
                    }
                }
                CropPreset.SQUARE -> {
                    val srcRatio = src.width.toFloat() / src.height.toFloat()
                    if (srcRatio > 1f) {
                        val offset = (1f - (1f / srcRatio)) / 2f
                        RectF(offset, 0f, 1f - offset, 1f)
                    } else {
                        val offset = (1f - srcRatio) / 2f
                        RectF(0f, offset, 1f, 1f - offset)
                    }
                }
            }

            working = ImageProcessingUtil.cropBitmap(working, cropRect)
            working = ImageProcessingUtil.applyFilter(working, selectedFilter)
            processedBitmap = working
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Scanner Dokumen Warga",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${resident.nama} (${resident.nik})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Document Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedJenis,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis / Kategori Dokumen") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        PendudukDocument.DOKUMEN_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedJenis = type
                                    dropdownExpanded = false
                                    if (type.contains("KTP", ignoreCase = true)) {
                                        selectedPreset = CropPreset.KTP
                                    } else if (type.contains("Keluarga", ignoreCase = true)) {
                                        selectedPreset = CropPreset.KK
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Capture / Pick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { triggerCamera() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kamera", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galeri / File", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preview Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1E1E),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    val bmp = processedBitmap ?: originalBitmap
                    if (bmp != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Pratinjau Dokumen",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )

                            // Rotate shortcut button overlay
                            IconButton(
                                onClick = {
                                    originalBitmap?.let {
                                        originalBitmap = ImageProcessingUtil.rotateBitmap(it, 90f)
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = "Putar 90°",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { triggerCamera() },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ketuk untuk Memfoto Dokumen",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "KTP / KK / Akta akan di-crop otomatis",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (originalBitmap != null) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-Crop Ratio Presets
                    Text(
                        text = "Ukuran / Preset Auto Crop:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CropPreset.values().forEach { preset ->
                            FilterChip(
                                selected = selectedPreset == preset,
                                onClick = { selectedPreset = preset },
                                label = { Text(preset.label, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Enhancements
                    Text(
                        text = "Peningkatan Kualitas Citra (Filter):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == ImageProcessingUtil.DocumentFilter.DOCUMENT_ENHANCE,
                            onClick = { selectedFilter = ImageProcessingUtil.DocumentFilter.DOCUMENT_ENHANCE },
                            label = { Text("Dokumen Jernih", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = selectedFilter == ImageProcessingUtil.DocumentFilter.ORIGINAL,
                            onClick = { selectedFilter = ImageProcessingUtil.DocumentFilter.ORIGINAL },
                            label = { Text("Warna Asli", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = selectedFilter == ImageProcessingUtil.DocumentFilter.BLACK_AND_WHITE,
                            onClick = { selectedFilter = ImageProcessingUtil.DocumentFilter.BLACK_AND_WHITE },
                            label = { Text("Hitam Putih", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Folder Destination Preview
                val rwFolder = if (resident.rw.startsWith("rw", ignoreCase = true)) resident.rw else "RW ${resident.rw}"
                val rtFolder = if (resident.rt.startsWith("rt", ignoreCase = true)) resident.rt else "RT ${resident.rt}"
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lokasi Folder Google Drive Otomatis:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📁 SIMDes_Dokumen_Desa / $rwFolder / $rtFolder / ${resident.nik} - ${resident.nama} / $selectedJenis",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Button(
                    onClick = {
                        val bmp = processedBitmap ?: originalBitmap
                        if (bmp == null) {
                            Toast.makeText(context, "Silakan ambil atau pilih foto dokumen terlebih dahulu", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val bytes = ImageProcessingUtil.bitmapToJpegBytes(bmp, quality = 85)
                        onSaveDocument(selectedJenis, bytes)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = (processedBitmap != null || originalBitmap != null),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan & Unggah Dokumen", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                }
            }
        }
    }
}
