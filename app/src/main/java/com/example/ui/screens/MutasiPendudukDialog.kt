package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Penduduk
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutasiPendudukDialog(
    penduduk: Penduduk,
    onDismiss: () -> Unit,
    onSaveMutation: (updatedPenduduk: Penduduk, jenisMutasi: String) -> Unit,
    onRestoreActive: (nik: String) -> Unit
) {
    val context = LocalContext.current
    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Selected Tab / Type: "MENINGGAL" or "PINDAH"
    var selectedMutasiType by remember {
        mutableStateOf(
            if (penduduk.isMeninggal()) "MENINGGAL"
            else if (penduduk.isPindah()) "PINDAH"
            else "MENINGGAL"
        )
    }

    // Form State: Kematian
    var tanggalKematian by remember { mutableStateOf(penduduk.tanggalKematian.ifBlank { todayStr }) }
    var waktuKematian by remember { mutableStateOf(penduduk.waktuKematian.ifBlank { "08:00 WIB" }) }
    var tempatKematian by remember { mutableStateOf(penduduk.tempatKematian.ifBlank { "Rumah Tinggal" }) }
    var tempatKematianCustom by remember { mutableStateOf("") }
    var penyebabKematian by remember { mutableStateOf(penduduk.penyebabKematian.ifBlank { "Sakit Biasa / Medis" }) }
    var penyebabKematianCustom by remember { mutableStateOf("") }
    var tempatPemakaman by remember { mutableStateOf(penduduk.tempatPemakaman.ifBlank { "TPU Desa Cimanggu" }) }
    var noSuratKematian by remember { mutableStateOf(penduduk.noSuratKematian) }
    var namaPelaporKematian by remember { mutableStateOf(penduduk.namaPelaporKematian) }
    var hubunganPelaporKematian by remember { mutableStateOf(penduduk.hubunganPelaporKematian.ifBlank { "Keluarga / Ahli Waris" }) }
    var catatanKematian by remember { mutableStateOf(penduduk.catatanKematian) }

    // Form State: Kepindahan
    var tanggalPindah by remember { mutableStateOf(penduduk.tanggalPindah.ifBlank { todayStr }) }
    var alasanPindah by remember { mutableStateOf(penduduk.alasanPindah.ifBlank { "Pekerjaan / Dinas" }) }
    var alasanPindahCustom by remember { mutableStateOf("") }
    var klasifikasiPindah by remember { mutableStateOf(penduduk.klasifikasiPindah.ifBlank { "Anggota Keluarga Saja" }) }
    var alamatTujuan by remember { mutableStateOf(penduduk.alamatTujuan) }
    var rtTujuan by remember { mutableStateOf(penduduk.rtTujuan.ifBlank { "001" }) }
    var rwTujuan by remember { mutableStateOf(penduduk.rwTujuan.ifBlank { "001" }) }
    var desaTujuan by remember { mutableStateOf(penduduk.desaTujuan) }
    var kecamatanTujuan by remember { mutableStateOf(penduduk.kecamatanTujuan) }
    var kabupatenTujuan by remember { mutableStateOf(penduduk.kabupatenTujuan) }
    var provinsiTujuan by remember { mutableStateOf(penduduk.provinsiTujuan.ifBlank { "Jawa Tengah" }) }
    var kodePosTujuan by remember { mutableStateOf(penduduk.kodePosTujuan) }
    var noSuratPindah by remember { mutableStateOf(penduduk.noSuratPindah) }
    var catatanPindah by remember { mutableStateOf(penduduk.catatanPindah) }

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showDatePickerForKematian by remember { mutableStateOf(false) }
    var showDatePickerForPindah by remember { mutableStateOf(false) }

    val datePickerStateKematian = rememberDatePickerState()
    val datePickerStatePindah = rememberDatePickerState()

    // DatePicker Dialogs
    if (showDatePickerForKematian) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerForKematian = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerStateKematian.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = millis
                        tanggalKematian = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    }
                    showDatePickerForKematian = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerForKematian = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerStateKematian)
        }
    }

    if (showDatePickerForPindah) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerForPindah = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerStatePindah.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = millis
                        tanggalPindah = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    }
                    showDatePickerForPindah = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerForPindah = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerStatePindah)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedMutasiType == "MENINGGAL") Icons.Default.PersonOff else Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Form Mutasi Kependudukan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pencatatan status kematian atau kepindahan warga",
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

                // Resident Profile Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (penduduk.isMale()) Color(0xFF0288D1) else Color(0xFFC2185B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = penduduk.nama,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "NIK: ${penduduk.nik} • KK: ${penduduk.noKk} • RT ${penduduk.rt}/RW ${penduduk.rw}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Current Status Badge
                        val currentStatus = penduduk.statusMutasi.uppercase()
                        val badgeBg = when (currentStatus) {
                            "MENINGGAL" -> Color(0xFF424242)
                            "PINDAH" -> Color(0xFFE65100)
                            else -> Color(0xFF2E7D32)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = currentStatus,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mutasi Type Tabs / Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Meninggal Tab
                    val isMeninggalSelected = selectedMutasiType == "MENINGGAL"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isMeninggalSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedMutasiType = "MENINGGAL" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonOff,
                                contentDescription = null,
                                tint = if (isMeninggalSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1. Meninggal Dunia",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isMeninggalSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Pindah Tab
                    val isPindahSelected = selectedMutasiType == "PINDAH"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPindahSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedMutasiType = "PINDAH" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = if (isPindahSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2. Pindah Keluar",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isPindahSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (selectedMutasiType == "MENINGGAL") {
                            // ==================== FORM MENINGGAL DUNIA ====================
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Dokumentasi Kematian Penduduk",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Lengkapi data akta kematian sesuai laporan keluarga / RT",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Tanggal Kematian & Jam
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = tanggalKematian,
                                            onValueChange = { tanggalKematian = it },
                                            label = { Text("Tanggal Kematian *") },
                                            modifier = Modifier.weight(1.3f),
                                            trailingIcon = {
                                                IconButton(onClick = { showDatePickerForKematian = true }) {
                                                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Pilih Tanggal")
                                                }
                                            },
                                            placeholder = { Text("YYYY-MM-DD") },
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = waktuKematian,
                                            onValueChange = { waktuKematian = it },
                                            label = { Text("Waktu / Jam") },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("08:30 WIB") },
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Tempat Kematian Dropdown
                                    MutasiDropdownField(
                                        label = "Tempat Kematian",
                                        options = Penduduk.TEMPAT_KEMATIAN_OPTIONS,
                                        selectedValue = tempatKematian,
                                        onSelected = {
                                            tempatKematian = it
                                            if (it != "Lainnya") tempatKematianCustom = ""
                                        }
                                    )

                                    if (tempatKematian == "Lainnya") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = tempatKematianCustom,
                                            onValueChange = { tempatKematianCustom = it },
                                            label = { Text("Tuliskan Tempat Kematian Lainnya") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Penyebab Kematian Dropdown
                                    MutasiDropdownField(
                                        label = "Penyebab Kematian",
                                        options = Penduduk.PENYEBAB_KEMATIAN_OPTIONS,
                                        selectedValue = penyebabKematian,
                                        onSelected = {
                                            penyebabKematian = it
                                            if (it != "Lainnya") penyebabKematianCustom = ""
                                        }
                                    )

                                    if (penyebabKematian == "Lainnya") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = penyebabKematianCustom,
                                            onValueChange = { penyebabKematianCustom = it },
                                            label = { Text("Tuliskan Penyebab Kematian Lainnya") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Tempat Pemakaman
                                    OutlinedTextField(
                                        value = tempatPemakaman,
                                        onValueChange = { tempatPemakaman = it },
                                        label = { Text("Tempat Pemakaman (TPU)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("TPU Desa Cimanggu") },
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Nomor Surat / Akta Kematian
                                    OutlinedTextField(
                                        value = noSuratKematian,
                                        onValueChange = { noSuratKematian = it },
                                        label = { Text("Nomor Surat Kematian Desa / Akta") },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Contoh: 474.3/012/DS/2026") },
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Pelapor Kematian & Hubungan
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = namaPelaporKematian,
                                            onValueChange = { namaPelaporKematian = it },
                                            label = { Text("Nama Pelapor") },
                                            modifier = Modifier.weight(1.3f),
                                            placeholder = { Text("Nama Pelapor") },
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = hubunganPelaporKematian,
                                            onValueChange = { hubunganPelaporKematian = it },
                                            label = { Text("Hubungan Pelapor") },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("Anak / Istri / RT") },
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Catatan Tambahan Kematian
                                    OutlinedTextField(
                                        value = catatanKematian,
                                        onValueChange = { catatanKematian = it },
                                        label = { Text("Keterangan Tambahan (Opsional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Catatan riwayat sakit, administrasi, dll") },
                                        maxLines = 3
                                    )
                                }
                            }
                        } else {
                            // ==================== FORM PINDAH KELUAR ====================
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Dokumentasi Kepindahan (Pindah Keluar)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Lengkapi data tujuan kepindahan dan SKPWNI",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Tanggal Pindah
                                    OutlinedTextField(
                                        value = tanggalPindah,
                                        onValueChange = { tanggalPindah = it },
                                        label = { Text("Tanggal Kepindahan *") },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(onClick = { showDatePickerForPindah = true }) {
                                                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Pilih Tanggal")
                                            }
                                        },
                                        placeholder = { Text("YYYY-MM-DD") },
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Alasan Pindah Dropdown
                                    MutasiDropdownField(
                                        label = "Alasan Pindah",
                                        options = Penduduk.ALASAN_PINDAH_OPTIONS,
                                        selectedValue = alasanPindah,
                                        onSelected = {
                                            alasanPindah = it
                                            if (it != "Lainnya") alasanPindahCustom = ""
                                        }
                                    )

                                    if (alasanPindah == "Lainnya") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = alasanPindahCustom,
                                            onValueChange = { alasanPindahCustom = it },
                                            label = { Text("Tuliskan Alasan Pindah Lainnya") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Klasifikasi Pindah Dropdown
                                    MutasiDropdownField(
                                        label = "Klasifikasi Kepindahan",
                                        options = Penduduk.KLASIFIKASI_PINDAH_OPTIONS,
                                        selectedValue = klasifikasiPindah,
                                        onSelected = { klasifikasiPindah = it }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Alamat Tujuan Baru
                                    OutlinedTextField(
                                        value = alamatTujuan,
                                        onValueChange = { alamatTujuan = it },
                                        label = { Text("Alamat Lengkap Tujuan Baru *") },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Jl. Merpati No. 12 / Dusun Krajan") },
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // RT & RW Tujuan
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = rtTujuan,
                                            onValueChange = { rtTujuan = it },
                                            label = { Text("RT Tujuan") },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("001") },
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = rwTujuan,
                                            onValueChange = { rwTujuan = it },
                                            label = { Text("RW Tujuan") },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("002") },
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = kodePosTujuan,
                                            onValueChange = { kodePosTujuan = it },
                                            label = { Text("Kode Pos") },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("53256") },
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Desa / Kelurahan & Kecamatan Tujuan
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = desaTujuan,
                                            onValueChange = { desaTujuan = it },
                                            label = { Text("Desa / Kelurahan Tujuan *") },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("Nama Desa/Kelurahan") },
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = kecamatanTujuan,
                                            onValueChange = { kecamatanTujuan = it },
                                            label = { Text("Kecamatan Tujuan *") },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("Nama Kecamatan") },
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Kabupaten / Kota & Provinsi Tujuan
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = kabupatenTujuan,
                                            onValueChange = { kabupatenTujuan = it },
                                            label = { Text("Kabupaten / Kota Tujuan *") },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("Nama Kab/Kota") },
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = provinsiTujuan,
                                            onValueChange = { provinsiTujuan = it },
                                            label = { Text("Provinsi Tujuan") },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("Jawa Tengah") },
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Nomor Surat Pindah (SKPWNI)
                                    OutlinedTextField(
                                        value = noSuratPindah,
                                        onValueChange = { noSuratPindah = it },
                                        label = { Text("Nomor SKPWNI / Surat Pindah") },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Contoh: 471.2/045/SKPWNI/2026") },
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Catatan Tambahan Pindah
                                    OutlinedTextField(
                                        value = catatanPindah,
                                        onValueChange = { catatanPindah = it },
                                        label = { Text("Keterangan Tambahan Kepindahan") },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Catatan penyerahan berkas / pengikut") },
                                        maxLines = 3
                                    )
                                }
                            }
                        }

                        // Option to restore to active if currently mutated
                        if (penduduk.isMeninggal() || penduduk.isPindah()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Batalkan Status Mutasi",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "Kembalikan warga ini menjadi Warga Aktif tetap normal",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = { showRestoreConfirmDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pulihkan", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Batal", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Button(
                        onClick = {
                            if (selectedMutasiType == "MENINGGAL") {
                                if (tanggalKematian.isBlank()) {
                                    Toast.makeText(context, "Harap isi Tanggal Kematian!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val finalTempat = if (tempatKematian == "Lainnya" && tempatKematianCustom.isNotBlank()) tempatKematianCustom else tempatKematian
                                val finalPenyebab = if (penyebabKematian == "Lainnya" && penyebabKematianCustom.isNotBlank()) penyebabKematianCustom else penyebabKematian

                                val updated = penduduk.copy(
                                    statusMutasi = "MENINGGAL",
                                    tanggalKematian = tanggalKematian.trim(),
                                    waktuKematian = waktuKematian.trim(),
                                    tempatKematian = finalTempat.trim(),
                                    penyebabKematian = finalPenyebab.trim(),
                                    tempatPemakaman = tempatPemakaman.trim(),
                                    noSuratKematian = noSuratKematian.trim(),
                                    namaPelaporKematian = namaPelaporKematian.trim(),
                                    hubunganPelaporKematian = hubunganPelaporKematian.trim(),
                                    catatanKematian = catatanKematian.trim(),
                                    keterangan = "Meninggal Dunia (${tanggalKematian.trim()})"
                                )
                                onSaveMutation(updated, "MENINGGAL")
                            } else {
                                if (tanggalPindah.isBlank()) {
                                    Toast.makeText(context, "Harap isi Tanggal Kepindahan!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (desaTujuan.isBlank() && alamatTujuan.isBlank() && kabupatenTujuan.isBlank()) {
                                    Toast.makeText(context, "Harap lengkapi Alamat atau Desa/Kota Tujuan!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val finalAlasan = if (alasanPindah == "Lainnya" && alasanPindahCustom.isNotBlank()) alasanPindahCustom else alasanPindah

                                val updated = penduduk.copy(
                                    statusMutasi = "PINDAH",
                                    tanggalPindah = tanggalPindah.trim(),
                                    alasanPindah = finalAlasan.trim(),
                                    klasifikasiPindah = klasifikasiPindah.trim(),
                                    alamatTujuan = alamatTujuan.trim(),
                                    rtTujuan = Penduduk.formatRtRw(rtTujuan),
                                    rwTujuan = Penduduk.formatRtRw(rwTujuan),
                                    desaTujuan = desaTujuan.trim(),
                                    kecamatanTujuan = kecamatanTujuan.trim(),
                                    kabupatenTujuan = kabupatenTujuan.trim(),
                                    provinsiTujuan = provinsiTujuan.trim(),
                                    kodePosTujuan = kodePosTujuan.trim(),
                                    noSuratPindah = noSuratPindah.trim(),
                                    catatanPindah = catatanPindah.trim(),
                                    keterangan = "Pindah Keluar (${desaTujuan.ifBlank { kabupatenTujuan.ifBlank { "Luar Daerah" } }})"
                                )
                                onSaveMutation(updated, "PINDAH")
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedMutasiType == "MENINGGAL") "Simpan Kematian" else "Simpan Kepindahan",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog for restoring to active
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("Konfirmasi Pembatalan Mutasi") },
            text = {
                Text("Apakah Anda yakin ingin membatalkan status mutasi warga '${penduduk.nama}' dan mengembalikannya menjadi Warga Aktif normal?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        onRestoreActive(penduduk.nik)
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Ya, Pulihkan ke Aktif", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreConfirmDialog = false },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Batal", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        )
    }
}

@Composable
fun MutasiDropdownField(
    label: String,
    options: List<String>,
    selectedValue: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
