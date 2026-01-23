package com.arukai.uajpspeak.activity

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.util.FavoritePhrase
import com.arukai.uajpspeak.util.FavoritesManager
import java.util.Locale

class ZoomFragment : Fragment() {
    private var t1: TextToSpeech? = null
    private var favoritesManager: FavoritesManager? = null
    private var favoriteMenuItem: MenuItem? = null
    private var currentPhrase: FavoritePhrase? = null
    private var isTtsInitialized = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesManager = FavoritesManager(requireContext())

        // Initialize TextToSpeech once
        t1 = TextToSpeech(activity?.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                t1?.language = Locale.Builder().setLanguage("uk").build()
                t1?.setSpeechRate(1.0f)
                isTtsInitialized = true
            }
        }

        // Use modern MenuProvider instead of deprecated setHasOptionsMenu
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.action_search)?.isVisible = false
                menu.findItem(R.id.action_alphabet)?.isVisible = false
                menu.findItem(R.id.action_about)?.isVisible = false
                menu.findItem(R.id.action_gender_lang)?.isVisible = false
                menu.findItem(R.id.action_language)?.isVisible = false

                // Show favorite icon
                favoriteMenuItem = menu.findItem(R.id.action_favorite)
                favoriteMenuItem?.isVisible = true
                updateFavoriteIcon()
            }

            override fun onCreateMenu(menu: Menu, menuInflater: android.view.MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_favorite) {
                    toggleFavorite()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_zoom, container, false)

        val sourceView = rootView.findViewById<TextView>(R.id.zoomSource) // renamed id
        val ukrainianView = rootView.findViewById<TextView>(R.id.zoomUkrainian)
        val speakerIcon = rootView.findViewById<ImageView>(R.id.zoomSpeaker)
        val phoneticView = rootView.findViewById<TextView>(R.id.zoomPhonetic)

        val args = arguments
        val sourceText = args?.getString("japanese")
        val ukrainian = args?.getString("ukrainian")
        val phonetic = args?.getString("phonetic")

        sourceView.text = sourceText
        // Remove asterisks for display (they're only needed for transliteration)
        ukrainianView.text = ukrainian?.replace("*", "")?.uppercase()
        phoneticView.text = phonetic

        // Get current language settings
        val currentLanguage = com.arukai.uajpspeak.util.LocaleHelper.getSavedLanguage(MainActivity.context)

        // Save current phrase for favorites using Ukrainian text as identifier
        currentPhrase = com.arukai.uajpspeak.util.FavoritePhrase(
            ukrainian = ukrainian ?: "",
            language = currentLanguage
        )

        val lang = currentLanguage
        val sourceFlagRes = when (lang) {
            "en" -> R.drawable.uk
            "de" -> R.drawable.de
            "ja" -> R.drawable.jp
            else -> R.drawable.uk
        }

        // Set flags with padding
        sourceView.compoundDrawablePadding = 12
        sourceView.setCompoundDrawablesWithIntrinsicBounds(sourceFlagRes, 0, 0, 0)

        ukrainianView.compoundDrawablePadding = 12
        ukrainianView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ua, 0, 0, 0)

        phoneticView.compoundDrawablePadding = 10
        phoneticView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.record_voice_over_24px, 0, 0, 0)

        val clickListener = View.OnClickListener {
            if (isTtsInitialized) {
                // Stop any ongoing speech
                t1?.stop()
                // Remove asterisks from Ukrainian text before speaking
                val cleanUkrainian = ukrainian?.replace("*", "")
                // Start speaking from the beginning (QUEUE_FLUSH clears the queue)
                t1?.speak(cleanUkrainian, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
        ukrainianView.setOnClickListener(clickListener)
        speakerIcon.setOnClickListener(clickListener)

        // Load ad
        val zoomAdView = rootView.findViewById<com.google.android.gms.ads.AdView>(R.id.zoomAdView)
        val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
        zoomAdView.loadAd(adRequest)

        return rootView
    }

    private fun toggleFavorite() {
        currentPhrase?.let { phrase ->
            if (favoritesManager?.isFavorite(phrase.ukrainian, phrase.language) == true) {
                favoritesManager?.removeFavorite(phrase)
            } else {
                favoritesManager?.addFavorite(phrase)
            }
            updateFavoriteIcon()
        }
    }

    private fun updateFavoriteIcon() {
        currentPhrase?.let { phrase ->
            val isFavorite = favoritesManager?.isFavorite(phrase.ukrainian, phrase.language) ?: false
            favoriteMenuItem?.setIcon(
                if (isFavorite) R.drawable.favorite_filled_24px
                else R.drawable.favorite_24px
            )
        }
    }

    override fun onDestroy() {
        // Shutdown TextToSpeech to free up resources
        t1?.stop()
        t1?.shutdown()
        super.onDestroy()
    }

    companion object {
        fun newInstance(
            japanese: String,
            ukrainian: String,
            phonetic: String,
            audio: String?
        ): ZoomFragment {
            val f = ZoomFragment()
            val args = Bundle()
            args.putString("japanese", japanese)
            args.putString("ukrainian", ukrainian)
            args.putString("phonetic", phonetic)
            args.putString("audio", audio)
            f.arguments = args
            return f
        }
    }
}
