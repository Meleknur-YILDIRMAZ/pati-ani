package com.patikagunlugu.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.patikagunlugu.app.databinding.ItemVaccineBinding
import com.patikagunlugu.app.model.Vaccine
import com.patikagunlugu.app.utils.DateUtils

class VaccineAdapter(private val onDelete: ((Vaccine) -> Unit)? = null) : ListAdapter<Vaccine, VaccineAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemVaccineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVaccineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vaccine = getItem(position)
        holder.binding.tvVaccineName.text = vaccine.name
        holder.binding.tvVaccineDate.text = DateUtils.formatMemoryDate(vaccine.dateGiven)
        
        vaccine.nextDueDate?.let {
            val context = holder.binding.root.context
            holder.binding.tvNextDue.text = context.getString(com.patikagunlugu.app.R.string.next_dose_format, DateUtils.formatMemoryDate(it))
        }
        
        if (onDelete != null) {
            holder.binding.btnDelete.visibility = android.view.View.VISIBLE
            holder.binding.btnDelete.setOnClickListener { onDelete.invoke(vaccine) }
        } else {
            holder.binding.btnDelete.visibility = android.view.View.GONE
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Vaccine>() {
        override fun areItemsTheSame(oldItem: Vaccine, newItem: Vaccine): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Vaccine, newItem: Vaccine): Boolean = oldItem == newItem
    }
}
