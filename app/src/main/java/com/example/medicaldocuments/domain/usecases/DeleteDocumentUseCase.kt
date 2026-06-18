package com.example.medicaldocuments.domain.usecases

import com.example.medicaldocuments.data.local.repository.DocumentRepository
import com.example.medicaldocuments.data.model.MedicalDocument
import java.io.File
import javax.inject.Inject

class DeleteDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(document: MedicalDocument) {
        val file = File(document.filePath)
        if (file.exists()) {
            file.delete()
        }
        repository.delete(document)
    }
}