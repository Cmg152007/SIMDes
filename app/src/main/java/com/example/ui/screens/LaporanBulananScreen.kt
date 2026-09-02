package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Penduduk
import com.example.data.model.UserProfile
import com.example.data.util.DusunConfig
import com.example.data.util.Format1Row
import com.example.data.util.Format2AgamaKewarganegaraanRow
import com.example.data.util.Format2PendidikanPekerjaanRow
import com.example.data.util.Format3Row
import com.example.data.util.LaporanBulananGenerator
import com.example.data.util.LaporanPdfGenerator
import com.example.ui.viewmodel.MainViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanBulananScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val profile by viewModel.userProfile.collectAsState()
    val allPenduduk by viewModel.allPenduduk.collectAsState()

    // 1: Kelompok Umur, 2: Pendidikan & Pekerjaan, 3: Mutasi & Administrasi
    var selectedFormatType by remember { mutableIntStateOf(1) }

    // Display Mode: 0 = Kartu Ringkasan (Mudah Dipahami), 1 = Tabel Matriks Resmi (Format Cetak)
    var selectedDisplayMode by remember { mutableIntStateOf(0) }

    // Dynamic Month & Year
    val currentCalendar = Calendar.getInstance()
    var selectedMonthIndex by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH) + 1) } // 1..12
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }

    // Dynamic Titimangsa
    var customTitimangsa by remember {
        mutableStateOf(LaporanBulananGenerator.getAutoTitimangsa(selectedMonthIndex, selectedYear, profile.kabupaten))
    }
    var showEditTitimangsaDialog by remember { mutableStateOf(false) }

    // Helper to resolve active working area to dusun key or SEMUA
    fun resolveActiveWilayah(wk: String): String {
        val clean = wk.replace("Dusun", "", ignoreCase = true).trim()
        if (clean.isBlank() || clean.equals("Semua Wilayah", ignoreCase = true) || clean.equals("Semua", ignoreCase = true)) {
            return "SEMUA"
        }
        val match = LaporanBulananGenerator.DUSUN_CONFIG_LIST.firstOrNull {
            it.name.equals(clean, ignoreCase = true) || clean.contains(it.name, ignoreCase = true)
        }
        return match?.name ?: clean.uppercase()
    }

    // Wilayah Tugas Filter - automatically preset & synced to user's active working area
    val activeUserWilayah = remember(profile.wilayahKerja) {
        resolveActiveWilayah(profile.wilayahKerja)
    }
    var selectedWilayahFilter by remember(activeUserWilayah) { mutableStateOf(activeUserWilayah) }

    LaunchedEffect(profile.wilayahKerja) {
        selectedWilayahFilter = resolveActiveWilayah(profile.wilayahKerja)
    }

    fun updateDate(month: Int, year: Int) {
        selectedMonthIndex = month
        selectedYear = year
        customTitimangsa = LaporanBulananGenerator.getAutoTitimangsa(month, year, profile.kabupaten)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Laporan Bulanan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        val wilayahSubtitle = if (selectedWilayahFilter != "SEMUA") {
                            "Dusun ${selectedWilayahFilter.lowercase().replaceFirstChar { it.uppercase() }}"
                        } else {
                            "Semua Dusun"
                        }
                        Text(
                            text = "${profile.namaDesa} • $wilayahSubtitle • Bulan ${LaporanBulananGenerator.MONTH_NAMES.getOrNull(selectedMonthIndex - 1)} $selectedYear",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. Tiga Tombol Format Laporan
            item {
                Text(
                    text = "Format Laporan",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormatSelectorCard(
                        title = "Format 1",
                        subtitle = "Kelompok Umur",
                        badge = "Rentang Usia",
                        icon = Icons.Default.Groups,
                        isSelected = selectedFormatType == 1,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFormatType = 1 }
                    )
                    FormatSelectorCard(
                        title = "Format 2",
                        subtitle = "Pendidikan & Kerja",
                        badge = "Agama & WN",
                        icon = Icons.Default.School,
                        isSelected = selectedFormatType == 2,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFormatType = 2 }
                    )
                    FormatSelectorCard(
                        title = "Format 3",
                        subtitle = "Mutasi & Dokumen",
                        badge = "KTP, KK, KIA",
                        icon = Icons.Default.Description,
                        isSelected = selectedFormatType == 3,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedFormatType = 3 }
                    )
                }
            }

            // 2. Periode Bulan/Tahun & Titimangsa
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Periode Laporan",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (selectedMonthIndex == 1) updateDate(12, selectedYear - 1)
                                        else updateDate(selectedMonthIndex - 1, selectedYear)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Bulan Lalu",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${LaporanBulananGenerator.MONTH_NAMES.getOrNull(selectedMonthIndex - 1)} $selectedYear",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (selectedMonthIndex == 12) updateDate(1, selectedYear + 1)
                                        else updateDate(selectedMonthIndex + 1, selectedYear)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Bulan Depan",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Chips 12 Bulan
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(LaporanBulananGenerator.MONTH_NAMES.indices.toList()) { idx ->
                                val monthNum = idx + 1
                                val isSelected = selectedMonthIndex == monthNum
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { updateDate(monthNum, selectedYear) },
                                    label = {
                                        Text(
                                            text = LaporanBulananGenerator.MONTH_NAMES[idx].take(3),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Titimangsa Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Titimangsa:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = customTitimangsa,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            TextButton(
                                onClick = { showEditTitimangsaDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ubah", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // 3. Filter Wilayah Dusun
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Wilayah Laporan",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            if (activeUserWilayah != "SEMUA") {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "🎯 Tugas: Dusun $activeUserWilayah",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        if (activeUserWilayah != "SEMUA" && selectedWilayahFilter != activeUserWilayah) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { selectedWilayahFilter = activeUserWilayah },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Kembali ke Dusun $activeUserWilayah",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedWilayahFilter == "SEMUA",
                                    onClick = { selectedWilayahFilter = "SEMUA" },
                                    label = { Text("Semua Dusun") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                )
                            }
                            items(LaporanBulananGenerator.DUSUN_CONFIG_LIST) { cfg ->
                                val isSelected = selectedWilayahFilter == cfg.name
                                val isUserArea = activeUserWilayah.equals(cfg.name, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedWilayahFilter = cfg.name },
                                    label = {
                                        Text(if (isUserArea) "⭐ Dusun ${cfg.name}" else "Dusun ${cfg.name}")
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Mode Tampilan Switcher (Ringkasan vs Tabel Lengkap)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedDisplayMode == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedDisplayMode = 0 }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = if (selectedDisplayMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ringkasan",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (selectedDisplayMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedDisplayMode == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedDisplayMode = 1 }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableView,
                                    contentDescription = null,
                                    tint = if (selectedDisplayMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tabel Lengkap",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (selectedDisplayMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 5. Action Buttons (Bagikan PDF, Buka PDF, Cetak, Salin Teks)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Baris 1: Tombol Utama Sharing PDF & Pratinjau PDF
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                LaporanPdfGenerator.sharePdf(
                                    context = context,
                                    formatType = selectedFormatType,
                                    profile = profile,
                                    monthIndex1to12 = selectedMonthIndex,
                                    year = selectedYear,
                                    titimangsa = customTitimangsa,
                                    allPenduduk = allPenduduk,
                                    wilayahTugasFilter = selectedWilayahFilter
                                )
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E7E34)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bagikan PDF",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = {
                                LaporanPdfGenerator.openPdf(
                                    context = context,
                                    formatType = selectedFormatType,
                                    profile = profile,
                                    monthIndex1to12 = selectedMonthIndex,
                                    year = selectedYear,
                                    titimangsa = customTitimangsa,
                                    allPenduduk = allPenduduk,
                                    wilayahTugasFilter = selectedWilayahFilter
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Buka PDF",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Baris 2: Tombol Cetak & Salin Teks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val html = LaporanBulananGenerator.generateHtmlReport(
                                    formatType = selectedFormatType,
                                    profile = profile,
                                    monthIndex1to12 = selectedMonthIndex,
                                    year = selectedYear,
                                    titimangsa = customTitimangsa,
                                    allPenduduk = allPenduduk,
                                    wilayahTugasFilter = selectedWilayahFilter
                                )
                                LaporanBulananGenerator.printReport(
                                    context = context,
                                    htmlContent = html,
                                    jobName = "Laporan_Bulanan_Format${selectedFormatType}_${selectedMonthIndex}_$selectedYear"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cetak Spooler", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        OutlinedButton(
                            onClick = {
                                val text = LaporanBulananGenerator.generateShareText(
                                    formatType = selectedFormatType,
                                    profile = profile,
                                    monthIndex1to12 = selectedMonthIndex,
                                    year = selectedYear,
                                    titimangsa = customTitimangsa,
                                    allPenduduk = allPenduduk,
                                    wilayahTugasFilter = selectedWilayahFilter
                                )
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "Teks Ringkasan Laporan berhasil disalin", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salin Ringkasan", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // 6. KONTEN UTAMA SESUAI MODE TAMPILAN
            if (selectedDisplayMode == 0) {
                // MODE 0: KARTU RINGKASAN & GRAFIK (SANGAT MUDAH DIPAHAMI)
                item {
                    VisualSummarySection(
                        formatType = selectedFormatType,
                        allPenduduk = allPenduduk,
                        wilayahFilter = selectedWilayahFilter,
                        monthIndex = selectedMonthIndex,
                        year = selectedYear,
                        profile = profile
                    )
                }
            } else {
                // MODE 1: TABEL MATRIKS RESMI STANDAR EXCEL/PEMERINTAHAN
                item {
                    OfficialTableSection(
                        formatType = selectedFormatType,
                        allPenduduk = allPenduduk,
                        wilayahFilter = selectedWilayahFilter,
                        monthIndex = selectedMonthIndex,
                        year = selectedYear,
                        profile = profile,
                        customTitimangsa = customTitimangsa
                    )
                }
            }

            // 7. Signature Block (Tanda Tangan Kades & Kasi)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Lembar Pengesahan Laporan",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Mengetahui;", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "Kepala ${profile.namaDesa}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(40.dp))
                                Text(
                                    text = profile.namaKades,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = customTitimangsa, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "Kasi Pemerintahan",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(40.dp))
                                Text(
                                    text = profile.namaPetugas,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "NIP : ${profile.nipPetugas}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Dialog Edit Titimangsa & Pejabat
    if (showEditTitimangsaDialog) {
        var tempTitimangsa by remember { mutableStateOf(customTitimangsa) }
        var tempNamaKades by remember { mutableStateOf(profile.namaKades) }
        var tempNamaKasi by remember { mutableStateOf(profile.namaPetugas) }
        var tempNipKasi by remember { mutableStateOf(profile.nipPetugas) }

        AlertDialog(
            onDismissRequest = { showEditTitimangsaDialog = false },
            title = {
                Text(
                    text = "Sesuaikan Titimangsa & Pejabat",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tempTitimangsa,
                        onValueChange = { tempTitimangsa = it },
                        label = { Text("Titimangsa (Tempat & Tanggal)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempNamaKades,
                        onValueChange = { tempNamaKades = it },
                        label = { Text("Nama Kepala Desa") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempNamaKasi,
                        onValueChange = { tempNamaKasi = it },
                        label = { Text("Nama Kasi Pemerintahan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempNipKasi,
                        onValueChange = { tempNipKasi = it },
                        label = { Text("NIP Kasi Pemerintahan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        customTitimangsa = tempTitimangsa
                        viewModel.saveUserProfile(
                            profile.copy(
                                namaKades = tempNamaKades,
                                namaPetugas = tempNamaKasi,
                                nipPetugas = tempNipKasi
                            )
                        )
                        showEditTitimangsaDialog = false
                    }
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTitimangsaDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ==========================================
// COMPONENT: FORMAT SELECTOR CARD
// ==========================================
@Composable
fun FormatSelectorCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Surface(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.SemiBold),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

// ==========================================
// SECTION: VISUAL SUMMARY (SANGAT MUDAH DIPAHAMI)
// ==========================================
@Composable
fun VisualSummarySection(
    formatType: Int,
    allPenduduk: List<Penduduk>,
    wilayahFilter: String,
    monthIndex: Int,
    year: Int,
    profile: UserProfile
) {
    val activePenduduk = remember(allPenduduk) { allPenduduk.filter { it.isAktif() } }
    val filteredList = remember(activePenduduk, wilayahFilter) {
        if (wilayahFilter == "SEMUA" || wilayahFilter.isBlank()) {
            activePenduduk
        } else {
            activePenduduk.filter { LaporanBulananGenerator.getDusunForPenduduk(it).equals(wilayahFilter, ignoreCase = true) }
        }
    }

    val totalJiwa = filteredList.size
    val totalMale = filteredList.count { it.isMale() }
    val totalFemale = filteredList.count { it.isFemale() }
    val malePercent = if (totalJiwa > 0) (totalMale.toFloat() / totalJiwa * 100).toInt() else 0
    val femalePercent = if (totalJiwa > 0) (totalFemale.toFloat() / totalJiwa * 100).toInt() else 0

    val totalKk = filteredList.mapNotNull { it.noKk.takeIf { kk -> kk.isNotBlank() } }.distinct().size
        .let { if (it > 0) it else filteredList.count { p -> p.shdk.equals("KEPALA KELUARGA", ignoreCase = true) } }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Highlight Total Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (wilayahFilter == "SEMUA") "Total Penduduk Desa Cimanggu" else "Penduduk Dusun $wilayahFilter",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Data Real-Time Kependudukan Aktif",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "${LaporanBulananGenerator.formatNumber(totalJiwa)} Jiwa",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress bar gender ratio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1976D2))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Laki-laki: ${LaporanBulananGenerator.formatNumber(totalMale)} ($malePercent%)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE91E63))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Perempuan: ${LaporanBulananGenerator.formatNumber(totalFemale)} ($femalePercent%)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { if (totalJiwa > 0) totalMale.toFloat() / totalJiwa else 0.5f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF1976D2),
                    trackColor = Color(0xFFE91E63)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                // Mini stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MiniStatItem(label = "Kepala Keluarga", value = "${LaporanBulananGenerator.formatNumber(totalKk)} KK")
                    MiniStatItem(label = "Balita (0-5 Th)", value = "${filteredList.count { it.getEffectiveAge() in 0..5 }} Jiwa")
                    MiniStatItem(label = "Lansia (60+ Th)", value = "${filteredList.count { it.getEffectiveAge() >= 60 }} Jiwa")
                }
            }
        }

        // 2. Specific Visual Breakdown according to chosen Format (1, 2, or 3)
        when (formatType) {
            1 -> Format1VisualBreakdown(allPenduduk = allPenduduk, wilayahFilter = wilayahFilter)
            2 -> Format2VisualBreakdown(allPenduduk = allPenduduk, wilayahFilter = wilayahFilter)
            3 -> Format3VisualBreakdown(allPenduduk = allPenduduk, wilayahFilter = wilayahFilter, monthIndex = monthIndex, year = year)
        }

        // 3. Per-Dusun Summary Cards (Expandable and highly readable)
        Text(
            text = "Rincian Data Per Dusun",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        val dusunList = if (wilayahFilter != "SEMUA" && wilayahFilter.isNotBlank()) {
            LaporanBulananGenerator.DUSUN_CONFIG_LIST.filter { it.name.equals(wilayahFilter, ignoreCase = true) }
        } else {
            LaporanBulananGenerator.DUSUN_CONFIG_LIST
        }

        dusunList.forEach { cfg ->
            DusunSummaryCard(
                cfg = cfg,
                allPenduduk = allPenduduk,
                formatType = formatType
            )
        }
    }
}

@Composable
fun MiniStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
    }
}

// ==========================================
// DUSUN SUMMARY CARD (CLEAN & EXPANDABLE)
// ==========================================
@Composable
fun DusunSummaryCard(
    cfg: DusunConfig,
    allPenduduk: List<Penduduk>,
    formatType: Int
) {
    var isExpanded by remember { mutableStateOf(false) }
    val activeInDusun = remember(allPenduduk, cfg.name) {
        allPenduduk.filter { it.isAktif() && LaporanBulananGenerator.getDusunForPenduduk(it).equals(cfg.name, ignoreCase = true) }
    }

    val totalDusun = activeInDusun.size
    val maleDusun = activeInDusun.count { it.isMale() }
    val femaleDusun = activeInDusun.count { it.isFemale() }
    val totalKk = activeInDusun.mapNotNull { it.noKk.takeIf { kk -> kk.isNotBlank() } }.distinct().size
        .let { if (it > 0) it else activeInDusun.count { p -> p.shdk.equals("KEPALA KELUARGA", ignoreCase = true) } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = cfg.rwLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Dusun ${cfg.name}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${cfg.rtList.size} RT • ${cfg.luasKm} Km²",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${LaporanBulananGenerator.formatNumber(totalDusun)} Jiwa",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${LaporanBulananGenerator.formatNumber(totalKk)} KK",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gender chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF1976D2).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "👨 Laki-laki: $maleDusun",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = Color(0xFFE91E63).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "👩 Perempuan: $femaleDusun",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF880E4F),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Expandable Detail Block
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))

                    when (formatType) {
                        1 -> {
                            Text(
                                text = "Distribusi Kelompok Usia di Dusun ${cfg.name}:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            AgeGroupBarRow("0 - 5 Th (Balita)", activeInDusun.count { it.getEffectiveAge() in 0..5 }, totalDusun)
                            AgeGroupBarRow("6 - 12 Th (SD)", activeInDusun.count { it.getEffectiveAge() in 6..12 }, totalDusun)
                            AgeGroupBarRow("13 - 18 Th (Remaja)", activeInDusun.count { it.getEffectiveAge() in 13..18 }, totalDusun)
                            AgeGroupBarRow("19 - 59 Th (Produktif)", activeInDusun.count { it.getEffectiveAge() in 19..59 }, totalDusun)
                            AgeGroupBarRow("60+ Th (Lansia)", activeInDusun.count { it.getEffectiveAge() >= 60 }, totalDusun)
                        }
                        2 -> {
                            Text(
                                text = "Pendidikan & Pekerjaan di Dusun ${cfg.name}:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            AgeGroupBarRow("SD / SMP", activeInDusun.count { it.pendidikanTerakhir.uppercase().contains("SD") || it.pendidikanTerakhir.uppercase().contains("SMP") }, totalDusun)
                            AgeGroupBarRow("SMA / SMK", activeInDusun.count { it.pendidikanTerakhir.uppercase().contains("SMA") || it.pendidikanTerakhir.uppercase().contains("SMK") || it.pendidikanTerakhir.uppercase().contains("SLTA") }, totalDusun)
                            AgeGroupBarRow("Diploma / Sarjana (S1-S3)", activeInDusun.count { it.pendidikanTerakhir.uppercase().contains("DIPLOMA") || it.pendidikanTerakhir.uppercase().contains("STRATA") || it.pendidikanTerakhir.uppercase().contains("S1") }, totalDusun)
                            AgeGroupBarRow("Petani / Buruh", activeInDusun.count { it.pekerjaan.uppercase().contains("PETANI") || it.pekerjaan.uppercase().contains("BURUH") }, totalDusun)
                            AgeGroupBarRow("Wiraswasta / Karyawan", activeInDusun.count { it.pekerjaan.uppercase().contains("WIRASWASTA") || it.pekerjaan.uppercase().contains("KARYAWAN") }, totalDusun)
                        }
                        3 -> {
                            Text(
                                text = "Kepemilikan Dokumen di Dusun ${cfg.name}:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val wajibKtp = activeInDusun.filter { it.getEffectiveAge() >= 17 }
                            val ktpSudah = wajibKtp.count { it.kepemilikanEKtp.contains("SUDAH", ignoreCase = true) }
                            val aktaSudah = activeInDusun.count { it.kepemilikanAktaKelahiran.contains("ADA", ignoreCase = true) }
                            val anakList = activeInDusun.filter { it.getEffectiveAge() in 0..16 }
                            val kiaSudah = anakList.count { it.kartuKia.contains("ADA", ignoreCase = true) }

                            AgeGroupBarRow("E-KTP (Wajib KTP: ${wajibKtp.size})", ktpSudah, wajibKtp.size.coerceAtLeast(1))
                            AgeGroupBarRow("Akta Kelahiran", aktaSudah, totalDusun.coerceAtLeast(1))
                            AgeGroupBarRow("KIA Anak (Total: ${anakList.size})", kiaSudah, anakList.size.coerceAtLeast(1))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgeGroupBarRow(label: String, count: Int, total: Int) {
    val pct = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "$count Jiwa ($pct%)",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { if (total > 0) count.toFloat() / total else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// ==========================================
// VISUAL BREAKDOWN: FORMAT 1 (KELOMPOK UMUR)
// ==========================================
@Composable
fun Format1VisualBreakdown(allPenduduk: List<Penduduk>, wilayahFilter: String) {
    val active = remember(allPenduduk, wilayahFilter) {
        val list = allPenduduk.filter { it.isAktif() }
        if (wilayahFilter == "SEMUA" || wilayahFilter.isBlank()) list
        else list.filter { LaporanBulananGenerator.getDusunForPenduduk(it).equals(wilayahFilter, ignoreCase = true) }
    }
    val total = active.size

    val balita = active.count { it.getEffectiveAge() in 0..5 }
    val usiaSd = active.count { it.getEffectiveAge() in 6..12 }
    val usiaSmp = active.count { it.getEffectiveAge() in 13..15 }
    val usiaSma = active.count { it.getEffectiveAge() in 16..18 }
    val pemuda = active.count { it.getEffectiveAge() in 19..29 }
    val dewasa = active.count { it.getEffectiveAge() in 30..49 }
    val praLansia = active.count { it.getEffectiveAge() in 50..59 }
    val lansia = active.count { it.getEffectiveAge() >= 60 }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 Distribusi Kelompok Usia Utama",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            AgeGroupBarRow("👶 0 - 5 Th (Balita / Prasekolah)", balita, total)
            AgeGroupBarRow("🎒 6 - 12 Th (Usia SD)", usiaSd, total)
            AgeGroupBarRow("🏫 13 - 15 Th (Usia SMP)", usiaSmp, total)
            AgeGroupBarRow("🎓 16 - 18 Th (Usia SMA)", usiaSma, total)
            AgeGroupBarRow("💼 19 - 29 Th (Pemuda / Usia Kerja)", pemuda, total)
            AgeGroupBarRow("👨‍👩‍👦 30 - 49 Th (Dewasa Produktif)", dewasa, total)
            AgeGroupBarRow("🧓 50 - 59 Th (Pra-Lansia)", praLansia, total)
            AgeGroupBarRow("👴 60+ Th (Lanjut Usia / Lansia)", lansia, total)
        }
    }
}

// ==========================================
// VISUAL BREAKDOWN: FORMAT 2 (PENDIDIKAN & PEKERJAAN)
// ==========================================
@Composable
fun Format2VisualBreakdown(allPenduduk: List<Penduduk>, wilayahFilter: String) {
    val active = remember(allPenduduk, wilayahFilter) {
        val list = allPenduduk.filter { it.isAktif() }
        if (wilayahFilter == "SEMUA" || wilayahFilter.isBlank()) list
        else list.filter { LaporanBulananGenerator.getDusunForPenduduk(it).equals(wilayahFilter, ignoreCase = true) }
    }
    val total = active.size

    val (t1, _, t2Pair) = remember(active) {
        LaporanBulananGenerator.generateFormat2(active, "SEMUA")
    }
    val (_, totalAgama) = t2Pair

    val sd = active.count { it.pendidikanTerakhir.uppercase().contains("SD") && !it.pendidikanTerakhir.uppercase().contains("TIDAK") }
    val smp = active.count { it.pendidikanTerakhir.uppercase().contains("SMP") || it.pendidikanTerakhir.uppercase().contains("SLTP") }
    val sma = active.count { it.pendidikanTerakhir.uppercase().contains("SMA") || it.pendidikanTerakhir.uppercase().contains("SMK") || it.pendidikanTerakhir.uppercase().contains("SLTA") }
    val perguruanTinggi = active.count { it.pendidikanTerakhir.uppercase().contains("DIPLOMA") || it.pendidikanTerakhir.uppercase().contains("STRATA") || it.pendidikanTerakhir.uppercase().contains("S1") || it.pendidikanTerakhir.uppercase().contains("S2") }

    val petani = active.count { it.pekerjaan.uppercase().contains("PETANI") || it.pekerjaan.uppercase().contains("TANI") || it.pekerjaan.uppercase().contains("KEBUN") }
    val buruh = active.count { it.pekerjaan.uppercase().contains("BURUH") || it.pekerjaan.uppercase().contains("TUKANG") }
    val wiraswasta = active.count { it.pekerjaan.uppercase().contains("WIRASWASTA") || it.pekerjaan.uppercase().contains("PEDAGANG") }
    val karyawan = active.count { it.pekerjaan.uppercase().contains("KARYAWAN") || it.pekerjaan.uppercase().contains("SWASTA") }
    val pns = active.count { it.pekerjaan.uppercase().contains("PNS") || it.pekerjaan.uppercase().contains("TNI") || it.pekerjaan.uppercase().contains("POLRI") || it.pekerjaan.uppercase().contains("ASN") }
    val pelajar = active.count { it.pekerjaan.uppercase().contains("PELAJAR") || it.pekerjaan.uppercase().contains("MAHASISWA") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎓 Jenjang Pendidikan Terbanyak",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                AgeGroupBarRow("SD / Sederajat", sd, total)
                AgeGroupBarRow("SMP / Sederajat", smp, total)
                AgeGroupBarRow("SMA / SMK / Sederajat", sma, total)
                AgeGroupBarRow("Diploma / Sarjana (D1 - S3)", perguruanTinggi, total)
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💼 Mata Pencaharian Dominan",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                AgeGroupBarRow("🌾 Petani / Pekebun", petani, total)
                AgeGroupBarRow("🔨 Buruh Harian / Tukang", buruh, total)
                AgeGroupBarRow("🏪 Wiraswasta / Pedagang", wiraswasta, total)
                AgeGroupBarRow("🏢 Karyawan Swasta / Honorer", karyawan, total)
                AgeGroupBarRow("👔 PNS / TNI / POLRI", pns, total)
                AgeGroupBarRow("📚 Pelajar / Mahasiswa", pelajar, total)
            }
        }
    }
}

// ==========================================
// VISUAL BREAKDOWN: FORMAT 3 (MUTASI & ADMINISTRASI)
// ==========================================
@Composable
fun Format3VisualBreakdown(
    allPenduduk: List<Penduduk>,
    wilayahFilter: String,
    monthIndex: Int,
    year: Int
) {
    val active = remember(allPenduduk, wilayahFilter) {
        val list = allPenduduk.filter { it.isAktif() }
        if (wilayahFilter == "SEMUA" || wilayahFilter.isBlank()) list
        else list.filter { LaporanBulananGenerator.getDusunForPenduduk(it).equals(wilayahFilter, ignoreCase = true) }
    }
    val total = active.size

    val wajibKtp = active.filter { it.getEffectiveAge() >= 17 }
    val ktpSudah = wajibKtp.count { it.kepemilikanEKtp.contains("SUDAH", ignoreCase = true) }
    val aktaSudah = active.count { it.kepemilikanAktaKelahiran.contains("ADA", ignoreCase = true) }
    val anakList = active.filter { it.getEffectiveAge() in 0..16 }
    val kiaSudah = anakList.count { it.kartuKia.contains("ADA", ignoreCase = true) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📑 Rekapitulasi Kepemilikan Dokumen",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            AgeGroupBarRow("Wajib KTP (Sudah Milik KTP)", ktpSudah, wajibKtp.size.coerceAtLeast(1))
            AgeGroupBarRow("Akta Kelahiran Warga", aktaSudah, total.coerceAtLeast(1))
            AgeGroupBarRow("Kartu Identitas Anak (KIA)", kiaSudah, anakList.size.coerceAtLeast(1))
        }
    }
}

// ==========================================
// SECTION: OFFICIAL TABLE (FORMAT STANDAR EXCEL/CETAK)
// ==========================================
@Composable
fun OfficialTableSection(
    formatType: Int,
    allPenduduk: List<Penduduk>,
    wilayahFilter: String,
    monthIndex: Int,
    year: Int,
    profile: UserProfile,
    customTitimangsa: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LAPORAN DATA KEPENDUDUKAN",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${profile.namaDesa.uppercase()} KECAMATAN ${profile.kecamatan.uppercase()} KABUPATEN ${profile.kabupaten.uppercase()}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "BULAN : ${LaporanBulananGenerator.MONTH_NAMES.getOrNull(monthIndex - 1)} $year",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.TableView, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Geser tabel ke samping untuk melihat kolom lengkap",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            when (formatType) {
                1 -> Format1TablePreview(allPenduduk = allPenduduk, wilayahFilter = wilayahFilter)
                2 -> Format2TablePreview(allPenduduk = allPenduduk, wilayahFilter = wilayahFilter)
                3 -> Format3TablePreview(allPenduduk = allPenduduk, month = monthIndex, year = year, wilayahFilter = wilayahFilter)
            }
        }
    }
}

// ==========================================
// COMPONENT: TABLE PREVIEW FORMAT 1 (KELOMPOK UMUR)
// ==========================================
@Composable
fun Format1TablePreview(
    allPenduduk: List<Penduduk>,
    wilayahFilter: String
) {
    val (rows, total) = remember(allPenduduk, wilayahFilter) {
        LaporanBulananGenerator.generateFormat1(allPenduduk, wilayahFilter)
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .border(1.dp, Color.Gray)
    ) {
        Column {
            // Header Row 1
            Row(modifier = Modifier.background(Color(0xFFE8EEF5))) {
                TableCell(text = "NO", width = 36.dp, isHeader = true)
                TableCell(text = if (wilayahFilter != "SEMUA" && wilayahFilter.isNotBlank()) "RT / RW" else "DUSUN", width = 115.dp, isHeader = true)
                val ageHeaders = listOf(
                    "0-5 TH", "6-12 TH", "13-15 TH", "16-18 TH", "19-24 TH", "25-29 TH",
                    "30-34 TH", "35-39 TH", "40-44 TH", "45-49 TH", "50-54 TH", "55-59 TH",
                    "60-64 TH", "65-69 TH", "70-74 TH", "75+ TH", "JUMLAH"
                )
                ageHeaders.forEach { h ->
                    TableCell(text = h, width = 72.dp, isHeader = true)
                }
                TableCell(text = "GRAND TOTAL", width = 85.dp, isHeader = true)
            }

            // Header Row 2 (LK / PR)
            Row(modifier = Modifier.background(Color(0xFFD6E2EF))) {
                TableCell(text = "", width = 36.dp, isHeader = true)
                TableCell(text = "", width = 115.dp, isHeader = true)
                repeat(17) {
                    TableCell(text = "LK", width = 36.dp, isHeader = true, isSubHeader = true)
                    TableCell(text = "PR", width = 36.dp, isHeader = true, isSubHeader = true)
                }
                TableCell(text = "JIWA", width = 85.dp, isHeader = true, isSubHeader = true)
            }

            // Data Rows with Zebra Striping
            rows.forEachIndexed { idx, r ->
                val bg = if (idx % 2 == 0) Color.White else Color(0xFFF9FAFB)
                Row(modifier = Modifier.background(bg)) {
                    TableCell(text = "${r.no}", width = 36.dp)
                    TableCell(text = r.namaWilayah, width = 115.dp, alignLeft = true, isBold = true)
                    val agePairs = listOf(
                        r.age0to5, r.age6to12, r.age13to15, r.age16to18, r.age19to24, r.age25to29,
                        r.age30to34, r.age35to39, r.age40to44, r.age45to49, r.age50to54, r.age55to59,
                        r.age60to64, r.age65to69, r.age70to74, r.age75Above
                    )
                    agePairs.forEach { p ->
                        TableCell(text = if (p.male > 0) "${p.male}" else "-", width = 36.dp)
                        TableCell(text = if (p.female > 0) "${p.female}" else "-", width = 36.dp)
                    }
                    TableCell(text = LaporanBulananGenerator.formatNumber(r.totalMale), width = 36.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(r.totalFemale), width = 36.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(r.grandTotal), width = 85.dp, isBold = true)
                }
            }

            // Total Row
            Row(modifier = Modifier.background(Color(0xFFE2E8F0))) {
                TableCell(text = "", width = 36.dp, isBold = true)
                TableCell(text = total.namaWilayah, width = 115.dp, isBold = true, alignLeft = true)
                val totalAgePairs = listOf(
                    total.age0to5, total.age6to12, total.age13to15, total.age16to18, total.age19to24, total.age25to29,
                    total.age30to34, total.age35to39, total.age40to44, total.age45to49, total.age50to54, total.age55to59,
                    total.age60to64, total.age65to69, total.age70to74, total.age75Above
                )
                totalAgePairs.forEach { p ->
                    TableCell(text = "${p.male}", width = 36.dp, isBold = true)
                    TableCell(text = "${p.female}", width = 36.dp, isBold = true)
                }
                TableCell(text = LaporanBulananGenerator.formatNumber(total.totalMale), width = 36.dp, isBold = true)
                TableCell(text = LaporanBulananGenerator.formatNumber(total.totalFemale), width = 36.dp, isBold = true)
                TableCell(text = LaporanBulananGenerator.formatNumber(total.grandTotal), width = 85.dp, isBold = true)
            }
        }
    }
}

// ==========================================
// COMPONENT: TABLE PREVIEW FORMAT 2 (PENDIDIKAN & PEKERJAAN)
// ==========================================
@Composable
fun Format2TablePreview(
    allPenduduk: List<Penduduk>,
    wilayahFilter: String
) {
    val (t1, total1, t2Pair) = remember(allPenduduk, wilayahFilter) {
        LaporanBulananGenerator.generateFormat2(allPenduduk, wilayahFilter)
    }
    val (rows2, total2) = t2Pair

    val scrollState1 = rememberScrollState()
    val scrollState2 = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Tabel 1: Jenjang Pendidikan & Mata Pencaharian",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState1)
                .border(1.dp, Color.Gray)
        ) {
            Column {
                Row(modifier = Modifier.background(Color(0xFFE8EEF5))) {
                    TableCell(text = "NO", width = 36.dp, isHeader = true)
                    TableCell(text = if (wilayahFilter != "SEMUA" && wilayahFilter.isNotBlank()) "RT / RW" else "DUSUN", width = 115.dp, isHeader = true)
                    // Pendidikan
                    TableCell(text = "BELUM\nSEKOLAH", width = 55.dp, isHeader = true)
                    TableCell(text = "TIDAK\nTAMAT SD", width = 55.dp, isHeader = true)
                    TableCell(text = "TAMAT SD/\nSEDERAJAT", width = 60.dp, isHeader = true)
                    TableCell(text = "TAMAT SMP/\nSEDERAJAT", width = 62.dp, isHeader = true)
                    TableCell(text = "TAMAT SMA/\nSEDERAJAT", width = 62.dp, isHeader = true)
                    TableCell(text = "D1/D2", width = 45.dp, isHeader = true)
                    TableCell(text = "D3/SM", width = 45.dp, isHeader = true)
                    TableCell(text = "D4/S1", width = 48.dp, isHeader = true)
                    TableCell(text = "S2", width = 42.dp, isHeader = true)
                    TableCell(text = "S3", width = 42.dp, isHeader = true)
                    TableCell(text = "JUMLAH\nPENDIDIKAN", width = 72.dp, isHeader = true, isBold = true)
                    // Pekerjaan
                    TableCell(text = "PNS/TNI/\nPOLRI", width = 55.dp, isHeader = true)
                    TableCell(text = "KARYAWAN", width = 60.dp, isHeader = true)
                    TableCell(text = "BURUH", width = 48.dp, isHeader = true)
                    TableCell(text = "PETANI", width = 48.dp, isHeader = true)
                    TableCell(text = "PETERNAK", width = 52.dp, isHeader = true)
                    TableCell(text = "NELAYAN", width = 50.dp, isHeader = true)
                    TableCell(text = "WIRASWASTA", width = 65.dp, isHeader = true)
                    TableCell(text = "PELAJAR/\nMHS", width = 55.dp, isHeader = true)
                    TableCell(text = "BELUM\nBEKERJA", width = 55.dp, isHeader = true)
                    TableCell(text = "LAINNYA", width = 50.dp, isHeader = true)
                    TableCell(text = "JUMLAH\nPEKERJAAN", width = 72.dp, isHeader = true, isBold = true)
                }

                t1.forEachIndexed { idx, r ->
                    val bg = if (idx % 2 == 0) Color.White else Color(0xFFF9FAFB)
                    Row(modifier = Modifier.background(bg)) {
                        TableCell(text = "${r.no}", width = 36.dp)
                        TableCell(text = r.namaWilayah, width = 115.dp, alignLeft = true, isBold = true)
                        TableCell(text = if (r.belumSekolah > 0) "${r.belumSekolah}" else "-", width = 55.dp)
                        TableCell(text = if (r.tidakTamatSd > 0) "${r.tidakTamatSd}" else "-", width = 55.dp)
                        TableCell(text = if (r.tamatSd > 0) "${r.tamatSd}" else "-", width = 60.dp)
                        TableCell(text = if (r.tamatSmp > 0) "${r.tamatSmp}" else "-", width = 62.dp)
                        TableCell(text = if (r.tamatSma > 0) "${r.tamatSma}" else "-", width = 62.dp)
                        TableCell(text = if (r.diploma12 > 0) "${r.diploma12}" else "-", width = 45.dp)
                        TableCell(text = if (r.diploma3 > 0) "${r.diploma3}" else "-", width = 45.dp)
                        TableCell(text = if (r.diploma4S1 > 0) "${r.diploma4S1}" else "-", width = 48.dp)
                        TableCell(text = if (r.strata2 > 0) "${r.strata2}" else "-", width = 42.dp)
                        TableCell(text = if (r.strata3 > 0) "${r.strata3}" else "-", width = 42.dp)
                        TableCell(text = LaporanBulananGenerator.formatNumber(r.totalPendidikan), width = 72.dp, isBold = true)

                        TableCell(text = if (r.pnsTniPolri > 0) "${r.pnsTniPolri}" else "-", width = 55.dp)
                        TableCell(text = if (r.karyawan > 0) "${r.karyawan}" else "-", width = 60.dp)
                        TableCell(text = if (r.buruh > 0) "${r.buruh}" else "-", width = 48.dp)
                        TableCell(text = if (r.petani > 0) "${r.petani}" else "-", width = 48.dp)
                        TableCell(text = if (r.peternak > 0) "${r.peternak}" else "-", width = 52.dp)
                        TableCell(text = if (r.nelayan > 0) "${r.nelayan}" else "-", width = 50.dp)
                        TableCell(text = if (r.wiraswasta > 0) "${r.wiraswasta}" else "-", width = 65.dp)
                        TableCell(text = if (r.pelajar > 0) "${r.pelajar}" else "-", width = 55.dp)
                        TableCell(text = if (r.belumBekerja > 0) "${r.belumBekerja}" else "-", width = 55.dp)
                        TableCell(text = if (r.lainnya > 0) "${r.lainnya}" else "-", width = 50.dp)
                        TableCell(text = LaporanBulananGenerator.formatNumber(r.totalPekerjaan), width = 72.dp, isBold = true)
                    }
                }

                // Total Row
                Row(modifier = Modifier.background(Color(0xFFE2E8F0))) {
                    TableCell(text = "", width = 36.dp, isBold = true)
                    TableCell(text = total1.namaWilayah, width = 115.dp, isBold = true, alignLeft = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.belumSekolah), width = 55.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.tidakTamatSd), width = 55.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.tamatSd), width = 60.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.tamatSmp), width = 62.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.tamatSma), width = 62.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.diploma12), width = 45.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.diploma3), width = 45.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.diploma4S1), width = 48.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.strata2), width = 42.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.strata3), width = 42.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.totalPendidikan), width = 72.dp, isBold = true)

                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.pnsTniPolri), width = 55.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.karyawan), width = 60.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.buruh), width = 48.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.petani), width = 48.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.peternak), width = 52.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.nelayan), width = 50.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.wiraswasta), width = 65.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.pelajar), width = 55.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.belumBekerja), width = 55.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.lainnya), width = 50.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total1.totalPekerjaan), width = 72.dp, isBold = true)
                }
            }
        }

        Text(
            text = "Tabel 2: Agama & Kewarganegaraan",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState2)
                .border(1.dp, Color.Gray)
        ) {
            Column {
                Row(modifier = Modifier.background(Color(0xFFE8EEF5))) {
                    TableCell(text = "NO", width = 36.dp, isHeader = true)
                    TableCell(text = if (wilayahFilter != "SEMUA" && wilayahFilter.isNotBlank()) "RT / RW" else "DUSUN", width = 115.dp, isHeader = true)
                    TableCell(text = "ISLAM", width = 55.dp, isHeader = true)
                    TableCell(text = "KRISTEN", width = 55.dp, isHeader = true)
                    TableCell(text = "HINDU", width = 48.dp, isHeader = true)
                    TableCell(text = "BUDHA", width = 48.dp, isHeader = true)
                    TableCell(text = "KHONGHUCU", width = 65.dp, isHeader = true)
                    TableCell(text = "KEPERCAYAAN", width = 75.dp, isHeader = true)
                    TableCell(text = "JUMLAH\nAGAMA", width = 72.dp, isHeader = true, isBold = true)
                    TableCell(text = "WNA", width = 45.dp, isHeader = true)
                    TableCell(text = "WNI", width = 55.dp, isHeader = true)
                    TableCell(text = "JUMLAH\nWN", width = 72.dp, isHeader = true, isBold = true)
                }

                rows2.forEachIndexed { idx, r ->
                    val bg = if (idx % 2 == 0) Color.White else Color(0xFFF9FAFB)
                    Row(modifier = Modifier.background(bg)) {
                        TableCell(text = "${r.no}", width = 36.dp)
                        TableCell(text = r.namaWilayah, width = 115.dp, alignLeft = true, isBold = true)
                        TableCell(text = if (r.islam > 0) LaporanBulananGenerator.formatNumber(r.islam) else "-", width = 55.dp)
                        TableCell(text = if (r.kristen > 0) "${r.kristen}" else "-", width = 55.dp)
                        TableCell(text = if (r.hindu > 0) "${r.hindu}" else "-", width = 48.dp)
                        TableCell(text = if (r.budha > 0) "${r.budha}" else "-", width = 48.dp)
                        TableCell(text = if (r.khonghucu > 0) "${r.khonghucu}" else "-", width = 65.dp)
                        TableCell(text = if (r.kepercayaan > 0) "${r.kepercayaan}" else "-", width = 75.dp)
                        TableCell(text = LaporanBulananGenerator.formatNumber(r.totalAgama), width = 72.dp, isBold = true)

                        TableCell(text = if (r.wna > 0) "${r.wna}" else "-", width = 45.dp)
                        TableCell(text = LaporanBulananGenerator.formatNumber(r.wni), width = 55.dp)
                        TableCell(text = LaporanBulananGenerator.formatNumber(r.totalKewarganegaraan), width = 72.dp, isBold = true)
                    }
                }

                Row(modifier = Modifier.background(Color(0xFFE2E8F0))) {
                    TableCell(text = "", width = 36.dp, isBold = true)
                    TableCell(text = total2.namaWilayah, width = 115.dp, isBold = true, alignLeft = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total2.islam), width = 55.dp, isBold = true)
                    TableCell(text = if (total2.kristen > 0) "${total2.kristen}" else "-", width = 55.dp, isBold = true)
                    TableCell(text = if (total2.hindu > 0) "${total2.hindu}" else "-", width = 48.dp, isBold = true)
                    TableCell(text = if (total2.budha > 0) "${total2.budha}" else "-", width = 48.dp, isBold = true)
                    TableCell(text = if (total2.khonghucu > 0) "${total2.khonghucu}" else "-", width = 65.dp, isBold = true)
                    TableCell(text = if (total2.kepercayaan > 0) "${total2.kepercayaan}" else "-", width = 75.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total2.totalAgama), width = 72.dp, isBold = true)

                    TableCell(text = if (total2.wna > 0) "${total2.wna}" else "-", width = 45.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total2.wni), width = 55.dp, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatNumber(total2.totalKewarganegaraan), width = 72.dp, isBold = true)
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: TABLE PREVIEW FORMAT 3 (MUTASI & REKAPITULASI)
// ==========================================
@Composable
fun Format3TablePreview(
    allPenduduk: List<Penduduk>,
    month: Int,
    year: Int,
    wilayahFilter: String
) {
    val (rows, total) = remember(allPenduduk, month, year, wilayahFilter) {
        LaporanBulananGenerator.generateFormat3(allPenduduk, month, year, wilayahFilter)
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .border(1.dp, Color.Gray)
    ) {
        Column {
            // Header Row 1
            Row(modifier = Modifier.background(Color(0xFFE8EEF5))) {
                TableCell(text = "NO", width = 36.dp, isHeader = true)
                TableCell(text = if (wilayahFilter != "SEMUA" && wilayahFilter.isNotBlank()) "RT / RW" else "DUSUN", width = 115.dp, isHeader = true)
                TableCell(text = "LUAS\n(Km²)", width = 55.dp, isHeader = true)
                TableCell(text = "RT", width = 36.dp, isHeader = true)
                TableCell(text = "RW", width = 36.dp, isHeader = true)
                TableCell(text = "PENDUDUK\nBLN LALU", width = 90.dp, isHeader = true)
                TableCell(text = "LAHIR\nBLN INI", width = 70.dp, isHeader = true)
                TableCell(text = "MATI\nBLN INI", width = 70.dp, isHeader = true)
                TableCell(text = "DATANG\nBLN INI", width = 70.dp, isHeader = true)
                TableCell(text = "PINDAH\nBLN INI", width = 70.dp, isHeader = true)
                TableCell(text = "PENDUDUK\nBLN INI", width = 90.dp, isHeader = true)
                TableCell(text = "WAJIB KTP\n(S/B/JML)", width = 90.dp, isHeader = true)
                TableCell(text = "KK\n(JML)", width = 55.dp, isHeader = true)
                TableCell(text = "AKTE\n(ADA/JML)", width = 75.dp, isHeader = true)
                TableCell(text = "KIA\n(ADA/JML)", width = 75.dp, isHeader = true)
            }

            // Data Rows
            rows.forEachIndexed { idx, r ->
                val bg = if (idx % 2 == 0) Color.White else Color(0xFFF9FAFB)
                Row(modifier = Modifier.background(bg)) {
                    TableCell(text = "${r.no}", width = 36.dp)
                    TableCell(text = r.namaWilayah, width = 115.dp, alignLeft = true, isBold = true)
                    TableCell(text = LaporanBulananGenerator.formatDecimal(r.luasWilayahKm), width = 55.dp)
                    TableCell(text = "${r.jumlahRt}", width = 36.dp)
                    TableCell(text = "${r.jumlahRw}", width = 36.dp)
                    TableCell(text = "${r.blnLalu.total}", width = 90.dp)
                    TableCell(text = "${r.lahirBlnIni.total}", width = 70.dp)
                    TableCell(text = "${r.matiBlnIni.total}", width = 70.dp)
                    TableCell(text = "${r.datangBlnIni.total}", width = 70.dp)
                    TableCell(text = "${r.pindahBlnIni.total}", width = 70.dp)
                    TableCell(text = "${r.blnIni.total}", width = 90.dp, isBold = true)
                    TableCell(text = "${r.wajibKtpSudah}/${r.totalWajibKtp}", width = 90.dp)
                    TableCell(text = "${r.totalKk}", width = 55.dp)
                    TableCell(text = "${r.akteSudah}/${r.totalAkte}", width = 75.dp)
                    TableCell(text = "${r.kiaSudah}/${r.totalKia}", width = 75.dp)
                }
            }

            // Total Row
            Row(modifier = Modifier.background(Color(0xFFE2E8F0))) {
                TableCell(text = "", width = 36.dp, isBold = true)
                TableCell(text = total.namaWilayah, width = 115.dp, isBold = true, alignLeft = true)
                TableCell(text = LaporanBulananGenerator.formatDecimal(total.luasWilayahKm), width = 55.dp, isBold = true)
                TableCell(text = "${total.jumlahRt}", width = 36.dp, isBold = true)
                TableCell(text = "${total.jumlahRw}", width = 36.dp, isBold = true)
                TableCell(text = LaporanBulananGenerator.formatNumber(total.blnLalu.total), width = 90.dp, isBold = true)
                TableCell(text = "${total.lahirBlnIni.total}", width = 70.dp, isBold = true)
                TableCell(text = "${total.matiBlnIni.total}", width = 70.dp, isBold = true)
                TableCell(text = "${total.datangBlnIni.total}", width = 70.dp, isBold = true)
                TableCell(text = "${total.pindahBlnIni.total}", width = 70.dp, isBold = true)
                TableCell(text = LaporanBulananGenerator.formatNumber(total.blnIni.total), width = 90.dp, isBold = true)
                TableCell(text = "${total.wajibKtpSudah}/${total.totalWajibKtp}", width = 90.dp, isBold = true)
                TableCell(text = LaporanBulananGenerator.formatNumber(total.totalKk), width = 55.dp, isBold = true)
                TableCell(text = "${total.akteSudah}/${total.totalAkte}", width = 75.dp, isBold = true)
                TableCell(text = "${total.kiaSudah}/${total.totalKia}", width = 75.dp, isBold = true)
            }
        }
    }
}

// ==========================================
// COMPONENT: TABLE CELL
// ==========================================
@Composable
fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isSubHeader: Boolean = false,
    isBold: Boolean = false,
    alignLeft: Boolean = false
) {
    val isZero = !isHeader && text.trim() == "0"
    val displayText = if (isZero) "-" else text
    val textColor = when {
        isHeader -> MaterialTheme.colorScheme.onSurface
        isZero -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        isBold -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(if (isHeader && !isSubHeader && text.contains("\n")) 40.dp else if (isHeader) 28.dp else 26.dp)
            .border(0.5.dp, Color(0xFFCFD8DC))
            .padding(horizontal = 2.dp, vertical = 2.dp),
        contentAlignment = if (alignLeft) Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 8.5.sp,
                fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            ),
            textAlign = if (alignLeft) TextAlign.Start else TextAlign.Center,
            maxLines = 2,
            lineHeight = 10.sp
        )
    }
}
