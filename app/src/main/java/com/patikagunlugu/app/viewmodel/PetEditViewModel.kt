package com.patikagunlugu.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.patikagunlugu.app.db.AppDatabase
import com.patikagunlugu.app.model.Pet
import com.patikagunlugu.app.repository.PetRepository
import kotlinx.coroutines.launch

class PetEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PetRepository
    private val database = AppDatabase.getDatabase(application)

    init {
        repository = PetRepository(database.petDao())
    }

    fun getVaccinesForPet(petId: Long) = database.vaccineDao().getVaccinesForPet(petId)

    fun insertVaccine(vaccine: com.patikagunlugu.app.model.Vaccine) = viewModelScope.launch {
        database.vaccineDao().insertVaccine(vaccine)
    }

    fun deleteVaccine(vaccine: com.patikagunlugu.app.model.Vaccine) = viewModelScope.launch {
        database.vaccineDao().deleteVaccine(vaccine)
    }

    fun insertPet(pet: Pet, onComplete: () -> Unit) = viewModelScope.launch {
        repository.insertPet(pet)
        onComplete()
    }

    fun updatePet(pet: Pet, onComplete: () -> Unit) = viewModelScope.launch {
        repository.updatePet(pet)
        onComplete()
    }

    suspend fun getPetById(petId: Long): Pet? {
        return repository.getPetById(petId)
    }
}
