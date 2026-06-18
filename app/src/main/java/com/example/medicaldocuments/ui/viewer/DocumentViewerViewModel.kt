package com.example.medicaldocuments.ui.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicaldocuments.data.local.repository.DocumentRepository
import com.example.medicaldocuments.data.model.MedicalDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentViewerViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _document = MutableStateFlow<MedicalDocument?>(null)
    val document = _document.asStateFlow()

    fun loadDocument(id: Long) {
        viewModelScope.launch {
            _document.value = repository.getDocumentById(id)
        }
    }
}