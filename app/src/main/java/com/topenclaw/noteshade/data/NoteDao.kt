package com.topenclaw.noteshade.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    fun observeAll(): Flow<List<Note>>

    @Query("SELECT id FROM notes")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Note?

    @Query("SELECT * FROM notes WHERE showInNotification = 1 AND isArchived = 0")
    suspend fun getNotificationNotes(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}
