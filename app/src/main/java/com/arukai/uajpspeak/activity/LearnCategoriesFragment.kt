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
                // Hide all menu items except Alphabet and About
                menu.findItem(R.id.action_search)?.isVisible = false
                menu.findItem(R.id.action_favorite)?.isVisible = false
                menu.findItem(R.id.action_gender_lang)?.isVisible = false
                menu.findItem(R.id.action_language)?.isVisible = false
                menu.findItem(R.id.action_theme)?.isVisible = false
                // Keep Alphabet and About visible
                menu.findItem(R.id.action_alphabet)?.isVisible = true
                menu.findItem(R.id.action_about)?.isVisible = true
            }

            override fun onMenuItemSelected(item: android.view.MenuItem): Boolean {
                return false // Let MainActivity handle menu clicks
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return rootView
    }

    private fun initializeCategories() {
        categories.clear()

        val selectedIds = flashcardManager.getSelectedCategories()
        val categoryNames = resources.getStringArray(R.array.nav_drawer_labels)

        // Skip "All Phrases" (0) and "Favourites" (1), start from actual categories (2+)
        MainActivity.ALL_PHRASE_ARRAY_IDS.forEachIndexed { index, arrayId ->
            val position = index + 2 // Offset to match MainActivity position
            val stats = flashcardManager.getCategoryStats(arrayId)

            categories.add(
                LearnCategory(
                    categoryId = position,
                    categoryName = categoryNames[position],
                    arrayResourceId = arrayId,
                    isSelected = selectedIds.contains(arrayId),
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

        val checkbox = view.findViewById<CheckBox>(R.id.categoryCheckbox)
        val nameText = view.findViewById<TextView>(R.id.categoryName)
        val statsText = view.findViewById<TextView>(R.id.categoryStats)
        val progressText = view.findViewById<TextView>(R.id.categoryProgress)

        checkbox.isChecked = category.isSelected
        nameText.text = category.categoryName

        // Show statistics
        if (category.totalPhrases > 0) {
            val progress = (category.learnedPhrases * 100) / category.totalPhrases
            statsText.text = getString(
                R.string.learn_category_stats,
                category.learnedPhrases,
                category.totalPhrases
            )
            progressText.text = "$progress%"

            // Color code the progress
            val color = when {
                progress >= 80 -> ContextCompat.getColor(requireContext(), R.color.progress_high)
                progress >= 40 -> ContextCompat.getColor(requireContext(), R.color.progress_medium)
                else -> ContextCompat.getColor(requireContext(), R.color.progress_low)
            }
            progressText.setTextColor(color)

            if (category.dueForReview > 0) {
                statsText.append(" • ${category.dueForReview} due")
            }
        } else {
            statsText.text = getString(R.string.learn_category_not_started)
            progressText.text = "0%"
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

            // Save selected categories
            val selectedIds = selectedCategories.map { it.arrayResourceId }.toSet()
            flashcardManager.saveSelectedCategories(selectedIds)

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

            // Update toolbar
            (activity as? MainActivity)?.setActionBarTitle(getString(R.string.title_learn_flashcards))
            (activity as? MainActivity)?.enableBackButton(true)
            (activity as? MainActivity)?.setDrawerLocked(false)
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
        val stats = flashcardManager.getOverallStats()
        statsText.text = getString(
            R.string.overall_learning_stats,
            stats["learned"] ?: 0,
            stats["total"] ?: 0,
            stats["due"] ?: 0
        )
    }
}








