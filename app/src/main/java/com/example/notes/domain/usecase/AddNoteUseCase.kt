package com.example.notes.domain.usecase

import com.example.notes.data.local.entity.Note
import com.example.notes.repository.NoteRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/** Сценарий «добавить заметку»: правила (пустая заметка, заголовок по умолчанию) живут здесь, а не в ViewModel. */
class AddNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(title: String, text: String): Result<Unit> {
        if (title.isBlank() && text.isBlank()) {
            return Result.failure(IllegalArgumentException("Заметка не может быть пустой"))
        }
        val note = Note(
            title = title.trim().ifEmpty { "Без названия" },
            content = text.trim()
        )
        return try {
            repository.insert(note)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
