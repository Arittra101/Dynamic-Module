package com.example.feature_notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature_notes.data.Note
import com.example.feature_notes.data.NotesDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = NotesDatabase.get(application).noteDao()

    val notes = dao.getAllNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun addNote(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch { dao.insert(Note(content = content.trim())) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { dao.delete(note) }
    }
}
