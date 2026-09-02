package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Penduduk
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PendudukListScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pendudukList by viewModel.filteredPenduduk.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterRw by viewModel.filterRw.collectAsState()
    val filterRt by viewModel.filterRt.collectAsState()
    val filterGender by viewModel.filterGender.collectAsState()
    val filterBansos by viewModel.filterBansos.collectAsState()
    val filterDisabilitas by viewModel.filterDisabilitas.collectAsState()
    val filterMutasi by viewModel.filterMutasi.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var residentToDelete by remember { mutableStateOf<Penduduk?>(null) }
    var residentToMutate by remember { mutableStateOf<Penduduk?>(null) }
    var isTableView by remember { mutableStateOf(false) }

    var activeFilterCount = 0
    if (filterRt != "SEMUA") activeFilterCount++
    if (filterGender != "SEMUA") activeFilterCount++
    if (filterBansos != "SEMUA") activeFilterCount++
    if (filterDisabilitas != "SEMUA") activeFilterCount++
    if (filterMutasi != "SEMUA") activeFilterCount++
    if (sortBy != "NO") activeFilterCount++

    val hasActiveFilter = activeFilterCount > 0 || searchQuery.isNotBlank()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.PendudukForm(null)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah Warga", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Active Wilayah Kerja Scoping Banner (If set)
            if (profile.wilayahKerja.isNotBlank() && !profile.wilayahKerja.equals("Semua Wilayah", ignoreCase = true)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val cleanDusun = profile.wilayahKerja.replace("Dusun", "", ignoreCase = true).trim()
                        Text(
                            text = if (cleanDusun.isNotBlank() && !cleanDusun.equals("Semua Wilayah", ignoreCase = true)) {
                                "Dusun $cleanDusun"
                            } else {
                                "Semua Dusun"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Top Search Bar & Filter & View Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Cari NIK, Nama, KK, Alamat...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Cari", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Modern Filter & Sort Button with Badge
                Surface(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .height(52.dp)
                        .width(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (activeFilterCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activeFilterCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text("$activeFilterCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter & Urutkan",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter & Urutkan",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Removable Active Filter Pills Row (Only shown when filter is active)
            if (hasActiveFilter) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset All Pill
                    Surface(
                        onClick = {
                            viewModel.filterRt.value = "SEMUA"
                            viewModel.filterRw.value = "SEMUA"
                            viewModel.filterGender.value = "SEMUA"
                            viewModel.filterBansos.value = "SEMUA"
                            viewModel.filterDisabilitas.value = "SEMUA"
                            viewModel.filterMutasi.value = "SEMUA"
                            viewModel.sortBy.value = "NO"
                            viewModel.searchQuery.value = ""
                        },
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }

                    if (filterRt != "SEMUA") {
                        ActiveFilterPill(
                            label = if (filterRt.startsWith("RT")) filterRt else "RT $filterRt",
                            onRemove = { viewModel.filterRt.value = "SEMUA" }
                        )
                    }

                    if (filterGender != "SEMUA") {
                        ActiveFilterPill(
                            label = if (filterGender == "LAKI-LAKI") "Laki-laki" else "Perempuan",
                            onRemove = { viewModel.filterGender.value = "SEMUA" }
                        )
                    }

                    if (filterMutasi != "SEMUA") {
                        ActiveFilterPill(
                            label = "Status: $filterMutasi",
                            onRemove = { viewModel.filterMutasi.value = "SEMUA" }
                        )
                    }

                    if (filterBansos != "SEMUA") {
                        ActiveFilterPill(
                            label = "Bansos: $filterBansos",
                            onRemove = { viewModel.filterBansos.value = "SEMUA" }
                        )
                    }

                    if (filterDisabilitas != "SEMUA") {
                        ActiveFilterPill(
                            label = "Disabilitas",
                            onRemove = { viewModel.filterDisabilitas.value = "SEMUA" }
                        )
                    }

                    if (sortBy != "NO") {
                        val sortLabel = when (sortBy) {
                            "RT" -> "Urut: RT"
                            "NAMA" -> "Urut: Nama A-Z"
                            "NIK" -> "Urut: NIK"
                            "UMUR" -> "Urut: Usia"
                            else -> "Urut: $sortBy"
                        }
                        ActiveFilterPill(
                            label = sortLabel,
                            onRemove = { viewModel.sortBy.value = "NO" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Counter & View Switcher Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${pendudukList.size} Penduduk Terdaftar",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = if (isTableView) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable { isTableView = !isTableView }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isTableView) Icons.Default.ViewAgenda else Icons.Default.TableChart,
                            contentDescription = "Ganti Tampilan",
                            tint = if (isTableView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTableView) "Mode Kartu" else "Mode Tabel (46 Kolom)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isTableView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Resident List Content
            if (pendudukList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Tidak Ada Data Warga",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (hasActiveFilter) "Tidak ada data yang sesuai dengan kata kunci atau filter pencarian."
                            else "Belum ada data penduduk tersimpan di database lokal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (hasActiveFilter) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.filterRt.value = "SEMUA"
                                    viewModel.filterRw.value = "SEMUA"
                                    viewModel.filterGender.value = "SEMUA"
                                    viewModel.filterBansos.value = "SEMUA"
                                    viewModel.filterDisabilitas.value = "SEMUA"
                                    viewModel.filterMutasi.value = "SEMUA"
                                    viewModel.sortBy.value = "NO"
                                    viewModel.searchQuery.value = ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text("Reset Semua Filter", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            } else if (isTableView) {
                // Full 46 Columns Table View
                PendudukTableView(
                    pendudukList = pendudukList,
                    onViewDetail = { viewModel.navigateTo(Screen.PendudukDetail(it.nik)) },
                    onEdit = { viewModel.navigateTo(Screen.PendudukForm(it.nik)) },
                    onDelete = { residentToDelete = it },
                    onMutasi = { residentToMutate = it }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(pendudukList, key = { it.nik }) { resident ->
                        PendudukItemCard(
                            resident = resident,
                            onViewDetail = { viewModel.navigateTo(Screen.PendudukDetail(resident.nik)) },
                            onEdit = { viewModel.navigateTo(Screen.PendudukForm(resident.nik)) },
                            onDelete = { residentToDelete = resident },
                            onMutasi = { residentToMutate = resident }
                        )
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet Modal
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            FilterBottomSheetContent(
                viewModel = viewModel,
                profile = profile,
                filterRt = filterRt,
                filterGender = filterGender,
                filterMutasi = filterMutasi,
                filterBansos = filterBansos,
                filterDisabilitas = filterDisabilitas,
                sortBy = sortBy,
                onDismiss = { showFilterSheet = false }
            )
        }
    }

    // Mutasi Penduduk Dialog
    residentToMutate?.let { resident ->
        MutasiPendudukDialog(
            penduduk = resident,
            onDismiss = { residentToMutate = null },
            onSaveMutation = { updatedPenduduk, jenisMutasi ->
                residentToMutate = null
                viewModel.catatMutasi(updatedPenduduk, jenisMutasi) {
                    Toast.makeText(context, "Mutasi berhasil dicatat!", Toast.LENGTH_SHORT).show()
                }
            },
            onRestoreActive = { nikToRestore ->
                residentToMutate = null
                viewModel.batalkanMutasi(nikToRestore) {
                    Toast.makeText(context, "Status warga dipulihkan menjadi Aktif!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Delete Confirmation Dialog
    residentToDelete?.let { resident ->
        AlertDialog(
            onDismissRequest = { residentToDelete = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Data Penduduk?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Text("Anda yakin ingin menghapus data '${resident.nama}' (NIK: ${resident.nik})? Penghapusan ini akan dicatat ke log aktivitas.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePenduduk(resident) {
                            residentToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Hapus Warga", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { residentToDelete = null },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Batal", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        )
    }
}

@Composable
fun ActiveFilterPill(
    label: String,
    onRemove: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hapus",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FilterBottomSheetContent(
    viewModel: MainViewModel,
    profile: com.example.data.model.UserProfile,
    filterRt: String,
    filterGender: String,
    filterMutasi: String,
    filterBansos: String,
    filterDisabilitas: String,
    sortBy: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filter & Urutkan Data",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            TextButton(
                onClick = {
                    viewModel.filterRt.value = "SEMUA"
                    viewModel.filterRw.value = "SEMUA"
                    viewModel.filterGender.value = "SEMUA"
                    viewModel.filterBansos.value = "SEMUA"
                    viewModel.filterDisabilitas.value = "SEMUA"
                    viewModel.filterMutasi.value = "SEMUA"
                    viewModel.sortBy.value = "NO"
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Reset Filter", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        // 1. Urutkan Data
        Text(
            text = "Urutkan Berdasarkan",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val sortOptions = listOf(
                "NO" to "Default (#No)",
                "RT" to "Wilayah RT",
                "NAMA" to "Nama (A-Z)",
                "NIK" to "Nomor NIK",
                "UMUR" to "Usia (Tertua)"
            )
            sortOptions.forEach { (key, title) ->
                FilterChip(
                    selected = sortBy == key,
                    onClick = { viewModel.sortBy.value = key },
                    label = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Filter RT
        val rtList = listOf("SEMUA") + com.example.data.model.UserProfile.getRtListForWilayah(profile.wilayahKerja)
        Text(
            text = if (profile.wilayahKerja.isNotBlank() && !profile.wilayahKerja.equals("Semua Wilayah", ignoreCase = true))
                "Pilih RT (Dusun ${profile.wilayahKerja})"
            else "Pilih RT Wilayah",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rtList.forEach { rtKey ->
                val isSelected = filterRt == rtKey || (rtKey != "SEMUA" && (filterRt == rtKey || filterRt.toIntOrNull() == rtKey.toIntOrNull()))
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.filterRt.value = rtKey },
                    label = { Text(if (rtKey == "SEMUA") "Semua RT" else "RT $rtKey", fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Jenis Kelamin
        Text(
            text = "Jenis Kelamin",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("SEMUA" to "Semua", "LAKI-LAKI" to "Laki-laki", "PEREMPUAN" to "Perempuan").forEach { (key, title) ->
                FilterChip(
                    selected = filterGender == key,
                    onClick = { viewModel.filterGender.value = key },
                    label = { Text(title, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Status Kependudukan (Mutasi)
        Text(
            text = "Status Kependudukan",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("SEMUA" to "Semua", "AKTIF" to "Warga Aktif", "MENINGGAL" to "Meninggal", "PINDAH" to "Pindah").forEach { (key, title) ->
                FilterChip(
                    selected = filterMutasi == key,
                    onClick = { viewModel.filterMutasi.value = key },
                    label = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Bantuan Sosial & Disabilitas
        Text(
            text = "Bantuan Sosial & Kondisi Khusus",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = filterBansos == "PENERIMA BANSOS",
                onClick = { viewModel.filterBansos.value = if (filterBansos == "PENERIMA BANSOS") "SEMUA" else "PENERIMA BANSOS" },
                label = { Text("Penerima Bansos", fontSize = 12.sp) }
            )
            FilterChip(
                selected = filterBansos == "PKH",
                onClick = { viewModel.filterBansos.value = if (filterBansos == "PKH") "SEMUA" else "PKH" },
                label = { Text("PKH", fontSize = 12.sp) }
            )
            FilterChip(
                selected = filterBansos == "BPNT",
                onClick = { viewModel.filterBansos.value = if (filterBansos == "BPNT") "SEMUA" else "BPNT" },
                label = { Text("BPNT", fontSize = 12.sp) }
            )
            FilterChip(
                selected = filterDisabilitas == "ADA DISABILITAS",
                onClick = { viewModel.filterDisabilitas.value = if (filterDisabilitas == "ADA DISABILITAS") "SEMUA" else "ADA DISABILITAS" },
                label = { Text("Disabilitas", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Apply Button
        Button(
            onClick = { onDismiss() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text("Terapkan Filter", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PendudukItemCard(
    resident: Penduduk,
    onViewDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMutasi: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val isMale = resident.isMale()
    val avatarBg = if (isMale) Color(0xFFE3F2FD) else Color(0xFFFCE4EC)
    val avatarTint = if (isMale) Color(0xFF1976D2) else Color(0xFFC2185B)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetail() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (resident.isMeninggal()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (!resident.isAktif()) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Row 1: Avatar + Name + NIK + Status Badge + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Uniform Circle Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMale) Icons.Default.Male else Icons.Default.Female,
                        contentDescription = if (isMale) "Laki-laki" else "Perempuan",
                        tint = avatarTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name, NIK and SHDK
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resident.nama,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = resident.nik,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (resident.shdk.isNotBlank()) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = resident.shdk,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Status Badge (Aktif / Meninggal / Pindah)
                val statusText = resident.statusMutasi.uppercase().ifBlank { "AKTIF" }
                val (statusBg, statusTextColor) = when {
                    resident.isMeninggal() -> Color(0xFF424242) to Color.White
                    resident.isPindah() -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                    else -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        0.5.dp,
                        if (resident.isAktif()) Color(0xFFA5D6A7) else statusTextColor.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = statusTextColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                // 3-dots Menu Button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opsi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Lihat Biodata Lengkap") },
                            leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showMenu = false
                                onViewDetail()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mutasi (Meninggal / Pindah)") },
                            leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                            onClick = {
                                showMenu = false
                                onMutasi()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Data") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Hapus Warga", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            // Row 2: Standard Uniform Metadata Grid (Wilayah RT/RW • Usia • Pekerjaan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Wilayah Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "RT ${resident.rt.ifBlank { "-" }} / RW ${resident.rw.ifBlank { "-" }}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Age Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${resident.getEffectiveAge()} Thn",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Pekerjaan Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.2f, fill = false)
                ) {
                    Text(
                        text = resident.pekerjaan.ifBlank { "-" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Row 3: Bottom Badge / Sync Status Strip (Uniform & Structured)
            val hasBansos = resident.isPenerimaBansos()
            val hasDisabilitas = resident.hasDisabilitas()

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Tags or No KK reference
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (hasBansos) {
                        val bansosLabel = when {
                            resident.kartuPkh.isNotBlank() && !resident.kartuPkh.equals("TIDAK", ignoreCase = true) -> "PKH"
                            resident.kartuBpnt.isNotBlank() && !resident.kartuBpnt.equals("TIDAK", ignoreCase = true) -> "BPNT"
                            resident.kartuKip.isNotBlank() && !resident.kartuKip.equals("TIDAK", ignoreCase = true) -> "KIP"
                            else -> "Bansos"
                        }
                        Surface(
                            color = Color(0xFFEDE7F6),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, Color(0xFFD1C4E9))
                        ) {
                            Text(
                                text = bansosLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = Color(0xFF512DA8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (hasDisabilitas) {
                        Surface(
                            color = Color(0xFFFFF8E1),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, Color(0xFFFFE082))
                        ) {
                            Text(
                                text = "Disabilitas",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = Color(0xFFF57F17),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!hasBansos && !hasDisabilitas) {
                        Text(
                            text = "KK: ${resident.noKk.ifBlank { "-" }}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right side: Local Sync Status Indicator
                if (!resident.syncedWithSheets) {
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, Color(0xFFFFB74D))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Belum Sync",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Belum Sync",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Tersinkron",
                            tint = Color(0xFF43A047),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Tersinkron",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = Color(0xFF43A047),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendudukTableView(
    pendudukList: List<Penduduk>,
    onViewDetail: (Penduduk) -> Unit,
    onEdit: (Penduduk) -> Unit,
    onDelete: (Penduduk) -> Unit,
    onMutasi: (Penduduk) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                // Table Header Row
                item {
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(text = "AKSI", width = 145.dp, isHeader = true)
                        TableCell(text = "STATUS", width = 110.dp, isHeader = true)
                        TableCell(text = "1. NO", width = 60.dp, isHeader = true)
                        TableCell(text = "2. NAMA", width = 180.dp, isHeader = true)
                        TableCell(text = "3. NIK", width = 160.dp, isHeader = true)
                        TableCell(text = "4. JENIS KELAMIN", width = 130.dp, isHeader = true)
                        TableCell(text = "5. TEMPAT LAHIR", width = 130.dp, isHeader = true)
                        TableCell(text = "6. TANGGAL LAHIR", width = 120.dp, isHeader = true)
                        TableCell(text = "7. AGAMA", width = 100.dp, isHeader = true)
                        TableCell(text = "8. PENDIDIKAN TERAKHIR", width = 170.dp, isHeader = true)
                        TableCell(text = "9. PEKERJAAN", width = 150.dp, isHeader = true)
                        TableCell(text = "10. GDR", width = 80.dp, isHeader = true)
                        TableCell(text = "11. STATUS PERKAWINAN", width = 150.dp, isHeader = true)
                        TableCell(text = "12. BUKU NIKAH", width = 110.dp, isHeader = true)
                        TableCell(text = "13. SHDK", width = 140.dp, isHeader = true)
                        TableCell(text = "14. KEWARGANEGARAAN", width = 130.dp, isHeader = true)
                        TableCell(text = "15. NO. PASPOR", width = 120.dp, isHeader = true)
                        TableCell(text = "16. NO KITAS", width = 120.dp, isHeader = true)
                        TableCell(text = "17. NAMA AYAH", width = 150.dp, isHeader = true)
                        TableCell(text = "18. NAMA IBU", width = 150.dp, isHeader = true)
                        TableCell(text = "19. NO KK", width = 160.dp, isHeader = true)
                        TableCell(text = "20. NAMA KK", width = 180.dp, isHeader = true)
                        TableCell(text = "21. ALAMAT", width = 200.dp, isHeader = true)
                        TableCell(text = "22. RW", width = 70.dp, isHeader = true)
                        TableCell(text = "23. RT", width = 70.dp, isHeader = true)
                        TableCell(text = "24. UMUR", width = 70.dp, isHeader = true)
                        TableCell(text = "25. KEPEMILIKAN E-KTP", width = 160.dp, isHeader = true)
                        TableCell(text = "26. TANGGAL PENCETAKAN", width = 150.dp, isHeader = true)
                        TableCell(text = "27. KEPEMILIKAN AKTA KELAHIRAN", width = 180.dp, isHeader = true)
                        TableCell(text = "28. KARTU KIA", width = 100.dp, isHeader = true)
                        TableCell(text = "29. KARTU PKH", width = 100.dp, isHeader = true)
                        TableCell(text = "30. KARTU BPNT", width = 100.dp, isHeader = true)
                        TableCell(text = "31. KARTU BPJS/KIS", width = 150.dp, isHeader = true)
                        TableCell(text = "32. KARTU KIP", width = 100.dp, isHeader = true)
                        TableCell(text = "33. JENIS KB", width = 150.dp, isHeader = true)
                        TableCell(text = "34. USAHA YANG DIJALANKAN", width = 180.dp, isHeader = true)
                        TableCell(text = "35. LISTRIK (TOKEN/ PASCA BAYAR)", width = 180.dp, isHeader = true)
                        TableCell(text = "36. KEPEMILIKAN LISTRIK", width = 150.dp, isHeader = true)
                        TableCell(text = "37. DAYA LISTRIK", width = 120.dp, isHeader = true)
                        TableCell(text = "38. NO TOKEN / KWH", width = 150.dp, isHeader = true)
                        TableCell(text = "39. NO HANDPHONE", width = 130.dp, isHeader = true)
                        TableCell(text = "40. ANAK KE", width = 90.dp, isHeader = true)
                        TableCell(text = "41. KEPEMILIKAN RUMAH", width = 150.dp, isHeader = true)
                        TableCell(text = "42. UKURAN RUMAH", width = 120.dp, isHeader = true)
                        TableCell(text = "43. JENIS RUMAH", width = 130.dp, isHeader = true)
                        TableCell(text = "44. KETERANGAN", width = 130.dp, isHeader = true)
                        TableCell(text = "45. VAKSINASI", width = 130.dp, isHeader = true)
                        TableCell(text = "46. DISABILITAS", width = 130.dp, isHeader = true)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                }

                // Table Rows
                items(pendudukList, key = { it.nik }) { resident ->
                    val isEven = (resident.no % 2 == 0)
                    Row(
                        modifier = Modifier
                            .background(
                                if (isEven) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { onViewDetail(resident) }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Action Buttons Cell
                        Row(
                            modifier = Modifier.width(145.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onViewDetail(resident) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Visibility, contentDescription = "Detail", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onMutasi(resident) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "Mutasi", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onEdit(resident) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onDelete(resident) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Status Cell
                        TableCell(text = resident.statusMutasi.uppercase(), width = 110.dp, isBold = true)

                        // 46 Data Cells
                        TableCell(text = resident.no.toString(), width = 60.dp)
                        TableCell(text = resident.nama, width = 180.dp, isBold = true)
                        TableCell(text = resident.nik, width = 160.dp)
                        TableCell(text = resident.getFormattedGender(), width = 130.dp)
                        TableCell(text = resident.tempatLahir, width = 130.dp)
                        TableCell(text = resident.tanggalLahir, width = 120.dp)
                        TableCell(text = resident.agama, width = 100.dp)
                        TableCell(text = resident.pendidikanTerakhir, width = 170.dp)
                        TableCell(text = resident.pekerjaan, width = 150.dp)
                        TableCell(text = resident.gdr, width = 80.dp)
                        TableCell(text = resident.statusPerkawinan, width = 150.dp)
                        TableCell(text = resident.bukuNikah, width = 110.dp)
                        TableCell(text = resident.shdk, width = 140.dp)
                        TableCell(text = resident.kewarganegaraan, width = 130.dp)
                        TableCell(text = resident.noPaspor, width = 120.dp)
                        TableCell(text = resident.noKitas, width = 120.dp)
                        TableCell(text = resident.namaAyah, width = 150.dp)
                        TableCell(text = resident.namaIbu, width = 150.dp)
                        TableCell(text = resident.noKk, width = 160.dp)
                        TableCell(text = resident.namaKepalaKeluarga, width = 180.dp)
                        TableCell(text = resident.alamat, width = 200.dp)
                        TableCell(text = resident.rw, width = 70.dp)
                        TableCell(text = resident.rt, width = 70.dp)
                        TableCell(text = "${resident.getEffectiveAge()} Thn", width = 70.dp)
                        TableCell(text = resident.kepemilikanEKtp, width = 160.dp)
                        TableCell(text = resident.tanggalPencetakan, width = 150.dp)
                        TableCell(text = resident.kepemilikanAktaKelahiran, width = 180.dp)
                        TableCell(text = resident.kartuKia, width = 100.dp)
                        TableCell(text = resident.kartuPkh, width = 100.dp)
                        TableCell(text = resident.kartuBpnt, width = 100.dp)
                        TableCell(text = resident.kartuBpjsKis, width = 150.dp)
                        TableCell(text = resident.kartuKip, width = 100.dp)
                        TableCell(text = resident.jenisKb, width = 150.dp)
                        TableCell(text = resident.usahaYangDijalankan, width = 180.dp)
                        TableCell(text = resident.listrikJenis, width = 180.dp)
                        TableCell(text = resident.kepemilikanListrik, width = 150.dp)
                        TableCell(text = resident.dayaListrik, width = 120.dp)
                        TableCell(text = resident.noTokenKwh, width = 150.dp)
                        TableCell(text = resident.noHandphone, width = 130.dp)
                        TableCell(text = resident.anakKe.toString(), width = 90.dp)
                        TableCell(text = resident.kepemilikanRumah, width = 150.dp)
                        TableCell(text = resident.ukuranRumah, width = 120.dp)
                        TableCell(text = resident.jenisRumah, width = 130.dp)
                        TableCell(text = resident.keterangan, width = 130.dp)
                        TableCell(text = resident.vaksinasi, width = 130.dp)
                        TableCell(text = resident.disabilitas, width = 130.dp)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isBold: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = if (isHeader) MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            ) else MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = if (isHeader) 2 else 1
        )
    }
}
