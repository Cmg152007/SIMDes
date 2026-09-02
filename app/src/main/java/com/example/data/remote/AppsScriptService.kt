package com.example.data.remote

import android.util.Log
import com.example.data.model.ActivityLog
import com.example.data.model.Penduduk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

class AppsScriptService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Sanitizes and cleans the raw URL input from user
     */
    fun cleanUrl(rawUrl: String): String {
        var url = rawUrl.trim()
            .replace("\u200B", "") // Zero-width space
            .replace("\uFEFF", "") // Byte order mark
            .replace("\u00A0", " ") // Non-breaking space
            .replace("\"", "")
            .replace("'", "")
            .replace("`", "")
            .replace("\n", "")
            .replace("\r", "")
            .replace("\t", "")
            .trim()

        if (url.isBlank()) return ""

        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            url = "https://$url"
        }

        // Remove trailing slash if followed by query or directly
        if (url.endsWith("/") && !url.endsWith("/exec/")) {
            url = url.dropLast(1)
        }

        return url.trim()
    }

    /**
     * Validates if URL has proper Apps Script Web App format
     */
    fun validateUrl(rawUrl: String): String? {
        val cleaned = cleanUrl(rawUrl)
        if (cleaned.isBlank()) {
            return "URL Google Apps Script belum diisi"
        }
        if (cleaned.contains("docs.google.com/spreadsheets", ignoreCase = true)) {
            return "URL yang Anda masukkan adalah link Google Spreadsheet, bukan URL Web App Apps Script. Silakan buka Spreadsheet > Ekstensi > Apps Script > Terapkan (Deploy) > Penerapan Baru > Aplikasi Web, lalu salin URL yang berakhiran /exec."
        }
        if (cleaned.contains("script.google.com", ignoreCase = true) && cleaned.contains("/edit", ignoreCase = true)) {
            return "URL yang Anda masukkan adalah link editor script. Silakan klik tombol 'Terapkan' (Deploy) > 'Penerapan baru' > Jenis: 'Aplikasi Web' (Web app), lalu salin URL yang berakhiran /exec."
        }
        return null
    }

    /**
     * Executes HTTP Request with explicit follow-up for Google Apps Script 302 redirects
     */
    private fun executeRequestWithRedirects(initialRequest: Request): Pair<Int, String> {
        var currentRequest = initialRequest
        var response = client.newCall(currentRequest).execute()
        var redirectCount = 0

        while ((response.isRedirect || response.code in 300..399) && redirectCount < 5) {
            val location = response.header("Location") ?: break
            response.close()
            currentRequest = Request.Builder()
                .url(location)
                .get()
                .build()
            response = client.newCall(currentRequest).execute()
            redirectCount++
        }

        val code = response.code
        val body = response.body?.string() ?: ""
        response.close()
        return Pair(code, body)
    }

    suspend fun testConnection(rawScriptUrl: String): Result<String> = withContext(Dispatchers.IO) {
        val scriptUrl = cleanUrl(rawScriptUrl)
        val validationError = validateUrl(scriptUrl)
        if (validationError != null) {
            return@withContext Result.failure(Exception(validationError))
        }

        try {
            val targetUrl = if (scriptUrl.contains("?")) "$scriptUrl&action=test" else "$scriptUrl?action=test"
            val request = Request.Builder()
                .url(targetUrl)
                .get()
                .header("Accept", "application/json")
                .build()

            val (code, responseBody) = executeRequestWithRedirects(request)

            if (code !in 200..299) {
                return@withContext Result.failure(
                    Exception(formatHttpError(code, responseBody))
                )
            }

            if (responseBody.contains("accounts.google.com") || responseBody.contains("ServiceLogin")) {
                return@withContext Result.failure(
                    Exception("Akses Ditolak: Deployment Apps Script memerlukan login Google. Pastikan pada saat 'Terapkan (Deploy) > Aplikasi Web', opsi 'Siapa yang memiliki akses' diatur ke 'Siapa saja' (Anyone).")
                )
            }

            try {
                val json = JSONObject(responseBody)
                val status = json.optString("status", "")
                val message = json.optString("message", "")

                if (status == "error") {
                    return@withContext Result.failure(Exception("Apps Script Error: $message"))
                }

                val msg = if (message.isNotBlank()) message else "Koneksi Google Apps Script berhasil terhubung!"
                Result.success(msg)
            } catch (e: Exception) {
                if (responseBody.contains("success", ignoreCase = true) || responseBody.contains("OK", ignoreCase = true)) {
                    Result.success("Koneksi Google Apps Script berhasil terhubung!")
                } else {
                    Result.success("Terhubung ke Google Apps Script.")
                }
            }
        } catch (e: UnknownHostException) {
            Log.e("AppsScriptService", "DNS Host error", e)
            Result.failure(
                Exception("Tidak dapat menemukan host server (${e.message}). Pastikan HP terhubung ke internet dan URL Apps Script yang dimasukkan valid (berformat https://script.google.com/macros/s/.../exec).")
            )
        } catch (e: SocketTimeoutException) {
            Log.e("AppsScriptService", "Timeout error", e)
            Result.failure(Exception("Koneksi ke Google Apps Script memakan waktu terlalu lama (Timeout). Silakan coba beberapa saat lagi."))
        } catch (e: SSLException) {
            Log.e("AppsScriptService", "SSL error", e)
            Result.failure(Exception("Gagal melakukan koneksi aman SSL/HTTPS: ${e.localizedMessage}"))
        } catch (e: Exception) {
            Log.e("AppsScriptService", "Test connection error", e)
            Result.failure(Exception("Gagal menghubungi Google Apps Script: ${e.localizedMessage}"))
        }
    }

    suspend fun fetchData(rawScriptUrl: String): Result<Pair<List<Penduduk>, List<ActivityLog>>> = withContext(Dispatchers.IO) {
        val scriptUrl = cleanUrl(rawScriptUrl)
        val validationError = validateUrl(scriptUrl)
        if (validationError != null) {
            return@withContext Result.failure(Exception(validationError))
        }

        try {
            val targetUrl = if (scriptUrl.contains("?")) "$scriptUrl&action=get_all" else "$scriptUrl?action=get_all"
            val request = Request.Builder()
                .url(targetUrl)
                .get()
                .header("Accept", "application/json")
                .build()

            val (code, responseBody) = executeRequestWithRedirects(request)

            if (code !in 200..299) {
                return@withContext Result.failure(
                    Exception(formatHttpError(code, responseBody))
                )
            }

            if (responseBody.contains("accounts.google.com") || responseBody.contains("ServiceLogin")) {
                return@withContext Result.failure(
                    Exception("Akses Ditolak: Deployment Apps Script memerlukan login Google. Pastikan pada saat 'Terapkan (Deploy) > Aplikasi Web', opsi 'Siapa yang memiliki akses' diatur ke 'Siapa saja' (Anyone).")
                )
            }

            val json = JSONObject(responseBody)
            if (json.optString("status") == "error") {
                val errMsg = json.optString("message", "Terjadi kesalahan di Spreadsheet")
                return@withContext Result.failure(Exception("Apps Script Error: $errMsg"))
            }

            val pendudukList = mutableListOf<Penduduk>()

            // 1. Parse active data from sheet DataPenduduk or generic data array
            val dataArray = json.optJSONArray("data") ?: JSONArray()
            val dataAktifArray = json.optJSONArray("dataAktif")
            val targetAktifArray = if (dataAktifArray != null && dataAktifArray.length() > 0) dataAktifArray else dataArray

            for (i in 0 until targetAktifArray.length()) {
                val item = targetAktifArray.getJSONObject(i)
                val p = parsePendudukFromJson(item, i + 1, defaultStatus = "AKTIF")
                if (p.nik.isNotBlank() || p.nama.isNotBlank()) {
                    pendudukList.add(p)
                }
            }

            // 2. Parse deceased data from sheet Meninggal
            val dataMeninggalArray = json.optJSONArray("dataMeninggal")
            if (dataMeninggalArray != null) {
                for (i in 0 until dataMeninggalArray.length()) {
                    val item = dataMeninggalArray.getJSONObject(i)
                    val p = parsePendudukFromJson(item, pendudukList.size + 1, defaultStatus = "MENINGGAL")
                    if (p.nik.isNotBlank() || p.nama.isNotBlank()) {
                        // Check if already in list to avoid duplicates
                        val existingIndex = pendudukList.indexOfFirst { it.nik == p.nik }
                        if (existingIndex >= 0) {
                            pendudukList[existingIndex] = p
                        } else {
                            pendudukList.add(p)
                        }
                    }
                }
            }

            // 3. Parse relocated data from sheet Pindah
            val dataPindahArray = json.optJSONArray("dataPindah")
            if (dataPindahArray != null) {
                for (i in 0 until dataPindahArray.length()) {
                    val item = dataPindahArray.getJSONObject(i)
                    val p = parsePendudukFromJson(item, pendudukList.size + 1, defaultStatus = "PINDAH")
                    if (p.nik.isNotBlank() || p.nama.isNotBlank()) {
                        val existingIndex = pendudukList.indexOfFirst { it.nik == p.nik }
                        if (existingIndex >= 0) {
                            pendudukList[existingIndex] = p
                        } else {
                            pendudukList.add(p)
                        }
                    }
                }
            }

            val logsArray = json.optJSONArray("logs") ?: JSONArray()
            val logList = mutableListOf<ActivityLog>()
            for (i in 0 until logsArray.length()) {
                val item = logsArray.getJSONObject(i)
                logList.add(
                    ActivityLog(
                        timestamp = item.optString("timestamp", item.optString("TIMESTAMP", ActivityLog.getCurrentFormattedTime())),
                        operator = item.optString("operator", item.optString("OPERATOR", "Petugas Desa")),
                        action = item.optString("action", item.optString("AKSI", "SYNC")),
                        target = item.optString("target", item.optString("TARGET", "Spreadsheet")),
                        detail = item.optString("detail", item.optString("DETAIL", "Sinkronisasi")),
                        status = item.optString("status", item.optString("STATUS", "BERHASIL")),
                        dataBefore = item.optString("dataBefore", item.optString("DATA SEBELUM", "")).ifBlank { null },
                        dataAfter = item.optString("dataAfter", item.optString("DATA SESUDAH", "")).ifBlank { null },
                        syncedWithSheets = true
                    )
                )
            }

            Result.success(Pair(pendudukList, logList))
        } catch (e: UnknownHostException) {
            Log.e("AppsScriptService", "DNS Host error", e)
            Result.failure(
                Exception("Tidak dapat menemukan host server (${e.message}). Pastikan HP terhubung ke internet dan URL Apps Script yang dimasukkan valid.")
            )
        } catch (e: SocketTimeoutException) {
            Log.e("AppsScriptService", "Timeout error", e)
            Result.failure(Exception("Koneksi ke Google Apps Script memakan waktu terlalu lama (Timeout). Silakan periksa kembali koneksi internet Anda."))
        } catch (e: Exception) {
            Log.e("AppsScriptService", "Fetch data error", e)
            Result.failure(Exception("Gagal mengambil data dari Spreadsheet: ${e.localizedMessage}"))
        }
    }

    suspend fun syncAllData(
        rawScriptUrl: String,
        pendudukList: List<Penduduk>,
        logsList: List<ActivityLog>
    ): Result<String> = withContext(Dispatchers.IO) {
        val scriptUrl = cleanUrl(rawScriptUrl)
        val validationError = validateUrl(scriptUrl)
        if (validationError != null) {
            return@withContext Result.failure(Exception(validationError))
        }

        try {
            val rootJson = JSONObject()
            rootJson.put("action", "save_all")

            val allDataArray = JSONArray()
            val aktifArray = JSONArray()
            val meninggalArray = JSONArray()
            val pindahArray = JSONArray()

            pendudukList.forEach { p ->
                val jsonObj = pendudukToJson(p)
                allDataArray.put(jsonObj)
                when {
                    p.isMeninggal() -> meninggalArray.put(jsonObj)
                    p.isPindah() -> pindahArray.put(jsonObj)
                    else -> aktifArray.put(jsonObj)
                }
            }

            rootJson.put("data", allDataArray)
            rootJson.put("data_aktif", aktifArray)
            rootJson.put("data_meninggal", meninggalArray)
            rootJson.put("data_pindah", pindahArray)

            val logsArray = JSONArray()
            logsList.forEach { log ->
                val logObj = JSONObject().apply {
                    put("timestamp", log.timestamp)
                    put("operator", log.operator)
                    put("action", log.action)
                    put("target", log.target)
                    put("detail", log.detail)
                    put("status", log.status)
                    put("dataBefore", log.dataBefore ?: "")
                    put("dataAfter", log.dataAfter ?: "")
                }
                logsArray.put(logObj)
            }
            rootJson.put("logs", logsArray)

            val body = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(scriptUrl)
                .post(body)
                .header("Accept", "application/json")
                .build()

            val (code, responseBody) = executeRequestWithRedirects(request)

            if (code !in 200..299) {
                return@withContext Result.failure(
                    Exception(formatHttpError(code, responseBody))
                )
            }

            if (responseBody.contains("accounts.google.com") || responseBody.contains("ServiceLogin")) {
                return@withContext Result.failure(
                    Exception("Akses Ditolak: Deployment Apps Script memerlukan login Google. Pastikan opsi 'Siapa yang memiliki akses' diatur ke 'Siapa saja' (Anyone).")
                )
            }

            var msg = "Berhasil sinkronisasi ${aktifArray.length()} warga aktif (Sheet DataPenduduk), ${meninggalArray.length()} mutasi meninggal (Sheet Meninggal), dan ${pindahArray.length()} mutasi pindah (Sheet Pindah)."
            try {
                val json = JSONObject(responseBody)
                if (json.optString("status") == "error") {
                    return@withContext Result.failure(Exception(json.optString("message", "Gagal menyimpan data")))
                }
                val serverMsg = json.optString("message", "")
                if (serverMsg.isNotBlank()) {
                    msg = serverMsg
                }
            } catch (_: Exception) {}

            Result.success(msg)
        } catch (e: UnknownHostException) {
            Log.e("AppsScriptService", "Sync all DNS Host error", e)
            Result.failure(
                Exception("Tidak dapat menemukan host server (${e.message}). Pastikan HP terhubung ke internet dan URL Apps Script valid.")
            )
        } catch (e: Exception) {
            Log.e("AppsScriptService", "Sync all error", e)
            Result.failure(Exception("Gagal sinkronisasi data: ${e.localizedMessage}"))
        }
    }

    suspend fun pushSingleLog(
        rawScriptUrl: String,
        log: ActivityLog
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val scriptUrl = cleanUrl(rawScriptUrl)
        if (scriptUrl.isBlank()) return@withContext Result.success(false)
        try {
            val rootJson = JSONObject().apply {
                put("action", "add_log")
                put("log", JSONObject().apply {
                    put("timestamp", log.timestamp)
                    put("operator", log.operator)
                    put("action", log.action)
                    put("target", log.target)
                    put("detail", log.detail)
                    put("status", log.status)
                    put("dataBefore", log.dataBefore ?: "")
                    put("dataAfter", log.dataAfter ?: "")
                })
            }

            val body = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(scriptUrl)
                .post(body)
                .build()

            val (code, _) = executeRequestWithRedirects(request)
            Result.success(code in 200..299)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadDocumentToDrive(
        rawScriptUrl: String,
        nik: String,
        noKk: String,
        nama: String,
        rw: String,
        rt: String,
        jenisDokumen: String,
        fileName: String,
        base64Data: String,
        mimeType: String = "image/jpeg"
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val scriptUrl = cleanUrl(rawScriptUrl)
        val validationError = validateUrl(scriptUrl)
        if (validationError != null) {
            return@withContext Result.failure(Exception(validationError))
        }

        try {
            val rootJson = JSONObject().apply {
                put("action", "upload_document")
                put("nik", nik)
                put("no_kk", noKk)
                put("nama", nama)
                put("rw", rw)
                put("rt", rt)
                put("jenis_dokumen", jenisDokumen)
                put("file_name", fileName)
                put("file_base64", base64Data)
                put("mime_type", mimeType)
            }

            val body = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(scriptUrl)
                .post(body)
                .header("Accept", "application/json")
                .build()

            val (code, responseBody) = executeRequestWithRedirects(request)

            if (code !in 200..299) {
                return@withContext Result.failure(
                    Exception(formatHttpError(code, responseBody))
                )
            }

            val json = JSONObject(responseBody)
            if (json.optString("status") == "error") {
                return@withContext Result.failure(Exception(json.optString("message", "Gagal mengunggah dokumen ke Drive")))
            }

            val fileUrl = json.optString("file_url", json.optString("url", ""))
            val folderPath = json.optString("folder_path", "RW $rw / RT $rt / $nik - $nama / $jenisDokumen")

            Result.success(Pair(fileUrl, folderPath))
        } catch (e: Exception) {
            Log.e("AppsScriptService", "Upload document error", e)
            Result.failure(Exception("Gagal upload ke Google Drive: ${e.localizedMessage}"))
        }
    }

    private fun formatHttpError(code: Int, responseBody: String): String {
        return when (code) {
            404 -> "URL Google Apps Script tidak ditemukan (HTTP 404). Periksa kembali apakah URL yang disalin lengkap dan berakhiran /exec."
            403 -> "Akses Dilarang (HTTP 403). Pastikan hak akses deployment Apps Script diatur ke 'Siapa saja' (Anyone)."
            500 -> "Server Google Apps Script mengalami kesalahan internal (HTTP 500): $responseBody"
            else -> "Koneksi gagal (HTTP $code): ${if (responseBody.length > 100) responseBody.take(100) + "..." else responseBody}"
        }
    }

    private fun parsePendudukFromJson(item: JSONObject, fallbackNo: Int, defaultStatus: String = "AKTIF"): Penduduk {
        val tglLahir = item.optString("tanggalLahir", item.optString("TANGGAL LAHIR", ""))
        val calculatedAge = Penduduk.calculateAge(tglLahir)
        val rawJk = item.optString("jenisKelamin", item.optString("JENIS KELAMIN", item.optString("JK", item.optString("Jenis Kelamin", item.optString("jenis_kelamin", "LAKI-LAKI")))))
        val jk = Penduduk.normalizeGender(rawJk)

        val statusMut = item.optString("statusMutasi", item.optString("STATUS MUTASI", item.optString("STATUS", defaultStatus))).trim().uppercase()
        val finalStatus = if (statusMut in listOf("MENINGGAL", "PINDAH", "AKTIF")) statusMut else defaultStatus

        return Penduduk(
            no = item.optInt("no", item.optInt("NO", fallbackNo)),
            nama = item.optString("nama", item.optString("NAMA", "")),
            nik = item.optString("nik", item.optString("NIK", "")),
            jenisKelamin = jk,
            tempatLahir = item.optString("tempatLahir", item.optString("TEMPAT LAHIR", "")),
            tanggalLahir = tglLahir,
            agama = item.optString("agama", item.optString("AGAMA", "ISLAM")),
            pendidikanTerakhir = item.optString("pendidikanTerakhir", item.optString("PENDIDIKAN TERAKHIR", "SLTA / SEDERAJAT")),
            pekerjaan = item.optString("pekerjaan", item.optString("PEKERJAAN", "WIRASWASTA")),
            gdr = item.optString("gdr", item.optString("GDR", "TIDAK TAHU")),
            statusPerkawinan = item.optString("statusPerkawinan", item.optString("STATUS PERKAWINAN", "BELUM KAWIN")),
            bukuNikah = item.optString("bukuNikah", item.optString("BUKU NIKAH", "TIDAK ADA")),
            shdk = item.optString("shdk", item.optString("SHDK", "KEPALA KELUARGA")),
            kewarganegaraan = item.optString("kewarganegaraan", item.optString("KEWARGANEGARAAN", "WNI")),
            noPaspor = item.optString("noPaspor", item.optString("NO. PASPOR", "-")),
            noKitas = item.optString("noKitas", item.optString("NO KITAS", "-")),
            namaAyah = item.optString("namaAyah", item.optString("NAMA AYAH", "")),
            namaIbu = item.optString("namaIbu", item.optString("NAMA IBU", "")),
            noKk = item.optString("noKk", item.optString("NO KK", "")),
            namaKepalaKeluarga = item.optString("namaKk", item.optString("NAMA KK", item.optString("namaKepalaKeluarga", item.optString("NAMA Kepala Keluarga", item.optString("NAMA KEPALA KELUARGA", ""))))),
            alamat = item.optString("alamat", item.optString("ALAMAT", item.optString("ALAMAT ASAL", item.optString("ALAMAT TERAKHIR", "Dusun Cimanggu")))),
            rw = item.optString("rw", item.optString("RW", item.optString("RW ASAL", "01"))),
            rt = item.optString("rt", item.optString("RT", item.optString("RT ASAL", "01"))),
            umur = item.optInt("umur", item.optInt("UMUR", item.optInt("UMUR SAAT MENINGGAL", calculatedAge))),
            umurLakiLaki = item.optString("umurLakiLaki", item.optString("UMUR LAKI-LAKI", if (jk.equals("LAKI-LAKI", ignoreCase = true)) "$calculatedAge Tahun" else "-")),
            umurPerempuan = item.optString("umurPerempuan", item.optString("UMUR PEREMPUAN", if (jk.equals("PEREMPUAN", ignoreCase = true)) "$calculatedAge Tahun" else "-")),
            kepemilikanEKtp = item.optString("kepemilikanEKtp", item.optString("KEPEMILIKAN E-KTP", "SUDAH MEMILIKI")),
            tanggalPencetakan = item.optString("tanggalPencetakan", item.optString("TANGGAL PENCETAKAN", "")),
            kepemilikanAktaKelahiran = item.optString("kepemilikanAktaKelahiran", item.optString("KEPEMILIKAN AKTA KELAHIRAN", "ADA")),
            kartuKia = item.optString("kartuKia", item.optString("KARTU KIA", "ADA")),
            kartuPkh = item.optString("kartuPkh", item.optString("KARTU PKH", "TIDAK")),
            kartuBpnt = item.optString("kartuBpnt", item.optString("KARTU BPNT", "TIDAK")),
            kartuBpjsKis = item.optString("kartuBpjsKis", item.optString("KARTU BPJS/KIS", "BPJS PBI / KIS")),
            kartuKip = item.optString("kartuKip", item.optString("KARTU KIP", "TIDAK")),
            jenisKb = item.optString("jenisKb", item.optString("JENIS KB", "BUKAN PESERTA KB")),
            usahaYangDijalankan = item.optString("usahaYangDijalankan", item.optString("USAHA YANG DIJALANKAN", "-")),
            listrikJenis = item.optString("listrikJenis", item.optString("LISTRIK (TOKEN/ PASCA BAYAR)", "TOKEN")),
            kepemilikanListrik = item.optString("kepemilikanListrik", item.optString("KEPEMILIKAN LISTRIK", "SENDIRI")),
            dayaListrik = item.optString("dayaListrik", item.optString("DAYA LISTRIK", "900 VA")),
            noTokenKwh = item.optString("noTokenKwh", item.optString("NO TOKEN / KWH", "-")),
            noHandphone = item.optString("noHandphone", item.optString("NO HANDPHONE", "-")),
            anakKe = item.optInt("anakKe", item.optInt("ANAK KE", 1)),
            kepemilikanRumah = item.optString("kepemilikanRumah", item.optString("KEPEMILIKAN RUMAH", "MILIK SENDIRI")),
            ukuranRumah = item.optString("ukuranRumah", item.optString("UKURAN RUMAH", "6x8 Meter")),
            jenisRumah = item.optString("jenisRumah", item.optString("JENIS RUMAH", "PERMANEN")),
            keterangan = item.optString("keterangan", item.optString("KETERANGAN", "-")),
            vaksinasi = item.optString("vaksinasi", item.optString("VAKSINASI", "DOSIS 3")),
            disabilitas = item.optString("disabilitas", item.optString("DISABILITAS", "TIDAK")),
            // Status & Data Mutasi
            statusMutasi = finalStatus,
            // Mutasi Kematian
            tanggalKematian = item.optString("tanggalKematian", item.optString("TANGGAL KEMATIAN", "")),
            waktuKematian = item.optString("waktuKematian", item.optString("WAKTU KEMATIAN", "")),
            tempatKematian = item.optString("tempatKematian", item.optString("TEMPAT KEMATIAN", "")),
            penyebabKematian = item.optString("penyebabKematian", item.optString("PENYEBAB KEMATIAN", "")),
            tempatPemakaman = item.optString("tempatPemakaman", item.optString("TEMPAT PEMAKAMAN", "")),
            noSuratKematian = item.optString("noSuratKematian", item.optString("NO SURAT KEMATIAN", "")),
            namaPelaporKematian = item.optString("namaPelaporKematian", item.optString("NAMA PELAPOR KEMATIAN", item.optString("NAMA PELAPOR", ""))),
            hubunganPelaporKematian = item.optString("hubunganPelaporKematian", item.optString("HUBUNGAN PELAPOR KEMATIAN", item.optString("HUBUNGAN PELAPOR", ""))),
            catatanKematian = item.optString("catatanKematian", item.optString("CATATAN KEMATIAN", "")),
            // Mutasi Pindah
            tanggalPindah = item.optString("tanggalPindah", item.optString("TANGGAL PINDAH", "")),
            alasanPindah = item.optString("alasanPindah", item.optString("ALASAN PINDAH", "")),
            klasifikasiPindah = item.optString("klasifikasiPindah", item.optString("KLASIFIKASI PINDAH", "")),
            alamatTujuan = item.optString("alamatTujuan", item.optString("ALAMAT TUJUAN", "")),
            rtTujuan = item.optString("rtTujuan", item.optString("RT TUJUAN", "")),
            rwTujuan = item.optString("rwTujuan", item.optString("RW TUJUAN", "")),
            desaTujuan = item.optString("desaTujuan", item.optString("DESA TUJUAN", "")),
            kecamatanTujuan = item.optString("kecamatanTujuan", item.optString("KECAMATAN TUJUAN", "")),
            kabupatenTujuan = item.optString("kabupatenTujuan", item.optString("KABUPATEN TUJUAN", "")),
            provinsiTujuan = item.optString("provinsiTujuan", item.optString("PROVINSI TUJUAN", "")),
            kodePosTujuan = item.optString("kodePosTujuan", item.optString("KODE POS TUJUAN", "")),
            noSuratPindah = item.optString("noSuratPindah", item.optString("NO SURAT PINDAH", "")),
            catatanPindah = item.optString("catatanPindah", item.optString("CATATAN PINDAH", ""))
        )
    }

    private fun pendudukToJson(p: Penduduk): JSONObject {
        return JSONObject().apply {
            put("NO", p.no)
            put("NAMA", p.nama)
            put("NIK", p.nik)
            put("JENIS KELAMIN", p.jenisKelamin)
            put("TEMPAT LAHIR", p.tempatLahir)
            put("TANGGAL LAHIR", p.tanggalLahir)
            put("AGAMA", p.agama)
            put("PENDIDIKAN TERAKHIR", p.pendidikanTerakhir)
            put("PEKERJAAN", p.pekerjaan)
            put("GDR", p.gdr)
            put("STATUS PERKAWINAN", p.statusPerkawinan)
            put("BUKU NIKAH", p.bukuNikah)
            put("SHDK", p.shdk)
            put("KEWARGANEGARAAN", p.kewarganegaraan)
            put("NO. PASPOR", p.noPaspor)
            put("NO KITAS", p.noKitas)
            put("NAMA AYAH", p.namaAyah)
            put("NAMA IBU", p.namaIbu)
            put("NO KK", p.noKk)
            put("NAMA KK", p.namaKepalaKeluarga)
            put("NAMA KEPALA KELUARGA", p.namaKepalaKeluarga)
            put("NAMA Kepala Keluarga", p.namaKepalaKeluarga)
            put("ALAMAT", p.alamat)
            put("ALAMAT ASAL", p.alamat)
            put("ALAMAT TERAKHIR", p.alamat)
            put("RW", p.rw)
            put("RW ASAL", p.rw)
            put("RT", p.rt)
            put("RT ASAL", p.rt)
            put("UMUR", p.umur)
            put("UMUR SAAT MENINGGAL", p.umur)
            put("KEPEMILIKAN E-KTP", p.kepemilikanEKtp)
            put("TANGGAL PENCETAKAN", p.tanggalPencetakan)
            put("KEPEMILIKAN AKTA KELAHIRAN", p.kepemilikanAktaKelahiran)
            put("KARTU KIA", p.kartuKia)
            put("KARTU PKH", p.kartuPkh)
            put("KARTU BPNT", p.kartuBpnt)
            put("KARTU BPJS/KIS", p.kartuBpjsKis)
            put("KARTU KIP", p.kartuKip)
            put("JENIS KB", p.jenisKb)
            put("USAHA YANG DIJALANKAN", p.usahaYangDijalankan)
            put("LISTRIK (TOKEN/ PASCA BAYAR)", p.listrikJenis)
            put("KEPEMILIKAN LISTRIK", p.kepemilikanListrik)
            put("DAYA LISTRIK", p.dayaListrik)
            put("NO TOKEN / KWH", p.noTokenKwh)
            put("NO HANDPHONE", p.noHandphone)
            put("ANAK KE", p.anakKe)
            put("KEPEMILIKAN RUMAH", p.kepemilikanRumah)
            put("UKURAN RUMAH", p.ukuranRumah)
            put("JENIS RUMAH", p.jenisRumah)
            put("KETERANGAN", p.keterangan)
            put("VAKSINASI", p.vaksinasi)
            put("DISABILITAS", p.disabilitas)
            
            // Status & Mutasi
            put("STATUS MUTASI", p.statusMutasi)
            put("STATUS", p.statusMutasi)

            // Mutasi Kematian
            put("TANGGAL KEMATIAN", p.tanggalKematian)
            put("WAKTU KEMATIAN", p.waktuKematian)
            put("TEMPAT KEMATIAN", p.tempatKematian)
            put("PENYEBAB KEMATIAN", p.penyebabKematian)
            put("TEMPAT PEMAKAMAN", p.tempatPemakaman)
            put("NO SURAT KEMATIAN", p.noSuratKematian)
            put("NAMA PELAPOR", p.namaPelaporKematian)
            put("NAMA PELAPOR KEMATIAN", p.namaPelaporKematian)
            put("HUBUNGAN PELAPOR", p.hubunganPelaporKematian)
            put("HUBUNGAN PELAPOR KEMATIAN", p.hubunganPelaporKematian)
            put("CATATAN KEMATIAN", p.catatanKematian)

            // Mutasi Pindah
            put("TANGGAL PINDAH", p.tanggalPindah)
            put("ALASAN PINDAH", p.alasanPindah)
            put("KLASIFIKASI PINDAH", p.klasifikasiPindah)
            put("ALAMAT TUJUAN", p.alamatTujuan)
            put("RT TUJUAN", p.rtTujuan)
            put("RW TUJUAN", p.rwTujuan)
            put("DESA TUJUAN", p.desaTujuan)
            put("KECAMATAN TUJUAN", p.kecamatanTujuan)
            put("KABUPATEN TUJUAN", p.kabupatenTujuan)
            put("PROVINSI TUJUAN", p.provinsiTujuan)
            put("KODE POS TUJUAN", p.kodePosTujuan)
            put("NO SURAT PINDAH", p.noSuratPindah)
            put("CATATAN PINDAH", p.catatanPindah)
        }
    }

    companion object {
        val GOOGLE_APPS_SCRIPT_TEMPLATE = """
/**
 * GOOGLE APPS SCRIPT - SISTEM DATABASE KEPENDUDUKAN DESA CIMANGGU
 * (Multi-Sheet Otomatis: DataPenduduk, Meninggal, Pindah, & logaktivitas)
 * 
 * PETUNJUK PEMASANGAN LENGKAP:
 * 1. Buka Google Spreadsheet Anda (atau buat Spreadsheet baru di Google Drive).
 * 2. Buka menu: Ekstensi (Extensions) > Apps Script.
 * 3. Hapus SEMUA kode bawaan yang ada di editor, lalu TEMPEL (PASTE) seluruh kode ini.
 * 4. Klik tombol 'Simpan' (ikon disket / Ctrl+S).
 * 5. Klik tombol 'Terapkan' (Deploy) di pojok kanan atas > Pilih 'Penerapan baru' (New deployment).
 * 6. Klik ikon Roda Gigi di sebelah 'Pilih jenis' > Pilih 'Aplikasi Web' (Web app).
 * 7. Atur konfigurasi PENTING berikut:
 *    - Deskripsi: SIMDes Kependudukan Multi-Sheet Mutasi
 *    - Jalankan sebagai (Execute as): 'Saya' (Me)
 *    - Siapa yang memiliki akses (Who has access): 'Siapa saja' (Anyone)  <-- WAJIB PILIH INI!
 * 8. Klik 'Terapkan' (Deploy) lalu klik 'Beri Akses' (Authorize access) dan izinkan akun Google Anda.
 * 9. Salin 'URL Aplikasi Web' (URL berakhiran /exec) dan tempelkan ke aplikasi Android.
 * 
 * STRUKTUR SHEET OTOMATIS:
 * - "DataPenduduk" : 46 Kolom Database Warga Aktif
 * - "Meninggal"    : Data Lengkap Warga Meninggal + Berkas Kematian
 * - "Pindah"       : Data Lengkap Warga Pindah Keluar + Alamat Tujuan
 * - "logaktivitas" : Log Riwayat Transaksi Data & Mutasi
 */

// 1. HEADERS SHEET WARGA AKTIF (DataPenduduk)
const HEADERS_PENDUDUK = [
  "NO", "NAMA", "NIK", "JENIS KELAMIN", "TEMPAT LAHIR", "TANGGAL LAHIR", "AGAMA",
  "PENDIDIKAN TERAKHIR", "PEKERJAAN", "GDR", "STATUS PERKAWINAN", "BUKU NIKAH",
  "SHDK", "KEWARGANEGARAAN", "NO. PASPOR", "NO KITAS", "NAMA AYAH", "NAMA IBU",
  "NO KK", "NAMA KK", "ALAMAT", "RW", "RT", "UMUR",
  "KEPEMILIKAN E-KTP", "TANGGAL PENCETAKAN", "KEPEMILIKAN AKTA KELAHIRAN",
  "KARTU KIA", "KARTU PKH", "KARTU BPNT", "KARTU BPJS/KIS", "KARTU KIP", "JENIS KB",
  "USAHA YANG DIJALANKAN", "LISTRIK (TOKEN/ PASCA BAYAR)", "KEPEMILIKAN LISTRIK",
  "DAYA LISTRIK", "NO TOKEN / KWH", "NO HANDPHONE", "ANAK KE", "KEPEMILIKAN RUMAH",
  "UKURAN RUMAH", "JENIS RUMAH", "KETERANGAN", "VAKSINASI", "DISABILITAS"
];

// 2. HEADERS SHEET WARGA MENINGGAL (Meninggal)
const HEADERS_MENINGGAL = [
  "NO", "NAMA", "NIK", "JENIS KELAMIN", "TEMPAT LAHIR", "TANGGAL LAHIR", "AGAMA",
  "SHDK", "NO KK", "NAMA KK", "ALAMAT TERAKHIR", "RW", "RT", "UMUR SAAT MENINGGAL",
  "TANGGAL KEMATIAN", "WAKTU KEMATIAN", "TEMPAT KEMATIAN", "PENYEBAB KEMATIAN",
  "TEMPAT PEMAKAMAN", "NO SURAT KEMATIAN", "NAMA PELAPOR", "HUBUNGAN PELAPOR", "CATATAN KEMATIAN",
  "PENDIDIKAN TERAKHIR", "PEKERJAAN", "GDR", "STATUS PERKAWINAN", "BUKU NIKAH",
  "KEWARGANEGARAAN", "NAMA AYAH", "NAMA IBU", "KEPEMILIKAN E-KTP", "KEPEMILIKAN AKTA KELAHIRAN",
  "KARTU PKH", "KARTU BPNT", "KARTU BPJS/KIS", "DISABILITAS"
];

// 3. HEADERS SHEET WARGA PINDAH (Pindah)
const HEADERS_PINDAH = [
  "NO", "NAMA", "NIK", "JENIS KELAMIN", "TEMPAT LAHIR", "TANGGAL LAHIR", "AGAMA",
  "SHDK", "NO KK", "NAMA KK", "ALAMAT ASAL", "RW ASAL", "RT ASAL", "UMUR",
  "TANGGAL PINDAH", "ALASAN PINDAH", "KLASIFIKASI PINDAH",
  "ALAMAT TUJUAN", "RT TUJUAN", "RW TUJUAN", "DESA TUJUAN", "KECAMATAN TUJUAN", "KABUPATEN TUJUAN", "PROVINSI TUJUAN", "KODE POS TUJUAN",
  "NO SURAT PINDAH", "CATATAN PINDAH",
  "PENDIDIKAN TERAKHIR", "PEKERJAAN", "STATUS PERKAWINAN", "NAMA AYAH", "NAMA IBU",
  "KARTU PKH", "KARTU BPNT", "KARTU BPJS/KIS", "DISABILITAS"
];

// 4. HEADERS SHEET LOG AKTIVITAS (logaktivitas)
const LOG_HEADERS = ["TIMESTAMP", "OPERATOR", "AKSI", "TARGET", "DETAIL", "STATUS", "DATA SEBELUM", "DATA SESUDAH"];

function getSpreadsheet() {
  try {
    const active = SpreadsheetApp.getActiveSpreadsheet();
    if (active) return active;
  } catch (e) {}
  
  const propId = PropertiesService.getScriptProperties().getProperty("SPREADSHEET_ID");
  if (propId) {
    return SpreadsheetApp.openById(propId);
  }
  throw new Error("Spreadsheet tidak terhubung. Pastikan script dibuat dari menu Ekstensi > Apps Script di dalam Google Spreadsheet.");
}

function doGet(e) {
  try {
    const action = (e && e.parameter && e.parameter.action) ? e.parameter.action : "get_all";
    
    if (action === "test") {
      return createJsonResponse({
        status: "success",
        message: "Koneksi Google Apps Script berhasil terhubung! (Multi-Sheet Mutasi Aktif)",
        timestamp: new Date().toISOString()
      });
    }
    
    const ss = getSpreadsheet();
    const sheetData = getOrCreateSheet(ss, "DataPenduduk", HEADERS_PENDUDUK, "#1B5E20", "#FFFFFF");
    const sheetMeninggal = getOrCreateSheet(ss, "Meninggal", HEADERS_MENINGGAL, "#37474F", "#FFFFFF");
    const sheetPindah = getOrCreateSheet(ss, "Pindah", HEADERS_PINDAH, "#E65100", "#FFFFFF");
    const sheetLogs = getOrCreateSheet(ss, "logaktivitas", LOG_HEADERS, "#512DA8", "#FFFFFF");
    
    // Read Active Residents
    const rawAktif = sheetData.getDataRange().getValues();
    const listAktif = parseSheetRows(rawAktif, "AKTIF");
    
    // Read Deceased Residents
    const rawMeninggal = sheetMeninggal.getDataRange().getValues();
    const listMeninggal = parseSheetRows(rawMeninggal, "MENINGGAL");
    
    // Read Relocated Residents
    const rawPindah = sheetPindah.getDataRange().getValues();
    const listPindah = parseSheetRows(rawPindah, "PINDAH");
    
    // Combined all residents
    const allResidents = [].concat(listAktif, listMeninggal, listPindah);
    
    // Read Logs
    const rawLogs = sheetLogs.getDataRange().getValues();
    const logsList = [];
    if (rawLogs.length > 1) {
      for (let i = 1; i < rawLogs.length; i++) {
        const row = rawLogs[i];
        if (!row[0] && !row[1] && !row[2]) continue;
        logsList.push({
          timestamp: row[0] ? String(row[0]) : "",
          operator: row[1] ? String(row[1]) : "",
          action: row[2] ? String(row[2]) : "",
          target: row[3] ? String(row[3]) : "",
          detail: row[4] ? String(row[4]) : "",
          status: row[5] ? String(row[5]) : "BERHASIL",
          dataBefore: row[6] ? String(row[6]) : "",
          dataAfter: row[7] ? String(row[7]) : ""
        });
      }
    }
    
    return createJsonResponse({
      status: "success",
      message: "Data kependudukan multi-sheet berhasil dimuat",
      totalData: allResidents.length,
      stats: {
        totalAktif: listAktif.length,
        totalMeninggal: listMeninggal.length,
        totalPindah: listPindah.length,
        totalSemua: allResidents.length
      },
      data: allResidents,
      dataAktif: listAktif,
      dataMeninggal: listMeninggal,
      dataPindah: listPindah,
      logs: logsList
    });
  } catch (err) {
    return createJsonResponse({
      status: "error",
      message: "Terjadi kesalahan di Google Apps Script: " + (err.message || err.toString())
    });
  }
}

function parseSheetRows(rawData, defaultStatus) {
  const list = [];
  if (rawData.length <= 1) return list;
  
  const headers = rawData[0].map(function(h) { return String(h).trim().toUpperCase(); });
  for (let i = 1; i < rawData.length; i++) {
    const row = rawData[i];
    const hasContent = row.some(function(cell) {
      return cell !== null && cell !== undefined && String(cell).trim() !== "";
    });
    if (!hasContent) continue;
    
    const obj = {};
    for (let j = 0; j < headers.length; j++) {
      let val = row[j];
      if (val === null || val === undefined) {
        val = "";
      } else if (val instanceof Date) {
        val = Utilities.formatDate(val, Session.getScriptTimeZone() || "Asia/Jakarta", "yyyy-MM-dd");
      } else {
        val = String(val).trim();
      }
      obj[headers[j]] = val;
    }
    
    if (!obj["NAMA KK"]) {
      obj["NAMA KK"] = obj["NAMA KEPALA KELUARGA"] || obj["NAMA Kepala Keluarga"] || "";
    }
    if (!obj["STATUS MUTASI"] && !obj["STATUS"]) {
      obj["STATUS MUTASI"] = defaultStatus;
      obj["STATUS"] = defaultStatus;
    }
    
    list.push(obj);
  }
  return list;
}

function doPost(e) {
  try {
    let body = {};
    if (e && e.postData && e.postData.contents) {
      body = JSON.parse(e.postData.contents);
    } else if (e && e.parameter) {
      body = e.parameter;
    }
    
    const action = body.action || (e && e.parameter && e.parameter.action) || "save_all";
    const ss = getSpreadsheet();
    
    const sheetData = getOrCreateSheet(ss, "DataPenduduk", HEADERS_PENDUDUK, "#1B5E20", "#FFFFFF");
    const sheetMeninggal = getOrCreateSheet(ss, "Meninggal", HEADERS_MENINGGAL, "#37474F", "#FFFFFF");
    const sheetPindah = getOrCreateSheet(ss, "Pindah", HEADERS_PINDAH, "#E65100", "#FFFFFF");
    const sheetLogs = getOrCreateSheet(ss, "logaktivitas", LOG_HEADERS, "#512DA8", "#FFFFFF");
    
    if (action === "save_all") {
      let allItems = [];
      if (body.data) {
        allItems = typeof body.data === "string" ? JSON.parse(body.data) : body.data;
      }
      
      let aktifItems = [];
      let meninggalItems = [];
      let pindahItems = [];
      
      if (body.data_aktif || body.data_meninggal || body.data_pindah) {
        aktifItems = body.data_aktif ? (typeof body.data_aktif === "string" ? JSON.parse(body.data_aktif) : body.data_aktif) : [];
        meninggalItems = body.data_meninggal ? (typeof body.data_meninggal === "string" ? JSON.parse(body.data_meninggal) : body.data_meninggal) : [];
        pindahItems = body.data_pindah ? (typeof body.data_pindah === "string" ? JSON.parse(body.data_pindah) : body.data_pindah) : [];
      } else {
        // Separate items by mutation status
        for (let i = 0; i < allItems.length; i++) {
          const item = allItems[i];
          const status = (item["STATUS MUTASI"] || item["STATUS"] || item["statusMutasi"] || "").toUpperCase();
          if (status === "MENINGGAL" || item["TANGGAL KEMATIAN"] || item["tanggalKematian"]) {
            meninggalItems.push(item);
          } else if (status === "PINDAH" || item["TANGGAL PINDAH"] || item["tanggalPindah"]) {
            pindahItems.push(item);
          } else {
            aktifItems.push(item);
          }
        }
      }
      
      // 1. Populate Sheet DataPenduduk (Warga Aktif)
      writeItemsToSheet(sheetData, HEADERS_PENDUDUK, aktifItems);
      
      // 2. Populate Sheet Meninggal
      writeItemsToSheet(sheetMeninggal, HEADERS_MENINGGAL, meninggalItems);
      
      // 3. Populate Sheet Pindah
      writeItemsToSheet(sheetPindah, HEADERS_PINDAH, pindahItems);
      
      // 4. Populate Sheet logaktivitas
      if (body.logs) {
        const logsItems = typeof body.logs === "string" ? JSON.parse(body.logs) : body.logs;
        if (logsItems.length > 0) {
          sheetLogs.clearContents();
          sheetLogs.appendRow(LOG_HEADERS);
          const logRows = [];
          for (let i = 0; i < logsItems.length; i++) {
            const l = logsItems[i];
            logRows.push([
              l.timestamp || new Date().toISOString(),
              l.operator || "",
              l.action || "",
              l.target || "",
              l.detail || "",
              l.status || "BERHASIL",
              l.dataBefore || l.data_before || "",
              l.dataAfter || l.data_after || ""
            ]);
          }
          sheetLogs.getRange(2, 1, logRows.length, LOG_HEADERS.length).setNumberFormat("@").setValues(logRows);
        }
      }
      
      return createJsonResponse({
        status: "success",
        message: "Berhasil menyimpan: " + aktifItems.length + " warga aktif (Sheet DataPenduduk), " +
                 meninggalItems.length + " mutasi meninggal (Sheet Meninggal), dan " +
                 pindahItems.length + " mutasi pindah (Sheet Pindah).",
        stats: {
          aktif: aktifItems.length,
          meninggal: meninggalItems.length,
          pindah: pindahItems.length,
          total: (aktifItems.length + meninggalItems.length + pindahItems.length)
        }
      });
    }
    
    if (action === "add_log" && body.log) {
      const l = typeof body.log === "string" ? JSON.parse(body.log) : body.log;
      sheetLogs.appendRow([
        l.timestamp || new Date().toISOString(),
        l.operator || "",
        l.action || "",
        l.target || "",
        l.detail || "",
        l.status || "BERHASIL",
        l.dataBefore || l.data_before || "",
        l.dataAfter || l.data_after || ""
      ]);
      return createJsonResponse({ status: "success", message: "Log aktivitas berhasil dicatat" });
    }

    if (action === "upload_document") {
      const rw = String(body.rw || "01").trim();
      const rt = String(body.rt || "001").trim();
      const nik = String(body.nik || "0000").trim();
      const nama = String(body.nama || "Warga").trim();
      const jenisDokumen = String(body.jenis_dokumen || "Dokumen").trim();
      const fileName = String(body.file_name || (jenisDokumen + "_" + nik + "_" + new Date().getTime() + ".jpg")).trim();
      const fileBase64 = body.file_base64 || "";
      const mimeType = body.mime_type || "image/jpeg";

      if (!fileBase64) {
        return createJsonResponse({ status: "error", message: "Data file Base64 kosong" });
      }

      // 1. Folder Root: "SIMDes_Dokumen_Desa"
      const rootFolder = getOrCreateDriveFolder(DriveApp.getRootFolder(), "SIMDes_Dokumen_Desa");
      // 2. Folder RW
      const rwFolderName = rw.toLowerCase().startsWith("rw") ? rw : ("RW " + (rw.length === 1 ? "0" + rw : rw));
      const rwFolder = getOrCreateDriveFolder(rootFolder, rwFolderName);
      // 3. Folder RT
      const rtFolderName = rt.toLowerCase().startsWith("rt") ? rt : ("RT " + (rt.length === 1 ? "00" + rt : (rt.length === 2 ? "0" + rt : rt)));
      const rtFolder = getOrCreateDriveFolder(rwFolder, rtFolderName);
      // 4. Folder Warga: "[NIK] - [Nama]"
      const residentFolderName = nik + " - " + nama;
      const residentFolder = getOrCreateDriveFolder(rtFolder, residentFolderName);
      // 5. Folder Kategori Dokumen
      const categoryFolder = getOrCreateDriveFolder(residentFolder, jenisDokumen);

      // Simpan File
      const decodedBytes = Utilities.base64Decode(fileBase64);
      const blob = Utilities.newBlob(decodedBytes, mimeType, fileName);
      const file = categoryFolder.createFile(blob);

      try {
        file.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
      } catch (shareErr) {}

      const fileUrl = file.getUrl();
      const folderPath = "SIMDes_Dokumen_Desa / " + rwFolderName + " / " + rtFolderName + " / " + residentFolderName + " / " + jenisDokumen;

      return createJsonResponse({
        status: "success",
        message: "Dokumen berhasil diunggah ke Google Drive",
        file_url: fileUrl,
        file_id: file.getId(),
        folder_path: folderPath
      });
    }
    
    return createJsonResponse({ status: "error", message: "Aksi tidak dikenali: " + action });
  } catch (err) {
    return createJsonResponse({
      status: "error",
      message: "Gagal menyimpan ke Google Apps Script: " + (err.message || err.toString())
    });
  }
}

function writeItemsToSheet(sheet, headers, items) {
  sheet.clearContents();
  sheet.appendRow(headers);
  
  if (items.length === 0) return;
  
  const rows = [];
  for (let i = 0; i < items.length; i++) {
    const item = items[i];
    const row = [];
    for (let j = 0; j < headers.length; j++) {
      const headerKey = headers[j];
      let cellVal = item[headerKey];
      if (cellVal === undefined || cellVal === null) {
        cellVal = item[headerKey.toLowerCase()] || "";
      }
      row.push(String(cellVal));
    }
    rows.push(row);
  }
  
  if (rows.length > 0) {
    sheet.getRange(2, 1, rows.length, headers.length).setNumberFormat("@").setValues(rows);
  }
}

function getOrCreateSheet(ss, name, headers, bgColor, textColor) {
  let sheet = ss.getSheetByName(name);
  if (!sheet) {
    sheet = ss.insertSheet(name);
  }
  
  // Format Header Row
  if (sheet.getLastRow() === 0) {
    sheet.appendRow(headers);
  }
  
  try {
    sheet.setFrozenRows(1);
    const headerRange = sheet.getRange(1, 1, 1, headers.length);
    headerRange.setFontWeight("bold");
    if (bgColor) headerRange.setBackground(bgColor);
    if (textColor) headerRange.setFontColor(textColor);
  } catch (e) {}
  
  return sheet;
}

function getOrCreateDriveFolder(parentFolder, folderName) {
  const folders = parentFolder.getFoldersByName(folderName);
  if (folders.hasNext()) {
    return folders.next();
  } else {
    return parentFolder.createFolder(folderName);
  }
}

function createJsonResponse(data) {
  return ContentService.createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}
        """.trimIndent()

        val INDEX_HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SIMDes Kependudukan - Portal Data Penduduk & Mutasi</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <style>
    body { font-family: 'Plus Jakarta Sans', sans-serif; background-color: #F8F9FA; }
  </style>
</head>
<body class="text-slate-800 antialiased min-h-screen flex flex-col">
  <header class="bg-[#1B5E20] text-white shadow-md sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3.5 flex items-center justify-between">
      <div class="flex items-center space-x-3">
        <div class="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center text-white font-bold text-xl shadow-inner">
          <i class="fa-solid fa-landmark"></i>
        </div>
        <div>
          <h1 class="text-base sm:text-lg font-extrabold tracking-tight">SIMDes Kependudukan & Mutasi</h1>
          <p class="text-xs text-emerald-200">Desa Cimanggu • Multi-Sheet Terintegrasi Google Spreadsheet</p>
        </div>
      </div>
      <div class="flex items-center space-x-2">
        <span class="px-3 py-1 bg-white/15 rounded-full text-xs font-semibold flex items-center gap-1.5 shadow-sm">
          <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
          Live Sync Active
        </span>
      </div>
    </div>
  </header>

  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 flex-1 w-full space-y-6">
    <!-- Stat Summary Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white p-5 rounded-2xl shadow-sm border border-emerald-100 flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs font-bold uppercase tracking-wider text-emerald-800">Warga Aktif</span>
          <span class="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center text-sm font-bold"><i class="fa-solid fa-users"></i></span>
        </div>
        <div>
          <p id="statAktif" class="text-2xl sm:text-3xl font-extrabold text-emerald-900 mt-2">Loading...</p>
          <p class="text-xs text-emerald-600 mt-0.5">Sheet DataPenduduk</p>
        </div>
      </div>

      <div class="bg-white p-5 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs font-bold uppercase tracking-wider text-slate-700">Kepala Keluarga</span>
          <span class="w-8 h-8 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center text-sm font-bold"><i class="fa-solid fa-house-user"></i></span>
        </div>
        <div>
          <p id="statKk" class="text-2xl sm:text-3xl font-extrabold text-slate-900 mt-2">Loading...</p>
          <p class="text-xs text-slate-500 mt-0.5">KK Terdata</p>
        </div>
      </div>

      <div class="bg-white p-5 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs font-bold uppercase tracking-wider text-slate-700">Mutasi Meninggal</span>
          <span class="w-8 h-8 rounded-lg bg-slate-100 text-slate-700 flex items-center justify-center text-sm font-bold"><i class="fa-solid fa-cross"></i></span>
        </div>
        <div>
          <p id="statMeninggal" class="text-2xl sm:text-3xl font-extrabold text-slate-800 mt-2">0</p>
          <p class="text-xs text-slate-500 mt-0.5">Sheet Meninggal</p>
        </div>
      </div>

      <div class="bg-white p-5 rounded-2xl shadow-sm border border-orange-100 flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs font-bold uppercase tracking-wider text-orange-800">Mutasi Pindah</span>
          <span class="w-8 h-8 rounded-lg bg-orange-50 text-orange-600 flex items-center justify-center text-sm font-bold"><i class="fa-solid fa-truck-moving"></i></span>
        </div>
        <div>
          <p id="statPindah" class="text-2xl sm:text-3xl font-extrabold text-orange-900 mt-2">0</p>
          <p class="text-xs text-orange-600 mt-0.5">Sheet Pindah</p>
        </div>
      </div>
    </div>

    <!-- Tab Selector & Filter Toolbar -->
    <div class="bg-white p-4 sm:p-5 rounded-2xl shadow-sm border border-slate-200 space-y-4">
      <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 pb-3">
        <!-- Tabs -->
        <div class="flex space-x-1.5 bg-slate-100 p-1 rounded-xl text-xs font-bold">
          <button onclick="switchTab('aktif')" id="tabBtnAktif" class="px-3.5 py-1.5 rounded-lg bg-white shadow-sm text-emerald-800 font-extrabold transition">
            🟢 Warga Aktif (<span id="tabCountAktif">0</span>)
          </button>
          <button onclick="switchTab('meninggal')" id="tabBtnMeninggal" class="px-3.5 py-1.5 rounded-lg text-slate-600 hover:text-slate-900 transition">
            ⬛ Meninggal (<span id="tabCountMeninggal">0</span>)
          </button>
          <button onclick="switchTab('pindah')" id="tabBtnPindah" class="px-3.5 py-1.5 rounded-lg text-slate-600 hover:text-slate-900 transition">
            🟧 Pindah (<span id="tabCountPindah">0</span>)
          </button>
        </div>

        <button onclick="fetchData()" class="px-4 py-2 bg-[#1B5E20] text-white rounded-xl hover:bg-[#154a19] transition font-semibold text-xs flex items-center gap-2 shadow-sm">
          <i class="fa-solid fa-arrows-rotate"></i> Muat Ulang Data
        </button>
      </div>

      <div class="flex flex-col sm:flex-row gap-3">
        <div class="relative flex-1">
          <i class="fa-solid fa-magnifying-glass absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-xs"></i>
          <input type="text" id="searchInput" placeholder="Cari Nama, NIK, No KK, Alamat, atau Catatan..." 
                 class="w-full pl-10 pr-4 py-2 bg-slate-50 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 text-xs">
        </div>
        <select id="rwFilter" class="px-3 py-2 bg-slate-50 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 text-xs font-medium">
          <option value="">Semua RW</option>
          <option value="01">RW 01</option>
          <option value="02">RW 02</option>
          <option value="03">RW 03</option>
          <option value="04">RW 04</option>
          <option value="05">RW 05</option>
        </select>
        <select id="rtFilter" class="px-3 py-2 bg-slate-50 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 text-xs font-medium">
          <option value="">Semua RT</option>
          <option value="01">RT 01</option>
          <option value="02">RT 02</option>
          <option value="03">RT 03</option>
          <option value="04">RT 04</option>
          <option value="05">RT 05</option>
          <option value="06">RT 06</option>
          <option value="07">RT 07</option>
          <option value="08">RT 08</option>
        </select>
      </div>
    </div>

    <!-- Data Table Card -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
      <div class="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
        <h2 id="tableTitle" class="font-bold text-slate-800 text-xs sm:text-sm flex items-center gap-2">
          <i class="fa-solid fa-table-list text-emerald-700"></i>
          Daftar Warga Aktif (Sheet DataPenduduk)
        </h2>
        <span id="filteredCountBadge" class="text-xs px-2.5 py-1 bg-emerald-50 text-emerald-800 rounded-lg font-bold">0 Data</span>
      </div>

      <div class="overflow-x-auto max-h-[520px]">
        <table class="w-full text-left text-xs">
          <thead id="tableHead" class="bg-slate-50 text-slate-700 font-bold sticky top-0 z-10 uppercase tracking-wider border-b border-slate-200">
            <!-- Dynamic Headers based on Active Tab -->
          </thead>
          <tbody id="tableBody" class="divide-y divide-slate-100">
            <tr>
              <td colspan="10" class="px-4 py-12 text-center text-slate-400">
                <i class="fa-solid fa-spinner fa-spin text-2xl text-emerald-600 mb-2 block"></i>
                Memuat data dari Google Spreadsheet...
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </main>

  <footer class="bg-white border-t border-slate-200 py-4 text-center text-xs text-slate-500">
    SIMDes Kependudukan &copy; 2026 Pemerintah Desa Cimanggu. Terhubung Multi-Sheet Spreadsheet.
  </footer>

  <script>
    let rawDataAktif = [];
    let rawDataMeninggal = [];
    let rawDataPindah = [];
    let currentTab = 'aktif';

    async function fetchData() {
      const tbody = document.getElementById('tableBody');
      tbody.innerHTML = '<tr><td colspan="10" class="px-4 py-12 text-center text-slate-400"><i class="fa-solid fa-spinner fa-spin text-2xl text-emerald-600 mb-2 block"></i>Memuat data multi-sheet...</td></tr>';

      try {
        const baseUrl = window.location.href.split('?')[0];
        const response = await fetch(baseUrl + '?action=get_all');
        const result = await response.json();
        
        if (result.status === 'success' || result.data) {
          rawDataAktif = result.dataAktif || (result.data || []).filter(d => (d['STATUS MUTASI'] || d['STATUS'] || 'AKTIF') === 'AKTIF');
          rawDataMeninggal = result.dataMeninggal || (result.data || []).filter(d => (d['STATUS MUTASI'] || d['STATUS']) === 'MENINGGAL');
          rawDataPindah = result.dataPindah || (result.data || []).filter(d => (d['STATUS MUTASI'] || d['STATUS']) === 'PINDAH');
          
          document.getElementById('statAktif').innerText = rawDataAktif.length;
          document.getElementById('statMeninggal').innerText = rawDataMeninggal.length;
          document.getElementById('statPindah').innerText = rawDataPindah.length;
          
          const kks = new Set(rawDataAktif.map(d => d['NO KK']).filter(Boolean));
          document.getElementById('statKk').innerText = kks.size;
          
          document.getElementById('tabCountAktif').innerText = rawDataAktif.length;
          document.getElementById('tabCountMeninggal').innerText = rawDataMeninggal.length;
          document.getElementById('tabCountPindah').innerText = rawDataPindah.length;

          renderTable();
        } else {
          tbody.innerHTML = '<tr><td colspan="10" class="px-4 py-8 text-center text-red-500 font-semibold">' + (result.message || 'Gagal memuat data') + '</td></tr>';
        }
      } catch (err) {
        tbody.innerHTML = '<tr><td colspan="10" class="px-4 py-8 text-center text-red-500 font-semibold">Gagal memuat data: ' + err.message + '</td></tr>';
      }
    }

    function switchTab(tab) {
      currentTab = tab;
      
      const btnAktif = document.getElementById('tabBtnAktif');
      const btnMeninggal = document.getElementById('tabBtnMeninggal');
      const btnPindah = document.getElementById('tabBtnPindah');
      
      btnAktif.className = tab === 'aktif' ? 'px-3.5 py-1.5 rounded-lg bg-white shadow-sm text-emerald-800 font-extrabold transition' : 'px-3.5 py-1.5 rounded-lg text-slate-600 hover:text-slate-900 transition';
      btnMeninggal.className = tab === 'meninggal' ? 'px-3.5 py-1.5 rounded-lg bg-white shadow-sm text-slate-900 font-extrabold transition' : 'px-3.5 py-1.5 rounded-lg text-slate-600 hover:text-slate-900 transition';
      btnPindah.className = tab === 'pindah' ? 'px-3.5 py-1.5 rounded-lg bg-white shadow-sm text-orange-900 font-extrabold transition' : 'px-3.5 py-1.5 rounded-lg text-slate-600 hover:text-slate-900 transition';

      renderTable();
    }

    function renderTable() {
      const query = (document.getElementById('searchInput').value || '').toLowerCase();
      const rw = document.getElementById('rwFilter').value;
      const rt = document.getElementById('rtFilter').value;

      let dataset = [];
      let headHtml = '';
      const tableTitle = document.getElementById('tableTitle');

      if (currentTab === 'aktif') {
        dataset = rawDataAktif;
        tableTitle.innerHTML = '<i class="fa-solid fa-users text-emerald-700"></i> Daftar Warga Aktif (Sheet DataPenduduk)';
        headHtml = '<tr><th class="px-3 py-3">No</th><th class="px-3 py-3">Nama Lengkap</th><th class="px-3 py-3">NIK</th><th class="px-3 py-3">JK</th><th class="px-3 py-3">Umur</th><th class="px-3 py-3">RT/RW</th><th class="px-3 py-3">Pekerjaan</th><th class="px-3 py-3">SHDK</th><th class="px-3 py-3">Bansos</th><th class="px-3 py-3">E-KTP</th></tr>';
      } else if (currentTab === 'meninggal') {
        dataset = rawDataMeninggal;
        tableTitle.innerHTML = '<i class="fa-solid fa-cross text-slate-700"></i> Mutasi Meninggal Dunia (Sheet Meninggal)';
        headHtml = '<tr><th class="px-3 py-3">No</th><th class="px-3 py-3">Nama Almarhum/ah</th><th class="px-3 py-3">NIK</th><th class="px-3 py-3">Tgl Meninggal</th><th class="px-3 py-3">Tempat Kematian</th><th class="px-3 py-3">Penyebab</th><th class="px-3 py-3">Pemakaman</th><th class="px-3 py-3">Pelapor</th><th class="px-3 py-3">No Surat</th></tr>';
      } else {
        dataset = rawDataPindah;
        tableTitle.innerHTML = '<i class="fa-solid fa-truck-moving text-orange-700"></i> Mutasi Pindah Keluar (Sheet Pindah)';
        headHtml = '<tr><th class="px-3 py-3">No</th><th class="px-3 py-3">Nama Warga</th><th class="px-3 py-3">NIK</th><th class="px-3 py-3">Tgl Pindah</th><th class="px-3 py-3">Alasan Pindah</th><th class="px-3 py-3">Alamat Tujuan</th><th class="px-3 py-3">Desa / Kec Tujuan</th><th class="px-3 py-3">Kab / Prov Tujuan</th><th class="px-3 py-3">No SKPWNI</th></tr>';
      }

      document.getElementById('tableHead').innerHTML = headHtml;

      const filtered = dataset.filter(p => {
        const matchQuery = !query ||
          (p['NAMA'] || '').toLowerCase().includes(query) ||
          (p['NIK'] || '').toLowerCase().includes(query) ||
          (p['NO KK'] || '').toLowerCase().includes(query) ||
          (p['ALAMAT'] || p['ALAMAT ASAL'] || p['ALAMAT TUJUAN'] || '').toLowerCase().includes(query) ||
          (p['CATATAN KEMATIAN'] || p['CATATAN PINDAH'] || '').toLowerCase().includes(query);
        const matchRw = !rw || (p['RW'] === rw || p['RW ASAL'] === rw);
        const matchRt = !rt || (p['RT'] === rt || p['RT ASAL'] === rt);
        return matchQuery && matchRw && matchRt;
      });

      document.getElementById('filteredCountBadge').innerText = filtered.length + ' Data';
      const tbody = document.getElementById('tableBody');

      if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" class="px-4 py-8 text-center text-slate-400">Tidak ada data yang sesuai.</td></tr>';
        return;
      }

      tbody.innerHTML = filtered.map(function(p, index) {
        var jkBadge = p['JENIS KELAMIN'] === 'LAKI-LAKI' ? '<span class="text-blue-600 font-semibold">L</span>' : '<span class="text-pink-600 font-semibold">P</span>';
        
        if (currentTab === 'aktif') {
          var isBansos = p['KARTU PKH'] === 'YA' || p['KARTU BPNT'] === 'YA' || (p['KARTU BPJS/KIS'] || '').indexOf('PBI') !== -1;
          var bansosBadge = isBansos ? '<span class="px-1.5 py-0.5 bg-emerald-100 text-emerald-800 rounded font-bold text-[10px]">BANSOS</span>' : '<span class="text-slate-400">-</span>';
          var ektpBadge = (p['KEPEMILIKAN E-KTP'] || '').indexOf('SUDAH') !== -1 ? '<span class="text-emerald-600 font-semibold">Ada</span>' : '<span class="text-amber-600">Belum</span>';

          return '<tr class="hover:bg-slate-50 transition">' +
            '<td class="px-3 py-2.5 font-semibold text-slate-500">' + (p['NO'] || (index + 1)) + '</td>' +
            '<td class="px-3 py-2.5 font-bold text-slate-800">' + (p['NAMA'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 font-mono text-slate-600">' + (p['NIK'] || '-') + '</td>' +
            '<td class="px-3 py-2.5">' + jkBadge + '</td>' +
            '<td class="px-3 py-2.5">' + (p['UMUR'] || '-') + ' Thn</td>' +
            '<td class="px-3 py-2.5">RT ' + (p['RT'] || '01') + ' / RW ' + (p['RW'] || '01') + '</td>' +
            '<td class="px-3 py-2.5 text-slate-600">' + (p['PEKERJAAN'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 text-slate-600">' + (p['SHDK'] || '-') + '</td>' +
            '<td class="px-3 py-2.5">' + bansosBadge + '</td>' +
            '<td class="px-3 py-2.5">' + ektpBadge + '</td>' +
          '</tr>';
        } else if (currentTab === 'meninggal') {
          return '<tr class="hover:bg-slate-50 transition">' +
            '<td class="px-3 py-2.5 font-semibold text-slate-500">' + (p['NO'] || (index + 1)) + '</td>' +
            '<td class="px-3 py-2.5 font-bold text-slate-900">' + (p['NAMA'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 font-mono text-slate-600">' + (p['NIK'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 font-semibold text-red-600">' + (p['TANGGAL KEMATIAN'] || '-') + ' ' + (p['WAKTU KEMATIAN'] || '') + '</td>' +
            '<td class="px-3 py-2.5">' + (p['TEMPAT KEMATIAN'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 text-slate-600">' + (p['PENYEBAB KEMATIAN'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 text-slate-600">' + (p['TEMPAT PEMAKAMAN'] || '-') + '</td>' +
            '<td class="px-3 py-2.5">' + (p['NAMA PELAPOR'] || p['NAMA PELAPOR KEMATIAN'] || '-') + ' (' + (p['HUBUNGAN PELAPOR'] || '-') + ')</td>' +
            '<td class="px-3 py-2.5 font-mono text-slate-600">' + (p['NO SURAT KEMATIAN'] || '-') + '</td>' +
          '</tr>';
        } else {
          return '<tr class="hover:bg-slate-50 transition">' +
            '<td class="px-3 py-2.5 font-semibold text-slate-500">' + (p['NO'] || (index + 1)) + '</td>' +
            '<td class="px-3 py-2.5 font-bold text-orange-950">' + (p['NAMA'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 font-mono text-slate-600">' + (p['NIK'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 font-semibold text-orange-600">' + (p['TANGGAL PINDAH'] || '-') + '</td>' +
            '<td class="px-3 py-2.5">' + (p['ALASAN PINDAH'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 font-medium">' + (p['ALAMAT TUJUAN'] || '-') + ' RT ' + (p['RT TUJUAN'] || '-') + '/RW ' + (p['RW TUJUAN'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 text-slate-600">Desa ' + (p['DESA TUJUAN'] || '-') + ', Kec. ' + (p['KECAMATAN TUJUAN'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 text-slate-600">' + (p['KABUPATEN TUJUAN'] || '-') + ', ' + (p['PROVINSI TUJUAN'] || '-') + '</td>' +
            '<td class="px-3 py-2.5 font-mono text-slate-600">' + (p['NO SURAT PINDAH'] || '-') + '</td>' +
          '</tr>';
        }
      }).join('');
    }

    document.getElementById('searchInput').addEventListener('input', renderTable);
    document.getElementById('rwFilter').addEventListener('change', renderTable);
    document.getElementById('rtFilter').addEventListener('change', renderTable);

    window.addEventListener('DOMContentLoaded', fetchData);
  </script>
</body>
</html>
        """.trimIndent()
    }
}
