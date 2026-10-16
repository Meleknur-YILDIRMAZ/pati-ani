package com.patikagunlugu.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.patikagunlugu.app.db.AppDatabase
import com.patikagunlugu.app.model.Memory
import com.patikagunlugu.app.model.Pet
import com.patikagunlugu.app.repository.MemoryRepository
import com.patikagunlugu.app.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TimelineViewModel(application: Application) : AndroidViewModel(application) {

    private val memoryRepository: MemoryRepository
    private val petRepository: PetRepository

    val allPets: Flow<List<Pet>>

    init {
        val database = AppDatabase.getDatabase(application)
        memoryRepository = MemoryRepository(database.memoryDao())
        petRepository = PetRepository(database.petDao())
        allPets = petRepository.allPets
    }

    private val _selectedDate = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)
    val selectedDate: Flow<Long?> = _selectedDate

    fun getMemoriesForPet(petId: Long): Flow<List<Memory>> {
        return kotlinx.coroutines.flow.combine(
            memoryRepository.getMemoriesForPet(petId),
            _selectedDate
        ) { memories, date ->
            val calendar = java.util.Calendar.getInstance()
            if (date != null) {
                calendar.timeInMillis = date
            }
            val targetYear = calendar.get(java.util.Calendar.YEAR)
            val targetDayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)

            memories.filter {
                val memCal = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
                memCal.get(java.util.Calendar.YEAR) == targetYear &&
                        memCal.get(java.util.Calendar.DAY_OF_YEAR) == targetDayOfYear
            }
        }
    }

    fun setSelectedDate(timestamp: Long?) {
        _selectedDate.value = timestamp
    }

    val allMemories: Flow<List<Memory>> = kotlinx.coroutines.flow.combine(
        memoryRepository.getAllMemories(),
        _selectedDate
    ) { memories, date ->
        val calendar = java.util.Calendar.getInstance()
        if (date != null) {
            calendar.timeInMillis = date
        }
        val targetYear = calendar.get(java.util.Calendar.YEAR)
        val targetDayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)

        memories.filter {
            val memCal = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
            memCal.get(java.util.Calendar.YEAR) == targetYear &&
                    memCal.get(java.util.Calendar.DAY_OF_YEAR) == targetDayOfYear
        }
    }

    fun getPetFlow(petId: Long): Flow<Pet?> {
        return petRepository.getPetFlow(petId)
    }

    suspend fun getPetById(petId: Long): Pet? {
        return petRepository.getPetById(petId)
    }

    suspend fun getMemoryById(memoryId: Long): Memory? {
        return memoryRepository.getMemoryById(memoryId)
    }

    fun insertMemory(memory: Memory) = viewModelScope.launch {
        memoryRepository.insertMemory(memory)
    }

    fun insertMemories(memories: List<Memory>, onComplete: () -> Unit) = viewModelScope.launch {
        memoryRepository.insertMemories(memories)
        onComplete()
    }

    fun deleteMemory(memory: Memory) = viewModelScope.launch {
        memoryRepository.deleteMemory(memory)
    }

    fun getVaccinesForPet(petId: Long): Flow<List<com.patikagunlugu.app.model.Vaccine>> {
        return AppDatabase.getDatabase(getApplication()).vaccineDao().getVaccinesForPet(petId)
    }
}
