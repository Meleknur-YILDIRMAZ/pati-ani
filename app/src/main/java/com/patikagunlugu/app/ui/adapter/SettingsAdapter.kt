package com.patikagunlugu.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.patikagunlugu.app.databinding.ItemSettingsBinding
import com.patikagunlugu.app.model.SettingsItem

class SettingsAdapter(
    private val items: List<SettingsItem>,
    private val switchStates: Map<Int, Boolean> = emptyMap(),
    private val onSwitchChange: ((SettingsItem, Boolean) -> Unit)? = null,
    private val onItemClick: (SettingsItem) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSettingsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvTitle.text = item.title
        holder.binding.tvSubtitle.text = item.subtitle
        holder.binding.tvSubtitle.visibility = if (item.subtitle != null) View.VISIBLE else View.GONE
        holder.binding.ivIcon.setImageResource(item.iconRes)
        
        if (item.type == SettingsItem.Type.SWITCH) {
            holder.binding.ivChevron.visibility = View.GONE
            holder.binding.switchWidget.visibility = View.VISIBLE
            
            holder.binding.switchWidget.setOnCheckedChangeListener(null)
            holder.binding.switchWidget.isChecked = switchStates[item.id] ?: false
            
            holder.binding.switchWidget.setOnCheckedChangeListener { _, isChecked ->
                onSwitchChange?.invoke(item, isChecked)
            }
            holder.binding.root.setOnClickListener {
                holder.binding.switchWidget.toggle()
            }
        } else {
            holder.binding.ivChevron.visibility = View.VISIBLE
            holder.binding.switchWidget.visibility = View.GONE
            holder.binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = items.size
}
