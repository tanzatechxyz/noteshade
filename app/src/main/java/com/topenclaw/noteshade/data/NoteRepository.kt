package com.topenclaw.noteshade.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    fun observeNotes(): Flow<List<Note>> = noteDao.observeAll()
    suspend fun getAllNoteIds(): List<Long> = noteDao.getAllIds()
    suspend fun getNote(id: Long): Note? = noteDao.getById(id)
    suspend fun upsert(note: Note): Long = if (note.id == 0L) noteDao.insert(note) else { noteDao.update(note); note.id }
    suspend fun delete(note: Note) = noteDao.delete(note)
    suspend fun getNotificationNotes(): List<Note> = noteDao.getNotificationNotes()
}
