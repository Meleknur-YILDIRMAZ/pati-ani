package com.patikagunlugu.app.manager

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var isRecording = false

    fun startRecording(outputFile: File): Boolean {
        if (isRecording) return false

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        return try {
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun stopRecording(): Boolean {
        if (!isRecording) return false
        var success = false
        try {
            recorder?.apply {
                stop()
                release()
            }
            success = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
        isRecording = false
        return success
    }
}
