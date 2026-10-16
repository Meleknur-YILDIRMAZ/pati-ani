package com.patikagunlugu.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.patikagunlugu.app.databinding.FragmentCalendarBinding
import com.patikagunlugu.app.ui.adapter.MemoryTimelineAdapter
import com.patikagunlugu.app.utils.DateUtils
import com.patikagunlugu.app.viewmodel.TimelineViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TimelineViewModel by viewModels()
    private lateinit var adapter: MemoryTimelineAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupCalendar()
        observeMemories()
    }

    private fun setupRecyclerView() {
        adapter = MemoryTimelineAdapter { memory ->
            val action = CalendarFragmentDirections.actionGlobalMemoryDetailFragment(memory.id)
            findNavController().navigate(action)
        }
        binding.rvDayMemories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDayMemories.adapter = adapter
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            viewModel.setSelectedDate(selectedCal.timeInMillis)
            binding.tvSelectedDate.text = DateUtils.formatMemoryDate(selectedCal.timeInMillis)
        }
        
        val now = System.currentTimeMillis()
        viewModel.setSelectedDate(now)
        binding.tvSelectedDate.text = DateUtils.formatMemoryDate(now)
    }

    private fun observeMemories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Fetch memories for ALL pets on this date
                viewModel.allMemories.collect { memories ->
                    adapter.submitList(memories)
                    if (memories.isEmpty()) {
                        binding.emptyCalendarState.visibility = View.VISIBLE
                        binding.rvDayMemories.visibility = View.GONE
                    } else {
                        binding.emptyCalendarState.visibility = View.GONE
                        binding.rvDayMemories.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
