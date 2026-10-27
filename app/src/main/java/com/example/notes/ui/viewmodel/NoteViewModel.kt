package com.example.notes.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.local.SessionStorage
import com.example.notes.data.local.entity.Note
import com.example.notes.domain.usecase.AddNoteUseCase
import com.example.notes.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val addNoteUseCase: AddNoteUseCase,
    private val session: SessionStorage
) : ViewModel() {

    // ---------- Состояние экрана (StateFlow вместо LiveData) ----------
    private val _uiState = MutableStateFlow(NotesUiState(isLoading = true))
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    // ---------- Поиск ----------
    private val searchQueryFlow = MutableStateFlow("")

    val searchQuery: String
        get() = searchQueryFlow.value

    fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    init {
        observeNotes()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeNotes() {
        viewModelScope.launch {
            searchQueryFlow
                .flatMapLatest { query ->
                    if (query.isBlank()) repository.getNotes() else repository.searchNotes(query.trim())
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { list ->
                    _uiState.update { it.copy(notes = list, isLoading = false) }
                }
        }
    }

    // ---------- Профиль ----------
    val currentEmail: String
        get() = session.email.orEmpty()

    fun logout() = session.clear()

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ---------- Валидация (ошибка под полем в диалоге) ----------
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun clearError() {
        if (_error.value != null) _error.value = null
    }

    // ---------- Черновик диалога (переживает поворот экрана) ----------
    var draftTitle: String = ""
    var draftContent: String = ""

    fun clearDraft() {
        draftTitle = ""
        draftContent = ""
        clearError()
    }

    // ---------- Операции с заметками ----------

    private fun validate(title: String, content: String): Boolean {
        if (title.isBlank() && content.isBlank()) {
            _error.value = "Заметка не может быть пустой"
            return false
        }
        _error.value = null
        return true
    }

    fun addNote(title: String, content: String): Boolean {
        if (!validate(title, content)) return false
        viewModelScope.launch {
            addNoteUseCase(title, content).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
        return true
    }

    /** Сохранение изменений в открытой заметке (тот же id, REPLACE перезапишет запись). */
    fun updateNote(original: Note, title: String, content: String): Boolean {
        if (!validate(title, content)) return false
        val updated = original.copy(
            title = title.trim().ifEmpty { "Без названия" },
            content = content.trim()
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(updated)
        }
        return true
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(note)
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(note)
        }
    }
}
