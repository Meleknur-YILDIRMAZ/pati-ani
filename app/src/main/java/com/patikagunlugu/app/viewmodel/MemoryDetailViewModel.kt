package com.patikagunlugu.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.patikagunlugu.app.db.AppDatabase
import com.patikagunlugu.app.model.Memory
import com.patikagunlugu.app.repository.MemoryRepository
import kotlinx.coroutines.launch

class MemoryDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MemoryRepository

    init {
        val memoryDao = AppDatabase.getDatabase(application).memoryDao()
        repository = MemoryRepository(memoryDao)
    }

    suspend fun getMemoryById(memoryId: Long): Memory? {
        return repository.getMemoryById(memoryId)
    }

    fun updateMemory(memory: Memory, onComplete: () -> Unit) = viewModelScope.launch {
        repository.updateMemory(memory)
        onComplete()
    }

    fun deleteMemory(memory: Memory, onComplete: () -> Unit) = viewModelScope.launch {
        repository.deleteMemory(memory)
        onComplete()
    }
}
