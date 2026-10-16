package com.patikagunlugu.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patikagunlugu.app.model.Memory
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE petId = :petId OR sharedPetIdsStr LIKE '%,' || :petId || ',%' ORDER BY timestamp DESC")
    fun getMemoriesForPet(petId: Long): Flow<List<Memory>>

    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE id = :memoryId")
    suspend fun getMemoryById(memoryId: Long): Memory?

    @Query("SELECT COUNT(*) FROM memories WHERE imagePath = :path OR videoPath = :path OR audioPath = :path")
    suspend fun getCountForPath(path: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<Memory>): List<Long>

    @Update
    suspend fun updateMemory(memory: Memory)

    @Delete
    suspend fun deleteMemory(memory: Memory)

    @Query("DELETE FROM memories")
    suspend fun deleteAllMemories()
}
