package com.arukai.uajpspeak.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Encapsulates Ukrainian Text-to-Speech setup and teardown.
 * Initialises a [TextToSpeech] engine with the Ukrainian locale and a neutral speech rate.
 *
 * Usage:
 *   1. Create in onCreateView / onViewCreated.
 *   2. Call [speak] whenever you want to pronounce a Ukrainian string.
 *   3. Call [shutdown] in onDestroy.
 */
class UkrainianTtsHelper(context: Context) {

    private var tts: TextToSpeech? = null
    private var initialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.Builder().setLanguage("uk").build()
                tts?.setSpeechRate(1.0f)
                initialized = true
            }
        }
    }

    /**
     * Speak [text] aloud, stripping stress-marker asterisks first.
     * Does nothing if TTS is not yet initialised or [text] is blank.
     */
    fun speak(text: String) {
        if (!initialized || text.isBlank()) return
        val clean = text.replace("*", "")
        tts?.stop()
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    /** Stop any ongoing speech and release TTS resources. Call from onDestroy. */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        initialized = false
    }
}
