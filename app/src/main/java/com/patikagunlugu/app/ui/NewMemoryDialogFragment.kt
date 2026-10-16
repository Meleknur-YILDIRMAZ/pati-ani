package com.patikagunlugu.app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.DialogNewMemoryBinding
import com.patikagunlugu.app.manager.AudioRecorderManager
import com.patikagunlugu.app.model.Memory
import com.patikagunlugu.app.model.Pet
import com.patikagunlugu.app.utils.FileUtils
import com.patikagunlugu.app.utils.MediaUtils
import com.patikagunlugu.app.utils.PermissionHelper
import com.patikagunlugu.app.viewmodel.TimelineViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

class NewMemoryDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogNewMemoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TimelineViewModel by activityViewModels()
    private var currentPetId: Long = -1

    private var selectedMediaType: String = "text_only"
    private var currentMediaFile: File? = null
    private var durationSec: Int? = null

    private var audioRecorder: AudioRecorderManager? = null
    private var isRecording = false

    private val petCheckBoxes = mutableListOf<Pair<Pet, CheckBox>>()

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentMediaFile != null) {
            showMediaPreview("photo")
        } else {
            currentMediaFile = null
        }
    }

    private val captureVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success && currentMediaFile != null) {
            showMediaPreview("video")
        } else {
            currentMediaFile = null
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[android.Manifest.permission.RECORD_AUDIO] ?: false
        if (cameraGranted && selectedMediaType == "photo") launchCameraForPhoto()
        if (cameraGranted && audioGranted && selectedMediaType == "video") launchCameraForVideo()
        if (audioGranted && selectedMediaType == "voice") toggleAudioRecording()
    }

    companion object {
        fun newInstance(petId: Long): NewMemoryDialogFragment {
            val fragment = NewMemoryDialogFragment()
            val args = Bundle()
            args.putLong("PET_ID", petId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentPetId = arguments?.getLong("PET_ID") ?: -1
        audioRecorder = AudioRecorderManager(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogNewMemoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        loadPetsForSharedMemory()
    }

    private fun loadPetsForSharedMemory() {
        lifecycleScope.launch {
            val allPets = viewModel.allPets.firstOrNull() ?: emptyList()
            binding.rvSharedPets.removeAllViews() // Using LinearLayout manually as recycler can be tricky in scrollview
            
            val container = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
            
            allPets.forEach { pet ->
                if (pet.id != currentPetId) { // Don't show current pet
                    val itemView = layoutInflater.inflate(R.layout.item_pet_selection, container, false)
                    val cbSelect = itemView.findViewById<CheckBox>(R.id.cbSelectPet)
                    val tvName = itemView.findViewById<TextView>(R.id.tvPetName)
                    tvName.text = pet.name
                    container.addView(itemView)
                    petCheckBoxes.add(Pair(pet, cbSelect))
                }
            }
            if (petCheckBoxes.isEmpty()) {
                binding.rvSharedPets.visibility = View.GONE
            } else {
                binding.rvSharedPets.addView(container)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnPhoto.setOnClickListener {
            selectedMediaType = "photo"
            if (PermissionHelper.hasCameraPermission(requireContext())) {
                launchCameraForPhoto()
            } else {
                requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
            }
        }

        binding.btnVideo.setOnClickListener {
            selectedMediaType = "video"
            if (PermissionHelper.hasCameraPermission(requireContext()) && PermissionHelper.hasAudioPermission(requireContext())) {
                launchCameraForVideo()
            } else {
                requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO))
            }
        }

        binding.btnVoice.setOnClickListener {
            selectedMediaType = "voice"
            if (PermissionHelper.hasAudioPermission(requireContext())) {
                toggleAudioRecording()
            } else {
                requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.RECORD_AUDIO))
            }
        }

        binding.btnTextOnly.setOnClickListener {
            selectedMediaType = "text_only"
            currentMediaFile = null
            binding.mediaPreviewContainer.visibility = View.GONE
            binding.etNote.requestFocus()
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSaveMemory.setOnClickListener { saveMemory() }
    }

    private fun launchCameraForPhoto() {
        val photoFile = FileUtils.getPhotoFile(requireContext())
        currentMediaFile = photoFile
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
        takePhotoLauncher.launch(uri)
    }

    private fun launchCameraForVideo() {
        val videoFile = FileUtils.getVideoFile(requireContext())
        currentMediaFile = videoFile
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", videoFile)
        captureVideoLauncher.launch(uri)
    }

    private fun toggleAudioRecording() {
        if (!isRecording) {
            val audioFile = FileUtils.getAudioFile(requireContext())
            currentMediaFile = audioFile
            if (audioRecorder?.startRecording(audioFile) == true) {
                isRecording = true
                showMediaPreview("voice")
            } else {
                Toast.makeText(requireContext(), getString(R.string.record_start_failed), Toast.LENGTH_SHORT).show()
            }
        } else {
            val success = audioRecorder?.stopRecording() ?: false
            isRecording = false
            
            val file = currentMediaFile
            if (success && file != null && file.exists() && file.length() > 0) {
                binding.tvVoiceRecordingStatus.text = getString(R.string.record_completed)
                binding.tvVoiceRecordingStatus.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            } else {
                currentMediaFile = null
                binding.mediaPreviewContainer.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.record_failed_too_short), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMediaPreview(type: String) {
        binding.mediaPreviewContainer.visibility = View.VISIBLE
        binding.ivPreview.visibility = View.GONE
        binding.cardJellyfishPreview.visibility = View.GONE
        binding.ivPreviewPlayIcon.visibility = View.GONE
        binding.tvVoiceRecordingStatus.visibility = View.GONE

        when (type) {
            "photo" -> {
                binding.ivPreview.visibility = View.VISIBLE
                Glide.with(this).load(currentMediaFile).into(binding.ivPreview)
            }
            "video" -> {
                binding.cardJellyfishPreview.visibility = View.VISIBLE
            }
            "voice" -> {
                binding.tvVoiceRecordingStatus.visibility = View.VISIBLE
                binding.tvVoiceRecordingStatus.text = getString(R.string.record_started)
                binding.tvVoiceRecordingStatus.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            }
        }
    }

    private fun saveMemory() {
        if (isRecording) {
            Toast.makeText(requireContext(), getString(R.string.stop_record_first), Toast.LENGTH_SHORT).show()
            return
        }

        val noteText = binding.etNote.text.toString().trim()
        val mediaPath = currentMediaFile?.absolutePath

        if (selectedMediaType == "text_only" && noteText.isEmpty()) {
            binding.tilNote.error = getString(R.string.please_enter_note)
            return
        }

        val timestamp = System.currentTimeMillis()
        
        val sharedIds = petCheckBoxes.filter { it.second.isChecked }.map { it.first.id }
        val sharedPetIdsStr = if (sharedIds.isNotEmpty()) {
            "," + sharedIds.joinToString(",") + ","
        } else {
            ""
        }

        val memory = Memory(
            petId = currentPetId,
            sharedPetIdsStr = sharedPetIdsStr,
            timestamp = timestamp,
            noteText = noteText.ifEmpty { null },
            imagePath = if (selectedMediaType == "photo" || selectedMediaType == "video") mediaPath else null,
            audioPath = if (selectedMediaType == "voice") mediaPath else null,
            audioDuration = durationSec
        )

        val memories = listOf(memory)

        viewModel.insertMemories(memories) {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isRecording) {
            audioRecorder?.stopRecording()
        }
        _binding = null
    }
}
