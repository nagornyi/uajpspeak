package com.arukai.uajpspeak.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.adapter.MyRecyclerViewAdapter
import com.arukai.uajpspeak.model.DataObject
import com.arukai.uajpspeak.util.FavoritesManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class FavouritesFragment : Fragment() {

    private lateinit var mRecyclerView: RecyclerView
    lateinit var mAdapter: MyRecyclerViewAdapter
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var dataSet: ArrayList<DataObject>
    private lateinit var mAdView: AdView
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var emptyStateView: View

    // Map of Ukrainian text to full phrase string (for all arrays in current language)
    private var ukrainianToPhraseMap: Map<String, String> = emptyMap()

    companion object {
        var index = -1
        var top = -1

        fun newInstance(): FavouritesFragment {
            return FavouritesFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoritesManager = FavoritesManager(requireContext())

        // Build Ukrainian-to-phrase map once for performance
        buildUkrainianToPhraseMap()
    }

    private fun buildUkrainianToPhraseMap() {
        val map = mutableMapOf<String, String>()

        for (arrayId in MainActivity.ALL_PHRASE_ARRAY_IDS) {
            val phrases = resources.getStringArray(arrayId)
            for (phrase in phrases) {
                val parts = phrase.split("/")
                if (parts.size >= 3) {
                    val ukrainian = parts[2]
                    // Map Ukrainian text to full phrase string
                    map[ukrainian] = phrase
                }
            }
        }

        ukrainianToPhraseMap = map
    }

    private fun getDataSet(): ArrayList<DataObject> {
        val results = ArrayList<DataObject>()

        // Get current gender and language settings
        val currentGender = when (MainActivity.app_settings.getInt("gender_lang", 0)) {
            1 -> "f"
            else -> "m"
        }
        val currentLanguage = com.arukai.uajpspeak.util.LocaleHelper.getSavedLanguage(requireContext())

        // Get favorites for current gender AND language
        val favorites = favoritesManager.getFavoritesForCurrentSettings(currentGender, currentLanguage)

        // Build results by looking up each favorite using Ukrainian text
        for (fav in favorites) {
            val fullPhrase = ukrainianToPhraseMap[fav.ukrainian]
            if (fullPhrase != null) {
                val parts = fullPhrase.split("/")
                if (parts.size >= 3) {
                    val gender = parts[0]
                    val sourceText = parts[1]  // Current language text
                    val ukrainian = parts[2]

                    // Only show if gender matches or is neutral
                    if (gender == "n" || gender == currentGender) {
                        val obj = DataObject(gender, sourceText, ukrainian)
                        results.add(obj)
                    }
                }
            }
        }

        return results
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use modern MenuProvider to hide all menu items
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.action_search)?.isVisible = false
                menu.findItem(R.id.action_alphabet)?.isVisible = false
                menu.findItem(R.id.action_about)?.isVisible = false
                menu.findItem(R.id.action_gender_lang)?.isVisible = false
                menu.findItem(R.id.action_language)?.isVisible = false
                menu.findItem(R.id.action_favorite)?.isVisible = false
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
        val rootView = inflater.inflate(R.layout.fragment_favourites, container, false)

        mRecyclerView = rootView.findViewById(R.id.my_recycler_view)
        mRecyclerView.setHasFixedSize(true)
        emptyStateView = rootView.findViewById(R.id.empty_state)

        mLayoutManager = LinearLayoutManager(mRecyclerView.context)
        mRecyclerView.layoutManager = mLayoutManager
        dataSet = getDataSet()
        mAdapter = MyRecyclerViewAdapter(dataSet)
        mRecyclerView.adapter = mAdapter

        updateEmptyState()

        // Initialize ad banner
        mAdView = rootView.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()

        // Reload favorites in case they were modified
        dataSet = getDataSet()
        mAdapter = MyRecyclerViewAdapter(dataSet)
        mRecyclerView.adapter = mAdapter

        updateEmptyState()

        if (index != -1) {
            mLayoutManager.scrollToPosition(index)
        }

        mAdapter.setOnItemClickListener(object : MyRecyclerViewAdapter.MyClickListener {
            override fun onItemClick(position: Int, v: View) {
                if (MainActivity.isSearchOpened) {
                    val action = (activity as MainActivity).supportActionBar
                    action?.setDisplayShowCustomEnabled(false)
                    action?.setDisplayShowTitleEnabled(true)

                    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    view?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }

                    MainActivity.isSearchOpened = false
                }

                val sourceText = v.findViewById<TextView>(R.id.textView).text.toString()
                // Get Ukrainian text with asterisks from dataSet, not from TextView (which has them removed)
                val ukr = dataSet[position].mText3
                val phonetic = v.findViewById<TextView>(R.id.textView3).text.toString()

                index = position
                mLayoutManager.onSaveInstanceState()

                val fragment = ZoomFragment.newInstance(sourceText, ukr, phonetic, null)
                val fragmentManager = activity?.supportFragmentManager
                val fragmentTransaction = fragmentManager?.beginTransaction()
                fragmentTransaction?.replace(R.id.container_body, fragment, "ZOOM")
                fragmentTransaction?.addToBackStack(null)
                fragmentTransaction?.commit()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        val layoutManager = mRecyclerView.layoutManager as LinearLayoutManager
        index = layoutManager.findFirstVisibleItemPosition()
        val v = mRecyclerView.getChildAt(0)
        top = if (v == null) 0 else v.top - mRecyclerView.paddingTop
    }

    private fun updateEmptyState() {
        if (dataSet.isEmpty()) {
            emptyStateView.visibility = View.VISIBLE
            mRecyclerView.visibility = View.GONE
        } else {
            emptyStateView.visibility = View.GONE
            mRecyclerView.visibility = View.VISIBLE
        }
    }
}

