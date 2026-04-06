package com.topenclaw.noteshade.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepository(private val dao: NoteDao) {
    fun observeNotes(): Flow<List<NoteEntity>> = dao.observeAll()

    fun observeNote(id: Long): Flow<NoteEntity?> = dao.observeById(id)

    suspend fun createBlankNote(): Long = dao.insert(NoteEntity())

    suspend fun saveNote(id: Long, title: String, body: String) {
        val existing = dao.getById(id) ?: return
        dao.update(
            existing.copy(
                title = title,
                body = body,
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun updateNote(note: NoteEntity) {
        dao.update(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun togglePinned(id: Long) {
        val note = dao.getById(id) ?: return
        dao.update(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleArchived(id: Long) {
        val note = dao.getById(id) ?: return
        dao.update(note.copy(isArchived = !note.isArchived, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleNotification(id: Long) {
        val note = dao.getById(id) ?: return
        dao.update(note.copy(showInNotification = !note.showInNotification, updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(id: Long) {
        dao.getById(id)?.let { dao.delete(it) }
    }

    suspend fun getNote(id: Long): NoteEntity? = dao.getById(id)

    fun observeNotificationNotes(): Flow<List<NoteEntity>> = observeNotes().map { notes ->
        notes.filter { it.showInNotification && !it.isArchived }
    }
}
