package com.arukai.uajpspeak.util

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import androidx.core.content.edit

/**
 * Utility for checking whether the app versionCode has changed since a given
 * SharedPreferences key was last saved. Used to gate one-time-per-version sync operations.
 */
object AppVersionHelper {

    fun isDebugBuild(context: Context): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    fun currentVersion(context: Context): Long = try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toLong()
        }
    } catch (_: Exception) {
        -1L
    }

    /** Returns true if the stored version under [key] differs from the current versionCode. */
    fun hasVersionChanged(context: Context, prefs: SharedPreferences, key: String): Boolean {
        return currentVersion(context) != prefs.getLong(key, -1L)
    }

    /** Saves the current versionCode under [key]. */
    fun saveCurrentVersion(context: Context, prefs: SharedPreferences, key: String) {
        prefs.edit { putLong(key, currentVersion(context)) }
    }
}
