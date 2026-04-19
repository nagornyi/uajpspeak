package com.arukai.uajpspeak.model

import java.util.Calendar

/**
 * Represents a flashcard with spaced repetition data.
 * Uses SuperMemo SM-2 algorithm (similar to Anki).
 */
data class Flashcard(
    val ukrainian: String,      // Ukrainian phrase (with stress markers)
    val translation: String,    // Translation in user's language
    val categoryId: Int,        // Array resource ID for the category
    val gender: String? = null, // "m", "f", "n", or null for legacy cards (treated as "n")
    var easeFactor: Float = 2.5f,   // Ease factor (difficulty multiplier)
    var interval: Int = 0,          // Days until next review
    var repetitions: Int = 0,       // Number of successful reviews in a row
    var nextReviewDate: Long = System.currentTimeMillis(), // When to review next
    var lastReviewed: Long = 0L,    // Last review timestamp
    var totalReviews: Int = 0,      // Total number of reviews
    var correctReviews: Int = 0     // Number of correct answers
) {
    /**
     * Update flashcard after review using SM-2 algorithm.
     * @param quality Quality of response (0-5): 0=complete blackout, 5=perfect response
     */
    fun updateAfterReview(quality: Int) {
        totalReviews++
        lastReviewed = System.currentTimeMillis()

        // Quality < 3 is a fail, reset repetitions
        if (quality < 3) {
            repetitions = 0
            interval = 1
        } else {
            correctReviews++
            // Calculate new interval
            interval = when (repetitions) {
                0 -> 1
                1 -> 6
                else -> (interval * easeFactor).toInt()
            }
            repetitions++
        }

        // Update ease factor (SM-2 formula)
        easeFactor = (easeFactor + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f)))
            .coerceAtLeast(1.3f) // Minimum ease factor

        // Calculate next review date - schedule for midnight of the target day
        val calendar = Calendar.getInstance().apply {
            // Set to midnight of today
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Add the interval days
            add(Calendar.DAY_OF_YEAR, interval)
        }
        nextReviewDate = calendar.timeInMillis
    }

    /**
     * Check if this flashcard is due for review.
     */
    fun isDue(): Boolean = System.currentTimeMillis() >= nextReviewDate

    /**
     * Check if flashcard is "learned" (mastered).
     * Considered learned if: repetitions >= 3 and interval >= 21 days
     */
    fun isLearned(): Boolean = repetitions >= 3 && interval >= 21
}

/**
 * Category selection state for learning.
 */
data class LearnCategory(
    val categoryId: Int,
    val categoryName: String,
    val arrayResourceId: Int,
    var isSelected: Boolean = false,
    var totalPhrases: Int = 0,
    var learnedPhrases: Int = 0,
    var dueForReview: Int = 0
)
