package com.topenclaw.noteshade.ui.components

import com.topenclaw.noteshade.data.NoteEntity
import com.topenclaw.noteshade.data.NoteSortOption

fun List<NoteEntity>.searchAndSort(query: String, archived: Boolean, sort: NoteSortOption): List<NoteEntity> {
    val filtered = filter { note ->
        note.isArchived == archived &&
            (query.isBlank() || note.title.contains(query, ignoreCase = true) || note.body.contains(query, ignoreCase = true))
    }
    return when (sort) {
        NoteSortOption.RECENTLY_UPDATED -> filtered.sortedByDescending { it.updatedAt }
        NoteSortOption.NEWEST -> filtered.sortedByDescending { it.createdAt }
        NoteSortOption.OLDEST -> filtered.sortedBy { it.createdAt }
        NoteSortOption.PINNED_FIRST -> filtered.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.updatedAt })
    }
}
