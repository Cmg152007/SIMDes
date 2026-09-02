package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.AppNotification
import com.example.data.model.AppUpdateInfo
import com.example.data.model.Penduduk
import com.example.data.model.PendudukDocument
import com.example.data.model.UserProfile
import com.example.data.repository.PendudukRepository
import com.example.util.OtaUpdateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Dashboard : Screen()
    object PendudukList : Screen()
    data class PendudukDetail(val nik: String) : Screen()
    data class PendudukForm(val nik: String? = null, val initialNoKk: String? = null) : Screen()
    object ActivityLogs : Screen()
    object LaporanBulanan : Screen()
    object PusatAnalisis : Screen()
    object Profile : Screen()
    object Settings : Screen()
}

data class DashboardStats(
    val totalPenduduk: Int = 0,
    val totalAktif: Int = 0,
    val totalMeninggal: Int = 0,
    val totalPindah: Int = 0,
    val totalKk: Int = 0,
    val totalPendingSync: Int = 0,
    val totalLakiLaki: Int = 0,
    val totalPerempuan: Int = 0,
    val totalBalita: Int = 0,
    val totalAnak: Int = 0,
    val totalProduktif: Int = 0,
    val totalLansia: Int = 0,
    val totalPkh: Int = 0,
    val totalBpnt: Int = 0,
    val totalBpjsKis: Int = 0,
    val totalKip: Int = 0,
    val totalWajibKtp: Int = 0,
    val totalSudahKtp: Int = 0,
    val totalBelumKtp: Int = 0,
    val totalDisabilitas: Int = 0,
    val totalHakPilih: Int = 0,
    val kelengkapanDataPercent: Int = 100,
    val rtDistribution: Map<String, Int> = emptyMap(),
    val rwDistribution: Map<String, Int> = emptyMap(),
    val dusunDistribution: Map<String, Int> = emptyMap(),
    val agamaDistribution: Map<String, Int> = emptyMap(),
    val pekerjaanDistribution: Map<String, Int> = emptyMap(),
    val pendidikanDistribution: Map<String, Int> = emptyMap(),
    val statusKawinDistribution: Map<String, Int> = emptyMap(),
    val golonganDarahDistribution: Map<String, Int> = emptyMap()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PendudukRepository
    
    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = PendudukRepository(
            context = application,
            pendudukDao = db.pendudukDao(),
            activityLogDao = db.activityLogDao(),
            notificationDao = db.notificationDao(),
            documentDao = db.documentDao()
        )

        // Security Inactivity Ticker (3 Menit idle -> 10 Detik Countdown -> Auto Lock)
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (_isAuthenticated.value) {
                    if (_showInactivityWarning.value) {
                        val currentCount = _inactivityCountdown.value
                        if (currentCount <= 1) {
                            _showInactivityWarning.value = false
                            _inactivityCountdown.value = COUNTDOWN_TOTAL_SECONDS
                            logout()
                            repository.recordSecurityLockLog(
                                _userProfile.value.namaPetugas,
                                "Sesi aplikasi terkunci otomatis karena tidak ada aktivitas selama 3 menit."
                            )
                        } else {
                            _inactivityCountdown.value = currentCount - 1
                        }
                    } else {
                        val idleDuration = System.currentTimeMillis() - lastActivityTimestamp
                        if (idleDuration >= INACTIVITY_TIMEOUT_MS) {
                            _inactivityCountdown.value = COUNTDOWN_TOTAL_SECONDS
                            _showInactivityWarning.value = true
                        }
                    }
                }
            }
        }
    }

    // Navigation & Auth State
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Google Spreadsheet Modal State
    private val _showSpreadsheetModal = MutableStateFlow(false)
    val showSpreadsheetModal: StateFlow<Boolean> = _showSpreadsheetModal.asStateFlow()

    // Notification Drawer State
    private val _isNotificationSheetOpen = MutableStateFlow(false)
    val isNotificationSheetOpen: StateFlow<Boolean> = _isNotificationSheetOpen.asStateFlow()

    // Filter & Search states for Penduduk List
    val searchQuery = MutableStateFlow("")
    val filterRw = MutableStateFlow("SEMUA")
    val filterRt = MutableStateFlow("SEMUA")
    val filterGender = MutableStateFlow("SEMUA")
    val filterBansos = MutableStateFlow("SEMUA")
    val filterDisabilitas = MutableStateFlow("SEMUA")
    val filterMutasi = MutableStateFlow("SEMUA") // SEMUA, AKTIF, MENINGGAL, PINDAH
    val sortBy = MutableStateFlow("NO") // NO, NAMA, NIK, UMUR

    // Data streams
    val allPenduduk: StateFlow<List<Penduduk>> = repository.allPenduduk
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<ActivityLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<AppNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = repository.unreadNotificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // User Profile
    private val _userProfile = MutableStateFlow(repository.getUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Sync & Operation status
    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // PIN Protection State
    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    private var onPinSuccessCallback: (() -> Unit)? = null

    // Inactivity & Background Security State (3 Menit Inactivity -> 10 Detik Countdown -> Kunci)
    private val _showInactivityWarning = MutableStateFlow(false)
    val showInactivityWarning: StateFlow<Boolean> = _showInactivityWarning.asStateFlow()

    private val _inactivityCountdown = MutableStateFlow(10)
    val inactivityCountdown: StateFlow<Int> = _inactivityCountdown.asStateFlow()

    // Screenshot Watermark Notice State
    private val _screenshotWatermarkNotice = MutableStateFlow<String?>(null)
    val screenshotWatermarkNotice: StateFlow<String?> = _screenshotWatermarkNotice.asStateFlow()

    // OTA GitHub Update State
    private val otaUpdateManager = OtaUpdateManager(application)
    private val _showOtaDialog = MutableStateFlow(false)
    val showOtaDialog: StateFlow<Boolean> = _showOtaDialog.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _isDownloadingUpdate = MutableStateFlow(false)
    val isDownloadingUpdate: StateFlow<Boolean> = _isDownloadingUpdate.asStateFlow()

    private val _updateProgress = MutableStateFlow(0f)
    val updateProgress: StateFlow<Float> = _updateProgress.asStateFlow()

    private val _updateDownloadedBytes = MutableStateFlow(0L)
    val updateDownloadedBytes: StateFlow<Long> = _updateDownloadedBytes.asStateFlow()

    private val _updateTotalBytes = MutableStateFlow(0L)
    val updateTotalBytes: StateFlow<Long> = _updateTotalBytes.asStateFlow()

    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo.asStateFlow()

    private val _updateError = MutableStateFlow<String?>(null)
    val updateError: StateFlow<String?> = _updateError.asStateFlow()

    private var lastDownloadedApkFile: java.io.File? = null

    private var lastActivityTimestamp: Long = System.currentTimeMillis()
    private var appBackgroundTimestamp: Long = 0L

    companion object {
        const val INACTIVITY_TIMEOUT_MS = 3 * 60 * 1000L // 3 Menit (180 Detik)
        const val BACKGROUND_TIMEOUT_MS = 3 * 60 * 1000L // 3 Menit di background
        const val COUNTDOWN_TOTAL_SECONDS = 10
    }

    data class FilterParams(
        val query: String,
        val rw: String,
        val rt: String,
        val gender: String,
        val bansos: String,
        val disabilitas: String,
        val mutasi: String,
        val sort: String
    )

    private val filterParams: Flow<FilterParams> = combine(
        searchQuery,
        filterRw,
        filterRt,
        filterGender
    ) { q: String, rw: String, rt: String, g: String ->
        listOf(q, rw, rt, g)
    }.combine(
        combine(filterBansos, filterDisabilitas, filterMutasi, sortBy) { b: String, d: String, m: String, s: String ->
            listOf(b, d, m, s)
        }
    ) { f1: List<String>, f2: List<String> ->
        FilterParams(
            query = f1[0],
            rw = f1[1],
            rt = f1[2],
            gender = f1[3],
            bansos = f2[0],
            disabilitas = f2[1],
            mutasi = f2[2],
            sort = f2[3]
        )
    }

    // Combined filtered list (Respects User Wilayah Kerja Scope & RT Filters)
    val filteredPenduduk: StateFlow<List<Penduduk>> = combine(
        allPenduduk,
        filterParams,
        _userProfile
    ) { list: List<Penduduk>, params: FilterParams, profile: UserProfile ->
        val isWilayahActive = profile.wilayahKerja.isNotBlank() && !profile.wilayahKerja.equals("Semua Wilayah", ignoreCase = true)
        val cleanWilayah = profile.wilayahKerja.replace("Dusun", "", ignoreCase = true).trim()
        val wilayahRtNumbers = if (isWilayahActive) UserProfile.getRtListForWilayah(cleanWilayah).mapNotNull { it.toIntOrNull() } else emptyList()
        val wilayahRwNumber = UserProfile.getRwNumberForWilayah(cleanWilayah)
        val wilayahNameLower = cleanWilayah.lowercase()

        list.filter { p ->
            // 1. Wilayah Kerja Auto-scoping
            val matchWilayahKerja = if (isWilayahActive) {
                val resRtNum = p.rt.filter { it.isDigit() }.toIntOrNull()
                val resRwNum = p.rw.filter { it.isDigit() }.toIntOrNull()

                val rtMatched = resRtNum != null && wilayahRtNumbers.contains(resRtNum)
                val rwMatched = wilayahRwNumber != null && resRwNum != null && resRwNum == wilayahRwNumber
                val alamatMatched = wilayahNameLower.isNotBlank() && p.alamat.lowercase().contains(wilayahNameLower)

                rtMatched || rwMatched || alamatMatched
            } else {
                true // Wilayah Kerja kosong / Semua Wilayah -> tampilkan semua data
            }

            // 2. Query Search
            val matchQuery = params.query.isBlank() ||
                    p.nama.contains(params.query, ignoreCase = true) ||
                    p.nik.contains(params.query, ignoreCase = true) ||
                    p.noKk.contains(params.query, ignoreCase = true) ||
                    p.alamat.contains(params.query, ignoreCase = true) ||
                    p.pekerjaan.contains(params.query, ignoreCase = true)

            // 3. RW Manual Filter (if specified)
            val matchRw = if (params.rw == "SEMUA") true else {
                val targetRwNum = params.rw.filter { it.isDigit() }.toIntOrNull()
                val resRwNum = p.rw.filter { it.isDigit() }.toIntOrNull()
                if (targetRwNum != null && resRwNum != null) resRwNum == targetRwNum else p.rw.equals(params.rw, ignoreCase = true)
            }

            // 4. RT Manual Filter
            val matchRt = if (params.rt == "SEMUA") true else {
                val targetRtNum = params.rt.filter { it.isDigit() }.toIntOrNull()
                val resRtNum = p.rt.filter { it.isDigit() }.toIntOrNull()
                if (targetRtNum != null && resRtNum != null) resRtNum == targetRtNum else p.rt.equals(params.rt, ignoreCase = true)
            }

            // 5. Gender Filter
            val matchGender = when (params.gender) {
                "SEMUA" -> true
                "LAKI-LAKI" -> p.isMale()
                "PEREMPUAN" -> p.isFemale()
                else -> p.jenisKelamin.equals(params.gender, ignoreCase = true)
            }

            // 6. Bansos Filter
            val matchBansos = when (params.bansos) {
                "SEMUA" -> true
                "PENERIMA BANSOS" -> p.isPenerimaBansos()
                "BUKAN PENERIMA" -> !p.isPenerimaBansos()
                "PKH" -> p.kartuPkh.equals("YA", ignoreCase = true)
                "BPNT" -> p.kartuBpnt.equals("YA", ignoreCase = true)
                "KIS / PBI" -> p.kartuBpjsKis.contains("PBI", ignoreCase = true)
                "KIP" -> p.kartuKip.equals("YA", ignoreCase = true)
                else -> true
            }

            // 7. Disabilitas Filter
            val matchDisabilitas = when (params.disabilitas) {
                "SEMUA" -> true
                "ADA DISABILITAS" -> p.hasDisabilitas()
                "TIDAK ADA" -> !p.hasDisabilitas()
                else -> p.disabilitas.equals(params.disabilitas, ignoreCase = true)
            }

            // 8. Mutasi Status Filter
            val matchMutasi = when (params.mutasi) {
                "SEMUA" -> true
                "AKTIF" -> p.isAktif()
                "MENINGGAL" -> p.isMeninggal()
                "PINDAH" -> p.isPindah()
                else -> true
            }

            matchWilayahKerja && matchQuery && matchRw && matchRt && matchGender && matchBansos && matchDisabilitas && matchMutasi
        }.let { filtered ->
            when (params.sort) {
                "NAMA" -> filtered.sortedBy { it.nama }
                "NIK" -> filtered.sortedBy { it.nik }
                "UMUR" -> filtered.sortedByDescending { it.getEffectiveAge() }
                "RT" -> filtered.sortedWith(compareBy({ it.rw }, { it.rt }, { it.no }))
                else -> filtered.sortedBy { it.no }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics Calculation - Scoped to Active Wilayah Kerja
    val dashboardStats: StateFlow<DashboardStats> = combine(
        allPenduduk,
        _userProfile
    ) { list, profile ->
        val isWilayahActive = profile.wilayahKerja.isNotBlank() && !profile.wilayahKerja.equals("Semua Wilayah", ignoreCase = true)
        val cleanWilayah = profile.wilayahKerja.replace("Dusun", "", ignoreCase = true).trim()
        val wilayahRtNumbers = if (isWilayahActive) UserProfile.getRtListForWilayah(cleanWilayah).mapNotNull { it.toIntOrNull() } else emptyList()
        val wilayahRwNumber = UserProfile.getRwNumberForWilayah(cleanWilayah)
        val wilayahNameLower = cleanWilayah.lowercase()

        val scopedList = if (isWilayahActive) {
            list.filter { p ->
                val resRtNum = p.rt.filter { it.isDigit() }.toIntOrNull()
                val resRwNum = p.rw.filter { it.isDigit() }.toIntOrNull()

                val rtMatched = resRtNum != null && wilayahRtNumbers.contains(resRtNum)
                val rwMatched = wilayahRwNumber != null && resRwNum != null && resRwNum == wilayahRwNumber
                val alamatMatched = wilayahNameLower.isNotBlank() && p.alamat.lowercase().contains(wilayahNameLower)

                rtMatched || rwMatched || alamatMatched
            }
        } else {
            list
        }

        val total = scopedList.size
        val totalAktif = scopedList.count { it.isAktif() }
        val totalMeninggal = scopedList.count { it.isMeninggal() }
        val totalPindah = scopedList.count { it.isPindah() }
        val totalKk = scopedList.map { it.noKk }.filter { it.isNotBlank() }.distinct().size
        val male = scopedList.count { it.isMale() }
        val female = scopedList.count { it.isFemale() }
        val balita = scopedList.count { it.getEffectiveAge() in 0..5 }
        val anak = scopedList.count { it.getEffectiveAge() in 6..17 }
        val produktif = scopedList.count { it.getEffectiveAge() in 18..59 }
        val lansia = scopedList.count { it.getEffectiveAge() >= 60 }
        val pkh = scopedList.count { it.kartuPkh.equals("YA", ignoreCase = true) }
        val bpnt = scopedList.count { it.kartuBpnt.equals("YA", ignoreCase = true) }
        val bpjsKis = scopedList.count { it.kartuBpjsKis.contains("PBI", ignoreCase = true) }
        val kip = scopedList.count { it.kartuKip.equals("YA", ignoreCase = true) }
        val wajibKtp = scopedList.count { it.getEffectiveAge() >= 17 }
        val sudahKtp = scopedList.count { it.kepemilikanEKtp.contains("SUDAH", ignoreCase = true) }
        val belumKtp = scopedList.count { it.getEffectiveAge() >= 17 && !it.kepemilikanEKtp.contains("SUDAH", ignoreCase = true) }
        val disabilitas = scopedList.count { it.hasDisabilitas() }
        val hakPilih = scopedList.count { it.getEffectiveAge() >= 17 || !it.statusPerkawinan.contains("BELUM", ignoreCase = true) }
        val lengkapCount = scopedList.count { it.nik.length == 16 && it.noKk.isNotBlank() && it.nama.isNotBlank() }
        val kelengkapanPersen = if (total > 0) ((lengkapCount.toFloat() / total) * 100).toInt() else 100

        val rtMap = scopedList.groupingBy { "RT ${it.rt.ifBlank { "001" }}" }.eachCount()
        val rwMap = scopedList.groupingBy { "RW ${it.rw.ifBlank { "001" }}" }.eachCount()

        val dusunMap = scopedList.groupingBy { p ->
            val lower = p.alamat.lowercase()
            when {
                lower.contains("cibubuay") -> "Dusun Cibubuay"
                lower.contains("sundawenang") -> "Dusun Sundawenang"
                lower.contains("cimanggu") -> "Dusun Cimanggu"
                lower.contains("mekarlaksana") -> "Dusun Mekarlaksana"
                lower.contains("mekarjaya") -> "Dusun Mekarjaya"
                p.alamat.isNotBlank() -> p.alamat
                else -> "Dusun Cimanggu"
            }
        }.eachCount().toList().sortedByDescending { it.second }.toMap()

        val agamaMap = scopedList.groupingBy { it.agama.ifBlank { "ISLAM" } }
            .eachCount().toList().sortedByDescending { it.second }.toMap()

        val pekerjaanMap = scopedList.groupingBy { it.pekerjaan.ifBlank { "WIRASWASTA" } }
            .eachCount().toList().sortedByDescending { it.second }.take(6).toMap()

        val pendidikanMap = scopedList.groupingBy { it.pendidikanTerakhir.ifBlank { "SLTA / SEDERAJAT" } }
            .eachCount().toList().sortedByDescending { it.second }.take(6).toMap()

        val statusKawinMap = scopedList.groupingBy { it.statusPerkawinan.ifBlank { "BELUM KAWIN" } }
            .eachCount().toList().sortedByDescending { it.second }.toMap()

        val gdrMap = scopedList.groupingBy { it.gdr.ifBlank { "TIDAK TAHU" } }
            .eachCount().toList().sortedByDescending { it.second }.toMap()

        val totalPendingSync = scopedList.count { !it.syncedWithSheets }

        DashboardStats(
            totalPenduduk = total,
            totalAktif = totalAktif,
            totalMeninggal = totalMeninggal,
            totalPindah = totalPindah,
            totalKk = totalKk,
            totalPendingSync = totalPendingSync,
            totalLakiLaki = male,
            totalPerempuan = female,
            totalBalita = balita,
            totalAnak = anak,
            totalProduktif = produktif,
            totalLansia = lansia,
            totalPkh = pkh,
            totalBpnt = bpnt,
            totalBpjsKis = bpjsKis,
            totalKip = kip,
            totalWajibKtp = wajibKtp,
            totalSudahKtp = sudahKtp,
            totalBelumKtp = belumKtp,
            totalDisabilitas = disabilitas,
            totalHakPilih = hakPilih,
            kelengkapanDataPercent = kelengkapanPersen,
            rtDistribution = rtMap,
            rwDistribution = rwMap,
            dusunDistribution = dusunMap,
            agamaDistribution = agamaMap,
            pekerjaanDistribution = pekerjaanMap,
            pendidikanDistribution = pendidikanMap,
            statusKawinDistribution = statusKawinMap,
            golonganDarahDistribution = gdrMap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    private val _isBiometricEnabled = MutableStateFlow(repository.isBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    fun setBiometricEnabled(enabled: Boolean) {
        repository.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
        _userProfile.value = repository.getUserProfile()
    }

    fun loginWithBiometric(): Boolean {
        _isAuthenticated.value = true
        _loginError.value = null
        lastActivityTimestamp = System.currentTimeMillis()
        _showInactivityWarning.value = false
        _inactivityCountdown.value = COUNTDOWN_TOTAL_SECONDS
        return true
    }

    fun loginWithPin(pin: String): Boolean {
        if (repository.verifyLoginPin(pin)) {
            _isAuthenticated.value = true
            _loginError.value = null
            lastActivityTimestamp = System.currentTimeMillis()
            _showInactivityWarning.value = false
            _inactivityCountdown.value = COUNTDOWN_TOTAL_SECONDS
            return true
        } else {
            _loginError.value = "PIN salah! Masukkan PIN yang benar"
            return false
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun onUserActivity() {
        if (!_showInactivityWarning.value) {
            lastActivityTimestamp = System.currentTimeMillis()
        }
    }

    fun stayLoggedIn() {
        _showInactivityWarning.value = false
        _inactivityCountdown.value = COUNTDOWN_TOTAL_SECONDS
        lastActivityTimestamp = System.currentTimeMillis()
    }

    fun lockNow() {
        _showInactivityWarning.value = false
        _inactivityCountdown.value = COUNTDOWN_TOTAL_SECONDS
        logout()
        viewModelScope.launch {
            repository.recordSecurityLockLog(
                _userProfile.value.namaPetugas,
                "Sesi aplikasi dikunci secara manual oleh pengguna."
            )
        }
    }

    fun onAppBackgrounded() {
        if (_isAuthenticated.value) {
            appBackgroundTimestamp = System.currentTimeMillis()
        }
    }

    fun onAppForegrounded(): Boolean {
        if (_isAuthenticated.value && appBackgroundTimestamp > 0L) {
            val backgroundDuration = System.currentTimeMillis() - appBackgroundTimestamp
            appBackgroundTimestamp = 0L
            if (backgroundDuration >= BACKGROUND_TIMEOUT_MS) {
                logout()
                viewModelScope.launch {
                    repository.recordSecurityLockLog(
                        _userProfile.value.namaPetugas,
                        "Sesi aplikasi terkunci otomatis setelah berada di luar layar (background) selama lebih dari 3 menit."
                    )
                }
                return true
            } else {
                lastActivityTimestamp = System.currentTimeMillis()
            }
        }
        return false
    }

    fun logScreenshot(screenName: String, accountName: String, timeFormatted: String) {
        viewModelScope.launch {
            _screenshotWatermarkNotice.value = "$accountName • $timeFormatted WIB"
            repository.recordScreenshotLog(accountName, screenName)
            delay(4000L)
            _screenshotWatermarkNotice.value = null
        }
    }

    fun dismissScreenshotWatermark() {
        _screenshotWatermarkNotice.value = null
    }

    fun logout() {
        _isAuthenticated.value = false
        _showInactivityWarning.value = false
        _inactivityCountdown.value = COUNTDOWN_TOTAL_SECONDS
        _currentScreen.value = Screen.Dashboard
    }

    fun changeLoginPin(oldPin: String, newPin: String): Result<String> {
        if (!repository.verifyLoginPin(oldPin)) {
            return Result.failure(Exception("PIN Lama salah!"))
        }
        if (newPin.trim().length < 4) {
            return Result.failure(Exception("PIN baru minimal 4 angka!"))
        }
        repository.saveLoginPin(newPin.trim())
        return Result.success("PIN Login berhasil diperbarui!")
    }

    fun openSpreadsheetModal() {
        _showSpreadsheetModal.value = true
    }

    fun closeSpreadsheetModal() {
        _showSpreadsheetModal.value = false
    }

    private val backStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen, addToBackStack: Boolean = true) {
        if (_currentScreen.value == screen) return
        if (addToBackStack) {
            val current = _currentScreen.value
            if (backStack.isEmpty() || backStack.last() != current) {
                backStack.add(current)
            }
        }
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (_showPinDialog.value) {
            _showPinDialog.value = false
            return true
        }
        if (_showSpreadsheetModal.value) {
            _showSpreadsheetModal.value = false
            return true
        }
        if (_isNotificationSheetOpen.value) {
            _isNotificationSheetOpen.value = false
            return true
        }

        while (backStack.isNotEmpty()) {
            val prev = backStack.removeAt(backStack.size - 1)
            if (prev != _currentScreen.value) {
                _currentScreen.value = prev
                return true
            }
        }

        // If backStack is empty and not on Dashboard, return to Dashboard
        if (_currentScreen.value !is Screen.Dashboard) {
            _currentScreen.value = Screen.Dashboard
            return true
        }

        return false
    }

    fun filterByGender(gender: String) {
        filterGender.value = gender
        searchQuery.value = ""
        filterBansos.value = "SEMUA"
        filterDisabilitas.value = "SEMUA"
        _currentScreen.value = Screen.PendudukList
    }

    fun filterByBansos(bansos: String) {
        filterBansos.value = bansos
        searchQuery.value = ""
        filterGender.value = "SEMUA"
        filterDisabilitas.value = "SEMUA"
        _currentScreen.value = Screen.PendudukList
    }

    fun filterByDisabilitas(disabilitas: String) {
        filterDisabilitas.value = disabilitas
        searchQuery.value = ""
        filterGender.value = "SEMUA"
        filterBansos.value = "SEMUA"
        filterMutasi.value = "SEMUA"
        _currentScreen.value = Screen.PendudukList
    }

    fun filterByMutasi(mutasi: String) {
        filterMutasi.value = mutasi
        searchQuery.value = ""
        filterGender.value = "SEMUA"
        filterBansos.value = "SEMUA"
        filterDisabilitas.value = "SEMUA"
        _currentScreen.value = Screen.PendudukList
    }

    fun filterByCustomQuery(query: String) {
        searchQuery.value = query
        filterGender.value = "SEMUA"
        filterBansos.value = "SEMUA"
        filterDisabilitas.value = "SEMUA"
        filterRw.value = "SEMUA"
        filterRt.value = "SEMUA"
        _currentScreen.value = Screen.PendudukList
    }

    fun filterByRwRt(rw: String, rt: String) {
        filterRw.value = rw
        filterRt.value = rt
        searchQuery.value = ""
        filterGender.value = "SEMUA"
        filterBansos.value = "SEMUA"
        filterDisabilitas.value = "SEMUA"
        _currentScreen.value = Screen.PendudukList
    }

    fun filterByRt(rt: String) {
        filterRt.value = rt
        searchQuery.value = ""
        filterGender.value = "SEMUA"
        filterBansos.value = "SEMUA"
        filterDisabilitas.value = "SEMUA"
        _currentScreen.value = Screen.PendudukList
    }

    fun filterByDusun(dusun: String) {
        val clean = dusun.replace("Dusun", "", ignoreCase = true).trim()
        updateActiveWilayahKerja(clean)
        resetFiltersAndNavigateToList()
    }

    fun resetFiltersAndNavigateToList() {
        filterGender.value = "SEMUA"
        searchQuery.value = ""
        filterBansos.value = "SEMUA"
        filterDisabilitas.value = "SEMUA"
        filterRw.value = "SEMUA"
        filterRt.value = "SEMUA"
        _currentScreen.value = Screen.PendudukList
    }

    fun openNotificationSheet() {
        _isNotificationSheetOpen.value = true
    }

    fun closeNotificationSheet() {
        _isNotificationSheetOpen.value = false
    }

    // PIN Authentication Flow for Google Apps Script URL setup
    fun requestPinProtectedAction(onSuccess: () -> Unit) {
        onPinSuccessCallback = onSuccess
        _pinError.value = null
        _showPinDialog.value = true
    }

    fun verifyEnteredPin(pin: String): Boolean {
        if (repository.verifyPin(pin)) {
            _showPinDialog.value = false
            _pinError.value = null
            onPinSuccessCallback?.invoke()
            onPinSuccessCallback = null
            return true
        } else {
            _pinError.value = "PIN Otorisasi salah! Silakan coba lagi."
            return false
        }
    }

    fun dismissPinDialog() {
        _showPinDialog.value = false
        _pinError.value = null
        onPinSuccessCallback = null
    }

    fun verifySecurityPin(pin: String): Boolean {
        return repository.verifySecurityPin(pin)
    }

    fun saveAppsScriptUrl(url: String) {
        repository.saveAppsScriptUrl(url)
        _userProfile.value = repository.getUserProfile()
        testConnection()
    }

    fun saveUserProfile(profile: UserProfile) {
        repository.saveUserProfile(profile)
        _userProfile.value = repository.getUserProfile()
    }

    fun updateActiveWilayahKerja(wilayah: String) {
        val current = _userProfile.value
        val updated = current.copy(wilayahKerja = wilayah)
        saveUserProfile(updated)
    }

    fun testConnection() {
        val url = repository.getAppsScriptUrl()
        if (url.isBlank()) {
            _syncStatusMessage.value = "URL Apps Script belum diisi."
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatusMessage.value = "Menguji koneksi ke Google Apps Script..."
            val res = repository.testAppsScriptConnection(url)
            _isSyncing.value = false
            _syncStatusMessage.value = if (res.isSuccess) res.getOrThrow() else res.exceptionOrNull()?.message
        }
    }

    fun pullDataFromSpreadsheet() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatusMessage.value = "Mengunduh data kependudukan dari Google Spreadsheet..."
            val result = repository.pullDataFromSpreadsheet(_userProfile.value.namaPetugas)
            _isSyncing.value = false
            if (result.isSuccess) {
                _syncStatusMessage.value = result.getOrThrow()
            } else {
                _syncStatusMessage.value = "Tarik data gagal: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun syncWithSpreadsheet() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatusMessage.value = "Menghubungkan & menyinkronkan data dengan Google Spreadsheet..."
            val result = repository.syncWithSpreadsheet(_userProfile.value.namaPetugas)
            _isSyncing.value = false
            if (result.isSuccess) {
                _syncStatusMessage.value = result.getOrThrow()
            } else {
                _syncStatusMessage.value = "Sinkronisasi gagal: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearSyncStatusMessage() {
        _syncStatusMessage.value = null
    }

    fun savePenduduk(penduduk: Penduduk, isEdit: Boolean, oldNik: String? = null, onComplete: () -> Unit) {
        viewModelScope.launch {
            val res = if (isEdit) {
                repository.updatePenduduk(penduduk, _userProfile.value.namaPetugas, oldNik)
            } else {
                repository.insertPenduduk(penduduk, _userProfile.value.namaPetugas)
            }
            if (res.isSuccess) {
                onComplete()
            } else {
                _syncStatusMessage.value = "Gagal menyimpan: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun deletePenduduk(penduduk: Penduduk, onComplete: () -> Unit) {
        viewModelScope.launch {
            val res = repository.deletePenduduk(penduduk, _userProfile.value.namaPetugas)
            if (res.isSuccess) {
                onComplete()
            } else {
                _syncStatusMessage.value = "Gagal menghapus: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun catatMutasi(penduduk: Penduduk, jenisMutasi: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val res = repository.mutasiPenduduk(penduduk, jenisMutasi, _userProfile.value.namaPetugas)
            if (res.isSuccess) {
                onComplete()
            } else {
                _syncStatusMessage.value = "Gagal mencatat mutasi: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun batalkanMutasi(nik: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val res = repository.batalkanMutasi(nik, _userProfile.value.namaPetugas)
            if (res.isSuccess) {
                onComplete()
            } else {
                _syncStatusMessage.value = "Gagal membatalkan mutasi: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    // OTA UPDATE METHODS
    fun getCurrentAppVersionName(): String {
        return try {
            val pInfo = getApplication<Application>().packageManager.getPackageInfo(getApplication<Application>().packageName, 0)
            pInfo.versionName ?: "1.0.5"
        } catch (_: Exception) {
            "1.0.5"
        }
    }

    fun openOtaDialog() {
        _showOtaDialog.value = true
        if (_updateInfo.value == null && !_isCheckingUpdate.value) {
            checkForOtaUpdate(manual = false)
        }
    }

    fun dismissOtaDialog() {
        if (!_isDownloadingUpdate.value) {
            _showOtaDialog.value = false
        }
    }

    fun checkForOtaUpdate(manual: Boolean = true) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            _updateError.value = null
            if (manual) {
                _showOtaDialog.value = true
            }

            val currentVer = getCurrentAppVersionName()
            val repo = _userProfile.value.githubRepo.ifBlank { "rikkinurzaman/data-penduduk" }

            val result = otaUpdateManager.checkForUpdates(repo, currentVer)
            _isCheckingUpdate.value = false

            if (result.isSuccess) {
                val info = result.getOrNull()
                _updateInfo.value = info
                if (info != null && info.isUpdateAvailable) {
                    _showOtaDialog.value = true
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "Gagal memeriksa pembaruan dari GitHub"
                _updateError.value = err
                if (!manual && _updateInfo.value == null) {
                    // silently keep if background check
                } else {
                    _showOtaDialog.value = true
                }
            }
        }
    }

    fun startDownloadAndInstallUpdate() {
        val info = _updateInfo.value ?: return
        viewModelScope.launch {
            _isDownloadingUpdate.value = true
            _updateProgress.value = 0f
            _updateDownloadedBytes.value = 0L
            _updateTotalBytes.value = info.apkFileSize
            _updateError.value = null

            val result = otaUpdateManager.downloadApk(
                downloadUrl = info.apkDownloadUrl,
                fileName = info.apkFileName.ifBlank { "SIMDes-DataPenduduk-${info.versionName}.apk" },
                onProgress = { progress, downloaded, total ->
                    _updateProgress.value = progress
                    _updateDownloadedBytes.value = downloaded
                    _updateTotalBytes.value = total
                }
            )

            _isDownloadingUpdate.value = false

            if (result.isSuccess) {
                val apkFile = result.getOrNull()
                lastDownloadedApkFile = apkFile
                if (apkFile != null) {
                    val installResult = otaUpdateManager.installApk(apkFile)
                    if (installResult.isFailure) {
                        _updateError.value = installResult.exceptionOrNull()?.message
                    }
                }
            } else {
                _updateError.value = result.exceptionOrNull()?.message ?: "Gagal mengunduh APK pembaruan."
            }
        }
    }

    fun saveGithubRepo(repo: String) {
        val clean = repo.trim().removePrefix("https://github.com/").removeSuffix("/")
        repository.saveGithubRepo(clean)
        _userProfile.value = repository.getUserProfile()
    }

    // ==================== DOKUMEN & SCANNER UPLOAD STATE ====================

    enum class UploadStage {
        IDLE,
        PROCESSING_IMAGE,
        CREATING_FOLDERS,
        UPLOADING_DRIVE,
        SUCCESS,
        ERROR
    }

    data class UploadDocumentState(
        val isUploading: Boolean = false,
        val stage: UploadStage = UploadStage.IDLE,
        val progressMessage: String = "",
        val document: PendudukDocument? = null,
        val errorMessage: String? = null,
        val showModal: Boolean = false
    )

    private val _uploadState = MutableStateFlow(UploadDocumentState())
    val uploadState: StateFlow<UploadDocumentState> = _uploadState.asStateFlow()

    fun getDocumentsForResident(nik: String): Flow<List<PendudukDocument>> {
        return repository.getDocumentsForResident(nik)
    }

    fun getDocumentCountForResident(nik: String): Flow<Int> {
        return repository.getDocumentCount(nik)
    }

    fun saveAndUploadDocument(
        nik: String,
        noKk: String,
        namaWarga: String,
        rw: String,
        rt: String,
        jenisDokumen: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg",
        autoUploadDrive: Boolean = true
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadDocumentState(
                isUploading = true,
                stage = UploadStage.PROCESSING_IMAGE,
                progressMessage = "Memproses & mengompresi gambar dokumen...",
                showModal = true
            )

            try {
                delay(400L) // Visual rhythm for animation
                val savedDoc = repository.saveLocalDocument(
                    nik = nik,
                    noKk = noKk,
                    namaWarga = namaWarga,
                    rw = rw,
                    rt = rt,
                    jenisDokumen = jenisDokumen,
                    imageBytes = imageBytes,
                    mimeType = mimeType
                )

                if (autoUploadDrive) {
                    val scriptUrl = repository.getAppsScriptUrl()
                    if (scriptUrl.isBlank()) {
                        _uploadState.value = UploadDocumentState(
                            isUploading = false,
                            stage = UploadStage.SUCCESS,
                            progressMessage = "Dokumen tersimpan di memori perangkat! (URL Google Drive belum diatur di Pengaturan).",
                            document = savedDoc,
                            showModal = true
                        )
                        return@launch
                    }

                    _uploadState.value = _uploadState.value.copy(
                        stage = UploadStage.CREATING_FOLDERS,
                        progressMessage = "Menyiapkan hierarki folder di Google Drive (RW $rw > RT $rt > $namaWarga)...",
                        document = savedDoc
                    )

                    delay(600L) // Visual rhythm for animation

                    _uploadState.value = _uploadState.value.copy(
                        stage = UploadStage.UPLOADING_DRIVE,
                        progressMessage = "Mengunggah $jenisDokumen ke Google Drive..."
                    )

                    val uploadResult = repository.uploadDocumentToDrive(savedDoc, imageBytes)
                    uploadResult.fold(
                        onSuccess = { syncedDoc ->
                            _uploadState.value = UploadDocumentState(
                                isUploading = false,
                                stage = UploadStage.SUCCESS,
                                progressMessage = "Dokumen berhasil disimpan & diunggah ke Google Drive!",
                                document = syncedDoc,
                                showModal = true
                            )
                        },
                        onFailure = { error ->
                            _uploadState.value = UploadDocumentState(
                                isUploading = false,
                                stage = UploadStage.ERROR,
                                progressMessage = "Tersimpan di perangkat lokal, namun gagal sync ke Google Drive.",
                                document = savedDoc,
                                errorMessage = error.localizedMessage ?: "Gagal upload ke Drive",
                                showModal = true
                            )
                        }
                    )
                } else {
                    _uploadState.value = UploadDocumentState(
                        isUploading = false,
                        stage = UploadStage.SUCCESS,
                        progressMessage = "Dokumen berhasil disimpan ke penyimpanan lokal.",
                        document = savedDoc,
                        showModal = true
                    )
                }
            } catch (e: Exception) {
                _uploadState.value = UploadDocumentState(
                    isUploading = false,
                    stage = UploadStage.ERROR,
                    progressMessage = "Gagal memproses dokumen.",
                    errorMessage = e.localizedMessage ?: "Terjadi kesalahan saat memproses gambar",
                    showModal = true
                )
            }
        }
    }

    fun retryUploadDocumentToDrive(document: PendudukDocument) {
        viewModelScope.launch {
            _uploadState.value = UploadDocumentState(
                isUploading = true,
                stage = UploadStage.UPLOADING_DRIVE,
                progressMessage = "Mengunggah ${document.jenisDokumen} ke Google Drive...",
                document = document,
                showModal = true
            )

            val result = repository.uploadDocumentToDrive(document)
            result.fold(
                onSuccess = { syncedDoc ->
                    _uploadState.value = UploadDocumentState(
                        isUploading = false,
                        stage = UploadStage.SUCCESS,
                        progressMessage = "Dokumen berhasil diunggah ke Google Drive!",
                        document = syncedDoc,
                        showModal = true
                    )
                },
                onFailure = { err ->
                    _uploadState.value = UploadDocumentState(
                        isUploading = false,
                        stage = UploadStage.ERROR,
                        progressMessage = "Gagal sinkronisasi ke Drive.",
                        document = document,
                        errorMessage = err.localizedMessage,
                        showModal = true
                    )
                }
            )
        }
    }

    fun deleteDocument(document: PendudukDocument) {
        viewModelScope.launch {
            repository.deleteDocument(document)
        }
    }

    fun dismissUploadModal() {
        _uploadState.value = _uploadState.value.copy(showModal = false)
    }
}
