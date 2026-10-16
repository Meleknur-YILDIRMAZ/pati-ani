package com.patikagunlugu.app.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.ActivityPetEditBinding
import com.patikagunlugu.app.model.Pet
import com.patikagunlugu.app.utils.DateUtils
import com.patikagunlugu.app.utils.FileUtils
import com.patikagunlugu.app.utils.LocaleHelper
import com.patikagunlugu.app.utils.PermissionHelper
import com.patikagunlugu.app.viewmodel.PetEditViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class PetEditActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    private lateinit var binding: ActivityPetEditBinding
    private val viewModel: PetEditViewModel by viewModels()

    private var currentPhotoFile: File? = null
    private var birthDateTimestamp: Long? = null
    private var isEditMode = false
    private var petIdToEdit: Long = 0

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoFile != null) {
            com.patikagunlugu.app.utils.FileUtils.copyToPublicGallery(this, currentPhotoFile!!, false)
            Glide.with(this).load(currentPhotoFile).into(binding.ivProfilePhoto)
        } else {
            currentPhotoFile = null
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        petIdToEdit = intent.getLongExtra("PET_ID", 0)
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false) && petIdToEdit != 0L

        setupToolbar()
        setupSpinner()
        setupClickListeners()

        if (isEditMode) {
            loadPetData()
        }
    }

    private fun loadPetData() {
        lifecycleScope.launch {
            val pet = viewModel.getPetById(petIdToEdit)
            pet?.let {
                binding.etName.setText(it.name)
                binding.etBreed.setText(it.breed)
                birthDateTimestamp = it.birthDate
                birthDateTimestamp?.let { ts ->
                    binding.tvBirthDate.text = DateUtils.formatMemoryDate(ts)
                }
                val speciesArray = resources.getStringArray(R.array.species_array)
                val index = speciesArray.indexOf(it.species)
                if (index >= 0) binding.spinnerSpecies.setSelection(index)
                
                it.profilePhotoPath?.let { path ->
                    currentPhotoFile = File(path)
                    Glide.with(this@PetEditActivity).load(currentPhotoFile).into(binding.ivProfilePhoto)
                }
                
                binding.btnSave.text = getString(R.string.update)
                binding.toolbar.title = getString(R.string.pet_memories_format, it.name) // or create a specific string for edit profile
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.species_array,
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.spinnerSpecies.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.fabChangePhoto.setOnClickListener {
            if (PermissionHelper.hasCameraPermission(this)) {
                launchCamera()
            } else {
                requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }

        binding.tvBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            birthDateTimestamp?.let { calendar.timeInMillis = it }
            
            val dialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)
                birthDateTimestamp = selectedCal.timeInMillis
                binding.tvBirthDate.text = DateUtils.formatMemoryDate(birthDateTimestamp!!)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dialog.show()
            DateUtils.attachParrotToDatePicker(dialog)
        }

        binding.btnSave.setOnClickListener {
            savePet()
        }
    }

    private fun launchCamera() {
        val photoFile = FileUtils.getProfilePhotoFile(this, System.currentTimeMillis())
        currentPhotoFile = photoFile
        val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(uri)
    }

    private fun savePet() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.name_required)
            return
        }

        val species = binding.spinnerSpecies.selectedItem.toString()
        val breed = binding.etBreed.text.toString().trim()

        val pet = Pet(
            id = if (isEditMode) petIdToEdit else 0,
            name = name,
            species = species,
            breed = breed.ifEmpty { null },
            birthDate = birthDateTimestamp,
            profilePhotoPath = currentPhotoFile?.absolutePath
        )

        if (isEditMode) {
            viewModel.updatePet(pet) { finish() }
        } else {
            viewModel.insertPet(pet) { finish() }
        }
    }
}
