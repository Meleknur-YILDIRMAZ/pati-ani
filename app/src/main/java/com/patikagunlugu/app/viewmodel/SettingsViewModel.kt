package com.patikagunlugu.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.patikagunlugu.app.PatikaApplication
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = (application as PatikaApplication).settingsManager

    val themeFlow = settingsManager.themeFlow
    val languageFlow = settingsManager.languageFlow

    val unsyncedCountFlow = kotlinx.coroutines.flow.combine(
        com.patikagunlugu.app.db.AppDatabase.getDatabase(application).petDao().getAllPets(),
        com.patikagunlugu.app.db.AppDatabase.getDatabase(application).memoryDao().getAllMemories()
    ) { pets, memories ->
        var count = 0
        for (pet in pets) {
            if (!pet.profilePhotoPath.isNullOrEmpty() && !pet.profilePhotoPath.startsWith("http")) {
                count++
            }
        }
        for (memory in memories) {
            val hasPhoto = !memory.imagePath.isNullOrEmpty() && !memory.imagePath.startsWith("http")
            val hasVideo = !memory.videoPath.isNullOrEmpty() && !memory.videoPath.startsWith("http")
            val hasAudio = !memory.audioPath.isNullOrEmpty() && !memory.audioPath.startsWith("http")
            if (hasPhoto || hasVideo || hasAudio) {
                count++
            }
        }
        count
    }

    fun getSaveToGalleryState(): Boolean {
        val prefs = getApplication<Application>().getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean("save_to_gallery", true)
    }

    fun setSaveToGalleryState(enabled: Boolean) {
        val prefs = getApplication<Application>().getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("save_to_gallery", enabled).apply()
    }

    fun setTheme(theme: String) = viewModelScope.launch {
        settingsManager.setTheme(theme)
    }

    suspend fun setLanguage(language: String) {
        settingsManager.setLanguage(language)
    }

    fun clearAllData() = viewModelScope.launch {
        com.patikagunlugu.app.db.AppDatabase.getDatabase(getApplication()).clearAllTables()
    }
}
