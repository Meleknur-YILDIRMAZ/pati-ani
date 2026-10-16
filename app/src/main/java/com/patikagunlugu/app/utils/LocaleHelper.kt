package com.patikagunlugu.app.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    /**
     * Saves the language code to SharedPreferences (synchronous, safe to call from any thread).
     */
    fun saveLanguage(context: Context, language: String) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    /**
     * Returns the saved language code, defaulting to "en".
     */
    fun getSavedLanguage(context: Context): String {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "en") ?: "en"
    }

    /**
     * Wraps a context with the correct locale.
     * Call this from every Activity's attachBaseContext().
     */
    fun wrap(context: Context): Context {
        val language = getSavedLanguage(context)
        return applyLocaleToContext(context, language)
    }

    /**
     * Wraps a context with an explicit language code.
     */
    fun wrap(context: Context, language: String): Context {
        return applyLocaleToContext(context, language)
    }

    /**
     * Applies locale at runtime (call after user picks a language, then recreate Activity).
     */
    fun applyLocale(context: Context, language: String) {
        // Save to our own SharedPreferences first (synchronous)
        saveLanguage(context, language)

        // Also set via AppCompatDelegate for per-app locale support (API 33+)
        try {
            val localeList = LocaleListCompat.forLanguageTags(language)
            AppCompatDelegate.setApplicationLocales(localeList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyLocaleToContext(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
