package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PendudukDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM penduduk_documents WHERE nik = :nik ORDER BY createdAt DESC")
    fun getDocumentsByNik(nik: String): Flow<List<PendudukDocument>>

    @Query("SELECT * FROM penduduk_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<PendudukDocument>>

    @Query("SELECT * FROM penduduk_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): PendudukDocument?

    @Query("SELECT COUNT(*) FROM penduduk_documents WHERE nik = :nik")
    fun getDocumentCountByNik(nik: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: PendudukDocument): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documents: List<PendudukDocument>)

    @Update
    suspend fun updateDocument(document: PendudukDocument)

    @Delete
    suspend fun deleteDocument(document: PendudukDocument)

    @Query("DELETE FROM penduduk_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("DELETE FROM penduduk_documents WHERE nik = :nik")
    suspend fun deleteDocumentsByNik(nik: String)
}
