package com.example.medicaldocuments.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicaldocuments.data.local.repository.DocumentRepository
import com.example.medicaldocuments.data.model.MedicalDocument
import com.example.medicaldocuments.domain.usecases.DeleteDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val deleteDocumentUseCase: DeleteDocumentUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val searchResults = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchDocuments(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
}