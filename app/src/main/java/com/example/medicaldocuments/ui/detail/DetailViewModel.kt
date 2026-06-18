package com.example.medicaldocuments.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicaldocuments.data.local.repository.DocumentRepository
import com.example.medicaldocuments.data.model.MedicalDocument
import com.example.medicaldocuments.domain.usecases.DeleteDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val deleteDocumentUseCase: DeleteDocumentUseCase
) : ViewModel() {

    private val _document = MutableStateFlow<MedicalDocument?>(null)
    val document = _document.asStateFlow()

    fun loadDocument(id: Long) {
        viewModelScope.launch {
            _document.value = repository.getDocumentById(id)
        }
    }

    fun toggleFavorite(document: MedicalDocument) {
        viewModelScope.launch {
            repository.update(document.copy(isFavorite = !document.isFavorite))
            _document.value = repository.getDocumentById(document.id)
        }
    }

    fun deleteDocument(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _document.value?.let {
                    deleteDocumentUseCase(it)
                    onResult(true)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}