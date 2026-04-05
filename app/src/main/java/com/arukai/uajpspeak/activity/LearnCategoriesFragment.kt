package com.arukai.uajpspeak.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.model.LearnCategory
import com.arukai.uajpspeak.util.FlashcardManager

/**
 * Fragment for selecting phrase categories to learn with flashcards.
 */
class LearnCategoriesFragment : Fragment() {

    private lateinit var flashcardManager: FlashcardManager
    private val categories = mutableListOf<LearnCategory>()

    private lateinit var startButton: Button
    private lateinit var overallStatsText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_learn_categories, container, false)

        flashcardManager = FlashcardManager(requireContext())

        // Initialize categories
        initializeCategories()

        // Setup UI - initialize button/stats first, then categories list
        setupStartButton(rootView)
        setupCategoriesList(rootView)

        // Set up menu to hide unwanted items
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Menu already inflated by MainActivity
            }

            override fun onPrepareMenu(menu: Menu) {
                // Hide all menu items except Alphabet
                menu.findItem(R.id.action_search)?.isVisible = false
                menu.findItem(R.id.action_favorite)?.isVisible = false
                menu.findItem(R.id.action_gender_lang)?.isVisible = false
                menu.findItem(R.id.action_language)?.isVisible = false
                menu.findItem(R.id.action_theme)?.isVisible = false
                menu.findItem(R.id.action_about)?.isVisible = false
                // Keep only Alphabet visible
                menu.findItem(R.id.action_alphabet)?.isVisible = true
            }

            override fun onMenuItemSelected(item: android.view.MenuItem): Boolean {
                return false // Let MainActivity handle menu clicks
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        // Ensure toolbar is properly configured when returning from flashcards
        val mainActivity = activity as? MainActivity
        mainActivity?.setActionBarTitle(getString(R.string.title_learn_categories))
        mainActivity?.setDrawerLocked(true)
        mainActivity?.enableBackButton(false)
    }

    private fun currentGender(): String =
        when (MainActivity.app_settings.getInt("gender_lang", 0)) {
            1 -> "f"
            else -> "m"
        }

    private fun initializeCategories() {
        categories.clear()

        val categoryNames = resources.getStringArray(R.array.nav_drawer_labels)

        // Skip "All Phrases" (0) and "Favourites" (1), start from actual categories (2+)
        MainActivity.ALL_PHRASE_ARRAY_IDS.forEachIndexed { index, arrayId ->
            val position = index + 2 // Offset to match MainActivity position
            val stats = flashcardManager.getCategoryStats(arrayId, currentGender())

            // Auto-select categories that have been started (learned > 0) AND have phrases due for review
            val hasStarted = stats.second > 0  // learnedPhrases > 0
            val hasDueCards = stats.third > 0  // dueForReview > 0
            val shouldAutoSelect = hasStarted && hasDueCards

            categories.add(
                LearnCategory(
                    categoryId = position,
                    categoryName = categoryNames[position],
                    arrayResourceId = arrayId,
                    isSelected = shouldAutoSelect,
                    totalPhrases = stats.first,
                    learnedPhrases = stats.second,
                    dueForReview = stats.third
                )
            )
        }
    }

    private fun setupCategoriesList(rootView: View) {
        val container = rootView.findViewById<LinearLayout>(R.id.categoriesContainer)
        container.removeAllViews()

        categories.forEach { category ->
            val categoryView = createCategoryView(category)
            container.addView(categoryView)
        }
    }

    private fun createCategoryView(category: LearnCategory): View {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.item_learn_category, null)

        val indicator = view.findViewById<View>(R.id.categoryIndicator)
        val checkbox = view.findViewById<CheckBox>(R.id.categoryCheckbox)
        val nameText = view.findViewById<TextView>(R.id.categoryName)
        val statsText = view.findViewById<TextView>(R.id.categoryStats)
        val progressText = view.findViewById<TextView>(R.id.categoryProgress)

        checkbox.isChecked = category.isSelected
        nameText.text = category.categoryName

        // Determine if category has been started
        val hasStarted = category.totalPhrases > 0

        // Show statistics
        if (hasStarted) {
            val progress = (category.learnedPhrases * 100) / category.totalPhrases

            // Determine color based on progress
            val color = when {
                progress >= 80 -> ContextCompat.getColor(requireContext(), R.color.progress_high)
                progress >= 40 -> ContextCompat.getColor(requireContext(), R.color.progress_medium)
                else -> ContextCompat.getColor(requireContext(), R.color.progress_low)
            }

            // Show colored indicator bar
            indicator.setBackgroundColor(color)

            // Build stats text
            val statsBuilder = StringBuilder()
            statsBuilder.append(getString(
                R.string.learn_category_stats,
                category.learnedPhrases,
                category.totalPhrases
            ))

            if (category.dueForReview > 0) {
                statsBuilder.append(" • ")
                statsBuilder.append(resources.getQuantityString(
                    R.plurals.cards_due_for_review,
                    category.dueForReview,
                    category.dueForReview
                ))
            }

            statsText.text = statsBuilder.toString()
            progressText.text = "$progress%"
            progressText.setTextColor(color)
        } else {
            // Not started yet - no indicator bar
            indicator.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            statsText.text = getString(R.string.learn_category_not_started)
            statsText.alpha = 0.6f
            progressText.text = ""
        }

        // Toggle selection on click
        view.setOnClickListener {
            checkbox.isChecked = !checkbox.isChecked
        }

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            category.isSelected = isChecked
            // Update button state when selection changes
            updateStartButton(startButton, overallStatsText)
        }

        return view
    }

    private fun setupStartButton(rootView: View) {
        startButton = rootView.findViewById(R.id.startLearningButton)
        overallStatsText = rootView.findViewById(R.id.overallStats)

        updateStartButton(startButton, overallStatsText)

        startButton.setOnClickListener {
            val selectedCategories = categories.filter { it.isSelected }

            if (selectedCategories.isEmpty()) {
                // Show message to select at least one category
                return@setOnClickListener
            }

            // Navigate to learning screen
            val fragment = LearnFlashcardsFragment()
            val bundle = Bundle()
            bundle.putIntArray("categoryIds", selectedCategories.map { it.arrayResourceId }.toIntArray())
            fragment.arguments = bundle

            val fragmentManager = parentFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container_body, fragment, "LEARN_FLASHCARDS")
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            // Update toolbar - show back button for flashcards screen (like Zoom)
            val mainActivity = activity as? MainActivity
            mainActivity?.setActionBarTitle(getString(R.string.title_learn_flashcards))
            mainActivity?.setDrawerLocked(false)
            mainActivity?.setBackButtonEnabled()
        }

        // Update stats on resume
        rootView.post {
            updateOverallStats(overallStatsText)
        }
    }

    private fun updateStartButton(button: Button, statsText: TextView) {
        val selectedCount = categories.count { it.isSelected }
        button.isEnabled = selectedCount > 0
        button.text = if (selectedCount > 0) {
            getString(R.string.start_learning_with_count, selectedCount)
        } else {
            getString(R.string.select_categories_prompt)
        }

        updateOverallStats(statsText)
    }

    private fun updateOverallStats(statsText: TextView) {
        val stats = flashcardManager.getOverallStats(currentGender())
        statsText.text = getString(
            R.string.overall_learning_stats,
            stats["learned"] ?: 0,
            stats["total"] ?: 0,
            stats["due"] ?: 0
        )
    }
}
