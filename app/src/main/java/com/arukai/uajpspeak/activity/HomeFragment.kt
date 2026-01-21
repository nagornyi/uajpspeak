package com.arukai.uajpspeak.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.adapter.MyRecyclerViewAdapter
import com.arukai.uajpspeak.model.DataObject
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class HomeFragment : Fragment() {

    private lateinit var mRecyclerView: RecyclerView
    lateinit var mAdapter: MyRecyclerViewAdapter
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var dataSet: ArrayList<DataObject>
    private lateinit var mAdView: AdView

    companion object {
        var index = -1
        var top = -1
        var gender: Char = 'm'
        var phrases: Array<String>? = null

        fun newInstance(index: Int, selectedPhrases: Array<String>?): HomeFragment {
            val f = HomeFragment()
            gender = when (MainActivity.app_settings.getInt("gender_lang", 0)) {
                1 -> 'f'
                else -> 'm'
            }
            val args = Bundle()
            args.putInt("index", index)
            phrases = selectedPhrases
            f.arguments = args
            return f
        }

        fun newInstance(index: Int): HomeFragment {
            return newInstance(index, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun getDataSet(): ArrayList<DataObject> {
        val args = arguments
        val index = args?.getInt("index", 0) ?: 0
        val results = ArrayList<DataObject>()

        if (phrases == null) phrases = resources.getStringArray(index)

        phrases?.forEach { s ->
            val parts = s.split("/")
            val phraseGender = parts[0]
            if (phraseGender == gender.toString() || phraseGender == "n") {
                val obj = DataObject(parts[0], parts[1], parts[2])
                results.add(obj)
            }
        }
        return results
    }

    private fun getAudio(ukr: String): String? {
        for (item in dataSet) {
            if (item.mText3.replace("*", "") == ukr) return item.mText1
        }
        return null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        mRecyclerView = rootView.findViewById(R.id.my_recycler_view)
        mRecyclerView.setHasFixedSize(true)

        mLayoutManager = LinearLayoutManager(mRecyclerView.context)
        mRecyclerView.layoutManager = mLayoutManager
        dataSet = getDataSet()
        mAdapter = MyRecyclerViewAdapter(dataSet)
        mRecyclerView.adapter = mAdapter

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

                val jpn = v.findViewById<TextView>(R.id.textView).text.toString()
                // Get Ukrainian text with asterisks from dataSet, not from TextView (which has them removed)
                val ukr = dataSet[position].mText3
                val phonetic = v.findViewById<TextView>(R.id.textView3).text.toString()

                val fragment = ZoomFragment.newInstance(jpn, ukr, phonetic, getAudio(ukr.replace("*", "")))

                val fragmentManager = fragmentManager
                val fragmentTransaction = fragmentManager?.beginTransaction()
                fragmentTransaction?.replace(R.id.container_body, fragment, "ZOOM")
                fragmentTransaction?.addToBackStack(null)
                fragmentTransaction?.commit()

                val mainActivity = (activity as MainActivity)
                mainActivity.setActionBarTitle("")
                mainActivity.setDrawerLocked(false)
                mainActivity.setBackButtonEnabled()
                enableBackButton(true)
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


    fun enableBackButton(state: Boolean) {
        val actionbar = (activity as MainActivity).supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(state)
    }
}

