package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: String = getCurrentFormattedTime(),
    val operator: String = "Petugas Registrasi Desa",
    val action: String, // TAMBAH, EDIT, HAPUS, SYNC, CONFIG
    val target: String, // Nama / NIK / Sistem
    val detail: String,
    val status: String = "BERHASIL", // BERHASIL / GAGAL
    val dataBefore: String? = null, // JSON snapshot or formatted data before edit
    val dataAfter: String? = null, // JSON snapshot or formatted data after edit
    val syncedWithSheets: Boolean = false,
    val epochMillis: Long = System.currentTimeMillis()
) {
    companion object {
        fun getCurrentFormattedTime(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
