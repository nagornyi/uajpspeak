package com.arukai.uajpspeak.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.arukai.uajpspeak.R

class AboutFragment : Fragment() {

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

        val lang = com.arukai.uajpspeak.util.LocaleHelper.getSavedLanguage(MainActivity.context)
        val aboutText = view.findViewById<android.widget.TextView>(R.id.aboutText)
        val leftFlag = when (lang) {
            "en" -> R.drawable.uk
            "ja" -> R.drawable.jp
            else -> R.drawable.jp
        }
        aboutText.setCompoundDrawablesWithIntrinsicBounds(leftFlag, 0, R.drawable.ua, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_about, container, false)
        val imageView = rootView.findViewById<ImageView>(R.id.aboutPicture)

        // Native Android fade-in animation
        imageView.alpha = 0f
        imageView.animate()
            .alpha(1f)
            .setDuration(700)
            .start()

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
}

