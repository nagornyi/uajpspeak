package com.arukai.uajpspeak.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class FavoritePhrase(
    val ukrainian: String,   // Ukrainian text (with asterisks) - robust identifier
    val language: String     // "en", "de", or "ja"
)

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "favorites_prefs"
        private const val KEY_FAVORITES = "favorites_list"
        private const val KEY_LAST_SYNCED_VERSION = "last_synced_version"
    }

    fun addFavorite(phrase: FavoritePhrase) {
        val favorites = getFavorites().toMutableList()
        // Check if this exact combination (ukrainian + language) already exists
        if (!favorites.any {
            it.ukrainian == phrase.ukrainian &&
            it.language == phrase.language
        }) {
            favorites.add(phrase)
            saveFavorites(favorites)
        }
    }

    fun removeFavorite(phrase: FavoritePhrase) {
        val favorites = getFavorites().toMutableList()
        favorites.removeAll {
            it.ukrainian == phrase.ukrainian &&
            it.language == phrase.language
        }
        saveFavorites(favorites)
    }

    fun isFavorite(ukrainian: String, language: String): Boolean {
        return getFavorites().any {
            it.ukrainian == ukrainian &&
            it.language == language
        }
    }

    fun getFavoritesForCurrentSettings(language: String): List<FavoritePhrase> {
        return getFavorites().filter {
            it.language == language
        }
    }

    fun getFavorites(): List<FavoritePhrase> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        val type = object : TypeToken<List<FavoritePhrase>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveFavorites(favorites: List<FavoritePhrase>) {
        val json = gson.toJson(favorites)
        prefs.edit { putString(KEY_FAVORITES, json) }
    }

    /**
     * Run [cleanupOrphanedFavorites] only when the app version has changed since the last run.
     * The [validPhrasesProvider] lambda is **only invoked** when the version has actually changed,
     * so the expensive multi-language resource loading is skipped on every normal launch.
     */
    fun cleanupIfVersionChanged(
        context: Context,
        validPhrasesProvider: () -> Map<String, Set<String>>
    ) {
        val currentVersion = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toLong()
            }
        } catch (_: Exception) {
            -1L
        }

        val lastSynced = prefs.getLong(KEY_LAST_SYNCED_VERSION, -1L)
        if (currentVersion == lastSynced) return  // same version – nothing to clean up

        cleanupOrphanedFavorites(validPhrasesProvider())
        prefs.edit { putLong(KEY_LAST_SYNCED_VERSION, currentVersion) }
    }

    /**
     * Remove favorited phrases that no longer exist in the app's phrase arrays.
     * This should be called on app startup to clean up orphaned favorites.
     *
     * @param validUkrainianPhrases Map of language code to set of valid Ukrainian phrases for that language
     */
    fun cleanupOrphanedFavorites(validUkrainianPhrases: Map<String, Set<String>>) {
        val favorites = getFavorites().toMutableList()
        val originalSize = favorites.size

        // Remove favorites that don't exist in the valid phrases for their language
        favorites.removeAll { favorite ->
            val validPhrasesForLanguage = validUkrainianPhrases[favorite.language]
            validPhrasesForLanguage?.contains(favorite.ukrainian) != true
        }

        // Only save if something was removed
        if (favorites.size < originalSize) {
            saveFavorites(favorites)
        }
    }
}
