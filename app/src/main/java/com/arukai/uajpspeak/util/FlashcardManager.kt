package com.arukai.uajpspeak.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.arukai.uajpspeak.model.Flashcard
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

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
     * Only cards that have been reviewed at least once can be "due" – brand-new cards
     * (totalReviews == 0) are handled separately as new cards to avoid duplicates.
     */
    fun getDueFlashcards(categoryIds: List<Int>): List<Flashcard> {
        return getFlashcardsForCategories(categoryIds).filter { it.totalReviews > 0 && it.isDue() }
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
     * Statistics returned by [getCategoryStats].
     *
     * @param total      Total stored (initialized) flashcards for the category
     * @param learned    Fully-learned flashcards
     * @param due        Reviewed cards that are now due for re-review
     * @param hasStarted True only when at least one card has been reviewed at least once
     */
    data class CategoryStatsResult(
        val total: Int,
        val learned: Int,
        val due: Int,
        val hasStarted: Boolean
    )

    /**
     * Get statistics for a category.
     *
     * Only cards with totalReviews > 0 contribute to [CategoryStatsResult.due] and
     * [CategoryStatsResult.hasStarted], so freshly-initialised but never-answered
     * cards do NOT appear as "due for review" or cause the category to look "started".
     */
    fun getCategoryStats(categoryId: Int): CategoryStatsResult {
        val flashcards = getFlashcardsForCategories(listOf(categoryId))
        val total = flashcards.size
        val learned = flashcards.count { it.isLearned() }
        // Only count a card as "due" when the user has reviewed it at least once
        val due = flashcards.count { it.totalReviews > 0 && it.isDue() }
        val hasStarted = flashcards.any { it.totalReviews > 0 }
        return CategoryStatsResult(total, learned, due, hasStarted)
    }

    /**
     * Reset daily new cards counter if it's a new day.
     */
    private fun checkAndResetDailyCounter() {
        // Use Calendar to get local date
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
        val lastSessionDay = prefs.getLong(KEY_LAST_SESSION_DATE, 0)
        
        if (today.toLong() != lastSessionDay) {
            prefs.edit {
                putLong(KEY_LAST_SESSION_DATE, today.toLong())
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
     * Priority: 1) Due cards (previously reviewed, scheduled for re-review),
     *           2) New cards (never reviewed), limited to [maxNewCards] per session.
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

        // Limit new cards per session (no cross-session global tracking)
        val newCardsToShow = newCards.take(maxNewCards)

        // Combine due and new cards
        return (dueCards + newCardsToShow).take(maxCards)
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
     * "due" only counts cards that have been reviewed at least once and are
     * now scheduled for re-review; brand-new cards are reported under "new".
     */
    fun getOverallStats(): Map<String, Int> {
        val all = getAllFlashcards()
        return mapOf(
            "total" to all.size,
            "learned" to all.count { it.isLearned() },
            "due" to all.count { it.totalReviews > 0 && it.isDue() },
            "new" to all.count { it.totalReviews == 0 }
        )
    }
}






