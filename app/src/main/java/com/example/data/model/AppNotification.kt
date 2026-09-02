package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // SYNC, WARNING, INFO, BANSOS, KTP
    val timestamp: String = getFormattedTime(),
    val isRead: Boolean = false,
    val epochMillis: Long = System.currentTimeMillis()
) {
    companion object {
        fun getFormattedTime(): String {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            return sdf.format(Date())
        }
    }
}
