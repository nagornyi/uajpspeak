package com.arukai.uajpspeak.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import androidx.core.content.edit

object LocaleHelper {
    private const val PREF_KEY = "app_lang"
    private const val PREFS = "AppSettings"
    private const val FIRST_LAUNCH_KEY = "first_launch"

    fun getSavedLanguage(context: Context): String {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        // Check if this is the first launch
        val isFirstLaunch = sp.getBoolean(FIRST_LAUNCH_KEY, true)

        if (isFirstLaunch) {
            // Get system language
            val systemLang = Locale.getDefault().language

            // Map system language to supported languages
            val appLang = when (systemLang) {
                "en" -> "en"  // English
                "de" -> "de"  // German
                "es" -> "es"  // Spanish
                "fr" -> "fr"  // French
                "ja" -> "ja"  // Japanese
                else -> "en"  // Default to English for all other languages
            }

            // Save the selected language and mark first launch as complete
            sp.edit {
                putString(PREF_KEY, appLang)
                    .putBoolean(FIRST_LAUNCH_KEY, false)
            }

            return appLang
        }

        // Return saved language (default to "en" if not set)
        return sp.getString(PREF_KEY, "en") ?: "en"
    }

    fun setLanguage(context: Context, lang: String) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit { putString(PREF_KEY, lang) }
    }

    fun applyLocale(context: Context): Context {
        val lang = getSavedLanguage(context)
        return applyLocale(context, lang)
    }

    fun applyLocale(context: Context, lang: String): Context {
        val locale = Locale.Builder().setLanguage(lang).build()
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
