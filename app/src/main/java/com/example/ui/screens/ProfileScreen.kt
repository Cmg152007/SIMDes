package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.UserProfile
import com.example.ui.components.PinDialog
import com.example.ui.components.ProfilePhotoCropDialog
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.ImageProcessingUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Mode: View (false) vs Full Page Edit (true)
    var isEditMode by remember { mutableStateOf(false) }

    // PIN Authentication for entering edit mode
    var showPinAuthForEditProfile by remember { mutableStateOf(false) }
    var pinAuthError by remember { mutableStateOf<String?>(null) }

    // Form Edit States for Akun Petugas
    var editNamaPetugas by remember { mutableStateOf(profile.namaPetugas) }
    var editNipPetugas by remember { mutableStateOf(profile.nipPetugas) }
    var editJabatan by remember { mutableStateOf(profile.jabatan) }
    var editNoHp by remember { mutableStateOf(profile.noHp) }
    var editWilayahKerja by remember { mutableStateOf(profile.wilayahKerja.ifBlank { "Semua Wilayah" }) }
    var editFotoProfilPath by remember { mutableStateOf(profile.fotoProfilPath) }
    var isWilayahDropdownExpanded by remember { mutableStateOf(false) }

    // Form Edit States for Desa & Instansi
    var editNamaDesa by remember { mutableStateOf(profile.namaDesa) }
    var editKecamatan by remember { mutableStateOf(profile.kecamatan) }
    var editKabupaten by remember { mutableStateOf(profile.kabupaten) }
    var editProvinsi by remember { mutableStateOf(profile.provinsi) }
    var editKodePos by remember { mutableStateOf(profile.kodePos) }
    var editAlamatKantor by remember { mutableStateOf(profile.alamatKantor) }
    var editEmailDesa by remember { mutableStateOf(profile.emailDesa) }
    var editNamaKades by remember { mutableStateOf(profile.namaKades) }
    var editNipKades by remember { mutableStateOf(profile.nipKades) }
    var editTotalRw by remember { mutableStateOf(profile.totalRw.toString()) }
    var editTotalRt by remember { mutableStateOf(profile.totalRt.toString()) }

    // Crop Dialog State
    var rawPhotoBitmapToCrop by remember { mutableStateOf<Bitmap?>(null) }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val loadedBitmap = withContext(Dispatchers.IO) {
                    ImageProcessingUtil.loadBitmapFromUri(context, uri, maxDimension = 1600)
                }
                if (loadedBitmap != null) {
                    rawPhotoBitmapToCrop = loadedBitmap
                } else {
                    Toast.makeText(context, "Gagal memuat foto yang dipilih", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Helper to reset edit states to current saved profile
    val resetEditStates = {
        editNamaPetugas = profile.namaPetugas
        editNipPetugas = profile.nipPetugas
        editJabatan = profile.jabatan
        editNoHp = profile.noHp
        editWilayahKerja = profile.wilayahKerja.ifBlank { "Semua Wilayah" }
        editFotoProfilPath = profile.fotoProfilPath
        editNamaDesa = profile.namaDesa
        editKecamatan = profile.kecamatan
        editKabupaten = profile.kabupaten
        editProvinsi = profile.provinsi
        editKodePos = profile.kodePos
        editAlamatKantor = profile.alamatKantor
        editEmailDesa = profile.emailDesa
        editNamaKades = profile.namaKades
        editNipKades = profile.nipKades
        editTotalRw = profile.totalRw.toString()
        editTotalRt = profile.totalRt.toString()
    }

    // PIN Authorizer
    val handleAuthorizeProfileEdit: (String) -> Boolean = { pin ->
        if (viewModel.verifySecurityPin(pin)) {
            showPinAuthForEditProfile = false
            pinAuthError = null
            resetEditStates()
            isEditMode = true
            true
        } else {
            pinAuthError = "PIN Otorisasi salah! Akses edit ditolak."
            false
        }
    }

    // ==========================================
    // UI LAYOUT
    // ==========================================
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
    ) {
        // ==========================================
        // MODE EDIT: FULL PAGE FORM
        // ==========================================
        if (isEditMode) {
            // Header Bar Mode Edit
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                resetEditStates()
                                isEditMode = false
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Batal",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Edit Profil & Akun",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Ubah data akun petugas, instansi desa, dan pimpinan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section 1: Data Akun Pengguna / Petugas
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1. Informasi Akun Petugas",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Foto Profil Avatar Picker Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (editFotoProfilPath.isNotBlank() && File(editFotoProfilPath).exists()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(File(editFotoProfilPath)),
                                        contentDescription = "Foto Petugas",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }

                                // Overlay badge icon camera
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Foto Profil Petugas",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Ketuk untuk memilih foto dari galeri",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (editFotoProfilPath.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Hapus Foto",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.clickable { editFotoProfilPath = "" }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        OutlinedTextField(
                            value = editNamaPetugas,
                            onValueChange = { editNamaPetugas = it },
                            label = { Text("Nama Lengkap Petugas & Gelar") },
                            placeholder = { Text("cth. PENDI, S.Sos., M.Si") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editNipPetugas,
                            onValueChange = { editNipPetugas = it },
                            label = { Text("NIP Petugas") },
                            placeholder = { Text("cth. 19880409 06152007 0002") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editJabatan,
                            onValueChange = { editJabatan = it },
                            label = { Text("Jabatan / Posisi") },
                            placeholder = { Text("cth. Kasi Pemerintahan") },
                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editNoHp,
                            onValueChange = { editNoHp = it },
                            label = { Text("Nomor HP / WhatsApp") },
                            placeholder = { Text("cth. 0812-3456-7890") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Wilayah Kerja Dropdown Selector
                        ExposedDropdownMenuBox(
                            expanded = isWilayahDropdownExpanded,
                            onExpandedChange = { isWilayahDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val cleanDisplay = if (editWilayahKerja.isBlank() || editWilayahKerja.equals("Semua Wilayah", ignoreCase = true)) {
                                "Semua Dusun"
                            } else {
                                "Dusun ${editWilayahKerja.replace("Dusun", "", ignoreCase = true).trim()}"
                            }
                            OutlinedTextField(
                                value = cleanDisplay,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Wilayah Penugasan") },
                                leadingIcon = { Icon(Icons.Default.Map, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isWilayahDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = isWilayahDropdownExpanded,
                                onDismissRequest = { isWilayahDropdownExpanded = false }
                            ) {
                                UserProfile.WILAYAH_KERJA_OPTIONS.forEach { opt ->
                                    val isSelected = (opt == "Semua Wilayah" && (editWilayahKerja.isBlank() || editWilayahKerja.equals("Semua Wilayah", ignoreCase = true))) ||
                                            editWilayahKerja.equals(opt, ignoreCase = true)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = if (opt == "Semua Wilayah") "Semua Dusun" else "Dusun $opt",
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    Text(
                                                        text = UserProfile.getRtDescriptionForWilayah(opt),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            editWilayahKerja = if (opt == "Semua Wilayah") "" else opt
                                            isWilayahDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Data Instansi & Wilayah Pemerintahan Desa
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "2. Data Instansi & Wilayah Desa",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedTextField(
                            value = editNamaDesa,
                            onValueChange = { editNamaDesa = it },
                            label = { Text("Nama Desa / Kelurahan") },
                            placeholder = { Text("cth. Desa Cimanggu") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = editKecamatan,
                                onValueChange = { editKecamatan = it },
                                label = { Text("Kecamatan") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editKabupaten,
                                onValueChange = { editKabupaten = it },
                                label = { Text("Kabupaten/Kota") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = editProvinsi,
                                onValueChange = { editProvinsi = it },
                                label = { Text("Provinsi") },
                                modifier = Modifier.weight(1.3f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editKodePos,
                                onValueChange = { editKodePos = it },
                                label = { Text("Kode Pos") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.9f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = editAlamatKantor,
                            onValueChange = { editAlamatKantor = it },
                            label = { Text("Alamat Kantor Desa") },
                            placeholder = { Text("cth. Jl. Raya Puspahiang - Cimanggu") },
                            leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editEmailDesa,
                            onValueChange = { editEmailDesa = it },
                            label = { Text("Email Resmi Desa") },
                            placeholder = { Text("cth. desacimanggu07@gmail.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Section 3: Pimpinan Desa & Struktur Wilayah
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "3. Pimpinan Desa & Struktur Wilayah",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedTextField(
                            value = editNamaKades,
                            onValueChange = { editNamaKades = it },
                            label = { Text("Nama Kepala Desa") },
                            placeholder = { Text("cth. MAIL") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editNipKades,
                            onValueChange = { editNipKades = it },
                            label = { Text("NIP Kepala Desa (Jika Ada)") },
                            placeholder = { Text("cth. -") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = editTotalRw,
                                onValueChange = { editTotalRw = it },
                                label = { Text("Total RW") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editTotalRt,
                                onValueChange = { editTotalRt = it },
                                label = { Text("Total RT") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Bottom Action Buttons in Edit Mode
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            resetEditStates()
                            isEditMode = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Batal", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Button(
                        onClick = {
                            val cleanWilayah = if (editWilayahKerja.equals("Semua Wilayah", ignoreCase = true)) "" else editWilayahKerja.trim()
                            val updatedProfile = profile.copy(
                                namaPetugas = editNamaPetugas.trim().ifBlank { profile.namaPetugas },
                                nipPetugas = editNipPetugas.trim(),
                                jabatan = editJabatan.trim().ifBlank { profile.jabatan },
                                noHp = editNoHp.trim(),
                                wilayahKerja = cleanWilayah,
                                fotoProfilPath = editFotoProfilPath,
                                namaDesa = editNamaDesa.trim().ifBlank { profile.namaDesa },
                                kecamatan = editKecamatan.trim().ifBlank { profile.kecamatan },
                                kabupaten = editKabupaten.trim().ifBlank { profile.kabupaten },
                                provinsi = editProvinsi.trim().ifBlank { profile.provinsi },
                                kodePos = editKodePos.trim(),
                                alamatKantor = editAlamatKantor.trim(),
                                emailDesa = editEmailDesa.trim(),
                                namaKades = editNamaKades.trim().ifBlank { profile.namaKades },
                                nipKades = editNipKades.trim(),
                                totalRw = editTotalRw.toIntOrNull() ?: profile.totalRw,
                                totalRt = editTotalRt.toIntOrNull() ?: profile.totalRt
                            )

                            viewModel.saveUserProfile(updatedProfile)
                            isEditMode = false
                            Toast.makeText(context, "Data Profil & Akun Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Perubahan", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                    }
                }
            }
        } else {
            // ==========================================
            // MODE VIEW: TAMPILAN PROFIL AKUN & DATA DESA
            // ==========================================

            // 1. Hero Card: Informasi Akun Petugas / Pengguna
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Photo / Icon
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(3.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profile.fotoProfilPath.isNotBlank() && File(profile.fotoProfilPath).exists()) {
                                Image(
                                    painter = rememberAsyncImagePainter(File(profile.fotoProfilPath)),
                                    contentDescription = "Foto Petugas",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar Petugas",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PETUGAS ADMINISTRATOR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nama Petugas
                        Text(
                            text = profile.namaPetugas.ifBlank { "Petugas SIMDes" },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )

                        // NIP & Jabatan
                        if (profile.nipPetugas.isNotBlank() && profile.nipPetugas != "-") {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "NIP. ${profile.nipPetugas}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${profile.jabatan} • ${profile.namaDesa}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Detail Rows: Kontak HP & Wilayah Penugasan
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row No HP / WA
                            if (profile.noHp.isNotBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                val clean = profile.noHp.replace(Regex("[^0-9+]"), "")
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = profile.noHp,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "Hubungi",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Row Wilayah Penugasan
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    val cleanDusun = profile.wilayahKerja.replace("Dusun", "", ignoreCase = true).trim()
                                    Text(
                                        text = if (cleanDusun.isNotBlank() && !cleanDusun.equals("Semua Wilayah", ignoreCase = true)) {
                                            "Dusun $cleanDusun"
                                        } else {
                                            "Semua Dusun"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = UserProfile.getRtDescriptionForWilayah(profile.wilayahKerja),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tombol Beralih ke Form Edit (PIN Otorisasi)
                        Button(
                            onClick = {
                                pinAuthError = null
                                showPinAuthForEditProfile = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Profil & Informasi Akun",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 2. Card: Profil Instansi & Kantor Pemerintahan Desa
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Informasi Pemerintahan Desa",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Item Nama Desa & Alamat
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = profile.namaDesa,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Kecamatan ${profile.kecamatan}, ${profile.kabupaten}, ${profile.provinsi} ${if (profile.kodePos.isNotBlank()) "(${profile.kodePos})" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (profile.alamatKantor.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = profile.alamatKantor,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Item Kepala Desa
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Kepala Desa: ${profile.namaKades}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (profile.nipKades.isNotBlank() && profile.nipKades != "-") {
                                    Text(
                                        text = "NIP. ${profile.nipKades}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Item Email Desa
                        if (profile.emailDesa.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:${profile.emailDesa}")
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = profile.emailDesa,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Structure Summary (RW & RT)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Struktur Kewilayahan",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${profile.totalRw} RW • ${profile.totalRt} RT (5 Dusun)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Card: Pintasan Pengaturan Aplikasi
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.Settings) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pengaturan Aplikasi",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Keamanan & PIN, Integrasi Spreadsheet, serta Akses Pengembang",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Buka",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.navigateTo(Screen.Settings) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka Pengaturan Aplikasi", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // 4. Card: Kunci & Keluar Sesi
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Kunci Aplikasi",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Kunci sesi saat ini dan kembali ke halaman PIN login",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kunci Sesi Aplikasi", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // MODAL DIALOGS
    // ==========================================

    // PIN Dialog Otorisasi Masuk Edit Profil & Akun
    if (showPinAuthForEditProfile) {
        PinDialog(
            title = "Otorisasi Edit Profil & Akun",
            subtitle = "Masukkan Kode Keamanan / PIN untuk mengubah data akun petugas, instansi, dan pimpinan desa",
            confirmButtonText = "Buka Formulir Edit",
            errorMessage = pinAuthError,
            onDismiss = {
                showPinAuthForEditProfile = false
                pinAuthError = null
            },
            onPinSubmit = handleAuthorizeProfileEdit
        )
    }

    // Crop Dialog for Profile Photo
    rawPhotoBitmapToCrop?.let { bmp ->
        ProfilePhotoCropDialog(
            initialBitmap = bmp,
            onDismiss = { rawPhotoBitmapToCrop = null },
            onChangePhotoSource = {
                rawPhotoBitmapToCrop = null
                photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            onCropSuccess = { croppedBmp ->
                coroutineScope.launch {
                    try {
                        val file = withContext(Dispatchers.IO) {
                            val dir = File(context.filesDir, "profile_photos").apply { mkdirs() }
                            val targetFile = File(dir, "avatar_${System.currentTimeMillis()}.jpg")
                            val bytes = ImageProcessingUtil.bitmapToJpegBytes(croppedBmp, quality = 90)
                            FileOutputStream(targetFile).use { it.write(bytes) }
                            targetFile
                        }
                        editFotoProfilPath = file.absolutePath
                        rawPhotoBitmapToCrop = null
                        Toast.makeText(context, "Foto profil berhasil disesuaikan", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal menyimpan foto: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}
