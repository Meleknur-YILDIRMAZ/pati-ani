package com.patikagunlugu.app.repository

import com.patikagunlugu.app.dao.PetDao
import com.patikagunlugu.app.model.Pet
import com.patikagunlugu.app.model.PetWithMemories
import kotlinx.coroutines.flow.Flow

class PetRepository(private val petDao: PetDao) {

    val allPets: Flow<List<Pet>> = petDao.getAllPets()
    val petsWithMemories: Flow<List<PetWithMemories>> = petDao.getPetsWithMemories()

    fun getPetFlow(petId: Long): Flow<Pet?> {
        return petDao.getPetFlow(petId)
    }

    suspend fun getPetById(petId: Long): Pet? {
        return petDao.getPetById(petId)
    }

    suspend fun insertPet(pet: Pet): Long {
        return petDao.insertPet(pet)
    }

    suspend fun updatePet(pet: Pet) {
        petDao.updatePet(pet)
    }

    suspend fun deletePet(pet: Pet) {
        petDao.deletePet(pet)
    }
}
