package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Penduduk
import kotlinx.coroutines.flow.Flow

@Dao
interface PendudukDao {
    @Query("SELECT * FROM penduduk ORDER BY no ASC, nama ASC")
    fun getAllPenduduk(): Flow<List<Penduduk>>

    @Query("SELECT * FROM penduduk WHERE nik = :nik LIMIT 1")
    suspend fun getPendudukByNik(nik: String): Penduduk?

    @Query("SELECT * FROM penduduk WHERE nama LIKE '%' || :query || '%' OR nik LIKE '%' || :query || '%' OR noKk LIKE '%' || :query || '%' ORDER BY nama ASC")
    fun searchPenduduk(query: String): Flow<List<Penduduk>>

    @Query("SELECT COUNT(*) FROM penduduk")
    fun getTotalPendudukCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT noKk) FROM penduduk WHERE noKk != ''")
    fun getTotalKkCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM penduduk WHERE UPPER(jenisKelamin) = 'LAKI-LAKI' OR UPPER(jenisKelamin) = 'L' OR UPPER(jenisKelamin) LIKE 'LAKI%'")
    fun getMaleCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM penduduk WHERE UPPER(jenisKelamin) = 'PEREMPUAN' OR UPPER(jenisKelamin) = 'P' OR UPPER(jenisKelamin) LIKE 'PEREM%'")
    fun getFemaleCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPenduduk(penduduk: Penduduk)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pendudukList: List<Penduduk>)

    @Update
    suspend fun updatePenduduk(penduduk: Penduduk)

    @Delete
    suspend fun deletePenduduk(penduduk: Penduduk)

    @Query("DELETE FROM penduduk WHERE nik = :nik")
    suspend fun deleteByNik(nik: String)

    @Query("DELETE FROM penduduk")
    suspend fun deleteAll()

    @Query("SELECT * FROM penduduk WHERE syncedWithSheets = 0")
    suspend fun getUnsyncedPenduduk(): List<Penduduk>

    @Query("UPDATE penduduk SET syncedWithSheets = 1 WHERE nik IN (:niks)")
    suspend fun markAsSynced(niks: List<String>)
}
