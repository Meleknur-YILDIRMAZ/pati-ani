package com.patikagunlugu.app.ui

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.ActivityMemoryDetailBinding
import com.patikagunlugu.app.model.Memory
import com.patikagunlugu.app.utils.DateUtils
import com.patikagunlugu.app.utils.LocaleHelper
import com.patikagunlugu.app.viewmodel.MemoryDetailViewModel
import kotlinx.coroutines.launch
import java.io.File

class MemoryDetailActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    private lateinit var binding: ActivityMemoryDetailBinding
    private val viewModel: MemoryDetailViewModel by viewModels()
    private var memoryId: Long = -1
    private var currentMemory: Memory? = null
    
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        memoryId = intent.getLongExtra("MEMORY_ID", -1)
        if (memoryId == -1L) {
            finish()
            return
        }

        setupToolbar()
        loadMemory()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadMemory() {
        lifecycleScope.launch {
            currentMemory = viewModel.getMemoryById(memoryId)
            currentMemory?.let { memory ->
                binding.tvDate.text = "${DateUtils.formatMemoryDate(memory.timestamp)}, ${DateUtils.formatMemoryTime(memory.timestamp)}"
                binding.tvNote.text = memory.noteText

                if (!memory.imagePath.isNullOrEmpty()) {
                    binding.ivPhoto.visibility = View.VISIBLE
                    Glide.with(this@MemoryDetailActivity).load(File(memory.imagePath)).into(binding.ivPhoto)
                }

                if (!memory.audioPath.isNullOrEmpty()) {
                    binding.audioPanel.visibility = View.VISIBLE
                    binding.btnPlayAudio.setOnClickListener {
                        toggleAudio(memory.audioPath)
                    }
                }
            }
        }
    }

    private fun toggleAudio(path: String?) {
        if (path == null) return
        
        if (isPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            binding.btnPlayAudio.setImageResource(android.R.drawable.ic_media_play)
        } else {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(path)
                prepare()
                start()
                setOnCompletionListener {
                    this@MemoryDetailActivity.isPlaying = false
                    binding.btnPlayAudio.setImageResource(android.R.drawable.ic_media_play)
                }
            }
            isPlaying = true
            binding.btnPlayAudio.setImageResource(android.R.drawable.ic_media_pause)
        }
    }

    private fun setupClickListeners() {
        binding.btnDelete.setOnClickListener {
            currentMemory?.let { mem ->
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.delete_memory)
                    .setMessage(R.string.delete_memory_confirm)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        viewModel.deleteMemory(mem) {
                            finish()
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
