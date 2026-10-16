package com.patikagunlugu.app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.FragmentPetEditBinding
import com.patikagunlugu.app.model.Pet
import com.patikagunlugu.app.utils.DateUtils
import com.patikagunlugu.app.utils.FileUtils
import com.patikagunlugu.app.utils.PermissionHelper
import com.patikagunlugu.app.viewmodel.PetEditViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class PetEditFragment : Fragment() {

    private var _binding: FragmentPetEditBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PetEditViewModel by viewModels()
    private val args: PetEditFragmentArgs by navArgs()

    private var currentPhotoFile: File? = null
    private var birthDateTimestamp: Long? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoFile != null) {
            com.patikagunlugu.app.utils.FileUtils.copyToPublicGallery(requireContext(), currentPhotoFile!!, false)
            Glide.with(this).load(currentPhotoFile).into(binding.ivPetPhoto)
            updatePhotoState(true)
        }
    }

    private fun updatePhotoState(hasPhoto: Boolean) {
        if (hasPhoto) {
            binding.ivPetPhoto.visibility = android.view.View.VISIBLE
            binding.photoPlaceholder.visibility = android.view.View.GONE
        } else {
            binding.ivPetPhoto.visibility = android.view.View.GONE
            binding.photoPlaceholder.visibility = android.view.View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPetEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var vaccineAdapter: com.patikagunlugu.app.ui.adapter.VaccineAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        updatePhotoState(false)
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        
        if (args.petId != 0L) {
            loadPetData()
            observeVaccines()
            binding.toolbarTitle.text = getString(R.string.edit_pet_title)
            binding.btnSave.text = getString(R.string.save_changes)
        } else {
            binding.toolbar.title = getString(R.string.add_new_pet)
            binding.toolbarTitle.text = getString(R.string.add_pet_title)
            binding.btnSave.text = getString(R.string.save_my_pet)
        }
    }

    private fun setupRecyclerView() {
        vaccineAdapter = com.patikagunlugu.app.ui.adapter.VaccineAdapter { vaccine ->
            viewModel.deleteVaccine(vaccine)
        }
        binding.rvVaccines.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvVaccines.adapter = vaccineAdapter
    }

    private fun observeVaccines() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getVaccinesForPet(args.petId).collect { vaccines ->
                vaccineAdapter.submitList(vaccines)
            }
        }
    }

    private fun setupClickListeners() {
        val changePhotoAction = {
            if (PermissionHelper.hasCameraPermission(requireContext())) launchCamera()
            else requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        binding.fabChangePhoto.setOnClickListener { changePhotoAction() }
        binding.btnChangePhotoContainer.setOnClickListener { changePhotoAction() }

        binding.chipMale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.chipFemale.isChecked = false
            }
        }
        binding.chipFemale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.chipMale.isChecked = false
            }
        }

        binding.etBirthDate.setOnClickListener { showDatePicker() }

        binding.btnAddVaccine.setOnClickListener {
            if (args.petId != 0L) {
                showAddVaccineDialog()
            } else {
                Toast.makeText(requireContext(), getString(R.string.please_save_pet_first), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSave.setOnClickListener { savePet() }
    }

    private fun showAddVaccineDialog() {
        val dialogBinding = com.patikagunlugu.app.databinding.DialogAddVaccineBinding.inflate(layoutInflater)
        var dateGivenTs = System.currentTimeMillis()
        var nextDueTs = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)

        dialogBinding.etDateGiven.setText(DateUtils.formatMemoryDate(dateGivenTs))
        dialogBinding.etNextDue.setText(DateUtils.formatMemoryDate(nextDueTs))

        dialogBinding.etDateGiven.setOnClickListener {
            val cal = Calendar.getInstance()
            val dialog = DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                dateGivenTs = cal.timeInMillis
                dialogBinding.etDateGiven.setText(DateUtils.formatMemoryDate(dateGivenTs))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dialog.show()
            DateUtils.attachParrotToDatePicker(dialog)
        }

        dialogBinding.etNextDue.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = nextDueTs
            val dialog = DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                nextDueTs = cal.timeInMillis
                dialogBinding.etNextDue.setText(DateUtils.formatMemoryDate(nextDueTs))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dialog.show()
            DateUtils.attachParrotToDatePicker(dialog)
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_vaccine)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = dialogBinding.etVaccineName.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.insertVaccine(com.patikagunlugu.app.model.Vaccine(
                        petId = args.petId,
                        name = name,
                        dateGiven = dateGivenTs,
                        nextDueDate = nextDueTs,
                        notes = null
                    ))
                } else {
                    Toast.makeText(requireContext(), R.string.name_required, Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun loadPetData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val pet = viewModel.getPetById(args.petId)
            pet?.let {
                binding.etName.setText(it.name)
                binding.etBreed.setText(it.breed)
                birthDateTimestamp = it.birthDate
                binding.etBirthDate.setText(it.birthDate?.let { ts -> DateUtils.formatMemoryDate(ts) })
                binding.switchSterilized.isChecked = it.isSterilized
                
                binding.etWeight.setText(it.weight?.toString())
                binding.etMicrochip.setText(it.microchipNumber)
                binding.etVetName.setText(it.vetName)

                // Select corresponding species chip
                when (it.species) {
                    "Kedi", "Cat" -> binding.chipCat.isChecked = true
                    "Köpek", "Dog" -> binding.chipDog.isChecked = true
                    "Kuş", "Bird" -> binding.chipBird.isChecked = true
                    "Tavşan", "Rabbit" -> binding.chipRabbit.isChecked = true
                    else -> binding.chipOther.isChecked = true
                }

                if (it.gender == "Erkek") binding.chipMale.isChecked = true
                else if (it.gender == "Dişi") binding.chipFemale.isChecked = true

                it.profilePhotoPath?.let { path ->
                    currentPhotoFile = File(path)
                    Glide.with(this@PetEditFragment).load(currentPhotoFile).into(binding.ivPetPhoto)
                    updatePhotoState(true)
                }
            }
        }
    }

    private fun savePet() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.name_required)
            binding.tilName.requestFocus()
            return
        }

        // Get selected species from ChipGroup
        val species = when (binding.cgSpecies.checkedChipId) {
            R.id.chipCat -> "Kedi"
            R.id.chipDog -> "Köpek"
            R.id.chipBird -> "Kuş"
            R.id.chipRabbit -> "Tavşan"
            else -> "Diğer"
        }

        val gender = if (binding.chipMale.isChecked) "Erkek" else if (binding.chipFemale.isChecked) "Dişi" else null
        val weight = binding.etWeight.text.toString().toFloatOrNull()
        val microchip = binding.etMicrochip.text.toString().trim().ifEmpty { null }
        val vetName = binding.etVetName.text.toString().trim().ifEmpty { null }

        val pet = Pet(
            id = args.petId,
            name = name,
            species = species,
            breed = binding.etBreed.text.toString().trim().ifEmpty { null },
            birthDate = birthDateTimestamp,
            profilePhotoPath = currentPhotoFile?.absolutePath,
            isSterilized = binding.switchSterilized.isChecked,
            gender = gender,
            weight = weight,
            microchipNumber = microchip,
            vetName = vetName
        )

        Toast.makeText(requireContext(), getString(R.string.saving), Toast.LENGTH_SHORT).show()

        if (args.petId == 0L) {
            viewModel.insertPet(pet) {
                viewLifecycleOwner.lifecycleScope.launch {
                    Toast.makeText(requireContext(), getString(R.string.pet_saved), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        } else {
            viewModel.updatePet(pet) {
                viewLifecycleOwner.lifecycleScope.launch {
                    Toast.makeText(requireContext(), getString(R.string.pet_updated), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) launchCamera()
    }

    private fun launchCamera() {
        val photoFile = com.patikagunlugu.app.utils.FileUtils.getProfilePhotoFile(requireContext(), System.currentTimeMillis())
        currentPhotoFile = photoFile
        val uri = androidx.core.content.FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(uri)
    }

    private fun showDatePicker() {
        val calendar = java.util.Calendar.getInstance()
        birthDateTimestamp?.let { calendar.timeInMillis = it }
        val dialog = android.app.DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedCal = java.util.Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            birthDateTimestamp = selectedCal.timeInMillis
            binding.etBirthDate.setText(com.patikagunlugu.app.utils.DateUtils.formatMemoryDate(birthDateTimestamp!!))
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
        dialog.show()
        DateUtils.attachParrotToDatePicker(dialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
