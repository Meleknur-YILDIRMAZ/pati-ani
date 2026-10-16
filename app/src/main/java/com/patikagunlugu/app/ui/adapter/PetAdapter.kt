package com.patikagunlugu.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.patikagunlugu.app.databinding.ItemPetBinding
import com.patikagunlugu.app.model.Pet
import com.patikagunlugu.app.utils.DateUtils
import java.io.File

class PetAdapter(
    private val onItemClick: (Pet) -> Unit
) : ListAdapter<Pet, PetAdapter.PetViewHolder>(PetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PetViewHolder(private val binding: ItemPetBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(pet: Pet) {
            binding.tvPetName.text = pet.name
            val context = binding.root.context
            val localizedSpecies = when (pet.species) {
                "Kedi", "Cat" -> context.getString(com.patikagunlugu.app.R.string.species_cat)
                "Köpek", "Dog" -> context.getString(com.patikagunlugu.app.R.string.species_dog)
                "Kuş", "Bird" -> context.getString(com.patikagunlugu.app.R.string.species_bird)
                "Tavşan", "Rabbit" -> context.getString(com.patikagunlugu.app.R.string.species_rabbit)
                else -> context.getString(com.patikagunlugu.app.R.string.species_other)
            }
            binding.tvPetSpecies.text = if (!pet.breed.isNullOrEmpty()) "$localizedSpecies - ${pet.breed}" else localizedSpecies
            
            val ageResult = DateUtils.calculateAge(pet.birthDate)
            val ageText = when (ageResult) {
                is DateUtils.AgeResult.Years -> context.getString(com.patikagunlugu.app.R.string.age_years, ageResult.years)
                is DateUtils.AgeResult.Months -> context.getString(com.patikagunlugu.app.R.string.age_months, ageResult.months)
                is DateUtils.AgeResult.Newborn -> context.getString(com.patikagunlugu.app.R.string.age_newborn)
                else -> null
            }

            if (ageText != null) {
                binding.tvPetAge.text = ageText
                binding.tvPetAge.visibility = android.view.View.VISIBLE
            } else {
                binding.tvPetAge.visibility = android.view.View.GONE
            }

            if (!pet.profilePhotoPath.isNullOrEmpty()) {
                Glide.with(binding.ivPetPhoto.context)
                    .load(File(pet.profilePhotoPath))
                    .centerCrop()
                    .into(binding.ivPetPhoto)
            } else {
                binding.ivPetPhoto.setImageDrawable(null)
            }
        }
    }

    class PetDiffCallback : DiffUtil.ItemCallback<Pet>() {
        override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem == newItem
        }
    }
}
