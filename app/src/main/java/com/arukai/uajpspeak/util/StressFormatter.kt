package com.arukai.uajpspeak.util

/**
 * Converts a Ukrainian string with stress markers (asterisk after the stressed letter)
 * into a plain string where the stressed letter carries a combining
 * acute accent (U+0301).
 *
 * This is the standard linguistic convention used in Ukrainian dictionaries.
 * If there are no asterisks the input is returned unchanged.
 */
fun formatStress(raw: String): String = raw.replace("*", "\u0301")
