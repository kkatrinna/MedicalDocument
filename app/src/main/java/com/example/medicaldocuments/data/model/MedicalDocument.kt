package com.example.medicaldocuments.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "documents")
data class MedicalDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val category: String,
    val documentDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val comment: String? = null,
    val mimeType: String,
    val fileSize: Long,
    val isFavorite: Boolean = false
)