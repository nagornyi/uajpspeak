package com.arukai.uajpspeak.activity

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.util.LocaleHelper
import java.io.BufferedReader
import java.io.InputStreamReader

class AlphabetFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use modern MenuProvider instead of deprecated setHasOptionsMenu
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.action_search)?.isVisible = false
                menu.findItem(R.id.action_alphabet)?.isVisible = false
                menu.findItem(R.id.action_about)?.isVisible = false
                menu.findItem(R.id.action_gender_lang)?.isVisible = false
                menu.findItem(R.id.action_language)?.isVisible = false
            }

            override fun onCreateMenu(menu: Menu, menuInflater: android.view.MenuInflater) {
                // Menu already created by activity
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_alphabet, container, false)
        val body = rootView.findViewById<TextView>(R.id.alphabetText)

        // Load alphabet HTML from raw resources based on current language
        val htmlContent = loadAlphabetHtml()
        @Suppress("DEPRECATION")
        body.text = Html.fromHtml(htmlContent)

        val imageView = rootView.findViewById<ImageView>(R.id.abetkaImg)

        // Native Android fade-in animations
        imageView.alpha = 0f
        imageView.animate()
            .alpha(1f)
            .setDuration(700)
            .start()

        body.alpha = 0f
        body.animate()
            .alpha(1f)
            .setDuration(700)
            .start()

        return rootView
    }

    private fun loadAlphabetHtml(): String {
        val lang = LocaleHelper.getSavedLanguage(requireContext())

        // Map language codes to raw resource IDs
        val resourceId = when (lang) {
            "de" -> R.raw.alphabet_de
            "es" -> R.raw.alphabet_es
            "fr" -> R.raw.alphabet_fr
            "ja" -> R.raw.alphabet_ja
            else -> R.raw.alphabet_en  // Default to English
        }

        return try {
            val inputStream = resources.openRawResource(resourceId)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val stringBuilder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
                stringBuilder.append('\n')
            }

            reader.close()
            inputStream.close()

            stringBuilder.toString()
        } catch (_: Exception) {
            // Fallback to a simple error message if file cannot be loaded
            "<h3>Alphabet information unavailable</h3><p>Error loading content.</p>"
        }
    }

}

