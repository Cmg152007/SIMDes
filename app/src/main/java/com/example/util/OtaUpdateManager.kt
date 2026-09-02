package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.data.model.AppUpdateInfo
import com.example.data.model.GitHubRelease
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class OtaUpdateManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val releaseAdapter = moshi.adapter(GitHubRelease::class.java)

    /**
     * Check GitHub Releases for the latest version
     * repo: e.g. "rikkinurzaman/data-penduduk" or "username/repository"
     */
    suspend fun checkForUpdates(repo: String, currentVersionName: String): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val cleanRepo = repo.trim().removePrefix("https://github.com/").removeSuffix("/")
            if (cleanRepo.isBlank() || !cleanRepo.contains("/")) {
                return@withContext Result.failure(IllegalArgumentException("Format repositori GitHub tidak valid. Contoh: 'username/repo'"))
            }

            val url = "https://api.github.com/repos/$cleanRepo/releases/latest"
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "SIMDes-DataPenduduk-OTA")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorMsg = when (response.code) {
                    404 -> "Repositori '$cleanRepo' atau Rilis tidak ditemukan di GitHub."
                    403 -> "Batas kuota API GitHub tercapai atau repositori bersifat privat."
                    else -> "Gagal memeriksa rilis GitHub: Kode ${response.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("Respon kosong dari GitHub"))
            val release = releaseAdapter.fromJson(responseBody) ?: return@withContext Result.failure(Exception("Gagal membaca struktur data rilis GitHub"))

            // Find APK asset in assets list
            val apkAsset = release.assets.firstOrNull {
                it.name.endsWith(".apk", ignoreCase = true) ||
                it.contentType == "application/vnd.android.package-archive" ||
                it.contentType == "application/octet-stream" && it.name.endsWith(".apk", ignoreCase = true)
            }

            val tagName = release.tagName.trim()
            val newVersionName = tagName.removePrefix("v").removePrefix("V").trim()
            val cleanCurrentVersion = currentVersionName.removePrefix("v").removePrefix("V").trim()

            val isNewer = isVersionNewer(newVersionName, cleanCurrentVersion)

            val updateInfo = AppUpdateInfo(
                tagName = tagName,
                versionName = if (newVersionName.isNotBlank()) newVersionName else tagName,
                releaseTitle = release.name ?: tagName,
                releaseNotes = release.body ?: "Pembaruan versi terbaru SIMDes Data Penduduk.",
                apkDownloadUrl = apkAsset?.downloadUrl ?: release.htmlUrl ?: "",
                apkFileName = apkAsset?.name ?: "app-release-$newVersionName.apk",
                apkFileSize = apkAsset?.size ?: 0L,
                releaseHtmlUrl = release.htmlUrl ?: "https://github.com/$cleanRepo/releases",
                publishedAt = release.publishedAt ?: "",
                isUpdateAvailable = isNewer
            )

            Result.success(updateInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Download the APK with progress streaming
     */
    suspend fun downloadApk(
        downloadUrl: String,
        fileName: String,
        onProgress: (progress: Float, downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (downloadUrl.isBlank()) {
                return@withContext Result.failure(Exception("URL unduhan APK tidak valid."))
            }

            val request = Request.Builder()
                .url(downloadUrl)
                .addHeader("User-Agent", "SIMDes-DataPenduduk-OTA")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gagal mengunduh file APK: HTTP ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Respon unduhan kosong"))
            val totalBytes = body.contentLength()

            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val destinationFile = File(downloadDir, fileName)

            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            body.byteStream().use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalDownloaded = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead
                        val progress = if (totalBytes > 0) totalDownloaded.toFloat() / totalBytes.toFloat() else 0f
                        onProgress(progress.coerceIn(0f, 1f), totalDownloaded, totalBytes)
                    }
                    output.flush()
                }
            }

            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Install APK using Intent and FileProvider
     */
    fun installApk(apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists()) {
                return Result.failure(Exception("File APK tidak ditemukan di penyimpanan."))
            }

            // Check Unknown Sources permission for Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return Result.failure(Exception("Izin pemasangan dari sumber ini diperlukan. Silakan aktifkan izin lalu coba lagi."))
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Semantic version comparison
     * Returns true if candidate > current
     */
    private fun isVersionNewer(candidate: String, current: String): Boolean {
        try {
            val candidateParts = candidate.split(".", "-", "_").mapNotNull { it.toIntOrNull() }
            val currentParts = current.split(".", "-", "_").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(candidateParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val c = candidateParts.getOrElse(i) { 0 }
                val cur = currentParts.getOrElse(i) { 0 }
                if (c > cur) return true
                if (c < cur) return false
            }
            return false
        } catch (_: Exception) {
            return candidate != current && candidate.isNotBlank()
        }
    }
}
