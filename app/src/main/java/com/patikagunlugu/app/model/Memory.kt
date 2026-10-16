package com.patikagunlugu.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memories",
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["petId"])]
)
data class Memory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val sharedPetIdsStr: String = "", // Format: ",id1,id2,"
    val timestamp: Long,
    val noteText: String?,
    val imagePath: String? = null,
    val videoPath: String? = null,
    val audioPath: String? = null,
    val audioDuration: Int? = null,
    val mood: String? = null, // 😊, 😢, 🤣, 😍, 🌟
    val updatedAt: Long = System.currentTimeMillis()
)
