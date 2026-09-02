package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY epochMillis DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE action = :action ORDER BY epochMillis DESC")
    fun getLogsByAction(action: String): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<ActivityLog>)

    @Query("SELECT * FROM activity_logs WHERE syncedWithSheets = 0 ORDER BY epochMillis ASC")
    suspend fun getUnsyncedLogs(): List<ActivityLog>

    @Query("UPDATE activity_logs SET syncedWithSheets = 1 WHERE id IN (:ids)")
    suspend fun markLogsAsSynced(ids: List<Long>)

    @Query("DELETE FROM activity_logs")
    suspend fun clearAllLogs()
}
