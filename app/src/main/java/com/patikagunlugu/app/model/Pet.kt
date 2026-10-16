package com.patikagunlugu.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val species: String,
    val breed: String?,
    val birthDate: Long?,
    val profilePhotoPath: String?,
    val gender: String? = null, // Erkek, Dişi, Belirtilmedi
    val isSterilized: Boolean = false,
    val color: String? = null,
    val vetName: String? = null,
    val vetPhone: String? = null,
    val weight: Float? = null,
    val microchipNumber: String? = null,
    val accentColor: Int? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
