package com.arukai.uajpspeak.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.arukai.uajpspeak.model.Flashcard
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages flashcard learning progress with spaced repetition (Anki-like algorithm).
 */
class FlashcardManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "flashcard_prefs"
        private const val KEY_FLASHCARDS = "flashcards"
        private const val KEY_SELECTED_CATEGORIES = "selected_categories"
        private const val KEY_LAST_SESSION_DATE = "last_session_date"
        private const val KEY_NEW_CARDS_TODAY = "new_cards_today"
    }

    /**
     * Get all flashcards.
     */
    fun getAllFlashcards(): List<Flashcard> {
        val json = prefs.getString(KEY_FLASHCARDS, null) ?: return emptyList()
        val type = object : TypeToken<List<Flashcard>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Save flashcards to storage.
     */
    private fun saveFlashcards(flashcards: List<Flashcard>) {
        val json = gson.toJson(flashcards)
        prefs.edit {
            putString(KEY_FLASHCARDS, json)
        }
    }

    /**
     * Get or create a flashcard for a phrase.
     */
    fun getOrCreateFlashcard(ukrainian: String, translation: String, categoryId: Int): Flashcard {
        val flashcards = getAllFlashcards()
        return flashcards.find { it.ukrainian == ukrainian && it.categoryId == categoryId }
            ?: Flashcard(ukrainian, translation, categoryId)
    }

    /**
     * Update flashcard after review.
     */
    fun updateFlashcard(flashcard: Flashcard) {
        val flashcards = getAllFlashcards().toMutableList()
        val index = flashcards.indexOfFirst {
            it.ukrainian == flashcard.ukrainian && it.categoryId == flashcard.categoryId
        }

        if (index >= 0) {
            flashcards[index] = flashcard
        } else {
            flashcards.add(flashcard)
        }

        saveFlashcards(flashcards)
    }

    /**
     * Get flashcards for specific categories.
     */
    fun getFlashcardsForCategories(categoryIds: List<Int>): List<Flashcard> {
        return getAllFlashcards().filter { it.categoryId in categoryIds }
    }

    /**
     * Get flashcards that are due for review.
     */
    fun getDueFlashcards(categoryIds: List<Int>): List<Flashcard> {
        return getFlashcardsForCategories(categoryIds).filter { it.isDue() }
    }

    /**
     * Get flashcards that haven't been reviewed yet.
     */
    fun getNewFlashcards(categoryIds: List<Int>): List<Flashcard> {
        return getFlashcardsForCategories(categoryIds).filter { it.totalReviews == 0 }
    }

    /**
     * Get learned (mastered) flashcards.
     */
    fun getLearnedFlashcards(categoryIds: List<Int>): List<Flashcard> {
        return getFlashcardsForCategories(categoryIds).filter { it.isLearned() }
    }

    /**
     * Create flashcards for all phrases in selected categories if they don't exist.
     */
    fun initializeFlashcardsForCategories(
        context: Context,
        categoryIds: List<Int>,
        allPhrases: Map<Int, Array<String>>
    ) {
        val existingFlashcards = getAllFlashcards()
        val newFlashcards = mutableListOf<Flashcard>()
        
        categoryIds.forEach { categoryId ->
            val phrases = allPhrases[categoryId] ?: return@forEach
            
            phrases.forEach { phrase ->
                val parts = phrase.split("/")
                if (parts.size >= 3) {
                    val ukrainian = parts[2]
                    val translation = parts[1]
                    
                    // Check if flashcard already exists
                    val exists = existingFlashcards.any { 
                        it.ukrainian == ukrainian && it.categoryId == categoryId 
                    }
                    
                    if (!exists) {
                        newFlashcards.add(Flashcard(ukrainian, translation, categoryId))
                    }
                }
            }
        }
        
        if (newFlashcards.isNotEmpty()) {
            val allCards = existingFlashcards + newFlashcards
            saveFlashcards(allCards)
        }
    }

    /**
     * Get selected category IDs for learning.
     */
    fun getSelectedCategories(): Set<Int> {
        val json = prefs.getString(KEY_SELECTED_CATEGORIES, null) ?: return emptySet()
        val type = object : TypeToken<Set<Int>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Save selected categories.
     */
    fun saveSelectedCategories(categoryIds: Set<Int>) {
        val json = gson.toJson(categoryIds)
        prefs.edit {
            putString(KEY_SELECTED_CATEGORIES, json)
        }
    }

    /**
     * Get statistics for a category.
     */
    fun getCategoryStats(categoryId: Int): Triple<Int, Int, Int> {
        val flashcards = getFlashcardsForCategories(listOf(categoryId))
        val total = flashcards.size
        val learned = flashcards.count { it.isLearned() }
        val due = flashcards.count { it.isDue() }
        return Triple(total, learned, due)
    }

    /**
     * Reset daily new cards counter if it's a new day.
     */
    private fun checkAndResetDailyCounter() {
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        val lastSessionDay = prefs.getLong(KEY_LAST_SESSION_DATE, 0)
        
        if (today != lastSessionDay) {
            prefs.edit {
                putLong(KEY_LAST_SESSION_DATE, today)
                putInt(KEY_NEW_CARDS_TODAY, 0)
            }
        }
    }

    /**
     * Get number of new cards shown today.
     */
    fun getNewCardsToday(): Int {
        checkAndResetDailyCounter()
        return prefs.getInt(KEY_NEW_CARDS_TODAY, 0)
    }

    /**
     * Increment new cards counter.
     */
    fun incrementNewCardsToday() {
        val current = getNewCardsToday()
        prefs.edit {
            putInt(KEY_NEW_CARDS_TODAY, current + 1)
        }
    }

    /**
     * Build a learning session with prioritized flashcards.
     * Priority: 1) Due cards, 2) New cards (limited), 3) Recent cards
     */
    fun buildLearningSession(
        context: Context,
        categoryIds: List<Int>,
        allPhrases: Map<Int, Array<String>>,
        maxCards: Int = 20,
        maxNewCards: Int = 10
    ): List<Flashcard> {
        // Initialize flashcards for selected categories if needed
        initializeFlashcardsForCategories(context, categoryIds, allPhrases)

        val dueCards = getDueFlashcards(categoryIds).shuffled()
        val newCards = getNewFlashcards(categoryIds).shuffled()
        val newCardsToday = getNewCardsToday()

        // Limit new cards based on daily limit
        val newCardsAllowed = (maxNewCards - newCardsToday).coerceAtLeast(0)
        val newCardsToShow = newCards.take(newCardsAllowed)

        // Combine due and new cards
        val sessionCards = (dueCards + newCardsToShow).take(maxCards)

        return sessionCards
    }

    /**
     * Delete flashcards for a specific category.
     */
    fun deleteFlashcardsForCategory(categoryId: Int) {
        val flashcards = getAllFlashcards().toMutableList()
        flashcards.removeAll { it.categoryId == categoryId }
        saveFlashcards(flashcards)
    }

    /**
     * Get overall statistics.
     */
    fun getOverallStats(): Map<String, Int> {
        val all = getAllFlashcards()
        return mapOf(
            "total" to all.size,
            "learned" to all.count { it.isLearned() },
            "due" to all.count { it.isDue() },
            "new" to all.count { it.totalReviews == 0 }
        )
    }
}






