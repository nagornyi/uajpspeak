package com.arukai.uajpspeak.util

import android.content.Context
import android.content.SharedPreferences
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
        prefs.edit().putString(KEY_FAVORITES, json).apply()
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
