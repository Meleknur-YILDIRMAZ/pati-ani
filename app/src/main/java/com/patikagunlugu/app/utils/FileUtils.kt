package com.patikagunlugu.app.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    
    private const val FOLDER_PHOTOS = "photos"
    private const val FOLDER_VIDEOS = "videos"
    private const val FOLDER_AUDIO = "audio"
    private const val FOLDER_PROFILES = "profiles"

    fun getPhotoFile(context: Context): File {
        val dir = File(context.filesDir, FOLDER_PHOTOS)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "photo_${generateTimestamp()}.jpg")
    }

    fun getVideoFile(context: Context): File {
        val dir = File(context.filesDir, FOLDER_VIDEOS)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "video_${generateTimestamp()}.mp4")
    }

    fun getAudioFile(context: Context): File {
        val dir = File(context.filesDir, FOLDER_AUDIO)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "audio_${generateTimestamp()}.m4a")
    }

    fun getMemoryMediaFile(context: Context, timestamp: Long, extension: String): File {
        val folder = when (extension) {
            "jpg" -> FOLDER_PHOTOS
            "mp4" -> FOLDER_VIDEOS
            "m4a" -> FOLDER_AUDIO
            else -> FOLDER_PHOTOS
        }
        val dir = File(context.filesDir, folder)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "memory_${timestamp}.$extension")
    }

    fun getProfilePhotoFile(context: Context, petId: Long): File {
        val dir = File(context.filesDir, FOLDER_PROFILES)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "profile_${petId}_${System.currentTimeMillis()}.jpg")
    }

    fun deleteFile(path: String?) {
        if (path.isNullOrEmpty()) return
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }

    fun copyToPublicGallery(context: Context, sourceFile: File, isVideo: Boolean = false) {
        val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("save_to_gallery", true)) return

        try {
            val contentResolver = context.contentResolver
            val displayName = sourceFile.name
            val mimeType = if (isVideo) "video/mp4" else "image/jpeg"
            val relativePath = if (isVideo) "Movies/PatiAnı" else "Pictures/PatiAnı"

            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collectionUri = if (isVideo) {
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val uri = contentResolver.insert(collectionUri, contentValues) ?: return

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateTimestamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }
}
