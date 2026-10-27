package com.example.notes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.notes.data.local.entity.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes_table WHERE ownerEmail = :owner ORDER BY timestamp DESC")
    fun getAllNotes(owner: String): Flow<List<Note>>

    // Поиск по заголовку и тексту только среди заметок этого пользователя
    @Query("SELECT * FROM notes_table WHERE ownerEmail = :owner AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY timestamp DESC")
    fun searchNotes(owner: String, query: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Delete
    suspend fun delete(note: Note)
}
