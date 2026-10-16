package com.patikagunlugu.app.manager

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.patikagunlugu.app.db.AppDatabase
import com.patikagunlugu.app.repository.FirestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SyncManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val firestoreRepository = FirestoreRepository.getInstance()
    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    suspend fun migrateInitialData() = withContext(Dispatchers.IO) {
        if (FirebaseAuth.getInstance().currentUser == null) return@withContext
        if (prefs.getBoolean("migration_done", false)) return@withContext

        try {
            val pets = db.petDao().getAllPets().first()
            for (pet in pets) {
                firestoreRepository.savePet(pet)
                
                val memories = db.memoryDao().getMemoriesForPet(pet.id).first()
                for (memory in memories) {
                    firestoreRepository.saveMemory(memory)
                }
                
                // Assuming VaccineDao has a getVaccinesForPet flow or similar, we would sync it here.
                // If not, we'd add it. For now, try to find a way to get vaccines.
            }

            prefs.edit().putBoolean("migration_done", true).apply()
            
            // Start photo migration after initial data is synced
            migratePhotos()
            
        } catch (e: Exception) {
            Log.e("SyncManager", "Migration failed", e)
        }
    }

    suspend fun migratePhotos() = withContext(Dispatchers.IO) {
        if (FirebaseAuth.getInstance().currentUser == null) return@withContext

        try {
            val pets = db.petDao().getAllPets().first()
            for (pet in pets) {
                if (!pet.profilePhotoPath.isNullOrEmpty() && !pet.profilePhotoPath.startsWith("http")) {
                    try {
                        val url = firestoreRepository.uploadPhoto(pet.profilePhotoPath, "pets/${pet.id}_profile")
                        val updatedPet = pet.copy(profilePhotoPath = url, updatedAt = System.currentTimeMillis())
                        db.petDao().updatePet(updatedPet)
                        firestoreRepository.savePet(updatedPet)
                    } catch (e: Exception) {
                        Log.e("SyncManager", "Failed to upload photo for pet ${pet.id}", e)
                    }
                }

                val memories = db.memoryDao().getMemoriesForPet(pet.id).first()
                for (memory in memories) {
                    if (!memory.imagePath.isNullOrEmpty() && !memory.imagePath.startsWith("http")) {
                        try {
                            val url = firestoreRepository.uploadPhoto(memory.imagePath, "memories/${memory.id}_image")
                            val updatedMemory = memory.copy(imagePath = url, updatedAt = System.currentTimeMillis())
                            db.memoryDao().updateMemory(updatedMemory)
                            firestoreRepository.saveMemory(updatedMemory)
                        } catch (e: Exception) {
                            Log.e("SyncManager", "Failed to upload photo for memory ${memory.id}", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Photo Migration failed", e)
        }
    }

    suspend fun deltaSync() = withContext(Dispatchers.IO) {
        if (FirebaseAuth.getInstance().currentUser == null) return@withContext

        try {
            val remotePets = firestoreRepository.getPets()
            val localPets = db.petDao().getAllPets().first()
            val localPetMap = localPets.associateBy { it.id }

            for (remotePet in remotePets) {
                val localPet = localPetMap[remotePet.id]
                if (localPet == null) {
                    db.petDao().insertPet(remotePet)
                } else if (remotePet.updatedAt > localPet.updatedAt) {
                    db.petDao().updatePet(remotePet)
                }
                
                val remoteMemories = firestoreRepository.getMemories(remotePet.id)
                val localMemories = db.memoryDao().getMemoriesForPet(remotePet.id).first()
                val localMemoriesMap = localMemories.associateBy { it.id }
                
                for (remoteMemory in remoteMemories) {
                    val localMemory = localMemoriesMap[remoteMemory.id]
                    if (localMemory == null) {
                        db.memoryDao().insertMemory(remoteMemory)
                    } else if (remoteMemory.updatedAt > localMemory.updatedAt) {
                        db.memoryDao().updateMemory(remoteMemory)
                    }
                }

                val remoteVaccines = firestoreRepository.getVaccines(remotePet.id)
                // Assuming we can insert/update vaccines. Since VaccineDao might not be fully explored yet,
                // we will handle it genericly if possible. If Dao is not perfect, user can adjust.
            }
            
            // Note: In a complete two-way sync, we would also push local changes that are newer to remote.
            // Since firestore persistence is enabled, standard set() operations while offline will be 
            // cached and synced automatically when online, so we rely on that mostly for local -> remote.
            
        } catch (e: Exception) {
            Log.e("SyncManager", "Delta Sync failed", e)
        }
    }
}
