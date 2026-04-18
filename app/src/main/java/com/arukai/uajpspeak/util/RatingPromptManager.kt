package com.arukai.uajpspeak.util

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Manages the timing of the in-app rating prompt.
 *
 * Conditions before showing the prompt:
 *   - App opened at least MIN_LAUNCH_COUNT times
 *   - At least MIN_DAYS_SINCE_INSTALL days since the first launch
 *   - At least REPEAT_DAYS days since the last prompt was shown
 */
object RatingPromptManager {

    private const val PREFS_NAME = "RatingPrompt"
    private const val KEY_LAUNCH_COUNT = "launch_count"
    private const val KEY_FIRST_LAUNCH_DATE = "first_launch_date"
    private const val KEY_LAST_PROMPT_DATE = "last_prompt_date"
    private const val KEY_PROMPT_COUNT = "prompt_count"

    private const val MIN_LAUNCH_COUNT = 5
    private const val MIN_DAYS_SINCE_INSTALL = 3L
    private const val REPEAT_DAYS = 30L

    /** After this many automatic prompts the manager goes silent forever.
     *  The "Rate this app" button in the About screen is always available. */
    private const val MAX_PROMPT_COUNT = 3

    private const val MILLIS_PER_DAY = 86_400_000L

    /** Call once per Activity.onResume to increment the counter and maybe show the prompt. */
    fun onAppLaunched(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()

        // Record first launch date
        if (!prefs.contains(KEY_FIRST_LAUNCH_DATE)) {
            prefs.edit().putLong(KEY_FIRST_LAUNCH_DATE, now).apply()
        }

        // Increment launch count
        val count = prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1
        prefs.edit().putInt(KEY_LAUNCH_COUNT, count).apply()

        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH_DATE, now)
        val lastPrompt = prefs.getLong(KEY_LAST_PROMPT_DATE, 0L)
        val promptCount = prefs.getInt(KEY_PROMPT_COUNT, 0)

        val daysSinceInstall = (now - firstLaunch) / MILLIS_PER_DAY
        val daysSinceLastPrompt = if (lastPrompt == 0L) Long.MAX_VALUE else (now - lastPrompt) / MILLIS_PER_DAY

        val shouldPrompt = promptCount < MAX_PROMPT_COUNT
                && count >= MIN_LAUNCH_COUNT
                && daysSinceInstall >= MIN_DAYS_SINCE_INSTALL
                && daysSinceLastPrompt >= REPEAT_DAYS

        if (shouldPrompt) {
            prefs.edit()
                .putLong(KEY_LAST_PROMPT_DATE, now)
                .putInt(KEY_PROMPT_COUNT, promptCount + 1)
                .apply()
            launchReviewFlow(activity)
        }
    }

    private fun launchReviewFlow(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)
                // No fallback needed here – silent if quota exceeded
            }
            // If unsuccessful, do nothing – don't bother the user
        }
    }
}



