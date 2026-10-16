package com.patikagunlugu.app.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.ActivityTimelineBinding
import com.patikagunlugu.app.ui.adapter.MemoryTimelineAdapter
import com.patikagunlugu.app.utils.DateUtils
import com.patikagunlugu.app.utils.LocaleHelper
import com.patikagunlugu.app.viewmodel.TimelineViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class TimelineActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    private lateinit var binding: ActivityTimelineBinding
    private val viewModel: TimelineViewModel by viewModels()
    private lateinit var adapter: MemoryTimelineAdapter
    private var petId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimelineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        petId = intent.getLongExtra("PET_ID", -1)
        if (petId == -1L) {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupBottomNav()
        observeData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        lifecycleScope.launch {
            val pet = viewModel.getPetById(petId)
            binding.toolbar.title = getString(R.string.pet_memories_format, pet?.name ?: "")
        }
    }

    private fun setupRecyclerView() {
        adapter = MemoryTimelineAdapter { memory ->
            val intent = Intent(this, MemoryDetailActivity::class.java).apply {
                putExtra("MEMORY_ID", memory.id)
            }
            startActivity(intent)
        }
        binding.rvTimeline.layoutManager = LinearLayoutManager(this)
        binding.rvTimeline.adapter = adapter
    }

    private fun setupBottomNav() {
        binding.btnNavMemories.setOnClickListener {
            binding.rvTimeline.smoothScrollToPosition(0)
            viewModel.setSelectedDate(null)
        }

        binding.btnNavAdd.setOnClickListener {
            val dialog = NewMemoryDialogFragment.newInstance(petId)
            dialog.show(supportFragmentManager, "NewMemoryDialog")
        }

        binding.btnNavProfile.setOnClickListener {
            val intent = Intent(this, PetEditActivity::class.java).apply {
                putExtra("PET_ID", petId)
                putExtra("EDIT_MODE", true)
            }
            startActivity(intent)
        }
        
        binding.btnClearFilter.setOnClickListener {
            viewModel.setSelectedDate(null)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.getMemoriesForPet(petId).collect { memories ->
                        adapter.submitList(memories)
                        if (memories.isEmpty()) {
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.rvTimeline.visibility = View.GONE
                        } else {
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.rvTimeline.visibility = View.VISIBLE
                        }
                    }
                }
                
                launch {
                    viewModel.selectedDate.collect { date ->
                        if (date == null) {
                            binding.filterContainer.visibility = View.GONE
                        } else {
                            binding.filterContainer.visibility = View.VISIBLE
                            binding.tvFilterInfo.text = getString(R.string.filter_format, DateUtils.formatMemoryDate(date))
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_timeline, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_calendar -> {
                showDatePicker()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedCal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            viewModel.setSelectedDate(selectedCal.timeInMillis)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        
        dialog.show()
        com.patikagunlugu.app.utils.DateUtils.attachParrotToDatePicker(dialog)
    }
}
