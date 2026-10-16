package com.patikagunlugu.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.patikagunlugu.app.db.AppDatabase
import com.patikagunlugu.app.model.Pet
import com.patikagunlugu.app.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PetListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PetRepository
    val allPets: Flow<List<Pet>>
    val petsWithMemories: Flow<List<com.patikagunlugu.app.model.PetWithMemories>>

    init {
        val petDao = AppDatabase.getDatabase(application).petDao()
        repository = PetRepository(petDao)
        allPets = repository.allPets
        petsWithMemories = repository.petsWithMemories
    }

    fun deletePet(pet: Pet) = viewModelScope.launch {
        repository.deletePet(pet)
    }
}
