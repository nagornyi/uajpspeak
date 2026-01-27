package com.arukai.uajpspeak

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Apply saved theme early to avoid flicker/delay on startup.
        try {
            val prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            val theme = prefs.getInt("app_theme", 2)
            when (theme) {
                0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        } catch (e: Exception) {
            // Fail-safe: don't crash application startup if prefs can't be read
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}
