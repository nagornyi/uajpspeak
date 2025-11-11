package com.arukai.uajpspeak.activity

import android.os.Bundle
import android.speech.tts.TextToSpeech
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
import java.util.Locale

class ZoomFragment : Fragment() {
    private var t1: TextToSpeech? = null

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
        val rootView = inflater.inflate(R.layout.fragment_zoom, container, false)

        val japaneseView = rootView.findViewById<TextView>(R.id.zoomJapanese)
        val ukrainianView = rootView.findViewById<TextView>(R.id.zoomUkrainian)
        val speakerIcon = rootView.findViewById<ImageView>(R.id.zoomSpeaker)
        val phoneticView = rootView.findViewById<TextView>(R.id.zoomPhonetic)

        val args = arguments
        val japanese = args?.getString("japanese")
        val ukrainian = args?.getString("ukrainian")
        val phonetic = args?.getString("phonetic")

        japaneseView.text = japanese
        ukrainianView.text = ukrainian?.uppercase()
        phoneticView.text = phonetic

        val clickListener = View.OnClickListener {
            t1 = TextToSpeech(activity?.applicationContext) { status ->
                if (status != TextToSpeech.ERROR) {
                    t1?.language = Locale("uk")
                    t1?.setSpeechRate(0.5f)
                    t1?.speak(ukrainian, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }

        ukrainianView.setOnClickListener(clickListener)
        speakerIcon.setOnClickListener(clickListener)

        return rootView
    }

    companion object {
        fun newInstance(japanese: String, ukrainian: String, phonetic: String, audio: String?): ZoomFragment {
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

