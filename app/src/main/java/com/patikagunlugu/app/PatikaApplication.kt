package com.patikagunlugu.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.patikagunlugu.app.manager.SettingsManager
import com.patikagunlugu.app.utils.LocaleHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PatikaApplication : Application() {

    lateinit var settingsManager: SettingsManager

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)

        com.patikagunlugu.app.utils.NotificationHelper.createNotificationChannel(this)
        scheduleBackgroundTasks()

        // Apply saved theme synchronously at startup
        val theme = runBlocking { settingsManager.themeFlow.first() }
        applyTheme(theme)

        // Sync language from DataStore → SharedPreferences on first run
        // (so LocaleHelper.wrap() in activities can read it synchronously)
        MainScope().launch {
            val language = settingsManager.languageFlow.first()
            // Keep SharedPreferences in sync with DataStore
            LocaleHelper.saveLanguage(this@PatikaApplication, language)
        }
    }

    private fun scheduleBackgroundTasks() {
        val birthdayWorkRequest = androidx.work.PeriodicWorkRequestBuilder<com.patikagunlugu.app.worker.BirthdayWorker>(
            24, java.util.concurrent.TimeUnit.HOURS
        ).build()

        val vaccineWorkRequest = androidx.work.PeriodicWorkRequestBuilder<com.patikagunlugu.app.worker.VaccineWorker>(
            12, java.util.concurrent.TimeUnit.HOURS
        ).build()

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BirthdayWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            birthdayWorkRequest
        )

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "VaccineWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            vaccineWorkRequest
        )
    }

    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
