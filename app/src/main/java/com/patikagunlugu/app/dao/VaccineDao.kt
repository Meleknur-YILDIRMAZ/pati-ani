package com.patikagunlugu.app.dao

import androidx.room.*
import com.patikagunlugu.app.model.Vaccine
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccineDao {
    @Query("SELECT * FROM vaccines WHERE petId = :petId ORDER BY dateGiven DESC")
    fun getVaccinesForPet(petId: Long): Flow<List<Vaccine>>

    @Query("SELECT * FROM vaccines WHERE nextDueDate IS NOT NULL AND nextDueDate > :currentTime")
    suspend fun getUpcomingVaccines(currentTime: Long): List<Vaccine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccine(vaccine: Vaccine)

    @Update
    suspend fun updateVaccine(vaccine: Vaccine)

    @Delete
    suspend fun deleteVaccine(vaccine: Vaccine)
}
