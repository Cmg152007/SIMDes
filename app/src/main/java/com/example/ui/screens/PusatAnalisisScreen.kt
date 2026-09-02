package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLog
import com.example.data.model.UserProfile
import com.example.ui.components.DistributionProgressBar
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PusatAnalisisScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Semua Ringkasan", "1. Metrik Utama", "2. Demografi & Sosial", "3. Wilayah Dusun/RT", "4. Mutasi & Log")

    val activeWilayah = profile.wilayahKerja.ifBlank { "Semua Wilayah" }
    val isSpecificWilayah = activeWilayah.isNotBlank() && !activeWilayah.equals("Semua Wilayah", ignoreCase = true)
    val cleanActiveDusun = activeWilayah.replace("Dusun", "", ignoreCase = true).trim()
    val rwLabel = if (isSpecificWilayah) UserProfile.getRwLabelForWilayah(cleanActiveDusun) else "Semua RW"
    val rtList = if (isSpecificWilayah) UserProfile.getRtListForWilayah(cleanActiveDusun) else emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pusat Analisis & Statistik",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isSpecificWilayah) "Wilayah Kerja: Dusun $cleanActiveDusun ($rwLabel)" else "Wilayah Kerja: Semua Wilayah (Seluruh Desa)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar showing Active Working Area info
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSpecificWilayah) "Wilayah Kerja: Dusun $cleanActiveDusun ($rwLabel)" else "Wilayah Kerja: Semua Wilayah (Seluruh Desa)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Summary Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Jiwa: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${stats.totalPenduduk}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "KK: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${stats.totalKk}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Valid: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${stats.kelengkapanDataPercent}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }

            // Tabs Navigation
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                // Wilayah Kerja Info Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSpecificWilayah) {
                                    "Data tersaring otomatis untuk Dusun $cleanActiveDusun ($rwLabel)"
                                } else {
                                    "Menampilkan seluruh data gabungan Semua Dusun"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // SECTION 1: RINGKASAN UTAMA KEPENDUDUKAN (KEY METRICS)
                if (selectedTab == 0 || selectedTab == 1) {
                    item {
                        AnalisisSectionLabel(
                            title = "1. Ringkasan Utama Kependudukan",
                            subtitle = "Data total warga, kepala keluarga, dan hak pilih di ${if (isSpecificWilayah) "Dusun $cleanActiveDusun" else "Semua Dusun"}",
                            icon = Icons.Default.Assessment
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // 4 KPI Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    AnalisisKpiBox(
                                        title = "Total Penduduk",
                                        value = "${stats.totalPenduduk}",
                                        unit = "Jiwa Terdata",
                                        icon = Icons.Default.People,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.resetFiltersAndNavigateToList()
                                        }
                                    )
                                    AnalisisKpiBox(
                                        title = "Kepala Keluarga",
                                        value = "${stats.totalKk}",
                                        unit = "KK Terdaftar",
                                        icon = Icons.Default.FamilyRestroom,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.resetFiltersAndNavigateToList()
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    AnalisisKpiBox(
                                        title = "Hak Pilih (DPT)",
                                        value = "${stats.totalHakPilih}",
                                        unit = "Usia ≥ 17 / Kawin",
                                        icon = Icons.Default.HowToReg,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.filterByCustomQuery("SUDAH MEMILIKI")
                                        }
                                    )
                                    AnalisisKpiBox(
                                        title = "Kelengkapan NIK",
                                        value = "${stats.kelengkapanDataPercent}%",
                                        unit = "Valid & Lengkap",
                                        icon = Icons.Default.CheckCircle,
                                        tint = Color(0xFF00897B),
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.resetFiltersAndNavigateToList()
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Rasio Gender Visual Bar
                                Text(
                                    text = "Rasio Gender & Perbandingan",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val lkPercent = if (stats.totalPenduduk > 0) ((stats.totalLakiLaki.toFloat() / stats.totalPenduduk) * 100).toInt() else 0
                                val prPercent = if (stats.totalPenduduk > 0) 100 - lkPercent else 0

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            viewModel.filterByGender("LAKI-LAKI")
                                        }
                                    ) {
                                        Icon(Icons.Default.Male, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Laki-laki: ${stats.totalLakiLaki} ($lkPercent%)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF0288D1))
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            viewModel.filterByGender("PEREMPUAN")
                                        }
                                    ) {
                                        Icon(Icons.Default.Female, contentDescription = null, tint = Color(0xFFC2185B), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Perempuan: ${stats.totalPerempuan} ($prPercent%)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFC2185B))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                ) {
                                    if (stats.totalPenduduk > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(stats.totalLakiLaki.toFloat().coerceAtLeast(0.01f))
                                                .background(Color(0xFF0288D1))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(stats.totalPerempuan.toFloat().coerceAtLeast(0.01f))
                                                .background(Color(0xFFE91E63))
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 2: SEGMENTASI DEMOGRAFI & SOSIAL
                if (selectedTab == 0 || selectedTab == 2) {
                    item {
                        AnalisisSectionLabel(
                            title = "2. Segmentasi Demografi & Sosial",
                            subtitle = "Piramida usia, bantuan sosial, agama, pendidikan, dan pekerjaan di ${if (isSpecificWilayah) "Dusun $cleanActiveDusun" else "Semua Dusun"}",
                            icon = Icons.Default.People
                        )
                    }

                    // Kelompok Usia
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Kelompok Usia (Piramida Penduduk)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                DistributionProgressBar(
                                    label = "Balita (0 - 5 Tahun)",
                                    count = stats.totalBalita,
                                    total = stats.totalPenduduk,
                                    color = Color(0xFF26A69A)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                DistributionProgressBar(
                                    label = "Usia Sekolah / Remaja (6 - 17 Tahun)",
                                    count = stats.totalAnak,
                                    total = stats.totalPenduduk,
                                    color = Color(0xFF42A5F5)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                DistributionProgressBar(
                                    label = "Usia Produktif (18 - 59 Tahun)",
                                    count = stats.totalProduktif,
                                    total = stats.totalPenduduk,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                DistributionProgressBar(
                                    label = "Lansia (≥ 60 Tahun)",
                                    count = stats.totalLansia,
                                    total = stats.totalPenduduk,
                                    color = Color(0xFFFFA726)
                                )
                            }
                        }
                    }

                    // Program Bantuan Sosial (Bansos)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CardMembership, contentDescription = null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Rekapitulasi Bantuan Sosial (Bansos)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                    TextButton(
                                        onClick = {
                                            viewModel.filterByBansos("PENERIMA BANSOS")
                                        }
                                    ) {
                                        Text("Lihat Warga", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AnalisisBansosBadge(
                                        title = "PKH",
                                        count = stats.totalPkh,
                                        color = Color(0xFF6A1B9A),
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.filterByBansos("PKH")
                                        }
                                    )
                                    AnalisisBansosBadge(
                                        title = "BPNT",
                                        count = stats.totalBpnt,
                                        color = Color(0xFFAD1457),
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.filterByBansos("BPNT")
                                        }
                                    )
                                    AnalisisBansosBadge(
                                        title = "BPJS PBI",
                                        count = stats.totalBpjsKis,
                                        color = Color(0xFF00695C),
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.filterByBansos("KIS / PBI")
                                        }
                                    )
                                    AnalisisBansosBadge(
                                        title = "KIP",
                                        count = stats.totalKip,
                                        color = Color(0xFFE65100),
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.filterByBansos("KIP")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Pekerjaan & Perkawinan
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Profesi & Pekerjaan Terbanyak",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                stats.pekerjaanDistribution.forEach { (job, count) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                viewModel.filterByCustomQuery(job)
                                            },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = job, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Text(text = "$count Jiwa", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Status Perkawinan",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val topStatus = stats.statusKawinDistribution.entries.take(3)
                                    topStatus.forEach { entry ->
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = entry.key, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(text = "${entry.value}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 3: PENYEBARAN WILAYAH (DUSUN / RW / RT)
                if (selectedTab == 0 || selectedTab == 3) {
                    item {
                        AnalisisSectionLabel(
                            title = "3. Penyebaran Wilayah (Sebaran Dusun / RW / RT)",
                            subtitle = if (isSpecificWilayah) "Distribusi rincian RT di Dusun $cleanActiveDusun ($rwLabel)" else "Distribusi jumlah warga per dusun",
                            icon = Icons.Default.LocationOn
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (isSpecificWilayah) {
                                    Column {
                                        Text(
                                            text = "Sebaran RT di Wilayah Kerja Dusun $cleanActiveDusun ($rwLabel)",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Klik RT untuk memfilter warga pada tabel penduduk",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (rtList.isNotEmpty()) {
                                        rtList.forEach { rtNum ->
                                            val rtKey = "RT $rtNum"
                                            val count = stats.rtDistribution[rtKey] ?: stats.rtDistribution["RT ${rtNum.toIntOrNull() ?: rtNum}"] ?: 0

                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        viewModel.filterByRt("RT $rtNum")
                                                    },
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(28.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = rtNum,
                                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column {
                                                            Text(
                                                                text = "RT $rtNum / $rwLabel",
                                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                            )
                                                            Text(
                                                                text = "Dusun $cleanActiveDusun",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "$count Jiwa",
                                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Filter >",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.secondary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Belum ada RT terdaftar pada wilayah kerja ini.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Sebaran Penduduk per Dusun",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    stats.dusunDistribution.forEach { (dusun, count) ->
                                        DistributionProgressBar(
                                            label = if (dusun.startsWith("Dusun", ignoreCase = true)) dusun else "Dusun $dusun",
                                            count = count,
                                            total = stats.totalPenduduk,
                                            color = when {
                                                dusun.contains("Cibubuay", ignoreCase = true) -> Color(0xFF1E88E5)
                                                dusun.contains("Sundawenang", ignoreCase = true) -> Color(0xFF43A047)
                                                dusun.contains("Cimanggu", ignoreCase = true) -> Color(0xFF8E24AA)
                                                dusun.contains("Mekarlaksana", ignoreCase = true) -> Color(0xFFFB8C00)
                                                else -> Color(0xFF00ACC1)
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Cakupan RW", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = if (isSpecificWilayah) rwLabel else "${stats.rwDistribution.size} RW Aktif",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Column {
                                        Text(text = "Cakupan RT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = if (isSpecificWilayah) "${rtList.size} RT Terdaftar" else "${stats.rtDistribution.size} RT Aktif",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Column {
                                        Text(text = "Warga Disabilitas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "${stats.totalDisabilitas} Jiwa",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 4: AKTIVITAS & MUTASI TERKINI
                if (selectedTab == 0 || selectedTab == 4) {
                    item {
                        AnalisisSectionLabel(
                            title = "4. Aktivitas & Pembaruan Terkini",
                            subtitle = "Log riwayat mutasi warga dan pembaruan data sistem",
                            icon = Icons.Default.History
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "5 Riwayat Aktivitas Terbaru",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    TextButton(
                                        onClick = {
                                            viewModel.navigateTo(Screen.ActivityLogs)
                                        }
                                    ) {
                                        Text("Buka Semua Log", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val recentLogs = allLogs.take(5)
                                if (recentLogs.isEmpty()) {
                                    Text(
                                        text = "Belum ada riwayat aktivitas terbaru.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    recentLogs.forEach { log ->
                                        AnalisisRecentLogItem(log = log)
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 5: PINTASAN AKSI TERPADU
                item {
                    AnalisisSectionLabel(
                        title = "5. Pintasan Aksi Terpadu",
                        subtitle = "Akses cepat penambahan data warga dan tabel kependudukan",
                        icon = Icons.Default.Add
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.navigateTo(Screen.PendudukForm(null))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Warga", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.resetFiltersAndNavigateToList()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tabel Penduduk", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalisisSectionLabel(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnalisisKpiBox(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = tint,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AnalisisBansosBadge(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "$count", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Penerima", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AnalisisRecentLogItem(log: ActivityLog) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                log.action.contains("TAMBAH") -> Color(0xFF2E7D32)
                                log.action.contains("UBAH") || log.action.contains("UPDATE") -> Color(0xFF0288D1)
                                log.action.contains("HAPUS") -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = log.detail.ifBlank { log.action },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Petugas: ${log.operator}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = log.timestamp.takeLast(8),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
