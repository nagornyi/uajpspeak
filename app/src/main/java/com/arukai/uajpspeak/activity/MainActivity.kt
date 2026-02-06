package com.arukai.uajpspeak.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.util.FavoritesManager
import com.arukai.uajpspeak.util.LocaleHelper
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import java.util.Locale
import androidx.core.content.edit
import androidx.core.view.size
import androidx.core.view.get

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener, NavigationView.OnNavigationItemSelectedListener {

    private var mSearchAction: MenuItem? = null
    private var editSearch: EditText? = null
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle

    companion object {
        lateinit var PACKAGE_NAME: String
        var current_position = 0
        var fragment: Fragment? = null
        var category = ""
        const val APP_SETTINGS = "AppSettings"
        const val KEY_THEME = "app_theme"
        const val KEY_CURRENT_FRAGMENT_TAG = "current_fragment_tag"
        lateinit var app_settings: SharedPreferences
        var isSearchOpened = false
        lateinit var all_phrases: Array<String>

        // Centralized list of all phrase array resource IDs to avoid duplication
        val ALL_PHRASE_ARRAY_IDS = listOf(
            R.array.greetings, R.array.signs, R.array.troubleshooting,
            R.array.transportation, R.array.directions, R.array.hotel,
            R.array.numbers, R.array.time, R.array.weekdays,
            R.array.months, R.array.colors, R.array.common_words,
            R.array.restaurant, R.array.love, R.array.shopping,
            R.array.clothing, R.array.drugstore, R.array.driving, R.array.bank
        )
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable modern edge-to-edge display (Android 15+ compliant)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        app_settings = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)

        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}

        PACKAGE_NAME = applicationContext.packageName
        // Use applicationContext via App.appContext when a global context is required

        all_phrases = collectAllPhrases()

        // Restore selected section if available (preserve across recreate)
        if (savedInstanceState != null) {
            current_position = savedInstanceState.getInt("current_position", current_position)
        }

        // Clean up orphaned favorites (phrases that were removed in an app update)
        val validPhrasesMap = collectValidUkrainianPhrasesMap()
        val favoritesManager = FavoritesManager(this)
        favoritesManager.cleanupOrphanedFavorites(validPhrasesMap)

        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)

        // Ensure system bars match the current theme (status/navigation bar colors + icon contrast)
        updateSystemBarColors()

        // Initialize NavigationView
        navigationView = findViewById(R.id.navigation_view)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Set up ActionBarDrawerToggle
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            mToolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this)

        // Initialize header views
        val headerView = navigationView.getHeaderView(0)
        val banner = headerView.findViewById<ImageView>(R.id.banner)
        val phrasesCount = headerView.findViewById<TextView>(R.id.phrasesCount)

        // Set flag based on language
        val lang = LocaleHelper.getSavedLanguage(this)
        val sourceFlagRes = when (lang) {
            "en" -> R.drawable.uk
            "de" -> R.drawable.de
            "fr" -> R.drawable.fr
            "ja" -> R.drawable.jp
            else -> R.drawable.uk
        }
        banner.setImageResource(sourceFlagRes)

        // Calculate phrases count
        val pCounter = ALL_PHRASE_ARRAY_IDS.sumOf { resources.getStringArray(it).size }
        // Use formatted string resource with placeholder for localization
        phrasesCount.text = getString(R.string.phrases_counter, pCounter)

        // Determine if a specific fragment was visible before recreate
        var restoredFragmentTag = savedInstanceState?.getString("current_fragment_tag")
        if (restoredFragmentTag == null) {
            // Fallback: read from persistent prefs if activity recreate was triggered by recreate() and
            // onSaveInstanceState wasn't available to preserve the tag for some reason.
            restoredFragmentTag = app_settings.getString(KEY_CURRENT_FRAGMENT_TAG, null)
            // Clear the saved tag once read
            if (restoredFragmentTag != null) {
                app_settings.edit { remove(KEY_CURRENT_FRAGMENT_TAG) }
            }
        }
        if (restoredFragmentTag == null || restoredFragmentTag == "HOME") {
            // Show previously selected section (preserve user's place); default is 0
            displayView(current_position)
        } else {
            // Let FragmentManager restore the fragment state. After restore we need to ensure the
            // action bar and drawer are in correct state for the restored fragment.
            supportFragmentManager.executePendingTransactions()
            when (restoredFragmentTag) {
                "ZOOM" -> {
                    // For Zoom we don't want the drawer enabled and title empty
                    setActionBarTitle("")
                    setDrawerState(false)
                    enableBackButton(true)
                }
                "ABOUT" -> {
                    setActionBarTitle(getString(R.string.title_about))
                    setDrawerState(false)
                    enableBackButton(true)
                }
                "ALPHABET" -> {
                    setActionBarTitle(getString(R.string.title_alphabet))
                    setDrawerState(false)
                    enableBackButton(true)
                }
                else -> {
                    // If unknown tag, fallback to showing the selected section
                    displayView(current_position)
                }
            }
        }

        textToSpeech = TextToSpeech(this, this)

        // Handle back press with OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val zoom = supportFragmentManager.findFragmentByTag("ZOOM") as? ZoomFragment
                val about = supportFragmentManager.findFragmentByTag("ABOUT") as? AboutFragment
                val alphabet = supportFragmentManager.findFragmentByTag("ALPHABET") as? AlphabetFragment

                if ((zoom != null && zoom.isVisible) || (about != null && about.isVisible) ||
                    (alphabet != null && alphabet.isVisible)) {
                    val fragmentManager = supportFragmentManager

                    if (fragmentManager.backStackEntryCount != 0) {
                        fragmentManager.popBackStack()

                        // After popping, check what fragment is now visible and set appropriate title
                        fragmentManager.executePendingTransactions()
                        val currentHome = supportFragmentManager.findFragmentByTag("HOME")

                        if (currentHome != null && currentHome.isVisible) {
                            setActionBarTitle(category)
                            setDrawerState(true)
                            enableBackButton(false)
                            // Reset toolbar navigation to drawer toggle (hamburger icon)
                            drawerToggle.isDrawerIndicatorEnabled = true
                            drawerToggle.syncState()
                        } else {
                            // Default case for other fragments
                            fragment?.let {
                                setActionBarTitle(category)
                                enableBackButton(false)
                                setDrawerState(true)
                                // Reset toolbar navigation to drawer toggle (hamburger icon)
                                drawerToggle.isDrawerIndicatorEnabled = true
                                drawerToggle.syncState()
                            }
                        }
                    } else {
                        fragment?.let {
                            val fragmentTransaction = fragmentManager.beginTransaction()
                            fragmentTransaction.replace(R.id.container_body, it, "HOME")
                            fragmentTransaction.commit()
                            setActionBarTitle(category)
                            enableBackButton(false)
                            setDrawerState(true)
                            // Reset toolbar navigation to drawer toggle (hamburger icon)
                            drawerToggle.isDrawerIndicatorEnabled = true
                            drawerToggle.syncState()
                        }
                    }
                } else {
                    finish()
                }
            }
        })
    }

    private fun updateSystemBarColors() {
        val isDarkMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES

        val controller = WindowCompat.getInsetsController(window, window.decorView)

        controller.isAppearanceLightStatusBars = !isDarkMode
    }

    private fun applyTheme() {
        val theme = app_settings.getInt(KEY_THEME, 2) // Default to Automatic
        when (theme) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ukrainianLocale = Locale.forLanguageTag("uk")
            val result = textToSpeech.setLanguage(ukrainianLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Ukrainian language data is missing or not supported.")
            } else {
                Log.i("TTS", "Ukrainian language data is available.")
            }
        } else {
            Log.e("TTS", "TextToSpeech initialization failed.")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Enable icons in overflow menu
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val method = menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        mSearchAction = menu.findItem(R.id.action_search)

        // Tint only overflow menu icons (not action bar icons) to appropriate color based on theme
        val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        
        val tintColor = if (isDarkMode) {
            ContextCompat.getColor(this, android.R.color.white)
        } else {
            ContextCompat.getColor(this, android.R.color.black)
        }

        for (i in 0 until menu.size) {
            val menuItem = menu[i]
            // Only tint overflow menu items, not the search action
            if (menuItem.itemId == R.id.action_language ||
                menuItem.itemId == R.id.action_gender_lang ||
                menuItem.itemId == R.id.action_theme ||
                menuItem.itemId == R.id.action_about) {
                menuItem.icon?.setTint(tintColor)
            }
        }

        // Preserve cancel icon when search is open (with original color)
        if (isSearchOpened) {
            // Force the cancel icon to white so it matches other toolbar icons and remains visible
            val cancelIcon = androidx.appcompat.content.res.AppCompatResources.getDrawable(this, R.drawable.cancel_24px)
            cancelIcon?.setTint(Color.WHITE)
            mSearchAction?.icon = cancelIcon
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_about) {
            if (isSearchOpened) {
                hideSearchBar()
            }
            val fragment = AboutFragment()
            val title = getString(R.string.title_about)
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container_body, fragment, "ABOUT")
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            setActionBarTitle(title)
            setDrawerState(false)
            drawerToggle.setToolbarNavigationClickListener { onBackPressedDispatcher.onBackPressed() }
            enableBackButton(true)

            return true
        }

        if (id == R.id.action_alphabet) {
            if (isSearchOpened) {
                hideSearchBar()
            }
            val fragment = AlphabetFragment()
            val title = getString(R.string.title_alphabet)
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container_body, fragment, "ALPHABET")
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            setActionBarTitle(title)
            setDrawerState(false)
            drawerToggle.setToolbarNavigationClickListener { onBackPressedDispatcher.onBackPressed() }
            enableBackButton(true)

            return true
        }

        if (id == R.id.action_theme) {
            val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
            builder.setTitle(R.string.action_theme)
            
            val currentTheme = app_settings.getInt(KEY_THEME, 2)
            val items = resources.getStringArray(R.array.theme_selection)
            
            builder.setSingleChoiceItems(items, currentTheme) { dialog, which ->
                app_settings.edit { putInt(KEY_THEME, which) }
                applyTheme()
                dialog.dismiss()
                // Persist current fragment tag as a fallback so we can restore the exact view after recreate
                app_settings.edit { putString(KEY_CURRENT_FRAGMENT_TAG, getCurrentFragmentTag()) }
                recreate()
            }
            
            builder.setNegativeButton(R.string.cancel, null)
            
            val dialog = builder.create()
            dialog.show()
            return true
        }

        if (id == R.id.action_gender_lang) {
            val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
            builder.setTitle(R.string.action_gender_lang)
            val editor = app_settings.edit()

            val items = resources.getStringArray(R.array.gender_lang_selection)
            builder.setSingleChoiceItems(items, app_settings.getInt("gender_lang", 0)) { _, which ->
                editor.putInt("gender_lang", which)
            }

            val positiveText = getString(R.string.ok)
            builder.setPositiveButton(positiveText) { _, _ ->
                // Close search box and reset icon if open
                if (isSearchOpened) {
                    hideSearchBar()
                }
                editor.apply()
                // Refresh current view to apply gender change while staying in the same section
                displayView(current_position)
            }

            val negativeText = getString(R.string.cancel)
            builder.setNegativeButton(negativeText) { _, _ ->
                editor.clear()
            }

            val dialog = builder.create()
            dialog.show()

            return true
        }

        if (id == R.id.action_language) {
            val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
            builder.setTitle(R.string.action_language)
            val langs = arrayOf("English", "Deutsch", "Français", "日本語")
            val current = when (LocaleHelper.getSavedLanguage(this)) {
                "de" -> 1
                "fr" -> 2
                "ja" -> 3
                else -> 0
            }
            builder.setSingleChoiceItems(langs, current) { dialog, which ->
                val newLang = when (which) {
                    1 -> "de"
                    2 -> "fr"
                    3 -> "ja"
                    else -> "en"
                }
                if (newLang != LocaleHelper.getSavedLanguage(this)) {
                    if (isSearchOpened) {
                        hideSearchBar()
                    }
                    LocaleHelper.setLanguage(this, newLang)
                    dialog.dismiss()
                    // Persist fragment tag so we stay on the same fragment after language change
                    app_settings.edit {
                        putString(
                            KEY_CURRENT_FRAGMENT_TAG,
                            getCurrentFragmentTag()
                        )
                    }
                    recreate()
                }
            }

            val negativeText = getString(R.string.cancel)
            builder.setNegativeButton(negativeText, null)

            val dialog = builder.create()
            dialog.show()

            return true
        }

        if (id == R.id.action_search) {
            handleMenuSearch()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun handleMenuSearch() {
        val action = supportActionBar ?: return
        if (isSearchOpened) {
            hideSearchBar()
        } else {
            action.setDisplayShowCustomEnabled(true)
            action.setCustomView(R.layout.search_bar)
            // Ensure the custom view fills the action bar space (so EditText can expand up to the cancel icon)
            try {
                val custom = action.customView
                custom?.layoutParams = Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            action.setDisplayShowTitleEnabled(false)
            editSearch = action.customView.findViewById(R.id.editSearch)
            editSearch?.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT)

            // Determine toolbar background color if available (fall back to resource)
            val toolbarView = findViewById<Toolbar>(R.id.toolbar)
            val bg = toolbarView.background
            val toolbarColor = if (bg is ColorDrawable) bg.color else ContextCompat.getColor(this, R.color.colorPrimary)

            // Decide whether to use white text/cursor based on toolbar luminance
            val useWhite = ColorUtils.calculateLuminance(toolbarColor) < 0.5
            val textColor = if (useWhite) Color.WHITE else Color.BLACK
            val hintColor = if (useWhite) Color.argb(180, 255, 255, 255) else Color.argb(160, 0, 0, 0)

            // Apply text and hint color
            editSearch?.setTextColor(textColor)
            editSearch?.setHintTextColor(hintColor)
            editSearch?.isCursorVisible = true

            editSearch?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    doSearch(s.toString())
                    // Keep cancel icon white to match toolbar icons
                    val cancelDrawable = androidx.appcompat.content.res.AppCompatResources.getDrawable(this@MainActivity, R.drawable.cancel_24px)
                    cancelDrawable?.setTint(Color.WHITE)
                    mSearchAction?.icon = cancelDrawable
                }

                override fun afterTextChanged(s: Editable) {}
            })

            // Initial cancel icon tint: force white to match other toolbar icons
            val cancelDrawable = androidx.appcompat.content.res.AppCompatResources.getDrawable(this, R.drawable.cancel_24px)
            cancelDrawable?.setTint(Color.WHITE)
            mSearchAction?.icon = cancelDrawable

            isSearchOpened = true
        }
     }

    private fun hideSearchBar() {
        val action = supportActionBar ?: return
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editSearch?.windowToken, 0)
        action.setDisplayShowCustomEnabled(false)
        action.setDisplayShowTitleEnabled(true)
        mSearchAction?.setIcon(R.drawable.search_24px)
        isSearchOpened = false
        displayView(current_position)
    }

    private fun doSearch(query: String) {
        // Normalize candidates: remove asterisks (stress markers) so Ukrainian searches succeed
        val filtered = all_phrases.filter { raw ->
            val candidate = raw.replace("*", "")
            candidate.contains(query, ignoreCase = true)
        }.toTypedArray()
         val bundle = Bundle()
         bundle.putStringArray("phrases", filtered)
         bundle.putString("category", "Search Results")
         val searchFragment = HomeFragment()
         searchFragment.arguments = bundle
         supportFragmentManager.beginTransaction()
             .replace(R.id.container_body, searchFragment, "HOME")
             .commit()
     }

    private fun collectAllPhrases(): Array<String> {
        return ALL_PHRASE_ARRAY_IDS.flatMap { resources.getStringArray(it).toList() }.toTypedArray()
    }

    private fun collectValidUkrainianPhrasesMap(): Map<String, Set<String>> {
        val map = mutableMapOf<String, Set<String>>()
        val languages = listOf("en", "de", "fr", "ja")
        for (lang in languages) {
            val phrases = mutableSetOf<String>()
            val localizedContext = LocaleHelper.applyLocale(this, lang)
            for (arrayId in ALL_PHRASE_ARRAY_IDS) {
                val array = localizedContext.resources.getStringArray(arrayId)
                for (item in array) {
                    val parts = item.split("/")
                    if (parts.size >= 3) {
                        phrases.add(parts[2])
                    }
                }
            }
            map[lang] = phrases
        }
        return map
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val position = when (item.itemId) {
            R.id.nav_all_phrases -> 0
            R.id.nav_favourites -> 1
            R.id.nav_greetings -> 2
            R.id.nav_signs -> 3
            R.id.nav_troubleshooting -> 4
            R.id.nav_transportation -> 5
            R.id.nav_directions -> 6
            R.id.nav_hotel -> 7
            R.id.nav_numbers -> 8
            R.id.nav_time -> 9
            R.id.nav_weekdays -> 10
            R.id.nav_months -> 11
            R.id.nav_colors -> 12
            R.id.nav_common_words -> 13
            R.id.nav_restaurant -> 14
            R.id.nav_love -> 15
            R.id.nav_shopping -> 16
            R.id.nav_clothing -> 17
            R.id.nav_drugstore -> 18
            R.id.nav_driving -> 19
            R.id.nav_bank -> 20
            else -> 0
        }
        displayView(position)
        drawerLayout.closeDrawers()
        return true
    }

    private fun displayView(position: Int) {
        if (isSearchOpened) hideSearchBar()
        current_position = position
        val labels = resources.getStringArray(R.array.nav_drawer_labels)
        category = labels[position]

        val bundle = Bundle()
        fragment = when (position) {
            0 -> {
                bundle.putStringArray("phrases", all_phrases)
                bundle.putString("category", category)
                HomeFragment()
            }
            1 -> {
                bundle.putString("category", category)
                FavouritesFragment()
            }
            else -> {
                val arrayId = ALL_PHRASE_ARRAY_IDS[position - 2]
                bundle.putStringArray("phrases", resources.getStringArray(arrayId))
                bundle.putString("category", category)
                HomeFragment()
            }
        }

        fragment?.let {
            it.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_body, it, "HOME")
                .commit()
            setActionBarTitle(category)
        }
    }

    // Make public so fragments can call it (was private before)
    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    // Make public so fragments can enable/disable up button
    fun enableBackButton(enable: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enable)
        if (enable) {
            drawerToggle.setToolbarNavigationClickListener { onBackPressedDispatcher.onBackPressed() }
        }
    }

    // Public wrapper for drawer lock state (fragments expect setDrawerLocked)
    fun setDrawerLocked(isEnabled: Boolean) {
        setDrawerState(isEnabled)
    }

    // Convenience method that some callers expect
    fun setBackButtonEnabled() {
        enableBackButton(true)
    }

    private fun setDrawerState(isEnabled: Boolean) {
        if (isEnabled) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            drawerToggle.isDrawerIndicatorEnabled = true
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            drawerToggle.isDrawerIndicatorEnabled = false
        }
        drawerToggle.syncState()
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("current_position", current_position)

        // Save which fragment is currently visible so we can restore it after recreate
        val currentTag = getCurrentFragmentTag()
        outState.putString("current_fragment_tag", currentTag)
        // Also persist to prefs as a more durable fallback for recreate()
        app_settings.edit { putString(KEY_CURRENT_FRAGMENT_TAG, currentTag) }
    }

    private fun getCurrentFragmentTag(): String {
        return when {
            supportFragmentManager.findFragmentByTag("ZOOM")?.isVisible == true -> "ZOOM"
            supportFragmentManager.findFragmentByTag("ABOUT")?.isVisible == true -> "ABOUT"
            supportFragmentManager.findFragmentByTag("ALPHABET")?.isVisible == true -> "ALPHABET"
            else -> "HOME"
        }
    }
}
