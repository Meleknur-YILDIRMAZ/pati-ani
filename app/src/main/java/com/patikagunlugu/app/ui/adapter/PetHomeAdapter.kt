package com.patikagunlugu.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.patikagunlugu.app.databinding.ItemPetRedesignBinding
import com.patikagunlugu.app.model.PetWithMemories
import com.patikagunlugu.app.model.PetWithMemoriesDiffCallback
import com.patikagunlugu.app.utils.DateUtils
import java.io.File

class PetHomeAdapter(
    private val onItemClick: (com.patikagunlugu.app.model.Pet) -> Unit,
    private val onDeleteClick: (com.patikagunlugu.app.model.Pet) -> Unit
) : ListAdapter<PetWithMemories, PetHomeAdapter.PetViewHolder>(PetWithMemoriesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetRedesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PetViewHolder(private val binding: ItemPetRedesignBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position).pet)
                }
            }
            
            binding.btnDeletePet.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position).pet)
                }
            }
        }

        fun bind(item: PetWithMemories) {
            val pet = item.pet
            binding.tvPetName.text = pet.name
            
            val ageResult = DateUtils.calculateAge(pet.birthDate)
            val context = binding.root.context
            val ageText = when (ageResult) {
                is DateUtils.AgeResult.Years -> context.getString(com.patikagunlugu.app.R.string.age_years, ageResult.years)
                is DateUtils.AgeResult.Months -> context.getString(com.patikagunlugu.app.R.string.age_months, ageResult.months)
                is DateUtils.AgeResult.Newborn -> context.getString(com.patikagunlugu.app.R.string.age_newborn)
                null -> null
            }
            val localizedSpecies = when (pet.species) {
                "Kedi", "Cat" -> context.getString(com.patikagunlugu.app.R.string.species_cat)
                "Köpek", "Dog" -> context.getString(com.patikagunlugu.app.R.string.species_dog)
                "Kuş", "Bird" -> context.getString(com.patikagunlugu.app.R.string.species_bird)
                "Tavşan", "Rabbit" -> context.getString(com.patikagunlugu.app.R.string.species_rabbit)
                else -> context.getString(com.patikagunlugu.app.R.string.species_other)
            }
            binding.tvPetDetails.text = if (ageText != null) "$localizedSpecies · $ageText" else localizedSpecies

            if (!pet.profilePhotoPath.isNullOrEmpty()) {
                binding.ivPetPhoto.setPadding(0, 0, 0, 0)
                binding.ivPetPhoto.imageTintList = null
                binding.ivPetPhoto.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                Glide.with(binding.ivPetPhoto.context)
                    .load(File(pet.profilePhotoPath))
                    .centerCrop()
                    .into(binding.ivPetPhoto)
            } else {
                binding.ivPetPhoto.setPadding(14, 14, 14, 14)
                binding.ivPetPhoto.setImageResource(com.patikagunlugu.app.R.drawable.ic_profile_placeholder)
                binding.ivPetPhoto.imageTintList = android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(context, com.patikagunlugu.app.R.color.calendarMutedColor)
                )
                binding.ivPetPhoto.setBackgroundColor(
                    androidx.core.content.ContextCompat.getColor(context, com.patikagunlugu.app.R.color.colorPrimaryContainer)
                )
            }

            // Recent Memories
            val recent = item.recentMemories
            if (recent.isNotEmpty()) {
                binding.tvRecentMemoriesLabel.visibility = View.VISIBLE
                binding.rvRecentMemories.visibility = View.VISIBLE
                // Setup thumbnail adapter if needed, or just simple icons for now
                // For now, let's just show the first one if it exists
            } else {
                binding.tvRecentMemoriesLabel.visibility = View.GONE
                binding.rvRecentMemories.visibility = View.GONE
            }
        }
    }
}
