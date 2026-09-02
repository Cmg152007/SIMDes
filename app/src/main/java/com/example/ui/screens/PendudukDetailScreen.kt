package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.Penduduk
import com.example.data.model.PendudukDocument
import com.example.data.model.UserProfile
import com.example.ui.components.DocumentScannerDialog
import com.example.ui.components.DocumentViewerDialog
import com.example.ui.components.UploadProgressDialog
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendudukDetailScreen(
    nik: String,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allResidents by viewModel.allPenduduk.collectAsState()
    val resident = allResidents.find { it.nik == nik }
    val userProfile by viewModel.userProfile.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Family members grouped by No KK
    val familyMembers = remember(resident?.noKk, allResidents) {
        if (resident != null && resident.noKk.isNotBlank()) {
            allResidents.filter { it.noKk.trim().equals(resident.noKk.trim(), ignoreCase = true) }
                .sortedWith(
                    compareBy(
                        { member ->
                            when {
                                member.shdk.contains("KEPALA", ignoreCase = true) -> 1
                                member.shdk.contains("ISTRI", ignoreCase = true) -> 2
                                member.shdk.contains("SUAMI", ignoreCase = true) -> 3
                                member.shdk.contains("ANAK", ignoreCase = true) -> 4
                                member.shdk.contains("MENANTU", ignoreCase = true) -> 5
                                member.shdk.contains("CUCU", ignoreCase = true) -> 6
                                member.shdk.contains("ORANG TUA", ignoreCase = true) -> 7
                                member.shdk.contains("MERTUA", ignoreCase = true) -> 8
                                member.shdk.contains("FAMILI", ignoreCase = true) -> 9
                                else -> 10
                            }
                        },
                        { it.anakKe },
                        { -it.getEffectiveAge() }
                    )
                )
        } else {
            emptyList()
        }
    }

    // Documents state
    val residentDocuments by (if (resident != null) viewModel.getDocumentsForResident(resident.nik) else kotlinx.coroutines.flow.flowOf(emptyList())).collectAsState(initial = emptyList())
    val uploadState by viewModel.uploadState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMutasiDialog by remember { mutableStateOf(false) }
    var showHubungiDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var initialScannerCategory by remember { mutableStateOf("KTP") }
    var selectedViewingDoc by remember { mutableStateOf<PendudukDocument?>(null) }

    val hasValidPhone = remember(resident?.noHandphone) {
        val phone = resident?.noHandphone?.trim() ?: ""
        phone.isNotBlank() && phone != "-" && !phone.equals("tidak ada", ignoreCase = true) && !phone.equals("null", ignoreCase = true) && phone.any { it.isDigit() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detail Penduduk",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (resident != null) {
                            Text(
                                text = resident.nama,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.PendudukList) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    if (resident != null) {
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.PendudukForm(resident.nik)) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Data",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Data",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (resident == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Data penduduk tidak ditemukan.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.navigateTo(Screen.PendudukList) }) {
                        Text("Kembali ke Daftar Penduduk")
                    }
                }
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Modern Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Biodata", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FamilyRestroom,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kartu Keluarga", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal)
                                if (familyMembers.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = if (selectedTabIndex == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = familyMembers.size.toString(),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                            color = if (selectedTabIndex == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dokumen", fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal)
                                if (residentDocuments.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = if (selectedTabIndex == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = residentDocuments.size.toString(),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                            color = if (selectedTabIndex == 2) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                if (selectedTabIndex == 0) {
                    // TAB 0: BIODATA
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // 1. Profile Hero Card with Unified Actions
                        item {
                            ModernProfileHeroCard(
                                resident = resident,
                                hasValidPhone = hasValidPhone,
                                onContactClick = { showHubungiDialog = true },
                                onMutasiClick = { showMutasiDialog = true },
                                onEditClick = { viewModel.navigateTo(Screen.PendudukForm(resident.nik)) },
                                onViewKkClick = { selectedTabIndex = 1 }
                            )
                        }

                        // 2. Pending Sync Alert (Compact, shown only if not synced)
                        if (!resident.syncedWithSheets) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFF3E0),
                                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudOff,
                                                contentDescription = null,
                                                tint = Color(0xFFE65100),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Data Belum Tersinkron",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFFE65100)
                                                )
                                                Text(
                                                    text = "Perubahan tersimpan lokal di perangkat",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = Color(0xFF8D4004)
                                                )
                                            }
                                        }
                                        TextButton(
                                            onClick = { viewModel.syncWithSpreadsheet() },
                                            enabled = !isSyncing,
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            if (isSyncing) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFFE65100))
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFE65100))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Sinkronkan", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Mutation Detail Card (Only if Meninggal or Pindah)
                        if (resident.isMeninggal()) {
                            item {
                                DetailSectionCard(
                                    title = "Dokumentasi Kematian",
                                    icon = Icons.Default.PersonOff,
                                    iconTint = Color(0xFF212121)
                                ) {
                                    DetailGridRow(
                                        label1 = "Tanggal Kematian", value1 = resident.tanggalKematian,
                                        label2 = "Waktu Kematian", value2 = resident.waktuKematian
                                    )
                                    DetailGridRow(
                                        label1 = "Tempat Kematian", value1 = resident.tempatKematian,
                                        label2 = "Penyebab", value2 = resident.penyebabKematian
                                    )
                                    DetailRow(label = "Tempat Pemakaman", value = resident.tempatPemakaman)
                                    DetailGridRow(
                                        label1 = "No Surat Kematian", value1 = resident.noSuratKematian,
                                        label2 = "Pelapor", value2 = "${resident.namaPelaporKematian} (${resident.hubunganPelaporKematian})"
                                    )
                                    if (resident.catatanKematian.isNotBlank() || resident.keterangan.isNotBlank()) {
                                        DetailRow(label = "Catatan", value = resident.catatanKematian.ifBlank { resident.keterangan })
                                    }
                                }
                            }
                        }

                        if (resident.isPindah()) {
                            item {
                                DetailSectionCard(
                                    title = "Dokumentasi Kepindahan",
                                    icon = Icons.Default.FlightTakeoff,
                                    iconTint = Color(0xFFE65100)
                                ) {
                                    DetailGridRow(
                                        label1 = "Tanggal Pindah", value1 = resident.tanggalPindah,
                                        label2 = "Alasan Pindah", value2 = resident.alasanPindah
                                    )
                                    DetailRow(label = "Alamat Tujuan", value = resident.alamatTujuan)
                                    DetailGridRow(
                                        label1 = "RT / RW Tujuan", value1 = "RT ${resident.rtTujuan} / RW ${resident.rwTujuan}",
                                        label2 = "Desa / Kelurahan", value2 = resident.desaTujuan
                                    )
                                    DetailGridRow(
                                        label1 = "Kecamatan", value1 = resident.kecamatanTujuan,
                                        label2 = "Kabupaten / Kota", value2 = resident.kabupatenTujuan
                                    )
                                    DetailGridRow(
                                        label1 = "Provinsi", value1 = resident.provinsiTujuan,
                                        label2 = "No Surat Pindah", value2 = resident.noSuratPindah
                                    )
                                    if (resident.catatanPindah.isNotBlank() || resident.keterangan.isNotBlank()) {
                                        DetailRow(label = "Catatan", value = resident.catatanPindah.ifBlank { resident.keterangan })
                                    }
                                }
                            }
                        }

                        // 4. Section: Identitas Diri
                        item {
                            DetailSectionCard(
                                title = "Identitas Diri",
                                icon = Icons.Default.Badge,
                                iconTint = MaterialTheme.colorScheme.primary
                            ) {
                                DetailGridRow(
                                    label1 = "Jenis Kelamin", value1 = resident.getGenderDisplayLabel(),
                                    label2 = "Usia", value2 = "${resident.getEffectiveAge()} Tahun"
                                )
                                DetailGridRow(
                                    label1 = "Tempat Lahir", value1 = resident.tempatLahir,
                                    label2 = "Tanggal Lahir", value2 = resident.tanggalLahir
                                )
                                DetailGridRow(
                                    label1 = "Agama", value1 = resident.agama,
                                    label2 = "Golongan Darah", value2 = resident.gdr
                                )
                                DetailGridRow(
                                    label1 = "Status Perkawinan", value1 = resident.statusPerkawinan,
                                    label2 = "Buku Nikah", value2 = resident.bukuNikah
                                )
                                DetailGridRow(
                                    label1 = "Hubungan Keluarga", value1 = resident.shdk,
                                    label2 = "Kewarganegaraan", value2 = resident.kewarganegaraan
                                )
                            }
                        }

                        // 5. Section: Domisili & Kontak
                        item {
                            DetailSectionCard(
                                title = "Domisili & Kontak",
                                icon = Icons.Default.Home,
                                iconTint = Color(0xFF2E7D32)
                            ) {
                                DetailRow(label = "Alamat", value = resident.alamat)
                                DetailGridRow(
                                    label1 = "RT", value1 = resident.rt,
                                    label2 = "RW", value2 = resident.rw
                                )
                                // Interactive phone row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "No. Handphone / WA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = resident.noHandphone.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (hasValidPhone) {
                                        Surface(
                                            onClick = { showHubungiDialog = true },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFE8F5E9),
                                            border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Phone,
                                                    contentDescription = "Hubungi",
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Hubungi",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF2E7D32)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6. Section: Pendidikan & Pekerjaan
                        item {
                            DetailSectionCard(
                                title = "Pendidikan & Pekerjaan",
                                icon = Icons.Default.Work,
                                iconTint = Color(0xFF0288D1)
                            ) {
                                DetailGridRow(
                                    label1 = "Pendidikan Terakhir", value1 = resident.pendidikanTerakhir,
                                    label2 = "Pekerjaan", value2 = resident.pekerjaan
                                )
                                if (resident.usahaYangDijalankan.isNotBlank() && resident.usahaYangDijalankan != "-") {
                                    DetailRow(label = "Usaha yang Dijalankan", value = resident.usahaYangDijalankan)
                                }
                            }
                        }

                        // 7. Section: Orang Tua & Keluarga
                        item {
                            DetailSectionCard(
                                title = "Orang Tua & Keluarga",
                                icon = Icons.Default.FamilyRestroom,
                                iconTint = Color(0xFF7B1FA2)
                            ) {
                                DetailGridRow(
                                    label1 = "Nama Ayah", value1 = resident.namaAyah,
                                    label2 = "Nama Ibu", value2 = resident.namaIbu
                                )
                                DetailGridRow(
                                    label1 = "Kepala Keluarga", value1 = resident.namaKepalaKeluarga,
                                    label2 = "Anak Ke", value2 = "${resident.anakKe}"
                                )
                            }
                        }

                        // 8. Section: Kesehatan & Dokumen Sipil
                        item {
                            DetailSectionCard(
                                title = "Kesehatan & Dokumen Sipil",
                                icon = Icons.Default.MedicalServices,
                                iconTint = Color(0xFFD32F2F)
                            ) {
                                DetailGridRow(
                                    label1 = "BPJS / KIS", value1 = resident.kartuBpjsKis,
                                    label2 = "e-KTP", value2 = resident.kepemilikanEKtp
                                )
                                DetailGridRow(
                                    label1 = "Akta Kelahiran", value1 = resident.kepemilikanAktaKelahiran,
                                    label2 = "Kartu KIA", value2 = resident.kartuKia
                                )
                                DetailGridRow(
                                    label1 = "Disabilitas", value1 = resident.disabilitas,
                                    label2 = "Vaksinasi", value2 = resident.vaksinasi
                                )
                                if (resident.jenisKb.isNotBlank() && resident.jenisKb != "-") {
                                    DetailRow(label = "Peserta KB", value = resident.jenisKb)
                                }
                            }
                        }

                        // 9. Section: Bantuan Sosial & Kesejahteraan
                        item {
                            DetailSectionCard(
                                title = "Bansos & Kesejahteraan",
                                icon = Icons.Default.CardMembership,
                                iconTint = Color(0xFFF57C00)
                            ) {
                                DetailGridRow(
                                    label1 = "Kartu PKH", value1 = resident.kartuPkh,
                                    label2 = "Kartu BPNT", value2 = resident.kartuBpnt
                                )
                                DetailGridRow(
                                    label1 = "Kartu KIP", value1 = resident.kartuKip,
                                    label2 = "Status Rumah", value2 = resident.kepemilikanRumah
                                )
                                DetailGridRow(
                                    label1 = "Jenis/Ukuran Rumah", value1 = "${resident.jenisRumah} (${resident.ukuranRumah})",
                                    label2 = "Daya Listrik", value2 = "${resident.dayaListrik} (${resident.listrikJenis})"
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                } else if (selectedTabIndex == 1) {
                    // TAB 1: KARTU KELUARGA
                    KartuKeluargaTabContent(
                        currentResident = resident,
                        familyMembers = familyMembers,
                        userProfile = userProfile,
                        isSyncing = isSyncing,
                        onNavigateToDetail = { targetNik -> viewModel.navigateTo(Screen.PendudukDetail(targetNik)) },
                        onNavigateToEdit = { targetNik -> viewModel.navigateTo(Screen.PendudukForm(targetNik)) },
                        onAddNewMember = { viewModel.navigateTo(Screen.PendudukForm(nik = null, initialNoKk = resident.noKk)) },
                        onSyncTrigger = { viewModel.syncWithSpreadsheet() }
                    )
                } else {
                    // TAB 2: ARSIP DOKUMEN
                    val filteredDocs = if (selectedCategoryFilter == "Semua") {
                        residentDocuments
                    } else {
                        residentDocuments.filter { it.jenisDokumen.equals(selectedCategoryFilter, ignoreCase = true) }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // Header Bar with Scan Button
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Arsip Dokumen Warga",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "${residentDocuments.size} berkas tersimpan",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            initialScannerCategory = if (selectedCategoryFilter == "Semua") "KTP" else selectedCategoryFilter
                                            showScannerDialog = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pindai", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        // Category Filter Chips
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCategoryFilter == "Semua",
                                        onClick = { selectedCategoryFilter = "Semua" },
                                        label = { Text("Semua (${residentDocuments.size})") },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                items(PendudukDocument.DOKUMEN_TYPES) { type ->
                                    val count = residentDocuments.count { it.jenisDokumen.equals(type, ignoreCase = true) }
                                    FilterChip(
                                        selected = selectedCategoryFilter == type,
                                        onClick = { selectedCategoryFilter = type },
                                        label = { Text("$type ${if (count > 0) "($count)" else ""}") },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        // Document Items List or Clean Empty State
                        if (filteredDocs.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DocumentScanner,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = if (selectedCategoryFilter == "Semua") "Belum ada dokumen tersimpan" else "Belum ada dokumen '$selectedCategoryFilter'",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Gunakan kamera untuk memindai dokumen fisik warga seperti KTP atau KK.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                initialScannerCategory = if (selectedCategoryFilter == "Semua") "KTP" else selectedCategoryFilter
                                                showScannerDialog = true
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Pindai Dokumen Sekarang")
                                        }
                                    }
                                }
                            }
                        } else {
                            items(filteredDocs, key = { it.id }) { doc ->
                                DocumentItemCard(
                                    document = doc,
                                    onClick = { selectedViewingDoc = doc },
                                    onRetryUpload = { viewModel.retryUploadDocumentToDrive(doc) },
                                    onShare = {
                                        doc.localFilePath?.let { path ->
                                            val file = File(path)
                                            if (file.exists()) {
                                                try {
                                                    val uri = FileProvider.getUriForFile(
                                                        context,
                                                        "${context.packageName}.fileprovider",
                                                        file
                                                    )
                                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                                        type = doc.mimeType
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        putExtra(Intent.EXTRA_TEXT, "Dokumen ${doc.jenisDokumen} an. ${doc.namaWarga} (${doc.nik})")
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(intent, "Bagikan Dokumen"))
                                                } catch (e: Exception) {}
                                            }
                                        }
                                    },
                                    onDelete = { viewModel.deleteDocument(doc) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    // Scanner Dialog
    if (showScannerDialog && resident != null) {
        DocumentScannerDialog(
            resident = resident,
            initialJenisDokumen = initialScannerCategory,
            onDismiss = { showScannerDialog = false },
            onSaveDocument = { jenisDokumen, imageBytes ->
                showScannerDialog = false
                viewModel.saveAndUploadDocument(
                    nik = resident.nik,
                    noKk = resident.noKk,
                    namaWarga = resident.nama,
                    rw = resident.rw,
                    rt = resident.rt,
                    jenisDokumen = jenisDokumen,
                    imageBytes = imageBytes,
                    autoUploadDrive = true
                )
            }
        )
    }

    // Upload Progress Dialog
    UploadProgressDialog(
        uploadState = uploadState,
        onDismiss = { viewModel.dismissUploadModal() },
        onRetry = {
            uploadState.document?.let { doc ->
                viewModel.retryUploadDocumentToDrive(doc)
            }
        }
    )

    // Document Viewer Dialog
    selectedViewingDoc?.let { doc ->
        DocumentViewerDialog(
            document = doc,
            onDismiss = { selectedViewingDoc = null },
            onDelete = { docToDelete ->
                viewModel.deleteDocument(docToDelete)
                selectedViewingDoc = null
            },
            onRetryUpload = { docToRetry ->
                viewModel.retryUploadDocumentToDrive(docToRetry)
            }
        )
    }

    // Mutasi Dialog
    if (showMutasiDialog && resident != null) {
        MutasiPendudukDialog(
            penduduk = resident,
            onDismiss = { showMutasiDialog = false },
            onSaveMutation = { updatedPenduduk, jenisMutasi ->
                showMutasiDialog = false
                viewModel.catatMutasi(updatedPenduduk, jenisMutasi) {
                    Toast.makeText(context, "Mutasi berhasil dicatat!", Toast.LENGTH_SHORT).show()
                }
            },
            onRestoreActive = { nikToRestore ->
                showMutasiDialog = false
                viewModel.batalkanMutasi(nikToRestore) {
                    Toast.makeText(context, "Status warga berhasil dipulihkan menjadi Aktif!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Hubungi Dialog (Telepon, SMS, WhatsApp)
    if (showHubungiDialog && resident != null && hasValidPhone) {
        HubungiPendudukDialog(
            penduduk = resident,
            onDismiss = { showHubungiDialog = false }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog && resident != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Data Penduduk") },
            text = {
                Text("Apakah Anda yakin ingin menghapus data '${resident.nama}' (NIK: ${resident.nik})? Tindakan ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePenduduk(resident) {
                            showDeleteDialog = false
                            viewModel.navigateTo(Screen.PendudukList)
                        }
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Modern Profile Hero Card with Clean Action Buttons
 */
@Composable
fun ModernProfileHeroCard(
    resident: Penduduk,
    hasValidPhone: Boolean,
    onContactClick: () -> Unit,
    onMutasiClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewKkClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val isMale = resident.isMale()
    val avatarBg = when {
        resident.isMeninggal() -> Color(0xFF424242)
        resident.isPindah() -> Color(0xFFE65100)
        isMale -> Color(0xFF0288D1)
        else -> Color(0xFFC2185B)
    }

    val statusBadgeBg = when (resident.statusMutasi.uppercase()) {
        "MENINGGAL" -> Color(0xFF212121)
        "PINDAH" -> Color(0xFFE65100)
        else -> Color(0xFF2E7D32)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar, Name & Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            resident.isMeninggal() -> Icons.Default.PersonOff
                            resident.isPindah() -> Icons.Default.FlightTakeoff
                            else -> Icons.Default.Person
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Sub-details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resident.nama,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${resident.shdk} • ${resident.getGenderDisplayLabel()} • ${resident.getEffectiveAge()} Thn",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBadgeBg
                ) {
                    Text(
                        text = resident.statusMutasi.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // NIK & No KK Tile Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // NIK Tile
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NIK",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = resident.nik,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(resident.nik))
                                Toast.makeText(context, "NIK disalin", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin NIK",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // No KK Tile
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No KK",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = resident.noKk.ifBlank { "-" },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (resident.noKk.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(resident.noKk))
                                    Toast.makeText(context, "No KK disalin", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Salin KK",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Unified Non-Redundant Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hubungi (Only if valid phone is available)
                if (hasValidPhone) {
                    Button(
                        onClick = onContactClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hubungi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Mutasi Button
                OutlinedButton(
                    onClick = onMutasiClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mutasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Edit Data Button
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Modern Clean Detail Card with Icon & Title
 */
@Composable
fun DetailSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

/**
 * Two-Column Grid Row for Compact Display
 */
@Composable
fun DetailGridRow(
    label1: String,
    value1: String,
    label2: String,
    value2: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label1,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value1.ifBlank { "-" },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label2,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value2.ifBlank { "-" },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Single-line Key-Value Row
 */
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.1f)
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.4f)
        )
    }
}

/**
 * Modern Kartu Keluarga Tab Content
 */
@Composable
fun KartuKeluargaTabContent(
    currentResident: Penduduk,
    familyMembers: List<Penduduk>,
    userProfile: UserProfile,
    isSyncing: Boolean,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onAddNewMember: () -> Unit,
    onSyncTrigger: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    if (currentResident.noKk.isBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Nomor Kartu Keluarga Kosong",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Warga ${currentResident.nama} belum memiliki Nomor KK yang terdaftar.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { onNavigateToEdit(currentResident.nik) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Isi Nomor KK")
                    }
                }
            }
        }
        return
    }

    val familyHead = familyMembers.find { it.shdk.contains("KEPALA", ignoreCase = true) } ?: familyMembers.firstOrNull()
    val unsyncedCount = familyMembers.count { !it.syncedWithSheets }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Digital KK Header Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "KARTU KELUARGA",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "No: ${currentResident.noKk}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentResident.noKk))
                                Toast.makeText(context, "Nomor KK disalin", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin KK",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    val headName = familyHead?.nama ?: currentResident.namaKepalaKeluarga.ifBlank { currentResident.nama }
                    val alamatKk = familyHead?.alamat ?: currentResident.alamat
                    val rtKk = familyHead?.rt ?: currentResident.rt
                    val rwKk = familyHead?.rw ?: currentResident.rw

                    DetailRow("Kepala Keluarga", headName)
                    DetailRow("Alamat Domisili", "$alamatKk (RT $rtKk / RW $rwKk)")
                }
            }
        }

        // Family Summary Metric Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Jiwa", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${familyMembers.size}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0288D1).copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Laki-laki", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${familyMembers.count { it.isMale() }}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF0288D1))
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFC2185B).copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Perempuan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${familyMembers.count { it.isFemale() }}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFC2185B))
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Bansos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${familyMembers.count { it.isPenerimaBansos() }}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                    }
                }
            }
        }

        // Action Toolbar (Tambah Anggota & Bagikan)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddNewMember,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Anggota", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val kkText = buildFamilyCardSummaryText(currentResident, familyMembers, userProfile)
                        clipboardManager.setText(AnnotatedString(kkText))
                        Toast.makeText(context, "Susunan KK disalin", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin KK", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val kkText = buildFamilyCardSummaryText(currentResident, familyMembers, userProfile)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, kkText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Bagikan KK"))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bagikan", fontSize = 12.sp)
                }
            }
        }

        // Section Title: Anggota Keluarga
        item {
            Text(
                text = "Anggota Keluarga (${familyMembers.size} Jiwa)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // List of Family Members
        items(familyMembers) { member ->
            val index = familyMembers.indexOf(member) + 1
            val isCurrent = member.nik == currentResident.nik
            FamilyMemberCard(
                order = index,
                member = member,
                isCurrentResident = isCurrent,
                onViewDetail = { onNavigateToDetail(member.nik) },
                onEdit = { onNavigateToEdit(member.nik) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Modern Family Member Card
 */
@Composable
fun FamilyMemberCard(
    order: Int,
    member: Penduduk,
    isCurrentResident: Boolean,
    onViewDetail: () -> Unit,
    onEdit: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val shdkUpper = member.shdk.uppercase()
    val shdkBg = when {
        shdkUpper.contains("KEPALA") -> MaterialTheme.colorScheme.primaryContainer
        shdkUpper.contains("ISTRI") -> Color(0xFFFCE4EC)
        shdkUpper.contains("SUAMI") -> Color(0xFFE8EAF6)
        shdkUpper.contains("ANAK") -> Color(0xFFE8F5E9)
        else -> Color(0xFFFFF8E1)
    }
    val shdkTextColor = when {
        shdkUpper.contains("KEPALA") -> MaterialTheme.colorScheme.onPrimaryContainer
        shdkUpper.contains("ISTRI") -> Color(0xFFC2185B)
        shdkUpper.contains("SUAMI") -> Color(0xFF303F9F)
        shdkUpper.contains("ANAK") -> Color(0xFF2E7D32)
        else -> Color(0xFFF57F17)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentResident) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isCurrentResident) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Number + SHDK role badge + Sedang Dibuka
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$order",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = shdkBg
                    ) {
                        Text(
                            text = member.shdk.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = shdkTextColor
                        )
                    }
                }

                if (isCurrentResident) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "Sedang Dibuka",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nama & NIK
            Text(
                text = member.nama,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NIK: ${member.nik}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(member.nik))
                        Toast.makeText(context, "NIK disalin", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Salin NIK", modifier = Modifier.size(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))

            // Quick Info
            DetailGridRow(
                label1 = "Jenis Kelamin", value1 = member.getGenderDisplayLabel(),
                label2 = "Usia", value2 = "${member.getEffectiveAge()} Thn"
            )
            DetailGridRow(
                label1 = "Pendidikan", value1 = member.pendidikanTerakhir,
                label2 = "Pekerjaan", value2 = member.pekerjaan
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isCurrentResident) {
                    Button(
                        onClick = onViewDetail,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lihat Profil", fontSize = 11.sp)
                    }
                }
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Data", fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Modern Document Item Card
 */
@Composable
fun DocumentItemCard(
    document: PendudukDocument,
    onClick: () -> Unit,
    onRetryUpload: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when {
        document.jenisDokumen.contains("KTP", ignoreCase = true) -> Color(0xFF1976D2)
        document.jenisDokumen.contains("Keluarga", ignoreCase = true) -> Color(0xFF388E3C)
        document.jenisDokumen.contains("Akta", ignoreCase = true) -> Color(0xFFF57C00)
        document.jenisDokumen.contains("Nikah", ignoreCase = true) -> Color(0xFFC2185B)
        document.jenisDokumen.contains("BPJS", ignoreCase = true) || document.jenisDokumen.contains("KIS", ignoreCase = true) -> Color(0xFF0097A7)
        document.jenisDokumen.contains("Bansos", ignoreCase = true) -> Color(0xFF7B1FA2)
        else -> Color(0xFF5D4037)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = Color(0xFF212121)
            ) {
                val file = document.localFilePath?.let { File(it) }
                if (file != null && file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = document.jenisDokumen,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!document.driveFileUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = document.driveFileUrl,
                        contentDescription = document.jenisDokumen,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.LightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata & Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = document.jenisDokumen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = categoryColor
                        )
                    }

                    // Sync Status Indicator
                    if (document.isSynced) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Drive",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Drive",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Lokal",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Lokal",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = document.namaFile,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${document.getFormattedDate()} • ${document.getFormattedSize()}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Lihat",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Modern Hubungi Penduduk Dialog (Telepon, SMS, WhatsApp)
 */
@Composable
fun HubungiPendudukDialog(
    penduduk: Penduduk,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val cleanPhone = penduduk.noHandphone.trim()
    val digitsOnlyPhone = cleanPhone.filter { it.isDigit() || it == '+' }
    val waNumber = when {
        digitsOnlyPhone.startsWith("+62") -> digitsOnlyPhone.substring(1)
        digitsOnlyPhone.startsWith("0") -> "62" + digitsOnlyPhone.substring(1)
        digitsOnlyPhone.startsWith("62") -> digitsOnlyPhone
        digitsOnlyPhone.startsWith("+") -> digitsOnlyPhone.substring(1)
        else -> digitsOnlyPhone
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Hubungi Penduduk",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = penduduk.nama,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Phone number info tile with copy action
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
                        Column {
                            Text(
                                text = "Nomor Telepon",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = cleanPhone,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(cleanPhone))
                                Toast.makeText(context, "Nomor disalin ke clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin Nomor",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Option 1: Panggilan Telepon
                Surface(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${Uri.encode(digitsOnlyPhone)}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Tidak dapat membuka panggilan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Panggilan Telepon",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Buka dialer telepon ($cleanPhone)",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                // Option 2: Kirim Pesan SMS
                Surface(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:${Uri.encode(digitsOnlyPhone)}")
                                putExtra("sms_body", "Halo Bapak/Ibu ${penduduk.nama}, salam dari Kantor Desa.")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Tidak dapat membuka SMS: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE1F5FE),
                    border = BorderStroke(1.dp, Color(0xFF81D4FA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0288D1),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Message,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kirim SMS",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF01579B)
                            )
                            Text(
                                text = "Kirim SMS reguler",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFF0288D1)
                            )
                        }
                    }
                }

                // Option 3: WhatsApp
                Surface(
                    onClick = {
                        try {
                            val greeting = "Halo Bapak/Ibu ${penduduk.nama}, salam dari Kantor Desa."
                            val waUri = Uri.parse("https://wa.me/$waNumber?text=${Uri.encode(greeting)}")
                            val intent = Intent(Intent.ACTION_VIEW, waUri)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Tidak dapat membuka WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFF81C784)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF25D366),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chat WhatsApp",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Kirim chat WhatsApp (+${waNumber})",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

/**
 * Text formatter for sharing Kartu Keluarga
 */
fun buildFamilyCardSummaryText(
    currentResident: Penduduk,
    familyMembers: List<Penduduk>,
    userProfile: UserProfile
): String {
    val familyHead = familyMembers.find { it.shdk.contains("KEPALA", ignoreCase = true) } ?: familyMembers.firstOrNull()
    val headName = familyHead?.nama ?: currentResident.namaKepalaKeluarga.ifBlank { currentResident.nama }
    val alamat = familyHead?.alamat ?: currentResident.alamat
    val rt = familyHead?.rt ?: currentResident.rt
    val rw = familyHead?.rw ?: currentResident.rw
    val desa = userProfile.namaDesa.ifBlank { "Cimanggu" }
    val kec = userProfile.kecamatan.ifBlank { "Cimanggu" }
    val kab = userProfile.kabupaten.ifBlank { "Cilacap" }
    val prov = userProfile.provinsi.ifBlank { "Jawa Tengah" }

    val sb = StringBuilder()
    sb.appendLine("=== SALINAN KARTU KELUARGA ===")
    sb.appendLine("NO KK: ${currentResident.noKk}")
    sb.appendLine("Kepala Keluarga: $headName")
    sb.appendLine("Alamat: $alamat (RT $rt / RW $rw)")
    sb.appendLine("Desa/Kel: $desa, Kec: $kec")
    sb.appendLine("Kab/Kota: $kab, Prov: $prov")
    sb.appendLine("Total Anggota: ${familyMembers.size} Jiwa")
    sb.appendLine("---------------------------------------")
    sb.appendLine("SUSUNAN ANGGOTA KELUARGA:")
    familyMembers.forEachIndexed { i, m ->
        val statusText = if (m.isAktif()) "AKTIF" else m.statusMutasi.uppercase()
        sb.appendLine("${i + 1}. [${m.shdk}] ${m.nama}")
        sb.appendLine("   NIK: ${m.nik}")
        sb.appendLine("   JK: ${m.getGenderDisplayLabel()} | TTL: ${m.tempatLahir}, ${m.tanggalLahir} (${m.getEffectiveAge()} Thn)")
        sb.appendLine("   Agama: ${m.agama} | Pekerjaan: ${m.pekerjaan}")
        sb.appendLine("   Status: $statusText")
    }
    sb.appendLine("=======================================")
    sb.appendLine("Dicetak dari Sistem Informasi Desa (SIMDes Mobile)")
    return sb.toString()
}
