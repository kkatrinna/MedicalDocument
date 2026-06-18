package com.example.medicaldocuments.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicaldocuments.data.local.repository.DocumentRepository
import com.example.medicaldocuments.data.model.MedicalDocument
import com.example.medicaldocuments.domain.usecases.BackupUseCase
import com.example.medicaldocuments.domain.usecases.DeleteDocumentUseCase
import com.example.medicaldocuments.ui.theme.AppTheme
import com.example.medicaldocuments.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val backupUseCase: BackupUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("Все")
    private val _searchQuery = MutableStateFlow("")

    val selectedCategory = _selectedCategory.asStateFlow()

    private val documentsFlow = combine(
        _selectedCategory,
        _searchQuery
    ) { category, query ->
        Pair(category, query)
    }.flatMapLatest { (category, query) ->
        when {
            query.isNotBlank() -> repository.searchDocuments(query)
            category == "Избранное" -> repository.getFavoriteDocuments()
            category == "Все" -> repository.getAllDocuments()
            else -> repository.getDocumentsByCategory(category)
        }
    }

    private val allDocuments = repository.getAllDocuments()

    val categoryCounts: StateFlow<Map<String, Int>> = allDocuments.map { docs ->
        val counts = mutableMapOf<String, Int>()

        docs.groupBy { doc -> doc.category }.forEach { (category, documents) ->
            counts[category] = documents.size
        }

        counts["Все"] = docs.size
        counts["Избранное"] = docs.count { doc -> doc.isFavorite }

        counts.toMap()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    val uiState: StateFlow<MainUiState> = combine(
        documentsFlow,
        categoryCounts
    ) { documents, counts ->
        val grouped = documents.groupBy { doc ->
            DateUtils.getYear(doc.documentDate)
        }.toSortedMap(reverseOrder())

        MainUiState(
            documents = documents,
            groupedDocuments = grouped,
            selectedCategory = _selectedCategory.value,
            categoryCounts = counts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun toggleFavorite(document: MedicalDocument) {
        viewModelScope.launch {
            repository.update(document.copy(isFavorite = !document.isFavorite))
        }
    }

    fun deleteDocument(document: MedicalDocument) {
        viewModelScope.launch {
            deleteDocumentUseCase(document)
        }
    }

    fun exportBackup(activity: Activity) {
        viewModelScope.launch {
            try {
                val documents = repository.getAllDocuments().first()

                if (documents.isEmpty()) {
                    Toast.makeText(context, "Нет документов для бэкапа", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val backupDir = File(context.getExternalFilesDir(null), "MedDocBackups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }

                val result = backupUseCase.exportAllDocuments(backupDir)

                result.onSuccess { file ->
                    val filePath = file.absolutePath
                    Toast.makeText(
                        context,
                        "Бэкап создан: $filePath",
                        Toast.LENGTH_LONG
                    ).show()
                    shareBackupFile(activity, file)
                }.onFailure { error ->
                    Toast.makeText(context, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareBackupFile(activity: Activity, file: File) {
        try {
            if (!file.exists()) {
                Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
                return
            }

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            activity.startActivity(Intent.createChooser(shareIntent, "Поделиться бэкапом"))
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

}

data class MainUiState(
    val documents: List<MedicalDocument> = emptyList(),
    val groupedDocuments: Map<String, List<MedicalDocument>> = emptyMap(),
    val selectedCategory: String = "Все",
    val categoryCounts: Map<String, Int> = emptyMap(),
    val currentTheme: AppTheme = AppTheme.SYSTEM
)