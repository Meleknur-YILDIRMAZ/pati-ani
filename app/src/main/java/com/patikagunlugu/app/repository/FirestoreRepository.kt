package com.patikagunlugu.app.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.patikagunlugu.app.model.Memory
import com.patikagunlugu.app.model.Pet
import com.patikagunlugu.app.model.Vaccine
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance().apply {
        firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }
    }
    private val storage = FirebaseStorage.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    // Pets
    suspend fun savePet(pet: Pet) {
        val uid = getUserId() ?: return
        firestore.collection("users").document(uid)
            .collection("animals").document(pet.id.toString())
            .set(pet, SetOptions.merge())
            .await()
    }

    suspend fun getPets(): List<Pet> {
        val uid = getUserId() ?: return emptyList()
        val snapshot = firestore.collection("users").document(uid)
            .collection("animals")
            .get()
            .await()
        return snapshot.toObjects(Pet::class.java)
    }

    // Memories
    suspend fun saveMemory(memory: Memory) {
        val uid = getUserId() ?: return
        
        val sharedIds = memory.sharedPetIdsStr.split(",")
            .mapNotNull { it.toLongOrNull() }
        val allPetIds = (listOf(memory.petId) + sharedIds).distinct()

        val memoryMap = hashMapOf(
            "id" to memory.id,
            "petId" to memory.petId,
            "sharedPetIdsStr" to memory.sharedPetIdsStr,
            "timestamp" to memory.timestamp,
            "noteText" to memory.noteText,
            "imagePath" to memory.imagePath,
            "videoPath" to memory.videoPath,
            "audioPath" to memory.audioPath,
            "audioDuration" to memory.audioDuration,
            "mood" to memory.mood,
            "updatedAt" to memory.updatedAt,
            "petIds" to allPetIds
        )

        firestore.collection("users").document(uid)
            .collection("memories").document(memory.id.toString())
            .set(memoryMap, SetOptions.merge())
            .await()
    }

    suspend fun getMemories(petId: Long): List<Memory> {
        val uid = getUserId() ?: return emptyList()
        val snapshot = firestore.collection("users").document(uid)
            .collection("memories")
            .whereArrayContains("petIds", petId)
            .get()
            .await()
        return snapshot.toObjects(Memory::class.java)
    }

    // Vaccines
    suspend fun saveVaccine(vaccine: Vaccine) {
        val uid = getUserId() ?: return
        firestore.collection("users").document(uid)
            .collection("animals").document(vaccine.petId.toString())
            .collection("vaccines").document(vaccine.id.toString())
            .set(vaccine, SetOptions.merge())
            .await()
    }

    suspend fun getVaccines(petId: Long): List<Vaccine> {
        val uid = getUserId() ?: return emptyList()
        val snapshot = firestore.collection("users").document(uid)
            .collection("animals").document(petId.toString())
            .collection("vaccines")
            .get()
            .await()
        return snapshot.toObjects(Vaccine::class.java)
    }

    // Photo Upload
    suspend fun uploadPhoto(uriString: String, path: String): String {
        val uid = getUserId() ?: throw Exception("User not logged in")
        val storageRef = storage.reference.child("users/$uid/$path")
        val uploadTask = storageRef.putFile(Uri.parse(uriString)).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    companion object {
        @Volatile
        private var instance: FirestoreRepository? = null

        fun getInstance(): FirestoreRepository {
            return instance ?: synchronized(this) {
                instance ?: FirestoreRepository().also { instance = it }
            }
        }
    }
}
