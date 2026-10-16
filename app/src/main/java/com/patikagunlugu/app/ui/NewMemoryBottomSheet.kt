package com.patikagunlugu.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.BottomSheetNewMemoryBinding
import com.patikagunlugu.app.model.Memory
import com.patikagunlugu.app.viewmodel.TimelineViewModel
import android.net.Uri
import kotlinx.coroutines.launch

class NewMemoryBottomSheet : BottomSheetDialogFragment() {

    private var _binding: com.patikagunlugu.app.databinding.BottomSheetNewMemoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TimelineViewModel by viewModels()
    private val args: NewMemoryBottomSheetArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = com.patikagunlugu.app.databinding.BottomSheetNewMemoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
    }

    private var currentPhotoFile: java.io.File? = null
    private var currentVideoFile: java.io.File? = null
    private var currentAudioFile: java.io.File? = null
    private var isRecording = false
    private lateinit var audioRecorderManager: com.patikagunlugu.app.manager.AudioRecorderManager
    private var progressJob: kotlinx.coroutines.Job? = null

    private val takePictureLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoFile != null) {
            com.patikagunlugu.app.utils.FileUtils.copyToPublicGallery(requireContext(), currentPhotoFile!!, false)
            binding.ivMediaPreview.visibility = View.VISIBLE
            binding.cardJellyfishPreview.visibility = View.GONE
            binding.ivMediaPreview.setImageURI(Uri.fromFile(currentPhotoFile))
            Toast.makeText(requireContext(), getString(R.string.photo_added), Toast.LENGTH_SHORT).show()
        }
    }

    private val captureVideoLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.CaptureVideo()) { success ->
        if (success && currentVideoFile != null) {
            com.patikagunlugu.app.utils.FileUtils.copyToPublicGallery(requireContext(), currentVideoFile!!, true)
            binding.ivMediaPreview.visibility = View.GONE
            binding.cardJellyfishPreview.visibility = View.VISIBLE
            Toast.makeText(requireContext(), getString(R.string.video_added), Toast.LENGTH_SHORT).show()
        }
    }

    private val requestAudioPermissionLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) startVoiceRecording()
        else Toast.makeText(requireContext(), getString(R.string.mic_permission_denied), Toast.LENGTH_SHORT).show()
    }

    private fun setupClickListeners() {
        audioRecorderManager = com.patikagunlugu.app.manager.AudioRecorderManager(requireContext())
        
        binding.btnSave.setOnClickListener {
            saveMemory()
        }
        
        binding.btnPhoto.setOnClickListener {
            if (com.patikagunlugu.app.utils.PermissionHelper.hasCameraPermission(requireContext())) {
                launchCamera()
            } else {
                requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
        
        binding.btnVideo.setOnClickListener { 
            if (com.patikagunlugu.app.utils.PermissionHelper.hasCameraPermission(requireContext())) {
                launchVideoCamera()
            } else {
                requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
        
        binding.btnVoice.setOnClickListener {
            if (isRecording) {
                stopVoiceRecording()
            } else {
                if (com.patikagunlugu.app.utils.PermissionHelper.hasAudioPermission(requireContext())) {
                    startVoiceRecording()
                } else {
                    requestAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }
            }
        }

        binding.btnText.setOnClickListener { 
            binding.etNote.requestFocus()
        }
    }

    private var mediaPlayer: android.media.MediaPlayer? = null

    private fun startVoiceRecording() {
        val audioFile = com.patikagunlugu.app.utils.FileUtils.getMemoryMediaFile(requireContext(), System.currentTimeMillis(), "m4a")
        currentAudioFile = audioFile
        if (audioRecorderManager.startRecording(audioFile)) {
            isRecording = true
            binding.btnVoice.setCardBackgroundColor(resources.getColor(R.color.colorError, null))
            binding.cardAudioPreview.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.record_started), Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopVoiceRecording() {
        val success = audioRecorderManager.stopRecording()
        isRecording = false
        binding.btnVoice.setCardBackgroundColor(resources.getColor(R.color.colorSurface, null))
        
        val file = currentAudioFile
        if (success && file != null && file.exists() && file.length() > 0) {
            binding.cardAudioPreview.visibility = View.VISIBLE
            try {
                val tempPlayer = android.media.MediaPlayer()
                tempPlayer.setDataSource(file.absolutePath)
                tempPlayer.prepare()
                binding.fishProgressView.setTimes(0, tempPlayer.duration)
                binding.fishProgressView.setProgress(0.0f)
                tempPlayer.release()
            } catch (e: Exception) {
                binding.fishProgressView.setTimes(0, 0)
                binding.fishProgressView.setProgress(0.0f)
            }
            setupPreviewPlayer()
            Toast.makeText(requireContext(), getString(R.string.record_completed), Toast.LENGTH_SHORT).show()
        } else {
            currentAudioFile = null
            binding.cardAudioPreview.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.record_failed_too_short), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPreviewPlayer() {
        binding.btnPlayPreview.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                stopPreviewPlayback()
            } else {
                startPreviewPlayback()
            }
        }
    }

    private fun startPreviewPlayback() {
        val path = currentAudioFile?.absolutePath ?: return
        mediaPlayer = android.media.MediaPlayer().apply {
            try {
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(path)
                prepare()
                setVolume(1.0f, 1.0f)
                start()
                binding.btnPlayPreview.setIconResource(android.R.drawable.ic_media_pause)
                binding.tvAudioStatus.text = if (binding.tvAudioStatus.text.toString().contains("Voice")) "Playing..." else "Oynatılıyor..."
                
                val totalDuration = duration
                
                progressJob?.cancel()
                progressJob = viewLifecycleOwner.lifecycleScope.launch {
                    while (this@apply.isPlaying) {
                        val current = this@apply.currentPosition
                        binding.fishProgressView.setProgress(current.toFloat() / totalDuration.toFloat())
                        binding.fishProgressView.setTimes(current, totalDuration)
                        kotlinx.coroutines.delay(100)
                    }
                }
                
                setOnCompletionListener {
                    stopPreviewPlayback()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Playback error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopPreviewPlayback() {
        progressJob?.cancel()
        progressJob = null
        
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
            } catch (e: Exception) {}
            player.release()
        }
        mediaPlayer = null
        
        binding.btnPlayPreview.setIconResource(android.R.drawable.ic_media_play)
        binding.tvAudioStatus.text = getString(R.string.voice_note)
        
        binding.fishProgressView.setProgress(0.0f)
        
        currentAudioFile?.let { file ->
            try {
                val tempPlayer = android.media.MediaPlayer()
                tempPlayer.setDataSource(file.absolutePath)
                tempPlayer.prepare()
                binding.fishProgressView.setTimes(0, tempPlayer.duration)
                tempPlayer.release()
            } catch (e: Exception) {}
        }
    }

    override fun onStop() {
        super.onStop()
        stopPreviewPlayback()
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) launchCamera()
        else Toast.makeText(requireContext(), getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
    }

    private fun launchCamera() {
        val photoFile = com.patikagunlugu.app.utils.FileUtils.getMemoryMediaFile(requireContext(), System.currentTimeMillis(), "jpg")
        currentPhotoFile = photoFile
        val uri = androidx.core.content.FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(uri)
    }

    private fun launchVideoCamera() {
        val videoFile = com.patikagunlugu.app.utils.FileUtils.getMemoryMediaFile(requireContext(), System.currentTimeMillis(), "mp4")
        currentVideoFile = videoFile
        val uri = androidx.core.content.FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", videoFile)
        captureVideoLauncher.launch(uri)
    }

    private fun saveMemory() {
        val note = binding.etNote.text.toString().trim()
        val moodId = binding.cgMood.checkedChipId
        val mood = when (moodId) {
            R.id.moodHappy -> "😊"
            R.id.moodSad -> "😢"
            R.id.moodFunny -> "🤣"
            R.id.moodLove -> "😍"
            R.id.moodSpecial -> "🌟"
            else -> null
        }

        if (note.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.please_enter_note), Toast.LENGTH_SHORT).show()
            return
        }

        val memory = Memory(
            petId = args.petId,
            timestamp = System.currentTimeMillis(),
            noteText = note,
            imagePath = currentPhotoFile?.absolutePath,
            videoPath = currentVideoFile?.absolutePath,
            audioPath = currentAudioFile?.absolutePath,
            audioDuration = null,
            mood = mood
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.insertMemory(memory)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
