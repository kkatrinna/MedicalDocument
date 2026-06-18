package com.example.medicaldocuments.ui.add

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicaldocuments.domain.usecases.SaveDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddDocumentViewModel @Inject constructor(
    private val saveDocumentUseCase: SaveDocumentUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _expanded = MutableStateFlow(false)
    val expanded = _expanded.asStateFlow()

    fun toggleExpanded() {
        _expanded.value = !_expanded.value
    }

    fun saveDocument(
        uri: Uri,
        category: String,
        documentDate: Long,
        comment: String?,
        fileName: String?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val originalFileName = fileName ?: "document"
                val extension = getFileExtension(originalFileName, uri)


                val result = saveDocumentUseCase(
                    uri = uri,
                    category = category,
                    documentDate = documentDate,
                    comment = comment,
                    fileName = originalFileName,
                    fileExtension = extension
                )
                onResult(result)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    private fun getFileExtension(fileName: String, uri: Uri): String {
        val ext = fileName.substringAfterLast('.', "")
        if (ext.isNotEmpty() && ext != "file") {
            return ext
        }

        try {
            val mimeType = context.contentResolver.getType(uri)
            Log.d("AddDocument", "MIME type: $mimeType")

            return when {
                mimeType == null -> "file"

                mimeType.contains("word") ||
                        mimeType.contains("msword") ||
                        mimeType.contains("wordprocessingml") -> "docx"

                mimeType.contains("pdf") -> "pdf"

                mimeType.contains("image") -> {
                    when {
                        mimeType.contains("png") -> "png"
                        mimeType.contains("jpeg") -> "jpg"
                        mimeType.contains("gif") -> "gif"
                        else -> "jpg"
                    }
                }

                mimeType.contains("video") -> "mp4"

                mimeType.contains("audio") -> "mp3"

                mimeType.contains("text") -> "txt"

                mimeType.contains("excel") ||
                        mimeType.contains("spreadsheetml") -> "xlsx"

                mimeType.contains("powerpoint") ||
                        mimeType.contains("presentationml") -> "pptx"
                else -> "file"
            }
        } catch (e: Exception) {
            Log.e("AddDocument", "Error getting MIME type: ${e.message}")
            return "file"
        }
    }
}