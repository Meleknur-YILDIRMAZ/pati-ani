package com.patikagunlugu.app.model

import androidx.room.Embedded
import androidx.room.Relation

data class MemoryWithPet(
    @Embedded val memory: Memory,
    @Relation(
        parentColumn = "petId",
        entityColumn = "id"
    )
    val pet: Pet
)
