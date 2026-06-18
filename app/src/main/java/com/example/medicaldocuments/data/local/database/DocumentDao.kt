package com.example.medicaldocuments.data.local.database

import androidx.room.*
import com.example.medicaldocuments.data.model.MedicalDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY documentDate DESC")
    fun getAllDocuments(): Flow<List<MedicalDocument>>

    @Query("SELECT * FROM documents ORDER BY documentDate DESC")
    suspend fun getAllDocumentsSync(): List<MedicalDocument>

    @Query("SELECT * FROM documents WHERE category = :category ORDER BY documentDate DESC")
    fun getDocumentsByCategory(category: String): Flow<List<MedicalDocument>>

    @Query("SELECT * FROM documents WHERE isFavorite = 1 ORDER BY documentDate DESC")
    fun getFavoriteDocuments(): Flow<List<MedicalDocument>>

    @Query("""
        SELECT * FROM documents 
        WHERE fileName LIKE '%' || :query || '%' 
           OR comment LIKE '%' || :query || '%'
        ORDER BY documentDate DESC
    """)
    fun searchDocuments(query: String): Flow<List<MedicalDocument>>

    @Query("""
        SELECT DISTINCT strftime('%Y', documentDate / 1000, 'unixepoch') as year
        FROM documents
        ORDER BY year DESC
    """)
    fun getAllYears(): Flow<List<String>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): MedicalDocument?

    @Insert
    suspend fun insert(document: MedicalDocument): Long

    @Update
    suspend fun update(document: MedicalDocument)

    @Delete
    suspend fun delete(document: MedicalDocument)

    @Query("DELETE FROM documents")
    suspend fun deleteAll()
}