package com.arukai.uajpspeak.activity

import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.util.LocaleHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class AlphabetFragment : Fragment() {

    private data class TocItem(val id: String, val level: String, val text: String)

    private var tocItems: List<TocItem> = emptyList()
    private var webViewRef: WebView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Hide everything except TOC in Alphabet view
                menu.findItem(R.id.action_search)?.isVisible = false
                menu.findItem(R.id.action_alphabet)?.isVisible = false
                menu.findItem(R.id.action_about)?.isVisible = false
                menu.findItem(R.id.action_favorite)?.isVisible = false
                menu.findItem(R.id.action_gender_lang)?.isVisible = false
                menu.findItem(R.id.action_language)?.isVisible = false
                menu.findItem(R.id.action_theme)?.isVisible = false
                // Only show TOC button once items have been extracted
                menu.findItem(R.id.action_toc)?.isVisible = tocItems.isNotEmpty()
            }

            override fun onCreateMenu(menu: Menu, menuInflater: android.view.MenuInflater) {}

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_toc) {
                    showTocPopup()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        webViewRef = null
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_alphabet, container, false)

        val webView  = rootView.findViewById<WebView>(R.id.alphabetWebView)
        val progress = rootView.findViewById<ProgressBar>(R.id.alphabetProgress)

        // Load ad
        rootView.findViewById<AdView>(R.id.adView).loadAd(AdRequest.Builder().build())
        webViewRef = webView

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.settings.apply {
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            allowFileAccess = true
            @Suppress("SetJavaScriptEnabled")
            javaScriptEnabled = true  // Required for evaluateJavascript (TOC scrolling)
        }

        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        val bgColor      = if (isDark) "#1C1C1E" else "#FFFFFF"
        val textColor    = if (isDark) "#E5E5EA" else "#1C1C1E"
        val h3Color      = if (isDark) "#FFFFFF" else "#000000"
        val imgFilter    = if (isDark) "filter: invert(1) hue-rotate(180deg);" else ""
        val dividerColor = if (isDark) "#3A3A3C" else "#D1D1D6"

        val lang = LocaleHelper.getSavedLanguage(requireContext())
        val rawResId = when (lang) {
            "de" -> R.raw.alphabet_de
            "es" -> R.raw.alphabet_es
            "fr" -> R.raw.alphabet_fr
            "ja" -> R.raw.alphabet_ja
            else -> R.raw.alphabet_en
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val (processedHtml, toc) = withContext(Dispatchers.IO) {
                processHtml(loadRawHtml(rawResId))
            }
            tocItems = toc

            val html = buildHtml(bgColor, textColor, h3Color, imgFilter, dividerColor, processedHtml)
            webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
            progress.visibility = View.GONE
            webView.visibility = View.VISIBLE
            // Trigger onPrepareMenu so the TOC button becomes visible
            requireActivity().invalidateOptionsMenu()
        }

        return rootView
    }

    /** Adds sequential id attributes to every h2/h3 and builds the TOC list. */
    private fun processHtml(rawHtml: String): Pair<String, List<TocItem>> {
        val items = mutableListOf<TocItem>()
        var counter = 0

        // Collect heading texts — no backreference, matches any closing h2/h3 tag
        val textRegex = Regex(
            """<(h[23])[^>]*>(.*?)</h[23]>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        textRegex.findAll(rawHtml).forEach { m ->
            val level   = m.groupValues[1].lowercase()
            val rawText = HtmlCompat.fromHtml(
                m.groupValues[2].replace(Regex("<[^>]+>"), ""),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            ).toString().trim()
            if (rawText.isNotEmpty()) {
                items.add(TocItem("section-${counter++}", level, rawText))
            }
        }

        // Inject id attributes (reset counter)
        counter = 0
        val idRegex = Regex("""<(h[23])([^>]*)>""", RegexOption.IGNORE_CASE)
        val processedHtml = idRegex.replace(rawHtml) { m ->
            val tag   = m.groupValues[1]
            val attrs = m.groupValues[2].replace(Regex("""\s*id="[^"]*""""), "")
            "<$tag id=\"section-${counter++}\"$attrs>"
        }

        return processedHtml to items
    }

    private fun showTocPopup() {
        if (tocItems.isEmpty()) return
        val ctx = requireContext()

        fun resolveColor(attr: Int, fallback: Int): Int {
            val tv = TypedValue()
            return if (ctx.theme.resolveAttribute(attr, tv, true)) {
                if (tv.resourceId != 0) ctx.resources.getColor(tv.resourceId, ctx.theme) else tv.data
            } else fallback
        }

        val colorPrimary   = resolveColor(android.R.attr.textColorPrimary,   android.graphics.Color.BLACK)
        val colorSecondary = resolveColor(android.R.attr.textColorSecondary, android.graphics.Color.GRAY)

        // Resolve divider drawable from theme
        val divTv = TypedValue()
        val dividerDrawable = if (ctx.theme.resolveAttribute(android.R.attr.listDivider, divTv, true) && divTv.resourceId != 0)
            androidx.core.content.res.ResourcesCompat.getDrawable(ctx.resources, divTv.resourceId, ctx.theme) else null

        // Prepend a sentinel divider item so the list has clear separation from the title
        val dividerId = "__divider__"
        val displayItems = listOf(TocItem(dividerId, "divider", "")) + tocItems

        val adapter = object : ArrayAdapter<TocItem>(ctx, 0, displayItems) {

            private val TYPE_DIVIDER = 0
            private val TYPE_H2 = 1
            private val TYPE_H3 = 2

            override fun getViewTypeCount() = 3

            override fun getItemViewType(position: Int) = when (getItem(position)!!.level) {
                "divider" -> TYPE_DIVIDER
                "h2"      -> TYPE_H2
                else      -> TYPE_H3
            }

            override fun isEnabled(position: Int) = getItem(position)!!.level != "divider"

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val item = getItem(position)!!
                val dp   = ctx.resources.displayMetrics.density

                if (item.level == "divider") {
                    val v = View(ctx)
                    v.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, (1 * dp).toInt().coerceAtLeast(1)
                    )
                    dividerDrawable?.let { v.background = it }
                        ?: v.setBackgroundColor(colorSecondary and 0x40FFFFFF)
                    return v
                }

                val tv = (convertView as? TextView) ?: TextView(ctx)
                tv.text = item.text
                tv.isSingleLine = false
                tv.maxLines = 3

                if (item.level == "h2") {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    tv.setTypeface(null, Typeface.BOLD)
                    tv.setPadding(
                        (20 * dp).toInt(), (14 * dp).toInt(),
                        (16 * dp).toInt(), (6  * dp).toInt()
                    )
                    tv.setTextColor(colorPrimary)
                } else {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    tv.setTypeface(null, Typeface.NORMAL)
                    tv.setPadding(
                        (40 * dp).toInt(), (8 * dp).toInt(),
                        (16 * dp).toInt(), (8 * dp).toInt()
                    )
                    tv.setTextColor(colorSecondary)
                }
                tv.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                return tv
            }
        }

        AlertDialog.Builder(ContextThemeWrapper(ctx, R.style.AlertDialogCustom))
            .setTitle(getString(R.string.action_toc))
            .setAdapter(adapter) { _, index ->
                // Adjust for the prepended divider item
                val realIndex = index - 1
                if (realIndex >= 0) {
                    val id = tocItems[realIndex].id
                    webViewRef?.evaluateJavascript(
                        "document.getElementById('$id').scrollIntoView({behavior:'smooth'});",
                        null
                    )
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun buildHtml(
        bgColor: String, textColor: String, h3Color: String,
        imgFilter: String, dividerColor: String, bodyHtml: String
    ) = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <style>
            body  { background-color: $bgColor; color: $textColor; font-family: sans-serif;
                    padding: 16px; padding-top: 12px; margin: 0; line-height: 1.5; }
            h2    { color: $h3Color; margin-top: 0; }
            h3    { color: $h3Color; margin-top: 20px; }
            h4    { color: $h3Color; }
            p     { margin: 6px 0; }
            .section-label { font-weight: bold; font-size: 19px;
                             margin: 20px 0 8px 0; color: $h3Color; }
            .alphabet-img  { width: 100%; height: auto; display: block; border-radius: 8px; $imgFilter }
            .divider { border: none; border-top: 1px solid $dividerColor; margin: 20px 0; }
          </style>
        </head>
        <body>$bodyHtml</body>
        </html>
    """.trimIndent()

    private fun loadRawHtml(resourceId: Int): String {
        return try {
            val inputStream = resources.openRawResource(resourceId)
            BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { it.readText() }
        } catch (_: Exception) {
            "<h3>Content unavailable</h3>"
        }
    }
}

