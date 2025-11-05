package com.arukai.uajpspeak.activity

import android.app.Activity
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.arukai.uajpspeak.R
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo

class AlphabetFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_search).isVisible = false
        menu.findItem(R.id.action_alphabet).isVisible = false
        menu.findItem(R.id.action_about).isVisible = false
        menu.findItem(R.id.action_gender_lang).isVisible = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_alphabet, container, false)
        val body = rootView.findViewById<TextView>(R.id.alphabetText)
        @Suppress("DEPRECATION")
        body.text = Html.fromHtml(getString(R.string.alphabet_html))
        val imageView = rootView.findViewById<ImageView>(R.id.abetkaImg)
        YoYo.with(Techniques.FadeIn).duration(700).playOn(imageView)
        YoYo.with(Techniques.FadeIn).duration(700).playOn(body)
        return rootView
    }

    @Deprecated("Deprecated in Java")
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
    }
}

