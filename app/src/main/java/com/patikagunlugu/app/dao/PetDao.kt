package com.patikagunlugu.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patikagunlugu.app.model.Pet
import com.patikagunlugu.app.model.PetWithMemories
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets ORDER BY name ASC")
    fun getAllPets(): Flow<List<Pet>>

    @androidx.room.Transaction
    @Query("SELECT * FROM pets ORDER BY name ASC")
    fun getPetsWithMemories(): Flow<List<PetWithMemories>>

    @Query("SELECT * FROM pets WHERE id = :petId")
    fun getPetFlow(petId: Long): Flow<Pet?>

    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getPetById(petId: Long): Pet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: Pet): Long

    @Update
    suspend fun updatePet(pet: Pet)

    @Delete
    suspend fun deletePet(pet: Pet)
}
