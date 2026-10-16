package com.patikagunlugu.app.ui

import android.app.DatePickerDialog
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.FragmentTimelineBinding
import com.patikagunlugu.app.ui.adapter.MemoryTimelineAdapter
import com.patikagunlugu.app.utils.DateUtils
import com.patikagunlugu.app.viewmodel.TimelineViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class TimelineFragment : Fragment() {

    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TimelineViewModel by viewModels()
    private val args: TimelineFragmentArgs by navArgs()
    private lateinit var adapter: MemoryTimelineAdapter
    private var currentPet: com.patikagunlugu.app.model.Pet? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeData()
    }

    private fun setupToolbar() {
        binding.tvToolbarTitle.text = getString(R.string.pet_memories_format, args.petName)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        
        binding.btnCalendar.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupRecyclerView() {
        adapter = MemoryTimelineAdapter { memory ->
            val action = TimelineFragmentDirections.actionTimelineFragmentToMemoryDetailFragment(memory.id)
            findNavController().navigate(action)
        }
        binding.rvTimeline.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTimeline.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.fabAddMemory.setOnClickListener {
            val action = TimelineFragmentDirections.actionGlobalNewMemoryBottomSheet(args.petId)
            findNavController().navigate(action)
        }
        
        binding.btnFirstMemory.setOnClickListener {
            val action = TimelineFragmentDirections.actionGlobalNewMemoryBottomSheet(args.petId)
            findNavController().navigate(action)
        }
        
        binding.btnClearFilter.setOnClickListener {
            viewModel.setSelectedDate(null)
        }

        binding.profileHeader.setOnClickListener {
            val action = TimelineFragmentDirections.actionTimelineFragmentToPetEditFragment(args.petId)
            findNavController().navigate(action)
        }

        binding.btnBirthdayCard.setOnClickListener {
            val pet = currentPet
            if (pet != null && pet.birthDate != null) {
                showBirthdayCelebrationDialog(pet)
            } else {
                val action = TimelineFragmentDirections.actionTimelineFragmentToPetEditFragment(args.petId)
                findNavController().navigate(action)
            }
        }

        binding.btnVaccineCard.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val vaccineList = try {
                    viewModel.getVaccinesForPet(args.petId).first()
                } catch (e: Exception) {
                    emptyList()
                }
                showVaccineRecordsDialog(vaccineList)
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.getMemoriesForPet(args.petId).collect { memories ->
                        adapter.submitList(memories)
                        if (memories.isEmpty()) {
                            binding.emptyState.visibility = View.VISIBLE
                            binding.rvTimeline.visibility = View.GONE
                        } else {
                            binding.emptyState.visibility = View.GONE
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

                launch {
                    viewModel.getPetFlow(args.petId).collect { pet ->
                        pet?.let { bindPetHeader(it) }
                    }
                }
            }
        }
    }

    private fun bindPetHeader(pet: com.patikagunlugu.app.model.Pet) {
        currentPet = pet
        binding.tvPetNameHeader.text = pet.name
        binding.tvPetDetailsHeader.text = "${pet.species} · ${pet.breed ?: ""} · ${calculateAge(pet.birthDate)}"
        binding.tvPetMemoriesTitleHeader.text = getString(R.string.pet_memories_format, pet.name)
        binding.tvEmptySubtitle.text = getString(R.string.add_first_memory_desc, pet.name)
        
        val genderIcon = if (pet.gender == "Erkek") "♂" else "♀"
        val sterilizedText = if (pet.isSterilized) getString(R.string.sterilized_check) else ""
        binding.tvPetStatusHeader.text = "$genderIcon  $sterilizedText"

        if (!pet.profilePhotoPath.isNullOrEmpty()) {
            binding.ivPetAvatar.setPadding(0, 0, 0, 0)
            com.bumptech.glide.Glide.with(this)
                .load(pet.profilePhotoPath)
                .centerCrop()
                .into(binding.ivPetAvatar)
        } else {
            binding.ivPetAvatar.setPadding(18, 18, 18, 18)
            binding.ivPetAvatar.setImageResource(R.drawable.ic_profile_placeholder)
        }

        // For now, these are static/placeholder until we have full logic
        binding.tvMemoryCount.text = "..." 
        binding.tvNextBirthday.text = "..."
        binding.tvNextVaccine.text = "..."
    }

    private fun calculateAge(birthDate: Long?): String {
        if (birthDate == null) return ""
        val now = Calendar.getInstance()
        val birth = Calendar.getInstance().apply { timeInMillis = birthDate }
        var age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--
        return if (age > 0) getString(R.string.age_years, age) else getString(R.string.age_newborn)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedCal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            viewModel.setSelectedDate(selectedCal.timeInMillis)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        
        dialog.show()
        com.patikagunlugu.app.utils.DateUtils.attachParrotToDatePicker(dialog)
    }

    private fun showBirthdayCelebrationDialog(pet: com.patikagunlugu.app.model.Pet) {
        val dialog = android.app.Dialog(requireContext())
        val dialogBinding = com.patikagunlugu.app.databinding.DialogBirthdayCelebrationBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        
        dialogBinding.tvCelebrationPetName.text = pet.name
        
        val birthDate = pet.birthDate
        if (birthDate != null) {
            val now = Calendar.getInstance()
            val birth = Calendar.getInstance().apply { timeInMillis = birthDate }
            var age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--
            
            val ageText = if (age > 0) {
                getString(R.string.age_years, age)
            } else {
                getString(R.string.age_newborn)
            }
            
            // Set Title: Sparkles surrounding pet name (e.g. "✨ Pamuk ✨")
            dialogBinding.tvCelebrationTitle.text = "✨ ${pet.name} ✨"
            
            // Set Description: Sevgili dostunuzla geçireceğiniz nice mutlu yıllara...
            dialogBinding.tvCelebrationDate.text = getString(R.string.birthday_celebration_desc)
            
            // Set Badge: Registered birthday date (e.g. "15 Nisan 2021")
            dialogBinding.tvCelebrationAge.text = DateUtils.formatMemoryDate(birthDate)
        }
        
        if (!pet.profilePhotoPath.isNullOrEmpty()) {
            binding.ivPetAvatar.setPadding(0, 0, 0, 0)
            com.bumptech.glide.Glide.with(this)
                .load(pet.profilePhotoPath)
                .centerCrop()
                .into(dialogBinding.ivPetCelebrationAvatar)
        } else {
            dialogBinding.ivPetCelebrationAvatar.setImageResource(R.drawable.ic_profile_placeholder)
        }
        
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        
        // Animations
        dialogBinding.ivPetCelebrationAvatar.scaleX = 0f
        dialogBinding.ivPetCelebrationAvatar.scaleY = 0f
        dialogBinding.ivPetCelebrationAvatar.animate()
            .scaleX(1f)
            .scaleY(1.2f)
            .scaleY(1f)
            .setDuration(800)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()
            
        val pulseX = android.animation.ObjectAnimator.ofFloat(dialogBinding.tvCelebrationEmojis, "scaleX", 1f, 1.2f, 1f).apply {
            duration = 1500
            repeatCount = android.animation.ValueAnimator.INFINITE
        }
        val pulseY = android.animation.ObjectAnimator.ofFloat(dialogBinding.tvCelebrationEmojis, "scaleY", 1f, 1.2f, 1f).apply {
            duration = 1500
            repeatCount = android.animation.ValueAnimator.INFINITE
        }
        pulseX.start()
        pulseY.start()
        
        dialogBinding.tvCelebrationTitle.translationY = 50f
        dialogBinding.tvCelebrationTitle.alpha = 0f
        dialogBinding.tvCelebrationTitle.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(200)
            .start()
            
        dialogBinding.btnCelebrationClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showVaccineRecordsDialog(vaccines: List<com.patikagunlugu.app.model.Vaccine>) {
        val dialog = android.app.Dialog(requireContext())
        val dialogBinding = com.patikagunlugu.app.databinding.DialogVaccinesListBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        
        val pet = currentPet
        dialogBinding.tvVaccineListTitle.text = getString(R.string.vaccine_passport)
        if (pet != null) {
            val isTr = resources.configuration.locales[0].language == "tr"
            dialogBinding.tvVaccineListSubtitle.text = if (isTr) {
                "${pet.name} için kayıtlı aşı geçmişi"
            } else {
                "Registered vaccines for ${pet.name}"
            }
        }
        
        if (vaccines.isEmpty()) {
            dialogBinding.rvDialogVaccines.visibility = android.view.View.GONE
            dialogBinding.emptyStateContainer.visibility = android.view.View.VISIBLE
            val petName = pet?.name ?: "Dostunuz"
            val isTr = resources.configuration.locales[0].language == "tr"
            dialogBinding.tvVaccinesEmptyMessage.text = if (isTr) {
                "${petName} için kayıtlı aşı geçmişi bulunmamaktadır."
            } else {
                "There is no registered vaccine history for ${petName}."
            }
        } else {
            dialogBinding.rvDialogVaccines.visibility = android.view.View.VISIBLE
            dialogBinding.emptyStateContainer.visibility = android.view.View.GONE
            
            // Limit height dynamically to prevent dialog from overflowing on small screens
            val params = dialogBinding.rvDialogVaccines.layoutParams
            if (vaccines.size > 2) {
                params.height = (220 * resources.displayMetrics.density).toInt()
            } else {
                params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            }
            dialogBinding.rvDialogVaccines.layoutParams = params
            
            val vaccineAdapter = com.patikagunlugu.app.ui.adapter.VaccineAdapter(null)
            dialogBinding.rvDialogVaccines.layoutManager = LinearLayoutManager(requireContext())
            dialogBinding.rvDialogVaccines.adapter = vaccineAdapter
            vaccineAdapter.submitList(vaccines)
        }
        
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        
        dialogBinding.btnVaccineListClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.btnVaccineListManage.setOnClickListener {
            dialog.dismiss()
            val action = TimelineFragmentDirections.actionTimelineFragmentToPetEditFragment(args.petId)
            findNavController().navigate(action)
        }
        
        dialogBinding.root.translationY = 300f
        dialogBinding.root.animate()
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
            
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
