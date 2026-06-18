package com.example.medicaldocuments.domain.usecases

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.medicaldocuments.data.local.repository.DocumentRepository
import com.example.medicaldocuments.data.model.MedicalDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class SaveDocumentUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentRepository: DocumentRepository
) {

    suspend operator fun invoke(
        uri: Uri,
        category: String,
        documentDate: Long,
        comment: String?,
        fileName: String,
        fileExtension: String = "file"
    ): Boolean {
        return try {
            val contentResolver: ContentResolver = context.contentResolver

            val nameWithoutExtension = fileName.substringBeforeLast('.', fileName)

            val finalExtension = if (fileExtension.isNotEmpty() && fileExtension != "file") {
                if (fileName.endsWith(".$fileExtension", ignoreCase = true)) {
                    fileExtension
                } else {
                    fileExtension
                }
            } else {
                val ext = fileName.substringAfterLast('.', "")
                if (ext.isNotEmpty() && ext != "file") {
                    ext
                } else {
                    "file"
                }
            }

            val finalFileName = if (finalExtension != "file" && finalExtension.isNotEmpty()) {
                "$nameWithoutExtension.$finalExtension"
            } else {
                fileName
            }

            Log.d("SaveDocument", "=== SAVING DOCUMENT ===")
            Log.d("SaveDocument", "Original fileName: $fileName")
            Log.d("SaveDocument", "Name without extension: $nameWithoutExtension")
            Log.d("SaveDocument", "File extension: $finalExtension")
            Log.d("SaveDocument", "Final file name: $finalFileName")

            val timestamp = System.currentTimeMillis()
            val uniqueFileName = "${timestamp}_${finalFileName}"
            val file = File(context.filesDir, uniqueFileName)

            Log.d("SaveDocument", "Saving to: ${file.absolutePath}")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            } ?: return false

            val mimeType = contentResolver.getType(uri) ?: when (finalExtension.lowercase()) {
                "doc" -> "application/msword"
                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                "pdf" -> "application/pdf"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "mp4" -> "video/mp4"
                "mp3" -> "audio/mpeg"
                "txt" -> "text/plain"
                "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                else -> "application/octet-stream"
            }

            Log.d("SaveDocument", "MIME type: $mimeType")
            Log.d("SaveDocument", "File size: ${file.length()}")

            val document = MedicalDocument(
                fileName = finalFileName,
                filePath = file.absolutePath,
                category = category,
                documentDate = documentDate,
                createdAt = System.currentTimeMillis(),
                comment = comment,
                mimeType = mimeType,
                fileSize = file.length(),
                isFavorite = false
            )

            documentRepository.insert(document)

            Log.d("SaveDocument", "Document saved successfully: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e("SaveDocument", "Error saving document: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}