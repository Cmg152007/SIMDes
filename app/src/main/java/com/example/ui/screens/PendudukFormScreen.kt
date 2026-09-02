package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Penduduk
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendudukFormScreen(
    nikToEdit: String?,
    initialNoKk: String? = null,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allResidents by viewModel.allPenduduk.collectAsState()
    val existing = if (nikToEdit != null) allResidents.find { it.nik == nikToEdit } else null
    val isEdit = existing != null

    // Look up family member by initialNoKk if creating new member in existing KK
    val familyRef = remember(initialNoKk, allResidents) {
        if (!initialNoKk.isNullOrBlank()) {
            allResidents.firstOrNull { it.noKk.trim().equals(initialNoKk.trim(), ignoreCase = true) }
        } else null
    }
    val familyHeadRef = remember(initialNoKk, allResidents) {
        if (!initialNoKk.isNullOrBlank()) {
            allResidents.firstOrNull { it.noKk.trim().equals(initialNoKk.trim(), ignoreCase = true) && it.shdk.contains("KEPALA", ignoreCase = true) }
        } else null
    }

    // Form state variables for 48 fields
    var no by remember { mutableIntStateOf(existing?.no ?: (allResidents.size + 1)) }
    var nama by remember { mutableStateOf(existing?.nama ?: "") }
    var nik by remember { mutableStateOf(existing?.nik ?: "") }
    var jenisKelamin by remember { mutableStateOf(existing?.jenisKelamin ?: "LAKI-LAKI") }
    var tempatLahir by remember { mutableStateOf(existing?.tempatLahir ?: familyRef?.tempatLahir ?: "CILACAP") }
    var tanggalLahir by remember { mutableStateOf(existing?.tanggalLahir ?: "1990-01-01") }
    var agama by remember { mutableStateOf(existing?.agama ?: familyRef?.agama ?: "ISLAM") }
    var pendidikanTerakhir by remember { mutableStateOf(existing?.pendidikanTerakhir ?: "SLTA / SEDERAJAT") }
    var pekerjaan by remember { mutableStateOf(existing?.pekerjaan ?: "WIRASWASTA") }
    var gdr by remember { mutableStateOf(existing?.gdr ?: "TIDAK TAHU") }
    var statusPerkawinan by remember { mutableStateOf(existing?.statusPerkawinan ?: if (familyRef != null) "BELUM KAWIN" else "BELUM KAWIN") }
    var bukuNikah by remember { mutableStateOf(existing?.bukuNikah ?: "TIDAK ADA") }
    var shdk by remember { mutableStateOf(existing?.shdk ?: if (familyRef != null) "ANAK" else "KEPALA KELUARGA") }
    var kewarganegaraan by remember { mutableStateOf(existing?.kewarganegaraan ?: "WNI") }
    var noPaspor by remember { mutableStateOf(existing?.noPaspor ?: "-") }
    var noKitas by remember { mutableStateOf(existing?.noKitas ?: "-") }
    var namaAyah by remember { mutableStateOf(existing?.namaAyah ?: (familyHeadRef?.nama ?: familyRef?.namaKepalaKeluarga ?: "")) }
    var namaIbu by remember { mutableStateOf(existing?.namaIbu ?: "") }
    var noKk by remember { mutableStateOf(existing?.noKk ?: initialNoKk ?: "") }
    var namaKepalaKeluarga by remember { mutableStateOf(existing?.namaKepalaKeluarga ?: (familyHeadRef?.nama ?: familyRef?.namaKepalaKeluarga ?: "")) }
    var alamat by remember { mutableStateOf(existing?.alamat ?: (familyRef?.alamat ?: "Dusun Krajan")) }
    var rw by remember { mutableStateOf(existing?.rw?.let { Penduduk.formatRtRw(it) } ?: (familyRef?.rw?.let { Penduduk.formatRtRw(it) } ?: "001")) }
    var rt by remember { mutableStateOf(existing?.rt?.let { Penduduk.formatRtRw(it) } ?: (familyRef?.rt?.let { Penduduk.formatRtRw(it) } ?: "001")) }
    
    // Calculate age automatically
    val calculatedUmur = remember(tanggalLahir) { Penduduk.calculateAge(tanggalLahir) }
    var umur by remember { mutableIntStateOf(existing?.umur ?: calculatedUmur) }
    var umurLakiLaki by remember { mutableStateOf(existing?.umurLakiLaki ?: if (jenisKelamin == "LAKI-LAKI") "$calculatedUmur Tahun" else "-") }
    var umurPerempuan by remember { mutableStateOf(existing?.umurPerempuan ?: if (jenisKelamin == "PEREMPUAN") "$calculatedUmur Tahun" else "-") }

    var kepemilikanEKtp by remember { mutableStateOf(existing?.kepemilikanEKtp ?: if (calculatedUmur >= 17) "SUDAH MEMILIKI" else "BELUM WAJIB KTP") }
    var tanggalPencetakan by remember { mutableStateOf(existing?.tanggalPencetakan ?: "") }
    var kepemilikanAktaKelahiran by remember { mutableStateOf(existing?.kepemilikanAktaKelahiran ?: "ADA") }
    var kartuKia by remember { mutableStateOf(existing?.kartuKia ?: if (calculatedUmur <= 17) "ADA" else "TIDAK WAJIB") }
    var kartuPkh by remember { mutableStateOf(existing?.kartuPkh ?: "TIDAK") }
    var kartuBpnt by remember { mutableStateOf(existing?.kartuBpnt ?: "TIDAK") }
    var kartuBpjsKis by remember { mutableStateOf(existing?.kartuBpjsKis ?: "BPJS PBI / KIS") }
    var kartuKip by remember { mutableStateOf(existing?.kartuKip ?: "TIDAK") }
    var jenisKb by remember { mutableStateOf(existing?.jenisKb ?: "BUKAN PESERTA KB") }
    var usahaYangDijalankan by remember { mutableStateOf(existing?.usahaYangDijalankan ?: "-") }
    var listrikJenis by remember { mutableStateOf(existing?.listrikJenis ?: "TOKEN") }
    var kepemilikanListrik by remember { mutableStateOf(existing?.kepemilikanListrik ?: "SENDIRI") }
    var dayaListrik by remember { mutableStateOf(existing?.dayaListrik ?: "900 VA") }
    var noTokenKwh by remember { mutableStateOf(existing?.noTokenKwh ?: "-") }
    var noHandphone by remember { mutableStateOf(existing?.noHandphone ?: "-") }
    var anakKe by remember { mutableIntStateOf(existing?.anakKe ?: 1) }
    var kepemilikanRumah by remember { mutableStateOf(existing?.kepemilikanRumah ?: "MILIK SENDIRI") }
    var ukuranRumah by remember { mutableStateOf(existing?.ukuranRumah ?: "6x8 m") }
    var jenisRumah by remember { mutableStateOf(existing?.jenisRumah ?: "PERMANEN") }
    var keterangan by remember { mutableStateOf(existing?.keterangan ?: "Warga Aktif") }
    var vaksinasi by remember { mutableStateOf(existing?.vaksinasi ?: "DOSIS LENGKAP") }
    var disabilitas by remember { mutableStateOf(existing?.disabilitas ?: "TIDAK ADA") }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("1. Identitas", "2. Keluarga", "3. Profesi", "4. Dokumen & Bansos", "5. Hunian", "6. Kesehatan")

    var validationError by remember { mutableStateOf<String?>(null) }

    // Real-time NIK validation logic
    val cleanNik = nik.trim()
    val isNikNumeric = cleanNik.isEmpty() || cleanNik.all { it.isDigit() }
    val isNikLengthValid = cleanNik.length == 16
    val duplicateResident = remember(cleanNik, allResidents, isEdit, nikToEdit) {
        if (cleanNik.isBlank()) null
        else {
            allResidents.firstOrNull { res ->
                res.nik.trim() == cleanNik && (!isEdit || res.nik.trim() != (nikToEdit?.trim() ?: ""))
            }
        }
    }
    val isNikDuplicate = duplicateResident != null
    val isNikValid = cleanNik.length == 16 && cleanNik.all { it.isDigit() } && !isNikDuplicate

    // Real-time No KK validation logic
    val cleanNoKk = noKk.trim()
    val isNoKkNumeric = cleanNoKk.isEmpty() || cleanNoKk.all { it.isDigit() }
    val isNoKkLengthValid = cleanNoKk.length == 16
    val existingKkMembers = remember(cleanNoKk, allResidents) {
        if (cleanNoKk.isNotBlank() && cleanNoKk.length >= 10) {
            allResidents.filter { it.noKk.trim() == cleanNoKk }
        } else emptyList()
    }
    val isNoKkValid = cleanNoKk.length == 16 && cleanNoKk.all { it.isDigit() }

    // Date picker dialog
    val showDatePicker = { onDateSelected: (String) -> Unit ->
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val m = (month + 1).toString().padStart(2, '0')
                val d = dayOfMonth.toString().padStart(2, '0')
                onDateSelected("$year-$m-$d")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "Edit Biodata Penduduk" else "Perekaman Penduduk Baru",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Button(
                        onClick = {
                            if (nik.isBlank() || nama.isBlank() || noKk.isBlank()) {
                                validationError = "Mohon lengkapi NIK, Nama Lengkap, dan No KK!"
                                return@Button
                            }
                            if (!isNikNumeric) {
                                validationError = "NIK hanya boleh berisi karakter angka!"
                                return@Button
                            }
                            if (!isNikLengthValid) {
                                validationError = "NIK harus tepat 16 digit angka! (Saat ini: ${cleanNik.length} digit)"
                                return@Button
                            }
                            if (isNikDuplicate) {
                                validationError = "NIK '$cleanNik' sudah terdaftar atas nama ${duplicateResident?.nama ?: "penduduk lain"}!"
                                return@Button
                            }
                            if (!isNoKkNumeric) {
                                validationError = "No KK hanya boleh berisi karakter angka!"
                                return@Button
                            }
                            if (!isNoKkLengthValid) {
                                validationError = "No KK harus tepat 16 digit angka! (Saat ini: ${cleanNoKk.length} digit)"
                                return@Button
                            }
                            val autoAge = Penduduk.calculateAge(tanggalLahir)
                            val finalResident = Penduduk(
                                no = no,
                                nama = nama.trim().uppercase(),
                                nik = cleanNik,
                                jenisKelamin = jenisKelamin,
                                tempatLahir = tempatLahir.trim().uppercase(),
                                tanggalLahir = tanggalLahir,
                                agama = agama,
                                pendidikanTerakhir = pendidikanTerakhir,
                                pekerjaan = pekerjaan.trim().uppercase(),
                                gdr = gdr,
                                statusPerkawinan = statusPerkawinan,
                                bukuNikah = bukuNikah,
                                shdk = shdk,
                                kewarganegaraan = kewarganegaraan,
                                noPaspor = noPaspor.trim(),
                                noKitas = noKitas.trim(),
                                namaAyah = namaAyah.trim().uppercase(),
                                namaIbu = namaIbu.trim().uppercase(),
                                noKk = cleanNoKk,
                                namaKepalaKeluarga = namaKepalaKeluarga.trim().uppercase(),
                                alamat = alamat.trim(),
                                rw = Penduduk.formatRtRw(rw),
                                rt = Penduduk.formatRtRw(rt),
                                umur = autoAge,
                                umurLakiLaki = if (jenisKelamin == "LAKI-LAKI") "$autoAge Tahun" else "-",
                                umurPerempuan = if (jenisKelamin == "PEREMPUAN") "$autoAge Tahun" else "-",
                                kepemilikanEKtp = kepemilikanEKtp,
                                tanggalPencetakan = tanggalPencetakan,
                                kepemilikanAktaKelahiran = kepemilikanAktaKelahiran,
                                kartuKia = kartuKia,
                                kartuPkh = kartuPkh,
                                kartuBpnt = kartuBpnt,
                                kartuBpjsKis = kartuBpjsKis,
                                kartuKip = kartuKip,
                                jenisKb = jenisKb,
                                usahaYangDijalankan = usahaYangDijalankan.trim(),
                                listrikJenis = listrikJenis,
                                kepemilikanListrik = kepemilikanListrik,
                                dayaListrik = dayaListrik,
                                noTokenKwh = noTokenKwh.trim(),
                                noHandphone = noHandphone.trim(),
                                anakKe = anakKe,
                                kepemilikanRumah = kepemilikanRumah,
                                ukuranRumah = ukuranRumah.trim(),
                                jenisRumah = jenisRumah,
                                keterangan = keterangan.trim(),
                                vaksinasi = vaksinasi,
                                disabilitas = disabilitas,
                                syncedWithSheets = false
                            )

                            viewModel.savePenduduk(finalResident, isEdit, oldNik = nikToEdit) {
                                viewModel.navigateTo(Screen.PendudukList)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Tab Row for 6 sections
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (validationError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    // TAB 1: IDENTITAS PRIBADI
                    0 -> {
                        item {
                            OutlinedTextField(
                                value = nama,
                                onValueChange = { nama = it; validationError = null },
                                label = { Text("1. NAMA Lengkap *") },
                                placeholder = { Text("Contoh: BAMBANG SUDIRO") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = nik,
                                onValueChange = { 
                                    // Limit input to digits only and maximum 16 digits
                                    val filtered = it.filter { ch -> ch.isDigit() }.take(16)
                                    nik = filtered
                                    validationError = null 
                                },
                                label = { Text("2. NIK (Nomor Induk Kependudukan) *") },
                                placeholder = { Text("Contoh: 3301021508750001") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                enabled = true,
                                isError = cleanNik.isNotEmpty() && !isNikValid,
                                trailingIcon = {
                                    if (cleanNik.isNotEmpty()) {
                                        if (isNikValid) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "NIK Valid",
                                                tint = Color(0xFF2E7D32)
                                            )
                                        } else if (isNikDuplicate) {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = "NIK Duplikat",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "Belum 16 Digit",
                                                tint = Color(0xFFE65100)
                                            )
                                        }
                                    }
                                },
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = when {
                                                cleanNik.isEmpty() -> "Wajib 16 digit angka sesuai KTP-el"
                                                isNikDuplicate -> "⚠️ NIK terdaftar: ${duplicateResident?.nama}"
                                                !isNikNumeric -> "❌ Hanya boleh berisi angka"
                                                cleanNik.length < 16 -> "Kurang ${16 - cleanNik.length} digit lagi"
                                                isNikValid -> "✓ NIK 16 digit valid & unik"
                                                else -> ""
                                            },
                                            color = when {
                                                isNikValid -> Color(0xFF2E7D32)
                                                isNikDuplicate -> MaterialTheme.colorScheme.error
                                                cleanNik.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant
                                                else -> Color(0xFFE65100)
                                            },
                                            fontSize = 11.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${cleanNik.length}/16",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (cleanNik.length == 16) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "3. JENIS KELAMIN",
                                selectedValue = jenisKelamin,
                                options = Penduduk.GENDER_OPTIONS,
                                onSelect = { jenisKelamin = it }
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = tempatLahir,
                                onValueChange = { tempatLahir = it },
                                label = { Text("4. TEMPAT LAHIR") },
                                placeholder = { Text("Contoh: CILACAP") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = tanggalLahir,
                                    onValueChange = {
                                        tanggalLahir = it
                                        umur = Penduduk.calculateAge(it)
                                    },
                                    label = { Text("5. TANGGAL LAHIR (YYYY-MM-DD)") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        showDatePicker { picked ->
                                            tanggalLahir = picked
                                            umur = Penduduk.calculateAge(picked)
                                        }
                                    },
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Pilih Tanggal")
                                }
                            }
                        }
                        item {
                            OutlinedTextField(
                                value = "$calculatedUmur Tahun (Otomatis)",
                                onValueChange = {},
                                label = { Text("24. UMUR TERHITUNG") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "7. AGAMA",
                                selectedValue = agama,
                                options = Penduduk.AGAMA_OPTIONS,
                                onSelect = { agama = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "10. GDR (Golongan Darah)",
                                selectedValue = gdr,
                                options = Penduduk.GDR_OPTIONS,
                                onSelect = { gdr = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "14. KEWARGANEGARAAN",
                                selectedValue = kewarganegaraan,
                                options = Penduduk.KEWARGANEGARAAN_OPTIONS,
                                onSelect = { kewarganegaraan = it }
                            )
                        }
                    }

                    // TAB 2: KELUARGA & DOMISILI
                    1 -> {
                        item {
                            OutlinedTextField(
                                value = noKk,
                                onValueChange = { 
                                    // Limit input to digits only and maximum 16 digits
                                    val filtered = it.filter { ch -> ch.isDigit() }.take(16)
                                    noKk = filtered
                                    validationError = null 
                                },
                                label = { Text("19. NO KK (Kartu Keluarga) *") },
                                placeholder = { Text("Contoh: 3301021508750002") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = cleanNoKk.isNotEmpty() && !isNoKkValid,
                                trailingIcon = {
                                    if (cleanNoKk.isNotEmpty()) {
                                        if (isNoKkValid) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "No KK Valid",
                                                tint = Color(0xFF2E7D32)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "Belum 16 Digit",
                                                tint = Color(0xFFE65100)
                                            )
                                        }
                                    }
                                },
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = when {
                                                cleanNoKk.isEmpty() -> "Wajib 16 digit angka No Kartu Keluarga"
                                                !isNoKkNumeric -> "❌ Hanya boleh berisi angka"
                                                cleanNoKk.length < 16 -> "Kurang ${16 - cleanNoKk.length} digit lagi"
                                                isNoKkValid && existingKkMembers.isNotEmpty() -> "✓ Terhubung (${existingKkMembers.size} anggota keluarga terdaftar)"
                                                isNoKkValid -> "✓ Format No KK 16 digit valid (Keluarga Baru)"
                                                else -> ""
                                            },
                                            color = when {
                                                isNoKkValid -> Color(0xFF2E7D32)
                                                cleanNoKk.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant
                                                else -> Color(0xFFE65100)
                                            },
                                            fontSize = 11.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${cleanNoKk.length}/16",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (cleanNoKk.length == 16) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = namaKepalaKeluarga,
                                onValueChange = { namaKepalaKeluarga = it },
                                label = { Text("20. NAMA KK") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "13. SHDK (Status Hubungan Keluarga)",
                                selectedValue = shdk,
                                options = Penduduk.SHDK_OPTIONS,
                                onSelect = { shdk = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "11. STATUS PERKAWINAN",
                                selectedValue = statusPerkawinan,
                                options = Penduduk.STATUS_PERKAWINAN_OPTIONS,
                                onSelect = { statusPerkawinan = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "12. BUKU NIKAH",
                                selectedValue = bukuNikah,
                                options = Penduduk.BUKU_NIKAH_OPTIONS,
                                onSelect = { bukuNikah = it }
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = namaAyah,
                                onValueChange = { namaAyah = it },
                                label = { Text("17. NAMA AYAH") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = namaIbu,
                                onValueChange = { namaIbu = it },
                                label = { Text("18. NAMA IBU") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = alamat,
                                onValueChange = { alamat = it },
                                label = { Text("21. ALAMAT LENGKAP / DUSUN") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = rw,
                                    onValueChange = { input -> rw = input.filter { it.isDigit() }.take(3) },
                                    label = { Text("22. RW") },
                                    placeholder = { Text("001") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = rt,
                                    onValueChange = { input -> rt = input.filter { it.isDigit() }.take(3) },
                                    label = { Text("23. RT") },
                                    placeholder = { Text("001") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = anakKe.toString(),
                                    onValueChange = { anakKe = it.toIntOrNull() ?: 1 },
                                    label = { Text("42. ANAK KE") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        item {
                            OutlinedTextField(
                                value = noHandphone,
                                onValueChange = { noHandphone = it },
                                label = { Text("39. NO HANDPHONE / WHATSAPP") },
                                placeholder = { Text("Contoh: 081234567890") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // TAB 3: PROFESI & DOKUMEN LAIN
                    2 -> {
                        item {
                            DropdownSelector(
                                label = "8. PENDIDIKAN TERAKHIR",
                                selectedValue = pendidikanTerakhir,
                                options = Penduduk.PENDIDIKAN_OPTIONS,
                                onSelect = { pendidikanTerakhir = it }
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = pekerjaan,
                                onValueChange = { pekerjaan = it },
                                label = { Text("9. PEKERJAAN") },
                                placeholder = { Text("PETANI / WIRASWASTA / PNS") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = usahaYangDijalankan,
                                onValueChange = { usahaYangDijalankan = it },
                                label = { Text("36. USAHA YANG DIJALANKAN") },
                                placeholder = { Text("Contoh: Warung Kelontong / Peternakan") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = noHandphone,
                                onValueChange = { noHandphone = it },
                                label = { Text("41. NO HANDPHONE / WHATSAPP") },
                                placeholder = { Text("0812-xxxx-xxxx") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = noPaspor,
                                onValueChange = { noPaspor = it },
                                label = { Text("15. NO. PASPOR (Jika Ada)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = noKitas,
                                onValueChange = { noKitas = it },
                                label = { Text("16. NO KITAS (WNA)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // TAB 4: DOKUMEN & BANSOS
                    3 -> {
                        item {
                            DropdownSelector(
                                label = "27. KEPEMILIKAN E-KTP",
                                selectedValue = kepemilikanEKtp,
                                options = Penduduk.EKTP_OPTIONS,
                                onSelect = { kepemilikanEKtp = it }
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = tanggalPencetakan,
                                onValueChange = { tanggalPencetakan = it },
                                label = { Text("28. TANGGAL PENCETAKAN E-KTP") },
                                placeholder = { Text("YYYY-MM-DD") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "29. KEPEMILIKAN AKTA KELAHIRAN",
                                selectedValue = kepemilikanAktaKelahiran,
                                options = Penduduk.AKTA_OPTIONS,
                                onSelect = { kepemilikanAktaKelahiran = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "30. KARTU KIA (Kartu Identitas Anak)",
                                selectedValue = kartuKia,
                                options = Penduduk.KIA_OPTIONS,
                                onSelect = { kartuKia = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "31. KARTU PKH (Program Keluarga Harapan)",
                                selectedValue = kartuPkh,
                                options = Penduduk.YES_NO_OPTIONS,
                                onSelect = { kartuPkh = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "32. KARTU BPNT (Bantuan Pangan Non Tunai)",
                                selectedValue = kartuBpnt,
                                options = Penduduk.YES_NO_OPTIONS,
                                onSelect = { kartuBpnt = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "33. KARTU BPJS/KIS",
                                selectedValue = kartuBpjsKis,
                                options = Penduduk.BPJS_OPTIONS,
                                onSelect = { kartuBpjsKis = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "34. KARTU KIP (Indonesia Pintar)",
                                selectedValue = kartuKip,
                                options = Penduduk.YES_NO_OPTIONS,
                                onSelect = { kartuKip = it }
                            )
                        }
                    }

                    // TAB 5: HUNIAN & ENERGI LISTRIK
                    4 -> {
                        item {
                            DropdownSelector(
                                label = "43. KEPEMILIKAN RUMAH",
                                selectedValue = kepemilikanRumah,
                                options = Penduduk.KEPEMILIKAN_RUMAH_OPTIONS,
                                onSelect = { kepemilikanRumah = it }
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = ukuranRumah,
                                onValueChange = { ukuranRumah = it },
                                label = { Text("44. UKURAN RUMAH") },
                                placeholder = { Text("Contoh: 6x10 m / 60 m²") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "45. JENIS RUMAH",
                                selectedValue = jenisRumah,
                                options = Penduduk.JENIS_RUMAH_OPTIONS,
                                onSelect = { jenisRumah = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "37. LISTRIK (TOKEN/ PASCA BAYAR)",
                                selectedValue = listrikJenis,
                                options = Penduduk.LISTRIK_JENIS_OPTIONS,
                                onSelect = { listrikJenis = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "38. KEPEMILIKAN LISTRIK",
                                selectedValue = kepemilikanListrik,
                                options = Penduduk.KEPEMILIKAN_LISTRIK_OPTIONS,
                                onSelect = { kepemilikanListrik = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "39. DAYA LISTRIK",
                                selectedValue = dayaListrik,
                                options = Penduduk.DAYA_LISTRIK_OPTIONS,
                                onSelect = { dayaListrik = it }
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = noTokenKwh,
                                onValueChange = { noTokenKwh = it },
                                label = { Text("40. NO TOKEN / KWH METER") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // TAB 6: KESEHATAN, KB & DISABILITAS
                    5 -> {
                        item {
                            DropdownSelector(
                                label = "35. JENIS KB",
                                selectedValue = jenisKb,
                                options = Penduduk.KB_OPTIONS,
                                onSelect = { jenisKb = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "47. VAKSINASI COVID / IMUNISASI",
                                selectedValue = vaksinasi,
                                options = Penduduk.VAKSIN_OPTIONS,
                                onSelect = { vaksinasi = it }
                            )
                        }
                        item {
                            DropdownSelector(
                                label = "48. DISABILITAS",
                                selectedValue = disabilitas,
                                options = Penduduk.DISABILITAS_OPTIONS,
                                onSelect = { disabilitas = it }
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = keterangan,
                                onValueChange = { keterangan = it },
                                label = { Text("46. KETERANGAN") },
                                placeholder = { Text("Catatan kependudukan lainnya") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Next / Previous Tab Navigation buttons
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (selectedTab > 0) {
                            Button(
                                onClick = { selectedTab -= 1 },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text("Sebelumnya", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        if (selectedTab < tabTitles.size - 1) {
                            Button(
                                onClick = { selectedTab += 1 },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text("Lanjut", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (nik.isBlank() || nama.isBlank() || noKk.isBlank()) {
                                        validationError = "Mohon lengkapi NIK, Nama Lengkap, dan No KK!"
                                        return@Button
                                    }
                                    val autoAge = Penduduk.calculateAge(tanggalLahir)
                                    val finalResident = Penduduk(
                                        no = no,
                                        nama = nama.trim().uppercase(),
                                        nik = nik.trim(),
                                        jenisKelamin = jenisKelamin,
                                        tempatLahir = tempatLahir.trim().uppercase(),
                                        tanggalLahir = tanggalLahir,
                                        agama = agama,
                                        pendidikanTerakhir = pendidikanTerakhir,
                                        pekerjaan = pekerjaan.trim().uppercase(),
                                        gdr = gdr,
                                        statusPerkawinan = statusPerkawinan,
                                        bukuNikah = bukuNikah,
                                        shdk = shdk,
                                        kewarganegaraan = kewarganegaraan,
                                        noPaspor = noPaspor.trim(),
                                        noKitas = noKitas.trim(),
                                        namaAyah = namaAyah.trim().uppercase(),
                                        namaIbu = namaIbu.trim().uppercase(),
                                        noKk = noKk.trim(),
                                        namaKepalaKeluarga = namaKepalaKeluarga.trim().uppercase(),
                                        alamat = alamat.trim(),
                                        rw = Penduduk.formatRtRw(rw),
                                        rt = Penduduk.formatRtRw(rt),
                                        umur = autoAge,
                                        umurLakiLaki = if (jenisKelamin == "LAKI-LAKI") "$autoAge Tahun" else "-",
                                        umurPerempuan = if (jenisKelamin == "PEREMPUAN") "$autoAge Tahun" else "-",
                                        kepemilikanEKtp = kepemilikanEKtp,
                                        tanggalPencetakan = tanggalPencetakan,
                                        kepemilikanAktaKelahiran = kepemilikanAktaKelahiran,
                                        kartuKia = kartuKia,
                                        kartuPkh = kartuPkh,
                                        kartuBpnt = kartuBpnt,
                                        kartuBpjsKis = kartuBpjsKis,
                                        kartuKip = kartuKip,
                                        jenisKb = jenisKb,
                                        usahaYangDijalankan = usahaYangDijalankan.trim(),
                                        listrikJenis = listrikJenis,
                                        kepemilikanListrik = kepemilikanListrik,
                                        dayaListrik = dayaListrik,
                                        noTokenKwh = noTokenKwh.trim(),
                                        noHandphone = noHandphone.trim(),
                                        anakKe = anakKe,
                                        kepemilikanRumah = kepemilikanRumah,
                                        ukuranRumah = ukuranRumah.trim(),
                                        jenisRumah = jenisRumah,
                                        keterangan = keterangan.trim(),
                                        vaksinasi = vaksinasi,
                                        disabilitas = disabilitas,
                                        syncedWithSheets = false
                                    )

                                    viewModel.savePenduduk(finalResident, isEdit, oldNik = nikToEdit) {
                                        viewModel.navigateTo(Screen.PendudukList)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEdit) "Simpan Perubahan" else "Simpan Penduduk", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
