package com.patikagunlugu.app.manager

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExportManager {

    fun exportDataToZip(context: Context): File? {
        val exportFile = File(context.cacheDir, "PatikaGunlugu_Export.zip")
        try {
            val fos = FileOutputStream(exportFile)
            val zos = ZipOutputStream(fos)

            // Database file
            val dbFile = context.getDatabasePath("patikagunlugu_database")
            if (dbFile.exists()) {
                addFileToZip(dbFile, "database.db", zos)
            }

            // Media files
            val photosDir = File(context.filesDir, "photos")
            if (photosDir.exists()) {
                photosDir.listFiles()?.forEach { addFileToZip(it, "photos/${it.name}", zos) }
            }

            val videosDir = File(context.filesDir, "videos")
            if (videosDir.exists()) {
                videosDir.listFiles()?.forEach { addFileToZip(it, "videos/${it.name}", zos) }
            }

            val audioDir = File(context.filesDir, "audio")
            if (audioDir.exists()) {
                audioDir.listFiles()?.forEach { addFileToZip(it, "audio/${it.name}", zos) }
            }
            
            val profilesDir = File(context.filesDir, "profiles")
            if (profilesDir.exists()) {
                profilesDir.listFiles()?.forEach { addFileToZip(it, "profiles/${it.name}", zos) }
            }

            zos.close()
            fos.close()
            return exportFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun addFileToZip(file: File, zipPath: String, zos: ZipOutputStream) {
        val fis = FileInputStream(file)
        val zipEntry = ZipEntry(zipPath)
        zos.putNextEntry(zipEntry)

        val bytes = ByteArray(1024)
        var length: Int
        while (fis.read(bytes).also { length = it } >= 0) {
            zos.write(bytes, 0, length)
        }

        zos.closeEntry()
        fis.close()
    }
}
