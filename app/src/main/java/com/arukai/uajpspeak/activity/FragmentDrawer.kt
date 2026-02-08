package com.arukai.uajpspeak.activity

import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.adapter.NavigationDrawerAdapter
import com.arukai.uajpspeak.model.NavDrawerItem

class FragmentDrawer : Fragment() {

    private lateinit var recyclerView: RecyclerView
    lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var containerView: View
    private var drawerListener: FragmentDrawerListener? = null

    companion object {
        var adapter: NavigationDrawerAdapter? = null
        var activeData: MutableList<NavDrawerItem>? = null
        private var titles: Array<String>? = null
        private var pCounter = 0
    }

    interface FragmentDrawerListener {
        fun onDrawerItemSelected(view: View, position: Int)
    }

    interface ClickListener {
        fun onClick(view: View, position: Int)
        fun onLongClick(view: View, position: Int)
    }

    private fun getData(): MutableList<NavDrawerItem> {
        val data = ArrayList<NavDrawerItem>()

        titles?.forEach { title ->
            val navItem = NavDrawerItem()
            navItem.title = title
            data.add(navItem)
        }

        pCounter = resources.getStringArray(R.array.greetings).size +
                resources.getStringArray(R.array.signs).size +
                resources.getStringArray(R.array.troubleshooting).size +
                resources.getStringArray(R.array.transportation).size +
                resources.getStringArray(R.array.directions).size +
                resources.getStringArray(R.array.hotel).size +
                resources.getStringArray(R.array.numbers).size +
                resources.getStringArray(R.array.time).size +
                resources.getStringArray(R.array.weekdays).size +
                resources.getStringArray(R.array.months).size +
                resources.getStringArray(R.array.colors).size +
                resources.getStringArray(R.array.common_words).size +
                resources.getStringArray(R.array.restaurant).size +
                resources.getStringArray(R.array.love).size +
                resources.getStringArray(R.array.shopping).size +
                resources.getStringArray(R.array.clothing).size +
                resources.getStringArray(R.array.drugstore).size +
                resources.getStringArray(R.array.driving).size +
                resources.getStringArray(R.array.bank).size

        data[MainActivity.current_position].isSelected = true
        return data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titles = resources.getStringArray(R.array.nav_drawer_labels)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false)
        recyclerView = layout.findViewById(R.id.drawerList)
        activeData = getData()
        adapter = NavigationDrawerAdapter(requireActivity(), activeData!!)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(
                requireActivity(),
                recyclerView,
                object : ClickListener {
                    override fun onClick(view: View, position: Int) {
                        drawerListener?.onDrawerItemSelected(view, position)
                        mDrawerLayout.closeDrawer(containerView)
                    }

                    override fun onLongClick(view: View, position: Int) {
                    }
                })
        )
        layout.setOnClickListener(null)

        // Update the NavigationView header (NavigationView hosts the header layout `nav_header.xml`)
        // Find the NavigationView via the hosting Activity and update its header banner and phrasesCount
        val navView = activity?.findViewById<com.google.android.material.navigation.NavigationView>(R.id.navigation_view)
        navView?.getHeaderView(0)?.let { header ->
            val banner = header.findViewById<android.widget.ImageView>(R.id.banner)
            val lang = com.arukai.uajpspeak.util.LocaleHelper.getSavedLanguage(requireContext())
            val sourceFlagRes = when (lang) {
                "en" -> R.drawable.uk
                "de" -> R.drawable.de
                "es" -> R.drawable.es
                "fr" -> R.drawable.fr
                "ja" -> R.drawable.jp
                else -> R.drawable.uk
            }
            banner?.setImageResource(sourceFlagRes)

            val phrasesCountView = header.findViewById<TextView>(R.id.phrasesCount)
            phrasesCountView?.text = getString(R.string.phrases_counter, pCounter)
        }
        return layout
    }

    fun setUp(fragmentId: Int, drawerLayout: DrawerLayout, toolbar: Toolbar) {
        containerView = requireActivity().findViewById(fragmentId)
        mDrawerLayout = drawerLayout
        mDrawerToggle = object : ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                if (MainActivity.isSearchOpened) {
                    val action = (activity as MainActivity).supportActionBar
                    action?.setDisplayShowCustomEnabled(false)
                    action?.setDisplayShowTitleEnabled(true)

                    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    view?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }

                    MainActivity.isSearchOpened = false
                }

                super.onDrawerOpened(drawerView)
                activity?.invalidateOptionsMenu()
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                activity?.invalidateOptionsMenu()
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                toolbar.alpha = 1 - slideOffset / 2
            }
        }

        mDrawerLayout.addDrawerListener(mDrawerToggle)
        mDrawerLayout.post {
            mDrawerToggle.syncState()
        }
    }

    class RecyclerTouchListener(
        context: Context,
        private val recyclerView: RecyclerView,
        private val clickListener: ClickListener
    ) : RecyclerView.OnItemTouchListener {

        private val gestureDetector: GestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                if (child != null) {
                    clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        })

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val child = rv.findChildViewUnder(e.x, e.y)
            if (child != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child))
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        }
    }
}
