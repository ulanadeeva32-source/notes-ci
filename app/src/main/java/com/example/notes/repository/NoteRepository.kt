package com.example.notes.repository

import com.example.notes.data.local.SessionStorage
import com.example.notes.data.local.dao.NoteDao
import com.example.notes.data.local.entity.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Единая точка входа для данных. Все заметки привязаны к вошедшему пользователю. */
@Singleton
class NoteRepository @Inject constructor(
    private val dao: NoteDao,
    private val session: SessionStorage
) {
    private val owner: String
        get() = session.email.orEmpty()

    fun getNotes(): Flow<List<Note>> = dao.getAllNotes(owner)

    fun searchNotes(query: String): Flow<List<Note>> = dao.searchNotes(owner, query)

    /** Заметка всегда сохраняется на имя текущего пользователя. */
    suspend fun insert(note: Note) = dao.insert(note.copy(ownerEmail = owner))

    suspend fun delete(note: Note) = dao.delete(note)
}
