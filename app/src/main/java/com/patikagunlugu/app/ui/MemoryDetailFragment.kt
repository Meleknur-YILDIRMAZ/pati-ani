package com.patikagunlugu.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.patikagunlugu.app.databinding.FragmentMemoryDetailBinding
import com.patikagunlugu.app.utils.DateUtils
import com.patikagunlugu.app.viewmodel.TimelineViewModel
import kotlinx.coroutines.launch
import java.io.File
import com.patikagunlugu.app.R

class MemoryDetailFragment : Fragment() {

    private var _binding: FragmentMemoryDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TimelineViewModel by viewModels()
    private val args: MemoryDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMemoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        loadMemoryData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
        
        binding.btnShare.setOnClickListener {
            // Share logic (Placeholder)
            android.widget.Toast.makeText(requireContext(), "Paylaşma yakında!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_memory)
            .setMessage(R.string.delete_memory_confirm)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteMemory()
            }
            .show()
    }

    private fun deleteMemory() {
        viewLifecycleOwner.lifecycleScope.launch {
            val memory = viewModel.getMemoryById(args.memoryId)
            memory?.let {
                viewModel.deleteMemory(it)
                findNavController().popBackStack()
            }
        }
    }

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var progressJob: kotlinx.coroutines.Job? = null

    private fun loadMemoryData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val memory = viewModel.getMemoryById(args.memoryId)
            memory?.let {
                binding.tvDate.text = DateUtils.formatMemoryDate(it.timestamp)
                binding.tvTime.text = DateUtils.formatMemoryTime(it.timestamp)
                binding.tvNote.text = it.noteText
                binding.tvMood.text = it.mood ?: ""
                
                if (!it.imagePath.isNullOrEmpty()) {
                    binding.ivMedia.visibility = View.VISIBLE
                    binding.cardVideo.visibility = View.GONE
                    Glide.with(this@MemoryDetailFragment)
                        .load(File(it.imagePath))
                        .centerCrop()
                        .into(binding.ivMedia)
                } else if (!it.videoPath.isNullOrEmpty()) {
                    binding.ivMedia.visibility = View.GONE
                    binding.cardVideo.visibility = View.VISIBLE
                    setupVideoPlayer(it.videoPath)
                } else {
                    binding.ivMedia.visibility = View.GONE
                    binding.cardVideo.visibility = View.GONE
                }

                if (!it.audioPath.isNullOrEmpty()) {
                    binding.cardAudioPlayer.visibility = View.VISIBLE
                    setupAudioPlayer(it.audioPath)
                } else {
                    binding.cardAudioPlayer.visibility = View.GONE
                }
            }
        }
    }

    private var isPaused = false

    private fun setupAudioPlayer(path: String) {
        // Retrieve and display initial duration immediately
        try {
            val tempPlayer = android.media.MediaPlayer()
            tempPlayer.setDataSource(path)
            tempPlayer.prepare()
            val dur = tempPlayer.duration
            binding.fishProgressView.setTimes(0, dur)
            binding.fishProgressView.setProgress(0.0f)
            tempPlayer.release()
        } catch (e: Exception) {
            binding.fishProgressView.setTimes(0, 0)
        }

        binding.btnPlayAudio.setOnClickListener {
            val player = mediaPlayer
            if (player != null && player.isPlaying) {
                pausePlayback()
            } else {
                startPlayback(path)
            }
        }
    }

    private fun startPlayback(path: String) {
        if (mediaPlayer == null) {
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
                    
                    setOnCompletionListener {
                        stopPlayback()
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(requireContext(), getString(R.string.audio_play_error), android.widget.Toast.LENGTH_SHORT).show()
                    mediaPlayer = null
                    return
                }
            }
        }

        mediaPlayer?.let { player ->
            try {
                player.start()
                isPaused = false
                binding.btnPlayAudio.setIconResource(android.R.drawable.ic_media_pause)
                binding.tvAudioStatus.text = getString(R.string.playing)
                
                val totalDuration = player.duration
                
                progressJob?.cancel()
                progressJob = viewLifecycleOwner.lifecycleScope.launch {
                    while (player.isPlaying) {
                        val current = player.currentPosition
                        binding.fishProgressView.setProgress(current.toFloat() / totalDuration.toFloat())
                        binding.fishProgressView.setTimes(current, totalDuration)
                        kotlinx.coroutines.delay(100)
                    }
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(requireContext(), getString(R.string.audio_play_error), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pausePlayback() {
        progressJob?.cancel()
        progressJob = null
        
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                }
                isPaused = true
                binding.btnPlayAudio.setIconResource(android.R.drawable.ic_media_play)
                binding.tvAudioStatus.text = "Duraklatıldı"
            } catch (e: Exception) {}
        }
    }

    private fun setupVideoPlayer(path: String) {
        binding.videoView.setVideoPath(path)
        val mediaController = android.widget.MediaController(requireContext())
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)
        binding.videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            binding.videoView.start()
        }
    }

    private fun stopPlayback() {
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
        isPaused = false
        
        binding.btnPlayAudio.setIconResource(android.R.drawable.ic_media_play)
        binding.tvAudioStatus.text = getString(R.string.voice_note)
        
        binding.fishProgressView.setProgress(0.0f)
        
        // Restore total duration display
        viewLifecycleOwner.lifecycleScope.launch {
            val memory = viewModel.getMemoryById(args.memoryId)
            memory?.audioPath?.let { path ->
                try {
                    val tempPlayer = android.media.MediaPlayer()
                    tempPlayer.setDataSource(path)
                    tempPlayer.prepare()
                    binding.fishProgressView.setTimes(0, tempPlayer.duration)
                    tempPlayer.release()
                } catch (e: Exception) {}
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopPlayback()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
