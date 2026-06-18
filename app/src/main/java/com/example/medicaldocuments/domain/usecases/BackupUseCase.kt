package com.example.medicaldocuments.domain.usecases

import android.content.Context
import com.example.medicaldocuments.data.local.repository.DocumentRepository
import com.example.medicaldocuments.data.model.MedicalDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class BackupUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DocumentRepository
) {
    suspend fun exportAllDocuments(exportDir: File): Result<File> {
        return try {
            val documents = repository.getAllDocumentsSync()

            if (documents.isEmpty()) {
                return Result.failure(Exception("Нет документов для бэкапа"))
            }

            val zipFile = File(exportDir, "meddoc_backup_${System.currentTimeMillis()}.zip")

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                val json = Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }

                val metadataJson = json.encodeToString(documents)
                zos.putNextEntry(ZipEntry("metadata.json"))
                zos.write(metadataJson.toByteArray())
                zos.closeEntry()

                documents.forEach { doc ->
                    val file = File(doc.filePath)
                    if (file.exists()) {
                        val entryName = "files/${file.name}"
                        zos.putNextEntry(ZipEntry(entryName))
                        file.inputStream().use { input ->
                            input.copyTo(zos)
                        }
                        zos.closeEntry()
                    }
                }
            }

            Result.success(zipFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(zipFile: File): Result<Int> {
        return try {
            var importedCount = 0
            ZipFile(zipFile).use { zip ->
                val metadataEntry = zip.getEntry("metadata.json")
                if (metadataEntry == null) {
                    return Result.failure(Exception("Некорректный файл бэкапа"))
                }

                val json = Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }

                val jsonString = zip.getInputStream(metadataEntry).bufferedReader().readText()
                val documents: List<MedicalDocument> = json.decodeFromString(jsonString)

                documents.forEach { doc ->
                    val fileEntry = zip.getEntry("files/${File(doc.filePath).name}")
                    if (fileEntry != null) {
                        val newFile = File(context.filesDir, File(doc.filePath).name)
                        zip.getInputStream(fileEntry).use { input ->
                            newFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        val newDoc = doc.copy(
                            filePath = newFile.absolutePath,
                            id = 0
                        )
                        repository.insert(newDoc)
                        importedCount++
                    }
                }
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}