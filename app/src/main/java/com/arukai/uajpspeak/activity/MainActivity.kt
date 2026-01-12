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
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.util.LocaleHelper
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.util.ArrayUtils
import java.util.Locale

class MainActivity : AppCompatActivity(), FragmentDrawer.FragmentDrawerListener, TextToSpeech.OnInitListener {

    private var mSearchAction: MenuItem? = null
    private var editSearch: EditText? = null
    private lateinit var textToSpeech: TextToSpeech

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
        var drawerFragment: FragmentDrawer? = null
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

        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)

        drawerFragment = supportFragmentManager.findFragmentById(R.id.fragment_navigation_drawer) as FragmentDrawer
        drawerFragment?.setUp(R.id.fragment_navigation_drawer, findViewById(R.id.drawer_layout), mToolbar)
        drawerFragment?.setDrawerListener(this)

        displayView(0)

        textToSpeech = TextToSpeech(this, this)

        // Handle back press with OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val zoom = supportFragmentManager.findFragmentByTag("ZOOM") as? ZoomFragment
                val about = supportFragmentManager.findFragmentByTag("ABOUT") as? AboutFragment
                val alphabet = supportFragmentManager.findFragmentByTag("ALPHABET") as? AlphabetFragment

                if ((zoom != null && zoom.isVisible) || (about != null && about.isVisible) ||
                    (alphabet != null && alphabet.isVisible)) {
                    fragment?.let {
                        val fragmentManager = supportFragmentManager

                        if (fragmentManager.backStackEntryCount != 0) {
                            fragmentManager.popBackStack()
                        } else {
                            val fragmentTransaction = fragmentManager.beginTransaction()
                            fragmentTransaction.replace(R.id.container_body, it, "HOME")
                            fragmentTransaction.commit()
                        }
                        setActionBarTitle(category)
                        enableBackButton(false)
                        drawerFragment?.setDrawerState(true)
                    }
                } else {
                    finish()
                }
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale("uk"))
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
            drawerFragment?.setDrawerState(false)
            drawerFragment?.mDrawerToggle?.setToolbarNavigationClickListener { onBackPressedDispatcher.onBackPressed() }
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
            drawerFragment?.setDrawerState(false)
            drawerFragment?.mDrawerToggle?.setToolbarNavigationClickListener { onBackPressedDispatcher.onBackPressed() }
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
                val home = supportFragmentManager.findFragmentByTag("HOME") as? HomeFragment
                if (home != null && home.isVisible) {
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
        return ArrayUtils.concat(
            resources.getStringArray(R.array.greetings),
            resources.getStringArray(R.array.signs),
            resources.getStringArray(R.array.troubleshooting),
            resources.getStringArray(R.array.transportation),
            resources.getStringArray(R.array.directions),
            resources.getStringArray(R.array.hotel),
            resources.getStringArray(R.array.numbers),
            resources.getStringArray(R.array.time),
            resources.getStringArray(R.array.weekdays),
            resources.getStringArray(R.array.months),
            resources.getStringArray(R.array.colors),
            resources.getStringArray(R.array.common_words),
            resources.getStringArray(R.array.restaurant),
            resources.getStringArray(R.array.love),
            resources.getStringArray(R.array.shopping),
            resources.getStringArray(R.array.clothing),
            resources.getStringArray(R.array.drugstore),
            resources.getStringArray(R.array.driving),
            resources.getStringArray(R.array.bank)
        )
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


    override fun onDrawerItemSelected(view: View, position: Int) {
        displayView(position)
    }

    private fun displayView(position: Int) {
        prev_position = current_position
        current_position = position

        FragmentDrawer.activeData?.let { data ->
            val unselItem = data[prev_position]
            unselItem.isSelected = false
            val selItem = data[current_position]
            selItem.isSelected = true
            data[prev_position] = unselItem
            data[current_position] = selItem
            FragmentDrawer.adapter?.notifyItemChanged(prev_position)
            FragmentDrawer.adapter?.notifyItemChanged(current_position)
        }

        when (position) {
            0 -> {
                fragment = HomeFragment.newInstance(-1, all_phrases)
                category = getString(R.string.title_all)
            }
            1 -> {
                fragment = HomeFragment.newInstance(R.array.greetings)
                category = getString(R.string.title_greetings)
            }
            2 -> {
                fragment = HomeFragment.newInstance(R.array.signs)
                category = getString(R.string.title_signs)
            }
            3 -> {
                fragment = HomeFragment.newInstance(R.array.troubleshooting)
                category = getString(R.string.title_troubleshooting)
            }
            4 -> {
                fragment = HomeFragment.newInstance(R.array.transportation)
                category = getString(R.string.title_transportation)
            }
            5 -> {
                fragment = HomeFragment.newInstance(R.array.directions)
                category = getString(R.string.title_directions)
            }
            6 -> {
                fragment = HomeFragment.newInstance(R.array.hotel)
                category = getString(R.string.title_hotel)
            }
            7 -> {
                fragment = HomeFragment.newInstance(R.array.numbers)
                category = getString(R.string.title_numbers)
            }
            8 -> {
                fragment = HomeFragment.newInstance(R.array.time)
                category = getString(R.string.title_time)
            }
            9 -> {
                fragment = HomeFragment.newInstance(R.array.weekdays)
                category = getString(R.string.title_weekdays)
            }
            10 -> {
                fragment = HomeFragment.newInstance(R.array.months)
                category = getString(R.string.title_months)
            }
            11 -> {
                fragment = HomeFragment.newInstance(R.array.colors)
                category = getString(R.string.title_colors)
            }
            12 -> {
                fragment = HomeFragment.newInstance(R.array.common_words)
                category = getString(R.string.title_common_words)
            }
            13 -> {
                fragment = HomeFragment.newInstance(R.array.restaurant)
                category = getString(R.string.title_restaurant)
            }
            14 -> {
                fragment = HomeFragment.newInstance(R.array.love)
                category = getString(R.string.title_love)
            }
            15 -> {
                fragment = HomeFragment.newInstance(R.array.shopping)
                category = getString(R.string.title_shopping)
            }
            16 -> {
                fragment = HomeFragment.newInstance(R.array.clothing)
                category = getString(R.string.title_clothing)
            }
            17 -> {
                fragment = HomeFragment.newInstance(R.array.drugstore)
                category = getString(R.string.title_drugstore)
            }
            18 -> {
                fragment = HomeFragment.newInstance(R.array.driving)
                category = getString(R.string.title_driving)
            }
            19 -> {
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
}
