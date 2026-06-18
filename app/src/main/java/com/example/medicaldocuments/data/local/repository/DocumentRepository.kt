package com.example.medicaldocuments.data.local.repository

import com.example.medicaldocuments.data.model.MedicalDocument
import com.example.medicaldocuments.data.local.database.DocumentDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    private val documentDao: DocumentDao
) {
    fun getAllDocuments(): Flow<List<MedicalDocument>> = documentDao.getAllDocuments()

    suspend fun getAllDocumentsSync(): List<MedicalDocument> = documentDao.getAllDocumentsSync()

    fun getDocumentsByCategory(category: String): Flow<List<MedicalDocument>> =
        documentDao.getDocumentsByCategory(category)

    fun getFavoriteDocuments(): Flow<List<MedicalDocument>> =
        documentDao.getFavoriteDocuments()

    fun searchDocuments(query: String): Flow<List<MedicalDocument>> =
        documentDao.searchDocuments(query)

    suspend fun getDocumentById(id: Long): MedicalDocument? =
        documentDao.getDocumentById(id)

    suspend fun insert(document: MedicalDocument): Long =
        documentDao.insert(document)

    suspend fun update(document: MedicalDocument) =
        documentDao.update(document)

    suspend fun delete(document: MedicalDocument) =
        documentDao.delete(document)

}