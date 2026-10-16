package com.patikagunlugu.app.repository

import com.patikagunlugu.app.dao.MemoryDao
import com.patikagunlugu.app.model.Memory
import com.patikagunlugu.app.utils.FileUtils
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {

    fun getMemoriesForPet(petId: Long): Flow<List<Memory>> {
        return memoryDao.getMemoriesForPet(petId)
    }

    fun getAllMemories(): Flow<List<Memory>> {
        return memoryDao.getAllMemories()
    }

    suspend fun getMemoryById(memoryId: Long): Memory? {
        return memoryDao.getMemoryById(memoryId)
    }

    suspend fun insertMemory(memory: Memory): Long {
        return memoryDao.insertMemory(memory)
    }

    suspend fun insertMemories(memories: List<Memory>): List<Long> {
        return memoryDao.insertMemories(memories)
    }

    suspend fun updateMemory(memory: Memory) {
        memoryDao.updateMemory(memory)
    }

    suspend fun deleteMemory(memory: Memory) {
        memoryDao.deleteMemory(memory)
        // If no other memory uses these media files, delete them
        listOfNotNull(memory.imagePath, memory.videoPath, memory.audioPath).forEach { path ->
            val count = memoryDao.getCountForPath(path)
            if (count == 0) {
                FileUtils.deleteFile(path)
            }
        }
    }

    suspend fun deleteAllMemories() {
        memoryDao.deleteAllMemories()
    }
}
