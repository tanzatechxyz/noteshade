package com.topenclaw.noteshade.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.topenclaw.noteshade.data.NoteRepository
import com.topenclaw.noteshade.data.SettingsRepository

object ViewModelFactories {
    fun notes(repo: NoteRepository, settingsRepository: SettingsRepository, context: Context) = factory {
        NotesViewModel(repo, settingsRepository, context)
    }

    fun detail(repo: NoteRepository, settingsRepository: SettingsRepository, context: Context) = factory {
        NoteDetailViewModel(repo, settingsRepository, context)
    }

    fun settings(settingsRepository: SettingsRepository) = factory {
        SettingsViewModel(settingsRepository)
    }

    private inline fun <reified T : ViewModel> factory(crossinline block: () -> T): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = block() as VM
        }
}
