package com.patikagunlugu.app.model

import androidx.room.Embedded
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Relation

data class PetWithMemories(
    @Embedded val pet: Pet,
    @Relation(
        parentColumn = "id",
        entityColumn = "petId"
    )
    val memories: List<Memory>
) {
    val recentMemories: List<Memory>
        get() = memories.sortedByDescending { it.timestamp }.take(3)
}

class PetWithMemoriesDiffCallback : DiffUtil.ItemCallback<PetWithMemories>() {
    override fun areItemsTheSame(oldItem: PetWithMemories, newItem: PetWithMemories): Boolean {
        return oldItem.pet.id == newItem.pet.id
    }

    override fun areContentsTheSame(oldItem: PetWithMemories, newItem: PetWithMemories): Boolean {
        return oldItem == newItem
    }
}
