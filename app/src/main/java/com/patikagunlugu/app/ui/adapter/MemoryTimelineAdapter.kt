package com.patikagunlugu.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.patikagunlugu.app.databinding.ItemMemoryRedesignBinding
import com.patikagunlugu.app.databinding.ItemTimelineHeaderBinding
import com.patikagunlugu.app.model.Memory
import com.patikagunlugu.app.utils.DateUtils
import java.io.File

class MemoryTimelineAdapter(
    private val onItemClick: (Memory) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<TimelineItem>()

    fun submitList(memories: List<Memory>) {
        items.clear()
        val grouped = memories.groupBy { DateUtils.formatMemoryDate(it.timestamp) }
        
        for ((dateString, memList) in grouped) {
            items.add(TimelineItem.Header(dateString))
            for (mem in memList) {
                items.add(TimelineItem.MemoryItem(mem))
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is TimelineItem.Header -> VIEW_TYPE_HEADER
        is TimelineItem.MemoryItem -> VIEW_TYPE_MEMORY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val binding = ItemTimelineHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemMemoryRedesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            MemoryViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is HeaderViewHolder && item is TimelineItem.Header) {
            holder.bind(item.dateStr)
        } else if (holder is MemoryViewHolder && item is TimelineItem.MemoryItem) {
            holder.bind(item.memory)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(private val binding: ItemTimelineHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dateStr: String) {
            binding.tvDateHeader.text = dateStr
        }
    }

    inner class MemoryViewHolder(private val binding: ItemMemoryRedesignBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = items[position]
                    if (item is TimelineItem.MemoryItem) {
                        onItemClick(item.memory)
                    }
                }
            }
        }

        fun bind(memory: Memory) {
            binding.chipDate.text = DateUtils.formatMemoryTime(memory.timestamp)
            binding.tvNote.text = memory.noteText ?: ""
            binding.tvMood.text = memory.mood ?: ""
            
            if (!memory.imagePath.isNullOrEmpty()) {
                binding.ivMedia.visibility = View.VISIBLE
                Glide.with(binding.ivMedia.context)
                    .load(File(memory.imagePath))
                    .centerCrop()
                    .into(binding.ivMedia)
            } else {
                binding.ivMedia.visibility = View.GONE
            }
            
            binding.ivVoiceIndicator.visibility = if (!memory.audioPath.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_MEMORY = 1
    }

    sealed class TimelineItem {
        data class Header(val dateStr: String) : TimelineItem()
        data class MemoryItem(val memory: Memory) : TimelineItem()
    }
}
