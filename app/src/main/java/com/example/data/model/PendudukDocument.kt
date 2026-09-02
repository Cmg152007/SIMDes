package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "penduduk_documents")
data class PendudukDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nik: String,
    val noKk: String = "",
    val namaWarga: String,
    val rw: String = "01",
    val rt: String = "001",
    val jenisDokumen: String, // KTP, Kartu Keluarga, Akta Kelahiran, Surat Nikah, KIS / BPJS, Ijazah, Lainnya
    val namaFile: String,
    val localFilePath: String? = null,
    val driveFileUrl: String? = null,
    val driveFolderHierarchy: String = "",
    val fileSizeBytes: Long = 0,
    val mimeType: String = "image/jpeg",
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val syncError: String? = null
) {
    fun getFormattedDate(): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            sdf.format(Date(createdAt))
        } catch (e: Exception) {
            "-"
        }
    }

    fun getFormattedSize(): String {
        return when {
            fileSizeBytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", fileSizeBytes / (1024.0 * 1024.0))
            fileSizeBytes >= 1024 -> String.format(Locale.US, "%.0f KB", fileSizeBytes / 1024.0)
            fileSizeBytes > 0 -> "$fileSizeBytes B"
            else -> "-"
        }
    }

    companion object {
        val DOKUMEN_TYPES = listOf(
            "KTP",
            "Kartu Keluarga",
            "Akta Kelahiran",
            "Surat Nikah",
            "KIS / BPJS",
            "Ijazah",
            "Bukti Bansos",
            "Surat Keterangan",
            "Lainnya"
        )
    }
}
