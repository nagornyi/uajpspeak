package com.arukai.uajpspeak.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.util.FavoritesManager
import com.arukai.uajpspeak.util.LocaleHelper
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.util.ArrayUtils
import com.google.android.material.navigation.NavigationView
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener, NavigationView.OnNavigationItemSelectedListener {

    private var mSearchAction: MenuItem? = null
    private var editSearch: EditText? = null
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle

    companion object {
        lateinit var PACKAGE_NAME: String
        lateinit var context: Context
        var prev_position = 0
        var current_position = 0
        var fragment: Fragment? = null
        var category = ""
        const val APP_SETTINGS = "AppSettings"
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
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}


        PACKAGE_NAME = applicationContext.packageName
        context = applicationContext
        app_settings = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
        all_phrases = collectAllPhrases()

        // Clean up orphaned favorites (phrases that were removed in an app update)
        val validPhrases = collectValidUkrainianPhrasesForAllLanguages()
        val favoritesManager = FavoritesManager(this)
        favoritesManager.cleanupOrphanedFavorites(validPhrases)

        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)

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
            "ja" -> R.drawable.jp
            else -> R.drawable.uk
        }
        banner.setImageResource(sourceFlagRes)

        // Calculate phrases count
        val pCounter = ALL_PHRASE_ARRAY_IDS.sumOf { resources.getStringArray(it).size }
        phrasesCount.text = "$pCounter ${getString(R.string.phrases_counter)}"

        // Restore saved position or default to 0 (All Phrases)
        val savedPosition = app_settings.getInt("last_category_position", 0)
        displayView(savedPosition)

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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.Builder().setLanguage("uk").build())
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
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        mSearchAction = menu.findItem(R.id.action_search)
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
                editor.apply()
                // Refresh HomeFragment if visible
                val home = supportFragmentManager.findFragmentByTag("HOME") as? HomeFragment
                if (home != null && home.isVisible) {
                    displayView(current_position)
                }
                // Also refresh FavouritesFragment if visible (it's also tagged as "HOME")
                // Since Favourites uses the same tag, we need to check the fragment type
                val currentFragment = supportFragmentManager.findFragmentByTag("HOME")
                if (currentFragment is FavouritesFragment && currentFragment.isVisible) {
                    displayView(current_position)
                }
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
            val langs = arrayOf("English", "Deutsch", "日本語")
            val current = when (LocaleHelper.getSavedLanguage(this)) {
                "de" -> 1
                "ja" -> 2
                else -> 0
            }
            var selected = current
            builder.setSingleChoiceItems(langs, current) { _, which -> selected = which }
            builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
                val code = when (selected) {
                    1 -> "de"
                    2 -> "ja"
                    else -> "en"
                }
                LocaleHelper.setLanguage(this, code)
                // Save current position before recreating
                app_settings.edit().putInt("last_category_position", current_position).apply()
                recreate()
            }
            builder.setNegativeButton(getString(R.string.cancel), null)
            builder.show()
            return true
        }

        if (id == R.id.action_search) {
            handleMenuSearch()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun handleMenuSearch() {
        if (isSearchOpened) {
            hideSearchBar()
        } else {
            openSearchBar()
        }
    }

    private fun collectAllPhrases(): Array<String> {
        val arrays = ALL_PHRASE_ARRAY_IDS.map { resources.getStringArray(it) }.toTypedArray()
        return ArrayUtils.concat(*arrays)
    }

    /**
     * Collect all valid Ukrainian phrases for all supported languages.
     * This is used to clean up orphaned favorites.
     */
    private fun collectValidUkrainianPhrasesForAllLanguages(): Map<String, Set<String>> {
        val result = mutableMapOf<String, Set<String>>()
        val languages = listOf("en", "de", "ja")

        for (lang in languages) {
            val ukrainianPhrases = mutableSetOf<String>()

            // Temporarily switch to the target language to get its phrases
            val currentConfig = resources.configuration
            val locale = when (lang) {
                "de" -> Locale.Builder().setLanguage("de").build()
                "ja" -> Locale.Builder().setLanguage("ja").build()
                else -> Locale.Builder().setLanguage("en").build()
            }
            val config = android.content.res.Configuration(currentConfig)
            config.setLocale(locale)
            val localizedContext = createConfigurationContext(config)

            // Collect Ukrainian phrases for this language
            for (arrayId in ALL_PHRASE_ARRAY_IDS) {
                val phrases = localizedContext.resources.getStringArray(arrayId)
                for (phrase in phrases) {
                    val parts = phrase.split("/")
                    if (parts.size >= 3) {
                        ukrainianPhrases.add(parts[2]) // Ukrainian text with asterisks
                    }
                }
            }

            result[lang] = ukrainianPhrases
        }

        return result
    }

    fun hideSearchBar() {
        val action = supportActionBar

        action?.setDisplayShowCustomEnabled(false)
        action?.setDisplayShowTitleEnabled(true)

        currentFocus?.let { view ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        mSearchAction?.setIcon(resources.getDrawable(R.drawable.ic_action_search, null))

        displayView(prev_position)
        isSearchOpened = false
    }

    private fun openSearchBar() {
        displayView(0)

        val action = supportActionBar

        action?.setDisplayShowCustomEnabled(true)
        action?.setCustomView(R.layout.search_bar)
        action?.setDisplayShowTitleEnabled(false)

        editSearch = action?.customView?.findViewById(R.id.editSearch)

        editSearch?.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(v.text.toString())
                true
            } else {
                false
            }
        }

        editSearch?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(arg0: CharSequence?, arg1: Int, arg2: Int, arg3: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                doSearch(s.toString())
            }
        })

        editSearch?.requestFocus()

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        editSearch?.let { imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT) }

        mSearchAction?.setIcon(getDrawable(R.drawable.ic_action_close))

        isSearchOpened = true
    }

    private fun doSearch(searchString: String) {
        val home = supportFragmentManager.findFragmentByTag("HOME") as? HomeFragment
        home?.mAdapter?.filter?.filter(searchString)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_all_phrases -> displayView(0)
            R.id.nav_favourites -> displayView(1)
            R.id.nav_greetings -> displayView(2)
            R.id.nav_signs -> displayView(3)
            R.id.nav_troubleshooting -> displayView(4)
            R.id.nav_transportation -> displayView(5)
            R.id.nav_directions -> displayView(6)
            R.id.nav_hotel -> displayView(7)
            R.id.nav_numbers -> displayView(8)
            R.id.nav_time -> displayView(9)
            R.id.nav_weekdays -> displayView(10)
            R.id.nav_months -> displayView(11)
            R.id.nav_colors -> displayView(12)
            R.id.nav_common_words -> displayView(13)
            R.id.nav_restaurant -> displayView(14)
            R.id.nav_love -> displayView(15)
            R.id.nav_shopping -> displayView(16)
            R.id.nav_clothing -> displayView(17)
            R.id.nav_drugstore -> displayView(18)
            R.id.nav_driving -> displayView(19)
            R.id.nav_bank -> displayView(20)
        }

        drawerLayout.closeDrawers()
        return true
    }

    private fun displayView(position: Int) {
        prev_position = current_position
        current_position = position

        // Save current position for restoration after language change
        app_settings.edit().putInt("last_category_position", position).apply()

        // Update NavigationView selection
        val menuItemId = when (position) {
            0 -> R.id.nav_all_phrases
            1 -> R.id.nav_favourites
            2 -> R.id.nav_greetings
            3 -> R.id.nav_signs
            4 -> R.id.nav_troubleshooting
            5 -> R.id.nav_transportation
            6 -> R.id.nav_directions
            7 -> R.id.nav_hotel
            8 -> R.id.nav_numbers
            9 -> R.id.nav_time
            10 -> R.id.nav_weekdays
            11 -> R.id.nav_months
            12 -> R.id.nav_colors
            13 -> R.id.nav_common_words
            14 -> R.id.nav_restaurant
            15 -> R.id.nav_love
            16 -> R.id.nav_shopping
            17 -> R.id.nav_clothing
            18 -> R.id.nav_drugstore
            19 -> R.id.nav_driving
            20 -> R.id.nav_bank
            else -> R.id.nav_all_phrases
        }
        navigationView.setCheckedItem(menuItemId)

        when (position) {
            0 -> {
                fragment = HomeFragment.newInstance(-1, all_phrases)
                category = getString(R.string.title_all)
            }
            1 -> {
                // Favourites is now treated as a regular category section
                fragment = FavouritesFragment.newInstance()
                category = getString(R.string.nav_item_favourites)
            }
            2 -> {
                fragment = HomeFragment.newInstance(R.array.greetings)
                category = getString(R.string.title_greetings)
            }
            3 -> {
                fragment = HomeFragment.newInstance(R.array.signs)
                category = getString(R.string.title_signs)
            }
            4 -> {
                fragment = HomeFragment.newInstance(R.array.troubleshooting)
                category = getString(R.string.title_troubleshooting)
            }
            5 -> {
                fragment = HomeFragment.newInstance(R.array.transportation)
                category = getString(R.string.title_transportation)
            }
            6 -> {
                fragment = HomeFragment.newInstance(R.array.directions)
                category = getString(R.string.title_directions)
            }
            7 -> {
                fragment = HomeFragment.newInstance(R.array.hotel)
                category = getString(R.string.title_hotel)
            }
            8 -> {
                fragment = HomeFragment.newInstance(R.array.numbers)
                category = getString(R.string.title_numbers)
            }
            9 -> {
                fragment = HomeFragment.newInstance(R.array.time)
                category = getString(R.string.title_time)
            }
            10 -> {
                fragment = HomeFragment.newInstance(R.array.weekdays)
                category = getString(R.string.title_weekdays)
            }
            11 -> {
                fragment = HomeFragment.newInstance(R.array.months)
                category = getString(R.string.title_months)
            }
            12 -> {
                fragment = HomeFragment.newInstance(R.array.colors)
                category = getString(R.string.title_colors)
            }
            13 -> {
                fragment = HomeFragment.newInstance(R.array.common_words)
                category = getString(R.string.title_common_words)
            }
            14 -> {
                fragment = HomeFragment.newInstance(R.array.restaurant)
                category = getString(R.string.title_restaurant)
            }
            15 -> {
                fragment = HomeFragment.newInstance(R.array.love)
                category = getString(R.string.title_love)
            }
            16 -> {
                fragment = HomeFragment.newInstance(R.array.shopping)
                category = getString(R.string.title_shopping)
            }
            17 -> {
                fragment = HomeFragment.newInstance(R.array.clothing)
                category = getString(R.string.title_clothing)
            }
            18 -> {
                fragment = HomeFragment.newInstance(R.array.drugstore)
                category = getString(R.string.title_drugstore)
            }
            19 -> {
                fragment = HomeFragment.newInstance(R.array.driving)
                category = getString(R.string.title_driving)
            }
            20 -> {
                fragment = HomeFragment.newInstance(R.array.bank)
                category = getString(R.string.title_bank)
            }
        }

        fragment?.let {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container_body, it, "HOME")
            fragmentTransaction.commit()

            setActionBarTitle(category)
        }
    }

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    fun enableBackButton(state: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(state)
    }

    fun setDrawerLocked(locked: Boolean) {
        val lockMode = if (locked) DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        else DrawerLayout.LOCK_MODE_UNLOCKED
        drawerLayout.setDrawerLockMode(lockMode)
    }

    fun setBackButtonEnabled() {
        drawerToggle.isDrawerIndicatorEnabled = false
        drawerToggle.setToolbarNavigationClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setDrawerState(enabled: Boolean) {
        val lockMode = if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED
        else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        drawerLayout.setDrawerLockMode(lockMode)
        drawerToggle.isDrawerIndicatorEnabled = enabled
    }
}
