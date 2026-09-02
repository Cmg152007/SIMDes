package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.ActivityLogDao
import com.example.data.local.DocumentDao
import com.example.data.local.NotificationDao
import com.example.data.local.PendudukDao
import com.example.data.model.ActivityLog
import com.example.data.model.AppNotification
import com.example.data.model.Penduduk
import com.example.data.model.PendudukDiffUtil
import com.example.data.model.PendudukDocument
import com.example.data.model.UserProfile
import com.example.data.remote.AppsScriptService
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class PendudukRepository(
    private val context: Context,
    private val pendudukDao: PendudukDao,
    private val activityLogDao: ActivityLogDao,
    private val notificationDao: NotificationDao,
    private val documentDao: DocumentDao,
    private val appsScriptService: AppsScriptService = AppsScriptService()
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("simdes_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_LOGIN_PIN = "2007"
        const val DEFAULT_SECURITY_PIN = "3522"
        private const val KEY_LOGIN_PIN = "key_login_pin"
        private const val KEY_SCRIPT_URL = "key_script_url"
        private const val KEY_GITHUB_REPO = "key_github_repo"
        private const val KEY_NAMA_PETUGAS = "key_nama_petugas"
        private const val KEY_NIP_PETUGAS = "key_nip_petugas"
        private const val KEY_JABATAN = "key_jabatan"
        private const val KEY_NO_HP = "key_no_hp"
        private const val KEY_WILAYAH_KERJA = "key_wilayah_kerja"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_NAMA_KADES = "key_nama_kades"
        private const val KEY_NIP_KADES = "key_nip_kades"
        private const val KEY_NAMA_DESA = "key_nama_desa"
        private const val KEY_KECAMATAN = "key_kecamatan"
        private const val KEY_KABUPATEN = "key_kabupaten"
        private const val KEY_PROVINSI = "key_provinsi"
        private const val KEY_KODE_POS = "key_kode_pos"
        private const val KEY_EMAIL_DESA = "key_email_desa"
        private const val KEY_ALAMAT_KANTOR = "key_alamat_kantor"
        private const val KEY_TOTAL_RW = "key_total_rw"
        private const val KEY_TOTAL_RT = "key_total_rt"
        private const val KEY_FOTO_PROFIL = "key_foto_profil"
    }

    val allPenduduk: Flow<List<Penduduk>> = pendudukDao.getAllPenduduk()
    val allLogs: Flow<List<ActivityLog>> = activityLogDao.getAllLogs()
    val allNotifications: Flow<List<AppNotification>> = notificationDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = notificationDao.getUnreadCount()

    fun getLoginPin(): String {
        return prefs.getString(KEY_LOGIN_PIN, DEFAULT_LOGIN_PIN) ?: DEFAULT_LOGIN_PIN
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun verifyLoginPin(pin: String): Boolean {
        val currentPin = getLoginPin()
        return pin.trim() == currentPin.trim()
    }

    fun saveLoginPin(newPin: String) {
        prefs.edit().putString(KEY_LOGIN_PIN, newPin.trim()).apply()
    }

    fun verifyPin(pin: String): Boolean {
        return pin.trim() == DEFAULT_SECURITY_PIN || pin.trim() == getLoginPin()
    }

    fun verifySecurityPin(pin: String): Boolean {
        return pin.trim() == DEFAULT_SECURITY_PIN
    }

    fun getAppsScriptUrl(): String {
        val raw = prefs.getString(KEY_SCRIPT_URL, "") ?: ""
        return appsScriptService.cleanUrl(raw)
    }

    fun saveAppsScriptUrl(url: String) {
        val cleaned = appsScriptService.cleanUrl(url)
        prefs.edit().putString(KEY_SCRIPT_URL, cleaned).apply()
    }

    fun getGithubRepo(): String {
        return prefs.getString(KEY_GITHUB_REPO, "rikkinurzaman/data-penduduk") ?: "rikkinurzaman/data-penduduk"
    }

    fun saveGithubRepo(repo: String) {
        val clean = repo.trim().removePrefix("https://github.com/").removeSuffix("/")
        prefs.edit().putString(KEY_GITHUB_REPO, clean).apply()
    }

    fun getUserProfile(): UserProfile {
        return UserProfile(
            namaDesa = prefs.getString(KEY_NAMA_DESA, "Desa Cimanggu") ?: "Desa Cimanggu",
            kecamatan = prefs.getString(KEY_KECAMATAN, "Puspahiang") ?: "Puspahiang",
            kabupaten = prefs.getString(KEY_KABUPATEN, "Tasikmalaya") ?: "Tasikmalaya",
            provinsi = prefs.getString(KEY_PROVINSI, "Jawa Barat") ?: "Jawa Barat",
            kodePos = prefs.getString(KEY_KODE_POS, "46471") ?: "46471",
            emailDesa = prefs.getString(KEY_EMAIL_DESA, "desacimanggu07@gmail.com") ?: "desacimanggu07@gmail.com",
            namaPetugas = prefs.getString(KEY_NAMA_PETUGAS, "PENDI, S.Sos., M.Si") ?: "PENDI, S.Sos., M.Si",
            nipPetugas = prefs.getString(KEY_NIP_PETUGAS, "19880409 06152007 0002") ?: "19880409 06152007 0002",
            jabatan = prefs.getString(KEY_JABATAN, "Kasi Pemerintahan") ?: "Kasi Pemerintahan",
            noHp = prefs.getString(KEY_NO_HP, "0812-3456-7890") ?: "0812-3456-7890",
            namaKades = prefs.getString(KEY_NAMA_KADES, "MAIL") ?: "MAIL",
            nipKades = prefs.getString(KEY_NIP_KADES, "-") ?: "-",
            wilayahKerja = prefs.getString(KEY_WILAYAH_KERJA, "") ?: "",
            alamatKantor = prefs.getString(KEY_ALAMAT_KANTOR, "Jl. Raya Puspahiang - Cimanggu") ?: "Jl. Raya Puspahiang - Cimanggu",
            appsScriptUrl = getAppsScriptUrl(),
            githubRepo = getGithubRepo(),
            pinSecurity = DEFAULT_SECURITY_PIN,
            isBiometricEnabled = isBiometricEnabled(),
            totalRw = prefs.getInt(KEY_TOTAL_RW, 5),
            totalRt = prefs.getInt(KEY_TOTAL_RT, 22),
            fotoProfilPath = prefs.getString(KEY_FOTO_PROFIL, "") ?: ""
        )
    }

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_NAMA_DESA, profile.namaDesa)
            .putString(KEY_KECAMATAN, profile.kecamatan)
            .putString(KEY_KABUPATEN, profile.kabupaten)
            .putString(KEY_PROVINSI, profile.provinsi)
            .putString(KEY_KODE_POS, profile.kodePos)
            .putString(KEY_EMAIL_DESA, profile.emailDesa)
            .putString(KEY_ALAMAT_KANTOR, profile.alamatKantor)
            .putString(KEY_NAMA_KADES, profile.namaKades)
            .putString(KEY_NIP_KADES, profile.nipKades)
            .putString(KEY_NAMA_PETUGAS, profile.namaPetugas)
            .putString(KEY_NIP_PETUGAS, profile.nipPetugas)
            .putString(KEY_JABATAN, profile.jabatan)
            .putString(KEY_NO_HP, profile.noHp)
            .putString(KEY_WILAYAH_KERJA, profile.wilayahKerja)
            .putInt(KEY_TOTAL_RW, profile.totalRw)
            .putInt(KEY_TOTAL_RT, profile.totalRt)
            .putString(KEY_FOTO_PROFIL, profile.fotoProfilPath)
            .putString(KEY_GITHUB_REPO, profile.githubRepo.trim().removePrefix("https://github.com/").removeSuffix("/"))
            .putBoolean(KEY_BIOMETRIC_ENABLED, profile.isBiometricEnabled)
            .apply()
    }

    private suspend fun triggerAutoSyncToSpreadsheet(operatorName: String) {
        val scriptUrl = getAppsScriptUrl()
        if (scriptUrl.isBlank()) return
        try {
            val currentList = pendudukDao.getAllPenduduk().first()
            val currentLogs = activityLogDao.getAllLogs().first()
            appsScriptService.syncAllData(scriptUrl, currentList, currentLogs)
        } catch (_: Exception) {
            // Silently allow local operations to succeed even if offline
        }
    }

    suspend fun insertPenduduk(penduduk: Penduduk, operatorName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentList = pendudukDao.getAllPenduduk().first()
            val finalNo = if (penduduk.no <= 0) {
                (currentList.maxOfOrNull { it.no } ?: currentList.size) + 1
            } else {
                penduduk.no
            }
            val preparedPenduduk = penduduk.copy(no = finalNo, lastModifiedTimestamp = System.currentTimeMillis())

            pendudukDao.insertPenduduk(preparedPenduduk)
            val log = ActivityLog(
                action = "TAMBAH",
                operator = operatorName,
                target = "${preparedPenduduk.nama} (NIK: ${preparedPenduduk.nik})",
                detail = "Menambahkan data penduduk baru RT ${preparedPenduduk.rt} RW ${preparedPenduduk.rw} pada nomor urut $finalNo",
                status = "BERHASIL",
                dataBefore = null,
                dataAfter = PendudukDiffUtil.toSnapshotJson(preparedPenduduk)
            )
            activityLogDao.insertLog(log)
            
            notificationDao.insertNotification(
                AppNotification(
                    title = "Penduduk Baru Ditambahkan",
                    message = "Data '${preparedPenduduk.nama}' (NIK: ${preparedPenduduk.nik}) berhasil tersimpan dan langsung disinkronkan ke spreadsheet.",
                    type = "INFO"
                )
            )

            // Trigger real-time auto-sync to spreadsheet immediately
            triggerAutoSyncToSpreadsheet(operatorName)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePenduduk(penduduk: Penduduk, operatorName: String, oldNik: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val nikToSearch = oldNik ?: penduduk.nik
            val existing = pendudukDao.getPendudukByNik(nikToSearch)
            val diffs = if (existing != null) PendudukDiffUtil.getDiffList(existing, penduduk) else emptyList()
            
            val detailMsg = if (diffs.isNotEmpty()) {
                val summary = diffs.take(3).joinToString(", ") { "${it.fieldName} (${it.oldValue} → ${it.newValue})" }
                if (diffs.size > 3) "Memperbarui ${diffs.size} data: $summary, dan ${diffs.size - 3} lainnya"
                else "Memperbarui ${diffs.size} data: $summary"
            } else {
                "Memperbarui biodata penduduk RT ${penduduk.rt} RW ${penduduk.rw}"
            }

            if (oldNik != null && oldNik != penduduk.nik) {
                // If primary key (NIK) was changed, remove old record and insert updated one
                pendudukDao.deleteByNik(oldNik)
                pendudukDao.insertPenduduk(penduduk.copy(lastModifiedTimestamp = System.currentTimeMillis()))
            } else {
                pendudukDao.updatePenduduk(penduduk.copy(lastModifiedTimestamp = System.currentTimeMillis()))
            }

            val finalDetail = if (oldNik != null && oldNik != penduduk.nik) {
                "Ubah NIK ($oldNik → ${penduduk.nik}) • $detailMsg"
            } else {
                detailMsg
            }

            val log = ActivityLog(
                action = "EDIT",
                operator = operatorName,
                target = "${penduduk.nama} (NIK: ${penduduk.nik})",
                detail = finalDetail,
                status = "BERHASIL",
                dataBefore = existing?.let { PendudukDiffUtil.toSnapshotJson(it) },
                dataAfter = PendudukDiffUtil.toSnapshotJson(penduduk)
            )
            activityLogDao.insertLog(log)

            notificationDao.insertNotification(
                AppNotification(
                    title = "Data Penduduk Diperbarui",
                    message = "Perubahan data '${penduduk.nama}' (NIK: ${penduduk.nik}) berhasil disimpan dan langsung disinkronkan ke spreadsheet.",
                    type = "INFO"
                )
            )

            // Trigger real-time auto-sync to spreadsheet immediately
            triggerAutoSyncToSpreadsheet(operatorName)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePenduduk(penduduk: Penduduk, operatorName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            pendudukDao.deletePenduduk(penduduk)
            val log = ActivityLog(
                action = "HAPUS",
                operator = operatorName,
                target = "${penduduk.nama} (NIK: ${penduduk.nik})",
                detail = "Menghapus data penduduk dari database",
                status = "BERHASIL",
                dataBefore = PendudukDiffUtil.toSnapshotJson(penduduk),
                dataAfter = null
            )
            activityLogDao.insertLog(log)

            notificationDao.insertNotification(
                AppNotification(
                    title = "Data Penduduk Dihapus",
                    message = "Data '${penduduk.nama}' (NIK: ${penduduk.nik}) telah dihapus dari sistem.",
                    type = "WARNING"
                )
            )

            // Trigger real-time auto-sync to spreadsheet
            triggerAutoSyncToSpreadsheet(operatorName)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun mutasiPenduduk(
        penduduk: Penduduk,
        jenisMutasi: String, // "MENINGGAL" or "PINDAH"
        operatorName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = pendudukDao.getPendudukByNik(penduduk.nik)
            val isMeninggal = jenisMutasi.equals("MENINGGAL", ignoreCase = true)
            
            val updated = if (isMeninggal) {
                penduduk.copy(
                    statusMutasi = "MENINGGAL",
                    keterangan = "Meninggal (${penduduk.tanggalKematian.ifBlank { "Tercatat" }})",
                    lastModifiedTimestamp = System.currentTimeMillis()
                )
            } else {
                penduduk.copy(
                    statusMutasi = "PINDAH",
                    keterangan = "Pindah Keluar ke ${penduduk.desaTujuan.ifBlank { penduduk.alamatTujuan.ifBlank { "Luar Desa" } }}",
                    lastModifiedTimestamp = System.currentTimeMillis()
                )
            }

            pendudukDao.updatePenduduk(updated)

            val actionName = if (isMeninggal) "MUTASI_KEMATIAN" else "MUTASI_PINDAH"
            val detailMsg = if (isMeninggal) {
                "Pencatatan kematian warga: Tgl ${updated.tanggalKematian} di ${updated.tempatKematian.ifBlank { "-" }} (Penyebab: ${updated.penyebabKematian.ifBlank { "-" }}). Pelapor: ${updated.namaPelaporKematian.ifBlank { "-" }} (${updated.hubunganPelaporKematian.ifBlank { "-" }})"
            } else {
                "Pencatatan kepindahan warga (Pindah Keluar): Tgl ${updated.tanggalPindah} ke ${updated.alamatTujuan} RT ${updated.rtTujuan}/RW ${updated.rwTujuan}, Desa ${updated.desaTujuan}, Kec. ${updated.kecamatanTujuan}, Kab. ${updated.kabupatenTujuan}. Alasan: ${updated.alasanPindah}"
            }

            val log = ActivityLog(
                action = actionName,
                operator = operatorName,
                target = "${updated.nama} (NIK: ${updated.nik})",
                detail = detailMsg,
                status = "BERHASIL",
                dataBefore = existing?.let { PendudukDiffUtil.toSnapshotJson(it) },
                dataAfter = PendudukDiffUtil.toSnapshotJson(updated)
            )
            activityLogDao.insertLog(log)

            val notifTitle = if (isMeninggal) "Mutasi Kematian Dicatat" else "Mutasi Kepindahan Dicatat"
            val notifMsg = if (isMeninggal) {
                "Data kematian '${updated.nama}' (NIK: ${updated.nik}) berhasil didokumentasikan ke sistem mutasi kependudukan."
            } else {
                "Data kepindahan '${updated.nama}' ke ${updated.desaTujuan.ifBlank { updated.kabupatenTujuan.ifBlank { "Luar Daerah" } }} berhasil dicatat."
            }

            notificationDao.insertNotification(
                AppNotification(
                    title = notifTitle,
                    message = notifMsg,
                    type = "WARNING"
                )
            )

            triggerAutoSyncToSpreadsheet(operatorName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun batalkanMutasi(nik: String, operatorName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = pendudukDao.getPendudukByNik(nik) ?: return@withContext Result.failure(Exception("Data penduduk tidak ditemukan"))
            val oldStatus = existing.statusMutasi
            val updated = existing.copy(
                statusMutasi = "AKTIF",
                keterangan = "Warga Aktif (Status Mutasi Dibatalkan)",
                lastModifiedTimestamp = System.currentTimeMillis()
            )

            pendudukDao.updatePenduduk(updated)

            val log = ActivityLog(
                action = "BATAL_MUTASI",
                operator = operatorName,
                target = "${updated.nama} (NIK: ${updated.nik})",
                detail = "Membatalkan status mutasi ($oldStatus → AKTIF) dan mengembalikan status penduduk menjadi warga aktif normal",
                status = "BERHASIL",
                dataBefore = PendudukDiffUtil.toSnapshotJson(existing),
                dataAfter = PendudukDiffUtil.toSnapshotJson(updated)
            )
            activityLogDao.insertLog(log)

            notificationDao.insertNotification(
                AppNotification(
                    title = "Status Mutasi Dibatalkan",
                    message = "Status '${updated.nama}' dikembalikan menjadi Warga Aktif.",
                    type = "INFO"
                )
            )

            triggerAutoSyncToSpreadsheet(operatorName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testAppsScriptConnection(url: String): Result<String> {
        return appsScriptService.testConnection(url)
    }

    suspend fun pullDataFromSpreadsheet(operatorName: String): Result<String> = withContext(Dispatchers.IO) {
        val scriptUrl = getAppsScriptUrl()
        if (scriptUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL Google Apps Script belum diatur. Masukkan URL pada menu Profil."))
        }

        try {
            val remoteResult = appsScriptService.fetchData(scriptUrl)
            if (remoteResult.isSuccess) {
                val (remotePenduduk, remoteLogs) = remoteResult.getOrThrow()
                if (remotePenduduk.isNotEmpty()) {
                    pendudukDao.insertAll(remotePenduduk)
                }
                if (remoteLogs.isNotEmpty()) {
                    activityLogDao.insertAllLogs(remoteLogs)
                }

                val log = ActivityLog(
                    action = "TARIK DATA",
                    operator = operatorName,
                    target = "Google Spreadsheet",
                    detail = "Berhasil mengunduh ${remotePenduduk.size} data penduduk dan ${remoteLogs.size} log dari spreadsheet",
                    status = "BERHASIL",
                    syncedWithSheets = true
                )
                activityLogDao.insertLog(log)

                notificationDao.insertNotification(
                    AppNotification(
                        title = "Tarik Data Berhasil",
                        message = "Berhasil mengunduh ${remotePenduduk.size} data penduduk dari Google Spreadsheet ke aplikasi.",
                        type = "SYNC"
                    )
                )

                Result.success("Berhasil mengunduh ${remotePenduduk.size} data penduduk dari Google Spreadsheet.")
            } else {
                val errorMsg = remoteResult.exceptionOrNull()?.message ?: "Gagal mengunduh data dari spreadsheet"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncWithSpreadsheet(operatorName: String): Result<String> = withContext(Dispatchers.IO) {
        val scriptUrl = getAppsScriptUrl()
        if (scriptUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL Google Apps Script belum diatur. Masukkan URL pada menu Profil."))
        }

        try {
            val currentLocalList = pendudukDao.getAllPenduduk().first()
            val currentLogs = activityLogDao.getAllLogs().first()

            val syncResult = appsScriptService.syncAllData(scriptUrl, currentLocalList, currentLogs)
            if (syncResult.isSuccess) {
                pendudukDao.markAsSynced(currentLocalList.map { it.nik })

                val log = ActivityLog(
                    action = "SINKRONISASI",
                    operator = operatorName,
                    target = "Google Spreadsheet",
                    detail = "Sinkronisasi berhasil: ${currentLocalList.size} penduduk, ${currentLogs.size} log",
                    status = "BERHASIL",
                    syncedWithSheets = true
                )
                activityLogDao.insertLog(log)

                notificationDao.insertNotification(
                    AppNotification(
                        title = "Sinkronisasi Berhasil",
                        message = "Seluruh data kependudukan (${currentLocalList.size} data) berhasil diperbarui ke Google Spreadsheet.",
                        type = "SYNC"
                    )
                )
                Result.success(syncResult.getOrThrow())
            } else {
                val errorMsg = syncResult.exceptionOrNull()?.message ?: "Gagal sinkronisasi"
                val log = ActivityLog(
                    action = "SINKRONISASI",
                    operator = operatorName,
                    target = "Google Spreadsheet",
                    detail = "Gagal: $errorMsg",
                    status = "GAGAL"
                )
                activityLogDao.insertLog(log)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordScreenshotLog(operatorName: String, screenName: String) {
        withContext(Dispatchers.IO) {
            val log = ActivityLog(
                action = "SCREENSHOT",
                operator = operatorName,
                target = screenName,
                detail = "Tangkapan layar (Screenshot) diambil pada halaman $screenName. Watermark identitas $operatorName disematkan.",
                status = "BERHASIL"
            )
            activityLogDao.insertLog(log)
            notificationDao.insertNotification(
                AppNotification(
                    title = "Tangkapan Layar Terdeteksi",
                    message = "Tangkapan layar pada $screenName oleh $operatorName berhasil dicatat dalam riwayat audit keamanan.",
                    type = "INFO"
                )
            )
        }
    }

    suspend fun recordSecurityLockLog(operatorName: String, reason: String) {
        withContext(Dispatchers.IO) {
            val log = ActivityLog(
                action = "KUNCI_OTOMATIS",
                operator = operatorName,
                target = "Keamanan Sesi",
                detail = reason,
                status = "BERHASIL"
            )
            activityLogDao.insertLog(log)
        }
    }

    suspend fun markNotificationAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun clearAllNotifications() {
        notificationDao.clearAll()
    }

    // ==================== DOKUMEN & ARSIP DIGITAL ====================

    fun getDocumentsForResident(nik: String): Flow<List<PendudukDocument>> {
        return documentDao.getDocumentsByNik(nik)
    }

    fun getAllDocuments(): Flow<List<PendudukDocument>> {
        return documentDao.getAllDocuments()
    }

    fun getDocumentCount(nik: String): Flow<Int> {
        return documentDao.getDocumentCountByNik(nik)
    }

    suspend fun saveLocalDocument(
        nik: String,
        noKk: String,
        namaWarga: String,
        rw: String,
        rt: String,
        jenisDokumen: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): PendudukDocument = withContext(Dispatchers.IO) {
        val extension = if (mimeType.contains("png", ignoreCase = true)) "png" else "jpg"
        val timestamp = System.currentTimeMillis()
        val cleanJenis = jenisDokumen.replace(" ", "_").replace("/", "-")
        val fileName = "${cleanJenis}_${nik}_${timestamp}.$extension"

        // Save image to internal storage
        val docDir = File(context.filesDir, "documents/$nik")
        if (!docDir.exists()) {
            docDir.mkdirs()
        }
        val file = File(docDir, fileName)
        FileOutputStream(file).use { out ->
            out.write(imageBytes)
        }

        val rwFolder = if (rw.startsWith("rw", ignoreCase = true)) rw else "RW $rw"
        val rtFolder = if (rt.startsWith("rt", ignoreCase = true)) rt else "RT $rt"
        val hierarchy = "SIMDes_Dokumen_Desa / $rwFolder / $rtFolder / $nik - $namaWarga / $jenisDokumen"

        val document = PendudukDocument(
            nik = nik,
            noKk = noKk,
            namaWarga = namaWarga,
            rw = rw,
            rt = rt,
            jenisDokumen = jenisDokumen,
            namaFile = fileName,
            localFilePath = file.absolutePath,
            driveFileUrl = null,
            driveFolderHierarchy = hierarchy,
            fileSizeBytes = imageBytes.size.toLong(),
            mimeType = mimeType,
            createdAt = timestamp,
            isSynced = false
        )

        val insertedId = documentDao.insertDocument(document)
        val savedDoc = document.copy(id = insertedId)

        val user = getUserProfile()
        activityLogDao.insertLog(
            ActivityLog(
                action = "SIMPAN_DOKUMEN",
                operator = user.namaPetugas,
                target = "$jenisDokumen ($nik - $namaWarga)",
                detail = "Dokumen $jenisDokumen berhasil disimpan ke penyimpanan lokal perangkat (${savedDoc.getFormattedSize()}).",
                status = "BERHASIL"
            )
        )

        savedDoc
    }

    suspend fun uploadDocumentToDrive(
        document: PendudukDocument,
        imageBytes: ByteArray? = null
    ): Result<PendudukDocument> = withContext(Dispatchers.IO) {
        val scriptUrl = getAppsScriptUrl()
        if (scriptUrl.isBlank()) {
            val errorMsg = "URL Google Apps Script belum diisi di Pengaturan. Dokumen tersimpan di memori lokal."
            documentDao.updateDocument(document.copy(syncError = errorMsg))
            return@withContext Result.failure(Exception(errorMsg))
        }

        try {
            // Read bytes from file if null
            val bytes = imageBytes ?: run {
                val filePath = document.localFilePath ?: return@withContext Result.failure(Exception("File lokal tidak ditemukan"))
                val file = File(filePath)
                if (!file.exists()) return@withContext Result.failure(Exception("File lokal fisik tidak ditemukan"))
                file.readBytes()
            }

            val base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val uploadResult = appsScriptService.uploadDocumentToDrive(
                rawScriptUrl = scriptUrl,
                nik = document.nik,
                noKk = document.noKk,
                nama = document.namaWarga,
                rw = document.rw,
                rt = document.rt,
                jenisDokumen = document.jenisDokumen,
                fileName = document.namaFile,
                base64Data = base64Data,
                mimeType = document.mimeType
            )

            uploadResult.fold(
                onSuccess = { (fileUrl, folderPath) ->
                    val updatedDoc = document.copy(
                        driveFileUrl = fileUrl,
                        driveFolderHierarchy = folderPath,
                        isSynced = true,
                        syncError = null
                    )
                    documentDao.updateDocument(updatedDoc)

                    val user = getUserProfile()
                    activityLogDao.insertLog(
                        ActivityLog(
                            action = "UPLOAD_DRIVE",
                            operator = user.namaPetugas,
                            target = "${document.jenisDokumen} (${document.nik} - ${document.namaWarga})",
                            detail = "Dokumen berhasil diunggah ke Google Drive: $folderPath",
                            status = "BERHASIL"
                        )
                    )

                    notificationDao.insertNotification(
                        AppNotification(
                            title = "Dokumen Terunggah ke Drive",
                            message = "${document.jenisDokumen} milik ${document.namaWarga} berhasil disimpan ke folder Google Drive: $folderPath",
                            type = "SUCCESS"
                        )
                    )

                    Result.success(updatedDoc)
                },
                onFailure = { error ->
                    documentDao.updateDocument(document.copy(syncError = error.localizedMessage))
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            documentDao.updateDocument(document.copy(syncError = e.localizedMessage))
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(document: PendudukDocument) = withContext(Dispatchers.IO) {
        document.localFilePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Ignore file delete errors
            }
        }
        documentDao.deleteDocument(document)
        val user = getUserProfile()
        activityLogDao.insertLog(
            ActivityLog(
                action = "HAPUS_DOKUMEN",
                operator = user.namaPetugas,
                target = "${document.jenisDokumen} (${document.nik} - ${document.namaWarga})",
                detail = "Dokumen ${document.jenisDokumen} dihapus dari arsip.",
                status = "BERHASIL"
            )
        )
    }
}
