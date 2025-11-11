package com.arukai.uajpspeak.activity

import android.content.Context
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
        @Suppress("DEPRECATION")
        body.text = Html.fromHtml(getString(R.string.alphabet_html))
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
}

